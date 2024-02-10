package org.mars.cutter;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Polygon;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.mars.cutter.domain.Bounds;
import org.mars.cutter.domain.Rotation;
import org.mars.cutter.domain.Segment;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;

@Getter @Setter
public class Quadri implements Serializable {

  private String name;
  private Point p0;
  private Point p1;
  private Point p2;
  private Point p3;
  private Rotation rotation;
  private Color color;


  public Quadri(String name, Point p0, Point p2, Color color) {
    this(name, p0, new Point(p2.x, p0.y), p2, new Point(p0.x, p2.y), color);
  }

  public Quadri(String name, Bounds bounds, Color color) {
    this(name, bounds.getP0(), bounds.getP1(), bounds.getP2(), bounds.getP3(), color);
  }

  public Quadri(String name, Point p0, Point p1, Point p2, Point p3, Color color) {
    this(name, p0, p1, p2, p3, color, Rotation.UP);
  }

  public Quadri(String name, Point p0, Point p1, Point p2, Point p3, Color color, Rotation rotation) {
    this.name = name;
    this.p0 = p0;
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
    this.color = color;
    this.rotation = rotation;
  }


  public void set(Quadri q) {
    p0.x = q.p0.x;
    p0.y = q.p0.y;

    p1.x = q.p1.x;
    p1.y = q.p1.y;

    p2.x = q.p2.x;
    p2.y = q.p2.y;

    p3.x = q.p3.x;
    p3.y = q.p3.y;
  }

  public Quadri clone() {
    return new Quadri(name, p0.clone(), p1.clone(), p2.clone(), p3.clone(), color, rotation);
  }


  public Quadri translate(double dx, double dy) {
    return new Quadri(name,
        new Point(p0.x + dx, p0.y + dy),
        new Point(p1.x + dx, p1.y + dy),
        new Point(p2.x + dx, p2.y + dy),
        new Point(p3.x + dx, p3.y + dy),
        color,
        rotation);
  }

  public Quadri rotate(double angle, boolean withQuadrants) {
    if(withQuadrants && rotation != Rotation.UP) {
      angle = angle + rotation.getQuadrants() * Math.PI / 2.0;
    }
    double cos = Math.cos(angle), sin = Math.sin(angle);
    Point r0 = new Point(p0.x * cos - p0.y * sin, p0.x * sin + p0.y * cos);
    Point r1 = new Point(p1.x * cos - p1.y * sin, p1.x * sin + p1.y * cos);
    Point r2 = new Point(p2.x * cos - p2.y * sin, p2.x * sin + p2.y * cos);
    Point r3 = new Point(p3.x * cos - p3.y * sin, p3.x * sin + p3.y * cos);
    return new Quadri(name, r0, r1, r2, r3, color, withQuadrants ? Rotation.UP : rotation);
  }


  public Stream<Point> stream() {
    return Stream.of(p0, p1, p2, p3);
  }

  public List<Point> toPoints() {
    return List.of(p0, p1, p2, p3);
  }

  public List<Segment> toSegments() {
    return List.of(
        new Segment(p0, p1),
        new Segment(p1, p2),
        new Segment(p2, p3),
        new Segment(p3, p0)
    );
  }

  public double[] toAngles() {
    return toSegments().stream().mapToDouble(Segment::angle).toArray();
  }

  public Point getCenter() {
    return new Point((getLeftest().x + getRightest().x)/2, (getTopest().y + getBottomest().y)/2);
  }

  public Point getLeftest() {
    return stream().min(Comparator.comparingDouble(p -> p.x)).get();
  }
  public Point getRightest() {
    return stream().max(Comparator.comparingDouble(p -> p.x)).get();
  }
  public Point getTopest() {
    return stream().min(Comparator.comparingDouble(p -> p.y)).get();
  }
  public Point getBottomest() {
    return stream().max(Comparator.comparingDouble(p -> p.y)).get();
  }


  public Rect2d getEnclosingRectNoRotation() {
    Point leftest = getLeftest(), rightest = getRightest();
    Point topest = getTopest(), bottomest = getBottomest();
    return new Rect2d(leftest.x, topest.y, (rightest.x - leftest.x), (bottomest.y - topest.y));
  }

  public Rect2d getEnclosingRectRotation() {
    return getEnclosingRectRotation(0, true);
  }

  public Rect2d getEnclosingRectRotation(double angle, boolean withQuadrants) {
    Rect2d rect = getEnclosingRectNoRotation();
    double w = rect.width, h = rect.height;

    if(withQuadrants && rotation != Rotation.UP) {
      angle = angle + rotation.getQuadrants() * Math.PI / 2.0;
    }
    double cos = Math.cos(angle), sin = Math.sin(angle);

    // anchor point is 0,0
    double x1 = w * cos, y1 = w * sin; //x,0
    double x3 = - h * sin, y3 = h * cos; //0,y
    double x2 = x1 + x3, y2 = y1 + y3; //x,y

    double xmin = min(min(min(x1, x2), x3), .0);
    double ymin = min(min(min(y1, y2), y3), .0);
    double xmax = max(max(max(x1, x2), x3), .0);
    double ymax = max(max(max(y1, y2), y3), .0);

    return new Rect2d(xmin, ymin, xmax - xmin, ymax - ymin);
  }



  public Polygon toShape() {
    Point[] points = {p0, p1, p2, p3};
    int count = points.length;
    int[] x = new int[count];
    int[] y = new int[count];
    for (int i = 0; i < count; i++) {
      Point point = points[i];
      x[i] = (int)Math.round(point.x);
      y[i] = (int)Math.round(point.y);
    }
    return new Polygon(x, y, 4);
  }

  @Override
  public String toString() {
    return toPoints().toString();
  }
}
