package org.mars.cutter.ui;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static org.mars.cutter.ui.SwingUtils.newGridBagConstraints;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.CutterService;
import org.mars.cutter.DetectionParams;
import org.opencv.core.Point;

@Slf4j
public class CutterDialog extends JFrame implements KeyEventDispatcher {

  final JImage jImage;
  final JControlsPanel jControlsPanel;
  final JQuadrisPanel jQuadrisPanel;
  final JLookingGlass jLookingGlass;
  final CutterController controller;
  final JQuadriResizer jQuadriResizer;

  public CutterDialog(CutterService service) {

    setTitle("Image Cutter");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

    setLayout(new GridBagLayout());

    jImage = new JImage();
    jQuadrisPanel = new JQuadrisPanel();
    jLookingGlass = new JLookingGlass();
    jControlsPanel = new JControlsPanel(DetectionParams.DEFAULTS);
    controller = new CutterController(service, this);

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BorderLayout());
    JButton addRectButton = new JButton("➕");
    addRectButton.addActionListener(l -> controller.addGenericQuadri());
    rightPanel.add(addRectButton, BorderLayout.NORTH);

    rightPanel.add(jQuadrisPanel, BorderLayout.CENTER);
    rightPanel.add(jLookingGlass, BorderLayout.SOUTH);

    add(jControlsPanel, newGridBagConstraints(0, 0, 2, 1, 1, 0.01, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL));
    GridBagConstraints constraints = newGridBagConstraints(0, 1, 1, 1, 0.9, 0.99, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
    add(jImage, constraints);
    add(rightPanel, newGridBagConstraints(1, 1, 1, 1, 0.1, 0.99, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE));


    jControlsPanel.resetButton.addActionListener(e -> controller.reset());
    jControlsPanel.skipButton.addActionListener(e -> controller.skip());
    jControlsPanel.confirmButton.addActionListener(e -> controller.confirm());

    jImage.addMouseOperationsListener(new JQuadriDragger(controller));
    jQuadriResizer = new JQuadriResizer(controller);
    jImage.addMouseOperationsListener(jQuadriResizer);
    jImage.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        controller.centerOn(e.getPoint());
      }
    });
  }

  public void skip() {
    controller.skip();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent e) {
    if (e.getID() == KeyEvent.KEY_RELEASED) {
      int keyCode = e.getKeyCode();
      if ((e.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK && keyCode == KeyEvent.VK_N) {
        controller.addGenericQuadri();
        return true;
      }

      if ((e.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK && keyCode == KeyEvent.VK_S) {
        controller.confirm();
        return true;
      }

      // Reorder quadris
      if ((e.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK && keyCode == KeyEvent.VK_UP) {
        controller.upSelectedRectangle();
        return true;
      }

      if ((e.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK && keyCode == KeyEvent.VK_DOWN) {
        controller.downSelectedRectangle();
        return true;
      }

      // Select quadris
      if ((keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12)) {
        controller.setSelectedRectangle(keyCode - KeyEvent.VK_F1);
        return true;
      }

      if (keyCode == KeyEvent.VK_SPACE) {
        jQuadriResizer.pinpointCorner();
        return true;
      }
    } else if(e.getID() == KeyEvent.KEY_PRESSED) {
      int keyCode = e.getKeyCode();
      Point imageCoords = controller.getMousePointer();

      if (keyCode == KeyEvent.VK_LEFT) {
        imageCoords.x--;
        controller.centerOn(imageCoords);
        return true;
      }
      if (keyCode == KeyEvent.VK_RIGHT) {
        imageCoords.x++;
        controller.centerOn(imageCoords);
        return true;
      }
      if (keyCode == KeyEvent.VK_UP) {
        imageCoords.y--;
        controller.centerOn(imageCoords);
        return true;
      }
      if (keyCode == KeyEvent.VK_DOWN) {
        imageCoords.y++;
        controller.centerOn(imageCoords);
        return true;
      }
    }

    return false;
  }
}
