package org.mars.cutter.domain;

import org.opencv.core.Point;

public record Segment(Point p1, Point p2, Hough hough) {

  public double x1() {
    return p1.x;
  }

  public double y1() {
    return p1.y;
  }

  public double x2() {
    return p2.x;
  }

  public double y2() {
    return p2.y;
  }
}
