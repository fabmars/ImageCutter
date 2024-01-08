package org.mars.cutter;

import static org.mars.cutter.util.FileUtils.dotExtension;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.mars.cutter.domain.Bounds;
import org.mars.cutter.domain.HVHoughs;
import org.mars.cutter.domain.Hough;
import org.mars.cutter.domain.HoughFlock;
import org.mars.cutter.domain.Outline;
import org.mars.cutter.domain.Segment;
import org.mars.cutter.util.FileUtils;
import org.mars.cutter.util.ImageUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * https://vincmazet.github.io/bip/detection/lines.html
 * https://learnopencv.com/edge-detection-using-opencv/
 * https://docs.opencv.org/4.9.0/dd/d1a/group__imgproc__feature.html#ga46b4e588934f6c8dfd509cc6e0e4545a
 * https://docs.opencv.org/4.9.0/da/d5c/tutorial_canny_detector.html
 * https://docs.opencv.org/4.9.0/d7/de1/tutorial_js_canny.html
 */
@Slf4j
public class RectangleDetector {

  public static final int TEMP_DIMENSION = 3000;
  public static final int PIXEL_COUNT_THRESHOLD = TEMP_DIMENSION/10;
  public static final double CANNY_THRESHOLD = 40.0;
  public static final double CANNY_RATIO = 6.0; // Canny recommends 3 but a higher value works better in our case
  public static final double HOUGH_THETA = Math.PI/(180*5);
  public static final int HOUGH_THRESHOLD = 300; // higher threshold will reduce the amount of lines
  public static final double HV_THRESHOLD = Math.PI / 180.0 * 0.75; // How much of an angle tolerance am I allowing to declare whether the line is horizontal or vertical
  public static final DetectionParams DEFAULT_PARAMS = new DetectionParams(TEMP_DIMENSION, CANNY_THRESHOLD, CANNY_RATIO, HOUGH_THETA, HOUGH_THRESHOLD,
      HV_THRESHOLD);

  public static final String TEMP_FORMAT = "bmp";
  public static final String DESTINATION_FORMAT = "jpg";
  protected static final double[] CANNY_BLACK = {0.0, 0.0, 0.0};
  public static final Comparator<Hough> RHO_COMPARATOR = Comparator.comparing(Hough::getRho);
  public static final RescaleOp CONTRAST_ADJUSTMENT = new RescaleOp(1.f, 0.1f, null);

