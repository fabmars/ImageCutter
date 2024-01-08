package org.mars.cutter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.util.FileUtils;

@Slf4j
public class CutterController {
  public static final List<String> IMAGE_EXTENSIONS_ACCEPTED = List.of("bmp", "png");

  private Path currentImage;
  private final Iterator<Path> iterator;

  public CutterController(Path rootDir) throws IOException {
    try(Stream<Path> stream = Files.walk(rootDir)
        .filter(path -> Files.isRegularFile(path) && IMAGE_EXTENSIONS_ACCEPTED.contains(FileUtils.getExt(path)))) {
      iterator = stream.toList().iterator();
    }
  }

  private static BufferedImage process(Path imagePath) throws ImageLoadingException {
    try {
      BufferedImage image = ImageIO.read(imagePath.toFile());
      //TODO process lines
      return image;
    } catch (IOException e) {
      throw new ImageLoadingException(imagePath.getFileName().toString(), e);
    }
  }

  public BufferedImage reset() throws ImageLoadingException {
    log.info("Resetting: {}", currentImage);
    return process(currentImage);
  }

  public BufferedImage skip() throws ImageLoadingException {
    if(iterator.hasNext()) {
      currentImage = iterator.next();
      log.info("Moving to: {}", currentImage);
      return process(currentImage);
    } else {
      log.info("No more images to process");
      return null;
    }
  }

  public BufferedImage confirm() throws ImageLoadingException {
    //TODO
    return skip();
  }
}
