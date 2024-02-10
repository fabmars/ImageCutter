package org.mars.cutter;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.util.FileUtils;
import org.mars.cutter.util.ImageUtils;
import org.opencv.core.Rect2d;

@Slf4j
public class CutterService {
  public static final List<String> IMAGE_EXTENSIONS_ACCEPTED = Arrays.asList("bmp", "png");
  public static final String QUADRI_EXTENSION = "png";

  public static final String PREFIX_DONE = "done-";
  public static final String PREFIX_CROPPING = "cropping-";
  public static final String PREFIX_QUADRI = "quadri-";
  private static final List<String> SKIPPED_PREFIXES = Arrays.asList(PREFIX_DONE, PREFIX_QUADRI);



  private Path currentImagePath;
  private final Iterator<Path> iterator;

  public CutterService(Path rootDir) throws IOException {
    try(Stream<Path> stream = Files.walk(rootDir)
        .filter(path -> Files.isRegularFile(path)
            && IMAGE_EXTENSIONS_ACCEPTED.contains(FileUtils.getExt(path))
            && !knownPrefix(path))) {
      iterator = stream.toList().iterator();
    }
  }

  private boolean knownPrefix(Path path) {
    String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
    return SKIPPED_PREFIXES.stream().anyMatch(fileName::startsWith);
  }

  public ImageContext reset(DetectionParams params) throws IOException {
    log.info("Resetting: {}", currentImagePath);
    return analyzeImage(currentImagePath, params);
  }

  public ImageContext skip(DetectionParams params) throws IOException {
    if(iterator.hasNext()) {
      Path nextImage = iterator.next();
      String fileName = nextImage.getFileName().toString();
      if(fileName.startsWith(PREFIX_CROPPING)) {
        nextImage = Files.move(nextImage, nextImage.resolveSibling(fileName.substring(PREFIX_CROPPING.length())));
      }
      currentImagePath = nextImage;
      log.info("Analyzing: {}", currentImagePath);
      return analyzeImage(currentImagePath, params);
    } else {
      log.info("No more images to process");
      return null;
    }
  }


  private static boolean async = true;

  public void confirm(ImageContext imageContext) {
    if(imageContext.getQuadris().isEmpty()) {
      log.info("Nothing to crop in: {}", imageContext.getFile());
      return;
    }

    log.info("Start cropping: {}", imageContext.getFile());

    Path croppingFile = imageContext.getFile().resolveSibling(PREFIX_CROPPING + imageContext.getFileName());
    try {
      Files.move(imageContext.getFile(), croppingFile);
    } catch (IOException e) {
      log.error("Cannot rename {} to {}, aborting cropping", imageContext.getFile(), croppingFile);
      return;
    }

    BufferedImage originalImage = imageContext.getOriginalImage();
    List<Quadri> quadris = imageContext.getQuadrisOriginal();
    List<Path> quadriFiles = getUnusedImageFiles(imageContext.getFile().getParent(), PREFIX_QUADRI, '.'+QUADRI_EXTENSION, quadris.size());
    CompletableFuture[] futures = new CompletableFuture[quadris.size()];

    for (int q = 0; q < quadris.size(); q++) {
      Quadri quadri = quadris.get(q);
      Path quadriFile = quadriFiles.get(q);
      futures[q] = async
          ? CompletableFuture.runAsync(() -> cutAndSaveQuadri(originalImage, quadri, quadriFile))
          : CompletableFuture.completedFuture(cutAndSaveQuadri(originalImage, quadri, quadriFile));
    }

    CompletableFuture.allOf(futures).thenRun(() -> {
      Path doneFile = imageContext.getFile().resolveSibling(PREFIX_DONE + imageContext.getFileName());
      try {
        Files.move(croppingFile, doneFile);
      } catch (IOException e) {
        log.error("Cannot rename {} to {}", croppingFile, doneFile);
      }
      log.info("Finished cropping: {}", imageContext.getFile());
    });
  }

  private boolean cutAndSaveQuadri(BufferedImage originalImage, Quadri quadri, Path quadriFile) {
    BufferedImage quadriImage = cutQuadri(originalImage, quadri);
    try {
      return ImageIO.write(quadriImage, QUADRI_EXTENSION, quadriFile.toFile());
    } catch (IOException e) {
      log.error("Cannot save quadri from image: {}", originalImage);
      return false;
    }
  }