  public RectangleDetector() {
    //Loading the OpenCV core library
    OpenCV.loadShared();
    //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  @Deprecated
  public void process(Path file) throws IOException {
    process(file, DEFAULT_PARAMS);
  }

  @Deprecated
  public void process(Path file, DetectionParams params) throws IOException {
    log.info("Processing: {}", file);
    String[] fileExt = FileUtils.getFileExt(file);

    Path tempFile = Files.createTempFile(null, dotExtension(fileExt[1]));
    BufferedImage originalImage = ImageIO.read(file.toFile());
    Size originalSize = new Size(originalImage.getWidth(), originalImage.getHeight());
    BufferedImage scaledImage = ImageUtils.scaleBigger(originalImage, TEMP_DIMENSION, TEMP_DIMENSION, (img, infoflags, x, y, width, height) -> false);
    //scaledImage = CONTRAST_ADJUSTMENT.filter(scaledImage, scaledImage);
    ImageIO.write(scaledImage, TEMP_FORMAT, tempFile.toFile());

    Outline outline = detectOutline(tempFile, params, originalSize);
    log.info(outline.toString());
    BufferedImage cannyImage = outline.toImage();

    Path cannyFile = file.resolveSibling("canny-" + fileExt[0] + dotExtension(DESTINATION_FORMAT));
    Files.deleteIfExists(cannyFile);
    ImageIO.write(cannyImage, DESTINATION_FORMAT, cannyFile.toFile());

    Path linedFile = file.resolveSibling("lined-" + fileExt[0] + dotExtension(DESTINATION_FORMAT));
    Files.deleteIfExists(linedFile);
    drawLines(cannyImage, outline, Color.red);
    drawBounds(cannyImage, outline, Color.green);
    //drawCorners(scaledImage, outline, Color.blue);
    ImageIO.write(cannyImage, DESTINATION_FORMAT, linedFile.toFile());

    Path resultFile = file.resolveSibling("result-" + fileExt[0] + dotExtension(DESTINATION_FORMAT));
    Files.deleteIfExists(resultFile);
    //drawLines(scaledImage, outline, Color.red);
    drawBounds(scaledImage, outline, Color.green);
    //drawCorners(scaledImage, outline, Color.blue);
    ImageIO.write(scaledImage, DESTINATION_FORMAT, resultFile.toFile());

    // cleanup
    Files.delete(tempFile);
  }


  private static void drawLines(BufferedImage image, Outline outline, Color linesColor) {
    Graphics2D g = (Graphics2D) image.getGraphics();
    int width = image.getWidth();
    int height = image.getHeight();

    outline.hvHoughs().forEach(hough -> {
      double theta = hough.getTheta();
      double rho = hough.getRho();

      double a = Math.cos(theta);
      double b = Math.sin(theta);
      double x0 = a * rho;
      double y0 = b * rho;

      // Drawing lines on the image. Majoring as if x0,y0 were in the center of a segment whose length is twice the image's diagonal.
      // Hence, those coords will exceed the image bounds unless they are exactly vertical or horizontal.

      g.setColor(linesColor);
      g.setStroke(new BasicStroke(1));
      int x1 = (int)Math.round(x0 - width * b);
      int y1 = (int)Math.round(y0 + height * a);
      int x2 = (int)Math.round(x0 + width * b);
      int y2 = (int)Math.round(y0 - height * a);
      g.drawLine(x1, y1, x2, y2);
    });
  }

  private static void drawBounds(BufferedImage image, Outline outline, Color boundsColor) {
    Graphics2D g = (Graphics2D) image.getGraphics();
    g.setStroke(new BasicStroke(3));
    g.setColor(boundsColor);
    for (Bounds bound : outline.bounds()) {
      for (Segment segment : bound.toSegments()) {
        g.drawLine((int)segment.x1(), (int)segment.y1(), (int)segment.x2(),(int) segment.y2());
      }
    }
  }

  private static void drawCorners(BufferedImage image, Outline outline, Color cornersColor) {
    Graphics2D g = (Graphics2D) image.getGraphics();
    g.setColor(cornersColor);
    for (Point corner : outline.corners()) {
      g.drawOval((int)corner.x, (int)corner.y, 5, 5);
    }
  }


  public Outline detectOutline(Path file, DetectionParams params, Size originalSize) {
    log.info("Identifying on: {}", file);
    Mat image = Imgcodecs.imread(file.toString());
    log.debug("Size: {}", image.size());
    log.debug("Empty: {}", image.empty());
    log.debug("Channels: {}", image.channels());
    log.debug("Type: {}", image.type());
    log.debug("Total: {}", image.total());

    //Converting the image to Gray
    Mat grayMat = new Mat();
    Imgproc.cvtColor(image, grayMat, Imgproc.COLOR_RGB2GRAY);

    // Detecting corners
    List<Point> corners = List.of(); // findCorners(grayMat, 0.1);

    //Detecting the edges
    Mat edgesMat = new Mat();
    Imgproc.Canny(grayMat, edgesMat, params.cannyThreshold(), params.cannyThreshold2(), 3, true);

    // Changing the color of the canny
    Mat cannyMat = new Mat();
    Imgproc.cvtColor(edgesMat, cannyMat, Imgproc.COLOR_GRAY2RGB);

    //Detecting the hough lines from (canny)
    Mat linesMat = new Mat();
    Imgproc.HoughLines(edgesMat, linesMat, 1, params.houghTheta(), params.houghThreshold());

    // filter horizontal/vertical lines
    HVHoughs hvHoughs = filterCardinal(linesMat, params.hvAngleTolerance());
    hvHoughs.forEach(hough -> log.debug(hough.toString()));

    List<Bounds> bounds = findRectangles(cannyMat, hvHoughs, originalSize);
    return new Outline(file, params, cannyMat, hvHoughs, List.of(), bounds);
  }

  private static List<Point> findCorners(Mat grayMat, double thresholdRatio) {
    Mat cornersMat = new Mat();
    Imgproc.cornerHarris(grayMat, cornersMat, 8, 3, 0.04, Core.BORDER_DEFAULT);
    int rows = cornersMat.rows();
    int cols = cornersMat.cols();
    double max = 0;
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        double[] doubles = cornersMat.get(y, x);
        double val = doubles[0];
        if(val > max) {
          max = val;
        }
      }
    }

