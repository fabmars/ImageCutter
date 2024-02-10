package org.mars.cutter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public final class DetectionParams {

  public static final int TEMP_DIMENSION = 3000;
  public static final double CANNY_THRESHOLD = 40.0;
  public static final double CANNY_RATIO = 6.0; // Canny recommends 3 but a higher value works better in our case
  public static final int HOUGH_THRESHOLD = 300; // higher threshold will reduce the amount of lines
  public static final double HOUGH_THETA = Math.PI/(180*5);
  public static final double HV_THRESHOLD = Math.PI / 180.0 * 0.75; // How much of an angle tolerance am I allowing to declare whether the line is horizontal or vertical
  public static final DetectionParams DEFAULTS = new DetectionParams(TEMP_DIMENSION, CANNY_THRESHOLD, CANNY_RATIO, HOUGH_THETA, HOUGH_THRESHOLD, HV_THRESHOLD);

  private final int tempDimension;
  private final double cannyThreshold;
  private final double cannyRatio;
  private final double houghTheta;
  private final int houghThreshold;
  private final double hvAngleTolerance;

  public DetectionParams(DetectionParams params) {
    this(params.getTempDimension(), params.cannyThreshold, params.cannyRatio, params.houghTheta, params.houghThreshold, params.hvAngleTolerance);
  }

  public double cannyThreshold2() {
    return cannyThreshold * cannyRatio;
  }
}
