package org.mars.cutter.domain;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import org.mars.cutter.DetectionParams;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;

public record Outline(
    Path path, DetectionParams params,
    Size originalSize, Mat cannyMat,
    HVHoughs hvHoughs,
    List<Point> corners,
    List<Bounds> bounds) {

  public BufferedImage getCannyImage() {
    return (BufferedImage)HighGui.toBufferedImage(cannyMat);
  }

  public Size cannySize() {
    return cannyMat.size();
  }

  public Size originalSize() {
    return originalSize;
  }

  public double scale() {
    return Math.min(originalSize.width/cannyMat.width(), originalSize.height/cannyMat.height());
  }
}
