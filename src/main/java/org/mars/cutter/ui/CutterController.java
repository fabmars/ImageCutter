package org.mars.cutter.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.swing.JOptionPane;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.CutterService;
import org.mars.cutter.DetectionParams;
import org.mars.cutter.ImageContext;
import org.mars.cutter.Quadri;

@Slf4j
public class CutterController implements Serializable {

  private final CutterService service;
  private final CutterDialog cutterDialog;

  private final JImage jImage;
  private final JQuadrisPanel jQuadrisPanel;
  private final JLookingGlass jLookingGlass;
  private final JControlsPanel jControlsPanel;

  private ImageContext currentContext;


  @Getter
  private org.opencv.core.Point mousePointer;

  public CutterController(@NonNull CutterService service, @NonNull CutterDialog cutterDialog) {
    this.service = service;

    this.cutterDialog = cutterDialog;
    this.jImage = requireNonNull(cutterDialog.jImage);
    this.jQuadrisPanel = requireNonNull(cutterDialog.jQuadrisPanel);
    this.jLookingGlass = requireNonNull(cutterDialog.jLookingGlass);
    this.jControlsPanel = requireNonNull(cutterDialog.jControlsPanel);
  }

  private void cleanImage() {
    if (currentContext != null) {
      try {
        currentContext.clean();
        jImage.clean();
        jQuadrisPanel.clean();
        jQuadrisPanel.revalidate();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      } finally {
        currentContext = null;
      }
    }
  }

  private void setContext(ImageContext imageContext) {
    cleanImage();

    this.currentContext = imageContext;
    jControlsPanel.setFileName(imageContext.getFileName());

    BufferedImage image = currentContext.getTempImage();
    jImage.setImage(image); // will draw quadris
    jImage.requestFocus(); // so further keyb actions don't trigger any selected button
    jLookingGlass.setImage(image); // will not draw quatris

    rebuildJQuadrisSetup();

    repaintImage();
  }

  private void rebuildJQuadrisSetup() {
    List<Quadri> quadris = currentContext.getQuadris();
    rebuildJQuadrisSetup(quadris.size() == 1 ? quadris.getFirst() : null);
  }

  private void rebuildJQuadrisSetup(Quadri selectedQuadri) {
    jQuadrisPanel.clean();
    List<Quadri> quadris = currentContext.getQuadris();
    if(quadris.isEmpty()) {
      addGenericQuadri();
    } else {
      for (Quadri quadri : quadris) {
        addQuadriInternal(quadri, quadri == selectedQuadri);
      }
    }
  }

  public DetectionParams getDetectionParams() {
    return jControlsPanel.getDetectionParams();
  }

  public void reset() {
    try {
      setContext(service.reset(getDetectionParams()));
    } catch (IOException e) {
      JOptionPane.showMessageDialog(cutterDialog, e.getMessage());
      skip();
    }
  }

