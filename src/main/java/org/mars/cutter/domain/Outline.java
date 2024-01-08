package org.mars.cutter.domain;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;
import org.mars.cutter.DetectionParams;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;

public record Outline(
    Path path,
    DetectionParams params, Mat cannyColor,
    HVHoughs hvHoughs,
    Collection<Point> corners,
    Collection<Bounds> bounds) {

  public BufferedImage toImage() {
    return (BufferedImage)HighGui.toBufferedImage(cannyColor);
  }

  public Size size() {
    return cannyColor.size();
  }

  public Stream<Hough> stream() {
    return hvHoughs.stream();
  }
}
