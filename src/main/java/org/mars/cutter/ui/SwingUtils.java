package org.mars.cutter.ui;

import java.awt.GridBagConstraints;

public final class SwingUtils {

  private SwingUtils() {}

  public static GridBagConstraints newGridBagConstraints(int gridx, int gridy, int gridWidth, int gridHeight, double weightx, double weighty, int anchor, int fill) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = gridx;
    c.gridy = gridy;
    c.gridwidth = gridWidth;
    c.gridheight = gridHeight;
    c.weightx = weightx;
    c.weighty = weighty;
    c.anchor = anchor;
    c.fill = fill;
    return c;
  }
}
