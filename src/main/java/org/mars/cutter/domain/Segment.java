package org.mars.cutter.domain;

import org.opencv.core.Point;

public record Segment(Point p0, Point p1) {

  public static final double TWO_PI = Math.PI * 2.0;

  public double x1() {
    return p0.x;
  }

  public double y1() {
    return p0.y;
  }

  public double x2() {
    return p1.x;
  }

  public double y2() {
    return p1.y;
  }


  /**
   * @return the angle of the segment from the first to the second point
   * The range is always [0, 2π]
   * If the segments are issued from a quadri built left-to-right, top-to-bottom, right-to-left, bottom-to-top:
   *  - on a rectangle, segments angles are 0, 3π/2, π, π/2
   *  - on a parallelogram, opposite segments have an angle difference of π
   */
  public double angle() {
    var angle = Math.atan2(p0.y - p1.y, p1.x - p0.x); // remember y's increase downwards
    return angle + Math.ceil( -angle / TWO_PI ) * TWO_PI;
  }
}
