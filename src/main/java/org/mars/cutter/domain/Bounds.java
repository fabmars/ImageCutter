package org.mars.cutter.domain;

import java.util.List;
import org.opencv.core.Point;

public record Bounds(
    Hough h1, Hough h2,
    Hough v1, Hough v2,
    double scale
) {

  public List<Segment> toSegments() {
    Point p1 = h1.intersect(v1);
    Point p2 = h1.intersect(v2);
    Point p3 = h2.intersect(v2);
    Point p4 = h2.intersect(v1);

    return List.of(
        new Segment(p1, p2, h1),
        new Segment(p2, p3, v2),
        new Segment(p3, p4, h2),
        new Segment(p4, p1, v1)
    );
  }
}