  private BufferedImage cutQuadri(BufferedImage originalImage, Quadri quadri) {
    log.info("quadri: {}", quadri);

    double quadriAngle;
    double[] angles = quadri.toAngles();
    double a0 = angles[0], a1 = angles[1], a2 = angles[2], a3 = angles[3];
    // trying to find which opposite sides are the most parallel
    if(Math.abs(a0 - a2) > Math.abs(a1 - a3)) { // sign inverted cause we should be comparing π-abs(...)
      quadriAngle = a2 - Math.PI; // the base, arbitrarily
      //log.info("angle base: {}", quadriAngle);
    } else {
      quadriAngle = a3 - Math.PI/2.0; // the left side, arbitrarily
      //log.info("angle left: {}", quadriAngle);
    }
    //angle = angle % (Math.PI/2.0); // we don't know if the angle is a vertical or horizontal one
    log.info("angle {}", quadriAngle);

    // crop the quadri out of the original image
    Rect2d enclosingRect = quadri.getEnclosingRectNoRotation(); // contains the outlined pic, possibly slightly rotated, and some chunks on the edges left by skewed borders
    Rectangle quadriRect = new Rectangle((int)enclosingRect.x, (int)enclosingRect.y, (int)enclosingRect.width, (int)enclosingRect.height);
    //log.info("enclosing rect: {}", quadriRect);
    BufferedImage enclosingImage = ImageUtils.createBufferedImage(ImageUtils.COLOR_MODEL_RGBA, quadriRect.width, quadriRect.height);

    // clip so we keep only the pixels from the actual quadri bounds, thus weeding out the chunks on the edges left by skewed borders
    Graphics g = enclosingImage.getGraphics();
    Polygon clipShape = quadri.toShape();
    clipShape.translate(-quadriRect.x, -quadriRect.y);
    g.setClip(clipShape);

    g.drawImage(originalImage, 0, 0, quadriRect.width -1, quadriRect.height-1,
        quadriRect.x, quadriRect.y, (quadriRect.x+quadriRect.width)-1, (quadriRect.y+quadriRect.height)-1, null);

    // Now we still need to apply angle and quadri rotation and trim usless pixels
    // So we're going to rotate around the enclosing rectangle's top-left coordinates
    // and apply rotated pixels to the actual final picture dimensions (no longer an enclosing one)
    // after translating so useless pixels are automatically trimmed out
    Quadri recenteredQuadri = quadri.translate(-enclosingRect.x, -enclosingRect.y);
    Rect2d enclosingRotated = recenteredQuadri.rotate(quadriAngle, true)
        .getEnclosingRectNoRotation(); // so we know the final picture dimensions
    BufferedImage quadriImage = ImageUtils.createBufferedImage(ImageUtils.COLOR_MODEL_RGBA, (int)Math.round(enclosingRotated.width), (int)Math.round(enclosingRotated.height));

    AffineTransform at = AffineTransform.getRotateInstance(quadriAngle);
    at.quadrantRotate(quadri.getRotation().getQuadrants());
    ((Graphics2D) quadriImage.getGraphics()).drawImage(enclosingImage, new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC), -(int)Math.round(enclosingRotated.x), -(int)Math.round(enclosingRotated.y));

    return quadriImage;
  }


  private static List<Path> getUnusedImageFiles(Path parent, String prefix, String suffix, int count) {
    return getUnusedImageFiles(parent, prefix, suffix, 1, count);
  }

  private static List<Path> getUnusedImageFiles(Path parent, String prefix, String suffix, int from, int count) {
    Path path;
    var files = new ArrayList<Path>(count);

    for (int c = 0; c < count;) {
      String name = prefix + String.format("%02d", from) + suffix;
      path = parent.resolve(name);

      if(!Files.exists(path)) {
        files.add(path);
        c++;
      }
      from++;
    }

    return files;
  }


  public ImageContext analyzeImage(Path path, DetectionParams params) throws IOException {
    ImageContext currentContext = new ImageContext(path);
    currentContext.detectOutline(params);
    return currentContext;
  }
}
