package org.mars.cutter.ui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.Quadri;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;

@Slf4j
public class JQuadriDragger extends MouseInputAdapter {

  private final CutterController controller;

  private JQuadriControl selectedRectangle;
  private Quadri originalQuadri;

  private Point basePoint;

  public JQuadriDragger(CutterController controller) {
    this.controller = controller;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if(e.getButton() == MouseEvent.BUTTON1) {
      selectedRectangle = controller.getSelectedRectangle();
      originalQuadri = selectedRectangle != null ? selectedRectangle.getQuadri().clone() : null;
      basePoint = controller.toImageCoords(e.getPoint());
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if(e.getButton() == MouseEvent.BUTTON1 && selectedRectangle != null) {
      selectedRectangle = null;
      originalQuadri = null;
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (selectedRectangle != null) {
      Point currPoint = controller.toImageCoords(e.getPoint());

      Quadri translatedQuadri = originalQuadri.translate(currPoint.x - basePoint.x, currPoint.y - basePoint.y);
      Rect2d currRect = translatedQuadri.getEnclosingRectNoRotation();
      Quadri currentQuadri = selectedRectangle.getQuadri();
      Rect2d prevRect = currentQuadri.getEnclosingRectNoRotation();
      currentQuadri.set(translatedQuadri);

      Point p0 = new Point(Math.min(currRect.x, prevRect.x), Math.min(currRect.y, prevRect.y));
      Point p1 = new Point(Math.max(prevRect.x + prevRect.width, currRect.x+currRect.width), Math.max(prevRect.y + prevRect.height, currRect.y+currRect.height));
      java.awt.Point dp0 = controller.toDisplayCoords(p0);
      java.awt.Point dp1 = controller.toDisplayCoords(p1);
      controller.repaintImage(new Rectangle(dp0.x - JImage.RECTANGLE_STROKE, dp0.y - JImage.RECTANGLE_STROKE,
                                     dp1.x - dp0.x + JImage.RECTANGLE_STROKE + 2, dp1.y - dp0.y + JImage.RECTANGLE_STROKE + 2));
    } else {
      log.warn("No selected rectangle");
    }
  }
}
