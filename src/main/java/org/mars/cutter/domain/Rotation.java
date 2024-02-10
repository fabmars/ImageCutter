package org.mars.cutter.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * In Swing rotating with a positive angle rotates points on the positive x axis (right) toward the positive y axis (down)
 * That's why one clockwise π/2 rotation is 1 quadrant instead of 3.
 */
@AllArgsConstructor @Getter
public enum Rotation {
  UP(0),
  ANTI(3),
  CLOCK(1),
  DOWN(2);

  private final int quadrants;
}
