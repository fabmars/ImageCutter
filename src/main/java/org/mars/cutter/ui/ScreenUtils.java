package org.mars.cutter.ui;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

public final class ScreenUtils {

  public static GraphicsEnvironment getGraphicsEnvironment() {
    return GraphicsEnvironment.getLocalGraphicsEnvironment();
  }

  public static GraphicsDevice[] getScreenDevices() {
    return getGraphicsEnvironment().getScreenDevices();
  }

  public static GraphicsDevice getDefaultScreenDevice() {
    return getGraphicsEnvironment().getDefaultScreenDevice();
  }

  public static int getScreenCount() {
    return getScreenDevices().length;
  }

  public static Rectangle getBounds(int screen) {
    Rectangle bounds = null;
    GraphicsDevice[] gs = getScreenDevices();
    if (screen >= 0 && gs.length > screen) {
      bounds = getBounds(gs[screen]);
    }
    return bounds;
  }

  public static Rectangle getBounds(GraphicsDevice screen) {
    Rectangle bounds = null;
    if (screen != null) {
      GraphicsConfiguration[] gc = screen.getConfigurations();
      if (gc.length > 0) {
        bounds = gc[0].getBounds();
      }
    }
    return bounds;
  }

  public static Rectangle getVirtualBounds() {
    Rectangle virtualBounds = new Rectangle();
    GraphicsDevice[] gs = getScreenDevices();
    for (int j = 0; j < gs.length; j++) {
      GraphicsConfiguration[] gc = gs[j].getConfigurations();
      for (int i = 0; i < gc.length; i++) {
        virtualBounds = virtualBounds.union(gc[i].getBounds());
      }
    }
    return virtualBounds;
  }

  public static int getScreen(Point p) {
    GraphicsDevice[] gs = getScreenDevices();
    for (int j = 0; j < gs.length; j++) {
      GraphicsConfiguration[] gc = gs[j].getConfigurations();
      for (int i = 0; i < gc.length; i++) {
        Rectangle bounds = gc[i].getBounds();
        if (bounds.contains(p)) {
          return j;
        }
      }
    }
    return -1;
  }

}
