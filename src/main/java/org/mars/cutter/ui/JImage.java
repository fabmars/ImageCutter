package org.mars.cutter.ui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.Quadri;
import org.mars.cutter.domain.Segment;
import org.opencv.core.Point;

@Slf4j
@Getter
public class JImage extends JComponent {

  private static final Dimension NO_DIMENSION = new Dimension(0, 0);
  public static final int RECTANGLE_STROKE = 2;

  private BufferedImage image;
  private Dimension scaledSize;
  private double ratio; // (canny)image to display ratio
  private Map<Quadri, JRotate> quadris = new HashMap<>();

  public JImage() {
    setLayout(null);
  }

  public void clean() {
    setImage(null);
  }

  public void setImage(BufferedImage image) {
    this.image = image;
    scaledSize = null;
    ratio = 0;
    quadris.clear();
    removeAll();
  }

  @Override
  public Dimension getPreferredSize() {
    return image == null ? super.getPreferredSize() : new Dimension(image.getWidth(), image.getHeight());
  }


  @Override
  public void repaint() {
    if(image != null) {
      this.ratio = Math.min((double)getWidth()/image.getWidth(), (double)getHeight()/image.getHeight());
      this.scaledSize = new Dimension((int)(image.getWidth() * ratio), (int)(image.getHeight() * ratio));
    } else {
      this.ratio = 1.0; // so it won't cause a divbyzero anywhere
      this.scaledSize = NO_DIMENSION;
    }

    super.repaint(); // will call repaint(coords) below
  }

  @Override
  public void repaint(long tm, int x, int y, int width, int height) {
    recenterJRotateTools();
    super.repaint(tm, x, y, width, height);
  }

  private void recenterJRotateTools() {
    quadris.forEach((quadri, jRotate) -> {
      java.awt.Point displayCoords = toDisplayCoords(quadri.getCenter());
      Dimension ps = jRotate.getPreferredSize();
      jRotate.setBounds(displayCoords.x-ps.width/2, displayCoords.y+ps.height/4, ps.width, ps.height);
    });
  }

  @Override
  protected void paintComponent(Graphics g) {
    if(image != null) {
      g.drawImage(image, 0, 0, scaledSize.width, scaledSize.height, this);
      drawQuadris((Graphics2D) g);
    }
  }

  private void drawQuadris(Graphics2D g) {
    g.setStroke(new BasicStroke(RECTANGLE_STROKE));
    for (Quadri quadri : quadris.keySet()) {
      g.setColor(quadri.getColor());
      for (Segment segment : quadri.toSegments()) {
        java.awt.Point p0 = toDisplayCoords(segment.p0());
        java.awt.Point p1 = toDisplayCoords(segment.p1());
        g.drawLine(p0.x, p0.y, p1.x, p1.y);
      }
    }
  }


  public void addQuadri(Quadri quadri) {
    JRotate jRotate = new JRotate(quadri);
    add(jRotate);
    quadris.put(quadri, jRotate);
  }

  public void removeQuadri(Quadri quadri) {
    JRotate jRotate = quadris.remove(quadri);
    remove(jRotate);
    revalidate();
  }

  public double toImageScale(double d) {
    return d / ratio;
  }

  public Point toImageCoords(java.awt.Point displayPoint) {
    return new Point(toImageScale(displayPoint.x), toImageScale(displayPoint.y));
  }

  public double toDisplayScale(double d) {
    return d * ratio;
  }

  public java.awt.Point toDisplayCoords(java.awt.Point imagePoint) {
    return new java.awt.Point((int)toDisplayScale(imagePoint.x), (int)toDisplayScale(imagePoint.y));
  }

  public java.awt.Point toDisplayCoords(Point imagePoint) {
    return new java.awt.Point((int)toDisplayScale(imagePoint.x), (int)toDisplayScale(imagePoint.y));
  }

  public void addMouseOperationsListener(MouseInputListener l) {
    addMouseListener(l);
    addMouseMotionListener(l);
  }
}
