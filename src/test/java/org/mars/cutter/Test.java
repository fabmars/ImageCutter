package org.mars.cutter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {

  public static void main(String[] args) throws IOException {

    Path file = Paths.get("E:\\Photos Scans\\Kléber et Georgette\\A découper\\1980-1981 Chez les Blanchard (peut-être)\\img059.bmp");
    new RectangleDetector().process(file);
  }
}