  public void skip() {
    try {
      ImageContext imageContext = service.skip(getDetectionParams());
      if (imageContext != null) {
        setContext(imageContext);
      } else {
        cleanImage();
        JOptionPane.showMessageDialog(cutterDialog, "Finished!");
        cutterDialog.dispose();
        System.exit(0);
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(cutterDialog, "Couldn't skip: " + e.getMessage());
      skip();
    }
  }

  public void confirm() {
    service.confirm(currentContext);
    skip();
  }

  public void centerOn(Point point) {
    centerOn(toImageCoords(point));
  }

  void centerOn(org.opencv.core.Point point) {
    this.mousePointer = point;
    jLookingGlass.centerOn(point);
  }

  public void repaintImage() {
    jImage.repaint();
  }

  public void repaintImage(Rectangle rectangle) {
    jImage.repaint(rectangle);
  }

  public BufferedImage getImage() {
    return currentContext.getTempImage();
  }

  public JQuadriControl getSelectedRectangle() {
    return jQuadrisPanel.getSelectedQuadriControl();
  }

  public void setSelectedRectangle(int r) {
    jQuadrisPanel.setSelectedQuadriControl(r);
  }

  public void selectQuadriAtMouseCoords() {
    jQuadrisPanel.getQuadriControls()
        .stream()
        .filter(jQuadriControl -> jQuadriControl.getQuadri().toShape().contains(mousePointer.x, mousePointer.y))
        .findAny().ifPresent(jQuadrisPanel::setSelectedQuadriControl);
  }


  public void upSelectedRectangle() {
    JQuadriControl selectedRectangle = getSelectedRectangle();
    if(selectedRectangle != null) {
      Quadri selectedQuadri = selectedRectangle.getQuadri();
      List<Quadri> quadris = currentContext.getQuadris();
      int i = quadris.indexOf(selectedQuadri);
      if(i > 0) {
        quadris.remove(i);
        quadris.add(i-1, selectedQuadri);
      }
      rebuildJQuadrisSetup(selectedQuadri);
    }
  }

  public void downSelectedRectangle() {
    JQuadriControl selectedRectangle = getSelectedRectangle();
    if(selectedRectangle != null) {
      Quadri selectedQuadri = selectedRectangle.getQuadri();
      List<Quadri> quadris = currentContext.getQuadris();
      int i = quadris.indexOf(selectedQuadri);
      if (i < quadris.size() - 1) {
        quadris.remove(i);
        quadris.add(i + 1, selectedQuadri);
      }
      rebuildJQuadrisSetup(selectedQuadri);
    }
  }

  public void addGenericQuadri() {
    Quadri newQuadri = createGenericQuadri();
    addQuadri(newQuadri, true);
  }

  private Quadri createGenericQuadri() {
    BufferedImage image = getImage();
    double width = image.getWidth();
    double height = image.getHeight();
    double x1 = width / 3;
    double x2 = x1 * 2;
    double y1 = height / 3;
    double y2 = y1 * 2;
    return new Quadri("Manual " + (currentContext.getQuadris().size()+1), new org.opencv.core.Point(x1, y1), new org.opencv.core.Point(x2, y2), getNextColor());
  }

  public void addQuadri(Quadri quadri, boolean select) {
    currentContext.add(quadri);
    addQuadriInternal(quadri, select);
    repaintImage();
  }

  private void addQuadriInternal(Quadri quadri, boolean select) {
    jImage.addQuadri(quadri);
    JQuadriControl jQuadriControl = jQuadrisPanel.add(quadri);
    jQuadriControl.addDeleteListener(l -> {
      currentContext.remove(quadri);
      jImage.removeQuadri(quadri);
      jQuadrisPanel.remove(jQuadriControl);
      List<JQuadriControl> remainingJQuadriControls = jQuadrisPanel.getQuadriControls();
      if(remainingJQuadriControls.size() == 1) {
        jQuadrisPanel.setSelectedQuadriControl(remainingJQuadriControls.get(0));
      }
      jQuadrisPanel.repaint();
      jImage.requestFocus(); // so further keyb actions don't trigger another button of the panel
      repaintImage();
    });
    if(select) {
      jQuadrisPanel.setSelectedQuadriControl(jQuadriControl);
    }
    jQuadrisPanel.revalidate();
  }

  public Color getNextColor() {
    return currentContext.getNextColor();
  }


  public double toImageScale(double d) {
    return jImage.toImageScale(d);
  }

  public org.opencv.core.Point toImageCoords(Point displayPoint) {
    return jImage.toImageCoords(displayPoint);
  }

  public double toDisplayScale(double d) {
    return jImage.toDisplayScale(d);
  }

  public Point toDisplayCoords(Point imagePoint) {
    return jImage.toDisplayCoords(imagePoint);
  }

  public Point toDisplayCoords(org.opencv.core.Point imagePoint) {
    return jImage.toDisplayCoords(imagePoint);
  }
}
