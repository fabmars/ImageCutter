package org.mars.cutter.ui;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Comparator;
import javax.swing.event.MouseInputAdapter;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.Quadri;
import org.opencv.core.Point;

@Slf4j
public class JQuadriResizer extends MouseInputAdapter {

  private final CutterController controller;

  public JQuadriResizer(CutterController controller) {
    this.controller = controller;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) {
      selectQuadriAtMouseCoords();
    } else if (e.getButton() == MouseEvent.BUTTON3) {
      pinpointCorner();
    }
  }

  private void selectQuadriAtMouseCoords() {
    controller.selectQuadriAtMouseCoords();
  }

  public void pinpointCorner() {
    JQuadriControl selectedRectangle = controller.getSelectedRectangle();
    if(selectedRectangle != null) {
      pinpointCorner(selectedRectangle.getQuadri(), controller.getMousePointer());
    } else {
      log.warn("No selected rectangle");
    }
  }


  public void pinpointCorner(Quadri quadri, Point point) {
    quadri.stream()
        .min(Comparator.comparingDouble(p -> Point2D.distanceSq(p.x, p.y, point.x, point.y)))
        .ifPresent(closestPoint -> {
          closestPoint.x = point.x;
          closestPoint.y = point.y;
          controller.repaintImage();
        });
  }
}
