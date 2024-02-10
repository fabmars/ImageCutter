package org.mars.cutter.domain;

import java.util.List;
import org.opencv.core.Point;

public record Bounds(
    Hough h1, Hough h2,
    Hough v1, Hough v2
) {

  public List<Point> toPoints() {
    return List.of(getP0(), getP1(), getP2(), getP3());
  }

  public Point getP0() {
    return h1.intersect(v1);
  }

  public Point getP1() {
    return h1.intersect(v2);
  }

  public Point getP2() {
    return h2.intersect(v2);
  }

  public Point getP3() {
    return h2.intersect(v1);
  }


  public List<Segment> toSegments() {
    Point p0 = getP0();
    Point p1 = getP1();
    Point p2 = getP2();
    Point p3 = getP3();

    return List.of(
        new Segment(p0, p1),
        new Segment(p1, p2),
        new Segment(p2, p3),
        new Segment(p3, p0)
    );
  }
}
