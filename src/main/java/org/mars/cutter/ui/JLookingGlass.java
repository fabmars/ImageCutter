package org.mars.cutter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;

@Slf4j
@Getter
public class JLookingGlass extends Component {

  @Setter
  private Image image;
  private Point center; // those are image coords, not screen coords

  @Override
  public Dimension getPreferredSize() {
    GraphicsDevice screenDevice = ScreenUtils.getDefaultScreenDevice();
    Rectangle screenBounds = ScreenUtils.getBounds(screenDevice);
    int dim = screenBounds.width / 8;
    return new Dimension(dim, dim);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  public void paint(Graphics g) {
    Dimension size = getSize();
    if(image != null && center != null) {
      int halfHeight = size.height / 2;
      int halfWidth = size.width / 2;
      g.drawImage(image, 0, 0, size.width - 1, size.height - 1,
          (int)(center.x - halfWidth), (int)(center.y - halfHeight), (int)(center.x + halfWidth), (int)(center.y + halfHeight),
          this);

      // crosshair
      g.setColor(Color.black);
      g.drawLine(halfWidth, 0, halfWidth, size.height-1);
      g.drawLine(0, halfHeight, size.width-1, halfHeight);
    }

    g.setColor(Color.black);
    g.drawRect(0, 0, size.width-1, size.height-1);
  }

  public void centerOn(Point center) {
    this.center = center;
    repaint();
  }
}
