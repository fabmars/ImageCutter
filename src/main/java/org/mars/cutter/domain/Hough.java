package org.mars.cutter.domain;

import lombok.Getter;
import lombok.ToString;
import org.opencv.core.Point;

@ToString(onlyExplicitlyIncluded = true)
public final class Hough {
  public static final double HALF_PI = Math.PI / 2;

  @Getter @ToString.Include
  private final double rho;
  @Getter @ToString.Include
  private final double theta;

  @Getter
  private final double x0;
  @Getter
  private final double y0;

  private final double gradient;
  private final double invGradient;


  public Hough(double rho, double theta) {
    this.rho = rho;
    this.theta = theta;

    this.x0 = rho * Math.cos(theta);
    this.y0 = rho * Math.sin(theta);
    this.gradient = theta == HALF_PI ? Double.NaN : Math.tan(theta);
    this.invGradient = theta == 0 ? Double.NaN : Math.cos(theta) / Math.sin(theta); // NOT 1/tan cause tan(π/2) → ∞
  }

  public double getX(double y) {
    if (theta == HALF_PI) {
      throw new IllegalArgumentException("Theta == π/2");
    }
    return x0 + (y0 - y) * gradient;
  }

  public double getY(double x) {
    if (theta == 0.0) {
      throw new IllegalArgumentException("Theta == 0.0");
    }
    return y0 + (x0 - x) * invGradient;
  }

  public Point intersect(Hough h2) {
    double cos1 = Math.cos(theta);
    double sin1 = Math.sin(theta);
    double cos2 = Math.cos(h2.theta);
    double sin2 = Math.sin(h2.theta);

    double s2c1 = sin2 * cos1;
    double s1c2 = sin1 * cos2;
    if (s1c2 == s2c1) {
      throw new IllegalArgumentException("The 2 lines are parallel or identical");
    }

    double x = (sin1 * sin2 * (y0 - h2.y0) + x0 * s2c1 - h2.x0 * s1c2) / (s2c1 - s1c2);
    double y = theta != 0 ? getY(x) : h2.getY(x); // one will work since angles are different
    return new Point(x, y);
  }

}