    double cornerThreshold = thresholdRatio * max;
    ArrayList<Point> corners = new ArrayList<>();
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        if(cornersMat.get(y, x)[0] > cornerThreshold) {
          corners.add(new Point(x, y));
        }
      }
    }

    return corners;
  }

  private static HVHoughs filterCardinal(Mat lines, double angleTolerance) {
    double tolerance = Math.abs(Math.sin(angleTolerance));
    var verticalLines = new ArrayList<Hough>(20);
    var horizontalLines = new ArrayList<Hough>(20);

    for (int i = 0; i < lines.rows(); i++) {
      double[] data = lines.get(i, 0);
      double rho = data[0];
      double theta = data[1];
      if (Math.abs(Math.cos(theta)) < tolerance) {
        horizontalLines.add(new Hough(rho, theta));
      } else if(Math.sin(theta) < tolerance) {
        verticalLines.add(new Hough(rho, theta));
      }
    }

    verticalLines.sort(RHO_COMPARATOR);
    horizontalLines.sort(RHO_COMPARATOR);
    return new HVHoughs(horizontalLines, verticalLines);
  }

  private List<Bounds> findRectangles(Mat cannyMat, HVHoughs hvHoughs, Size originalSize) {
    List<Bounds> bounds = new ArrayList<>();

    // giving preference to horizontal lines, grouping lines that are very close
    // so different horizontal flocks should be close to one photo boundary or the junction of 2 adjacent photos
    int height = cannyMat.rows();
    int width = cannyMat.cols();
    double scale = Math.min(originalSize.width/width, originalSize.height/height);
    List<HoughFlock> h1Flocks = splitBands(hvHoughs.horizontals(), height);

    // Now that the pic is split in horizontal bands
    // Let's try and find which vertical lines account of each photo's boundaries

    HoughFlock prevH1Flock = null;
    for (HoughFlock curH1Flock : h1Flocks) {
      if(prevH1Flock != null) {
        Hough photoTopApprox = prevH1Flock.getLast(), photoBottomApprox = curH1Flock.getFirst();
        // now, which vertical lines have pixels on this interval ?
        double photoTopApproxY = photoTopApprox.getY0(), photoBottomApproxY = photoBottomApprox.getY0();
        List<Hough> eligibleVLines = filterVerticalLinesWithPixels(cannyMat, hvHoughs.verticals(), photoTopApproxY, photoBottomApproxY);

        // and out of those, let's split in vertical bands this time
        HoughFlock prevV1Flock = null;
        for (HoughFlock curV1Flock : splitBands(eligibleVLines, width)) {
          if(prevV1Flock != null) {
            // we could stop here and outline rectangles from the crossing of each horizontal and vertical band
            // but we aren't sure a flock isn't gathering the edges of several photos laid out horizontally
            // and not perfectly aligned vertically (or not the same vertical size)
            // so let's do this third round on horizontal bands again
            Hough photoLeft = getRightest(prevV1Flock, photoTopApproxY, photoBottomApproxY), photoRight = getLeftest(curV1Flock, photoTopApproxY, photoBottomApproxY);
            // now, which horizontal lines have pixels on this interval ?
            double photoTopMin = prevH1Flock.getFirst().getY0(), photoBottomMax = curH1Flock.getLast().getY0();
            List<Hough> boundedHLines = hvHoughs.horizontals()
                .stream()
                .filter(hLine -> {
              double lineY = hLine.getY0();
              return lineY >= photoTopMin && lineY <= photoBottomMax;
            }).toList();
            double photoLeftX = photoLeft.getX0();
            double photoRightX = photoRight.getX0();
            List<Hough> eligibleHLines = filterHorizontalLinesWithPixels(cannyMat, boundedHLines, photoLeftX, photoRightX);
            HoughFlock prevH2Flock = null;
            for (HoughFlock curH2Flock : splitBands(eligibleHLines, height)) {
              if (prevH2Flock != null) {
                Hough photoTop = getBottomest(prevH2Flock, photoLeftX, photoRightX), photoBottom = getTopest(curH2Flock, photoLeftX, photoRightX);
                // and finally outline rectangles from the crossing of each horizontal and vertical band
                //TODO it may be bent a little bit, needs adjustment rotation
                bounds.add(new Bounds(photoTop, photoBottom, photoLeft, photoRight, scale));
              }
              prevH2Flock = curH2Flock;
            }
          }
          prevV1Flock = curV1Flock;
        }
      }
      prevH1Flock = curH1Flock;
    }
    return bounds;
  }



  private static List<HoughFlock> splitBands(List<Hough> lines, int dimension) {
    // on a given horizontal band, the first elements should be the bottom (right when vertical bands) of the previous pic
    // and the last should be the top (left when vertical) of the next
    List<HoughFlock> result = new ArrayList<>();

    HoughFlock working = new HoughFlock();
    Hough previous = null;
    for (Hough line : lines) {
      if(previous == null) {
        working.add(line);
      } else {
        double spacing = (line.getRho() - previous.getRho()) / dimension; // [0..1]
        if(spacing < 0.01) {
          working.add(line);
        } else /*if(spacing > 0.05)*/ { // [1%-5%] might be false positives, we'll see if more logic is needed
          result.add(working);
          working = new HoughFlock();
          working.add(line);
        }
      }
      previous = line;
    }
    if(!working.isEmpty()) {
      result.add(working);
    }
    return result;
  }

  private static List<Hough> filterVerticalLinesWithPixels(Mat canny, List<Hough> verticalLines, double topY, double bottomY) {
    return verticalLines.stream()
        .filter(vLine -> {
          int litPixels = countVerticalLitPixels(canny, vLine, topY, bottomY);
          if (litPixels >= PIXEL_COUNT_THRESHOLD) {
            log.debug("Lit pixels on vertical line {}: {}", vLine, litPixels);
            return true;
          } else {
            return false;
          }
        }).toList();
  }

  private static int countVerticalLitPixels(Mat canny, Hough vLine, double topY, double bottomY) {
    int pixels = 0;
    double p0x = vLine.getX0();
    double p0y = vLine.getY0();
    double gradient = Math.tan(vLine.getTheta());
    log.debug("Finding pixels on y: [{}, {}] on vertical line {}", topY, bottomY, vLine);
    for (double y = topY; y <= bottomY; y++) {
      double x = p0x + (p0y - y) * gradient;
      if (!isBlack(canny, (int) Math.round(x), (int) Math.round(y))) {
        pixels++;
      }
    }
    return pixels;
  }

  private static List<Hough> filterHorizontalLinesWithPixels(Mat canny, List<Hough> horizontalLines, double leftX, double rightX) {
    return horizontalLines.stream()
        .filter(hLine -> {
          int litPixels = countHorizontalLitPixels(canny, hLine, leftX, rightX);
          if (litPixels >= PIXEL_COUNT_THRESHOLD) {
            log.debug("Lit pixels on horizontal line {}: {}", hLine, litPixels);
            return true;
          } else {
            return false;
          }
        }).toList();
  }

  private static int countHorizontalLitPixels(Mat canny, Hough hLine, double leftX, double rightX) {
    int pixels = 0;
    double p0x = hLine.getX0();
    double p0y = hLine.getY0();
    double theta = hLine.getTheta();
    double gradient = Math.cos(theta) / Math.sin(theta); // NOT 1/tan cause tan(π/2) → ∞
    log.debug("Finding pixels on x: [{}, {}] on horizontal line {}", leftX, rightX, hLine);
    for (double x = leftX; x <= rightX; x++) {
      double y = p0y + (p0x - x) * gradient;
      int px = (int) Math.round(x);
      int py = (int) Math.round(y);
      if (px >= 0 && py >+ 0 && !isBlack(canny, px, py)) {
        pixels++;
      }
    }
    return pixels;
  }

  public static boolean isBlack(Mat canny, int x, int y) {
    double[] doubles = canny.get(y, x);
    return Arrays.equals(doubles, CANNY_BLACK);
  }

  public static Hough getLeftest(HoughFlock flock, double minY, double maxY) {
    double avgY = (minY + maxY)/2;
    return flock.stream().min(Comparator.comparing(h -> h.getX(avgY))).orElse(null);
  }

  public static Hough getRightest(HoughFlock flock, double minY, double maxY) {
    double avgY = (minY + maxY)/2;
    return flock.stream().max(Comparator.comparing(h -> h.getX(avgY))).orElse(null);
  }

  public static Hough getTopest(HoughFlock flock, double minX, double maxX) {
    double avgX = (minX + maxX)/2;
    return flock.stream().min(Comparator.comparing(h -> h.getY(avgX))).orElse(null);
  }

  public static Hough getBottomest(HoughFlock flock, double minX, double maxX) {
    double avgX = (minX + maxX)/2;
    return flock.stream().max(Comparator.comparing(h -> h.getY(avgX))).orElse(null);
  }
}
