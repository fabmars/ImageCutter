package org.mars.cutter;

public record DetectionParams(
    int tempDimension,
    double cannyThreshold,
    double cannyRatio,
    double houghTheta,
    int houghThreshold,
    double hvAngleTolerance
) {

  public double cannyThreshold2() {
    return cannyThreshold * cannyRatio;
  }
}
