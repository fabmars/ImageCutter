package org.mars.cutter;

import static org.mars.cutter.util.FileUtils.dotExtension;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mars.cutter.domain.Bounds;
import org.mars.cutter.domain.Outline;
import org.mars.cutter.domain.Segment;
import org.mars.cutter.util.FileUtils;
import org.mars.cutter.util.ImageUtils;
import org.opencv.core.Point;
import org.opencv.core.Size;

@Getter
@AllArgsConstructor
public final class ImageContext {

  public static final String TEMP_FORMAT = "bmp";
  public static final String DESTINATION_FORMAT = "jpg";

  public static final List<Color> BOUNDS_COLORS = List.of(Color.yellow, Color.green, Color.blue, Color.red, Color.gray, Color.orange, Color.cyan, Color.magenta, Color.pink, Color.black);


  private final Path file;

  private BufferedImage originalImage;
  private Size originalSize;
  private double ratio;
  private Path tempFile;
  private BufferedImage tempImage;
  private Outline outline;

  private List<Quadri> quadris;

  public ImageContext(Path file) {
    this.file = file;
  }


  public String getFileName() {
    return file.getFileName().toString();
  }

  public void detectOutline(DetectionParams params) throws IOException {
    if(tempFile == null) {
      String[] fileExt = FileUtils.getFileExt(file);

      this.tempFile = Files.createTempFile(null, dotExtension(fileExt[1]));
      this.originalImage = ImageIO.read(file.toFile());
      this.originalSize = new Size(originalImage.getWidth(), originalImage.getHeight());
      this.tempImage = ImageUtils.scaleBigger(originalImage, params.getTempDimension(), params.getTempDimension(), (img, infoflags, x, y, width, height) -> false);
      ImageIO.write(tempImage, TEMP_FORMAT, tempFile.toFile());
      this.ratio = (double)tempImage.getWidth() / (double)originalImage.getWidth();
    }
    this.outline = RectangleDetector.detectOutline(tempFile, params, originalSize);
    this.quadris = computeQuadris(outline.bounds());
  }

  public void clean() throws IOException {
    if(tempFile != null) {
      Files.delete(tempFile);
      tempFile = null;
    }
    tempImage = null;
    originalImage = null;
    originalSize = null;
    ratio = 0;
    outline = null;
    quadris = null;
  }

  public BufferedImage drawImage() throws IOException {
    BufferedImage image = ImageIO.read(tempFile.toFile());
    drawQuadris(image, quadris);
    return image;
  }

  public static void drawOutline(BufferedImage image, Outline outline) {
    drawQuadris(image, computeQuadris(outline.bounds()));
  }

  private static List<Quadri> computeQuadris(Collection<Bounds> boundz) {
    List<Quadri> quadris = new ArrayList<>(boundz.size());
    int i = 0;
    for (Bounds bounds : boundz) {
      quadris.add(new Quadri("Detect " + (i+1), bounds, getColor(i++)));
    }
    return quadris;
  }

  public static void drawLines(BufferedImage image, Outline outline, Color linesColor) {
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

  public static void drawQuadris(BufferedImage image, Collection<Quadri> quadris) {
    Graphics2D g = (Graphics2D) image.getGraphics();
    g.setStroke(new BasicStroke(3));

    for (Quadri quadri : quadris) {
      g.setColor(quadri.getColor());
      for (Segment segment : quadri.toSegments()) {
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

  public void add(Quadri quadri) {
    quadris.add(quadri);
  }

  public void remove(Quadri quadri) {
    quadris.remove(quadri);
  }

  public static Color getColor(int i) {
    return BOUNDS_COLORS.get(i % BOUNDS_COLORS.size());
  }

  public Color getNextColor() {
    return getColor(quadris.size());
  }


  public double toOriginalScale(double d) {
    return d / ratio;
  }

  public Point toOriginalCoords(Point tempPoint) {
    return new Point(toOriginalScale(tempPoint.x), toOriginalScale(tempPoint.y));
  }

  public List<Quadri> getQuadrisOriginal() {
    return quadris.stream().map(this::toOriginal).toList();
  }

  public Quadri toOriginal(Quadri quadri) {
    return new Quadri(quadri.getName(),
        toOriginalCoords(quadri.getP0()),
        toOriginalCoords(quadri.getP1()),
        toOriginalCoords(quadri.getP2()),
        toOriginalCoords(quadri.getP3()),
        quadri.getColor(),
        quadri.getRotation()
    );
  }
}
