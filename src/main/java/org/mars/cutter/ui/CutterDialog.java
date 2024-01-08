package org.mars.cutter.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import org.mars.cutter.CutterController;
import org.mars.cutter.ImageLoadingException;
import org.mars.cutter.RectangleDetector;
import org.mars.cutter.util.ImageUtils;

@Slf4j
public class CutterDialog extends JFrame implements KeyEventDispatcher {

  private final CutterController service;
  private final JPanel mainPanel = new JPanel();


  public CutterDialog(Path rootDir) throws IOException {
    this.service = new CutterController(rootDir);

    setTitle("Image Cutter");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

    setLayout(new BorderLayout());

    mainPanel.setLayout(new BorderLayout());

    JPanel rectanglesPanel = new JPanel();
    rectanglesPanel.setLayout(new GridLayout(8, 1));
    rectanglesPanel.add(new JRectangle("Rectangle 1"));
    rectanglesPanel.add(new JRectangle("Rectangle 2"));
    rectanglesPanel.add(new JRectangle("Rectangle 3"));
    rectanglesPanel.add(new JRectangle("Rectangle 4"));
    rectanglesPanel.add(new JRectangle("Rectangle 5"));

    add(createButtonsPanel(), BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
    add(rectanglesPanel, BorderLayout.EAST);
  }

  private JPanel createButtonsPanel() {
    JLabel cannyThresholdLabel = new JLabel("C.thresh");
    DoubleField cannyThresholdInput = new DoubleField("#.###", 5, RectangleDetector.CANNY_THRESHOLD);
    JLabel cannyRatioLabel = new JLabel("C.ratio");
    DoubleField cannyRatioInput = new DoubleField("#.###", 5, RectangleDetector.CANNY_RATIO);
    JLabel houghThresholdLabel = new JLabel("H.thresh");
    IntegerField houghThresholdInput = new IntegerField("#", 4, RectangleDetector.HOUGH_THRESHOLD);
    JLabel angleThresholdLabel = new JLabel("A.thresh");
    DoubleField angleThresholdInput = new DoubleField("#.################", 8, RectangleDetector.HV_THRESHOLD);

    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(e -> reset());
    JButton skipButton = new JButton("Skip");
    skipButton.addActionListener(e -> skip());
    JButton confirmButton = new JButton("Validate");
    confirmButton.addActionListener(e -> confirm());

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    buttonsPanel.add(cannyThresholdLabel);
    buttonsPanel.add(cannyThresholdInput);
    buttonsPanel.add(cannyRatioLabel);
    buttonsPanel.add(cannyRatioInput);
    buttonsPanel.add(houghThresholdLabel);
    buttonsPanel.add(houghThresholdInput);
    buttonsPanel.add(angleThresholdLabel);
    buttonsPanel.add(angleThresholdInput);
    buttonsPanel.add(resetButton);
    buttonsPanel.add(skipButton);
    buttonsPanel.add(confirmButton);
    return buttonsPanel;
  }


  public void reset() {
    try {
      BufferedImage image = service.reset();
      setImage(image);
    } catch (ImageLoadingException e) {
      JOptionPane.showMessageDialog(this, "Couldn't reset: " + e.getMessage());
      skip();
    }
  }

  public void skip() {
    try {
      BufferedImage image = service.skip();
      if (image == null) {
        JOptionPane.showConfirmDialog(this, "Finsihed!");
        setVisible(false);
      } else {
        setImage(image);
      }
    } catch (ImageLoadingException e) {
      JOptionPane.showMessageDialog(this, "Couldn't skip: " + e.getMessage());
      skip();
    }
  }

  public void confirm() {
    try {
      BufferedImage image = service.confirm();
      setImage(image);
    } catch (ImageLoadingException e) {
      JOptionPane.showMessageDialog(this, "Couldn't confirm: " + e.getMessage());
      skip();
    }
  }



  public void setImage(Image image) {
    Canvas imageCanvas = new Canvas() {
      @Override
      public void paint(Graphics g) {
        ImageUtils.drawImage(this, image, null, true);
      }
    };
    mainPanel.removeAll();
    mainPanel.add(imageCanvas, BorderLayout.CENTER);
    mainPanel.validate();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent e) {
    if (e.getID() == KeyEvent.KEY_RELEASED) {
      Component source = e.getComponent();

      int key = e.getKeyCode();
      log.info("Pressed: {}", key);
      return true;
    }
    return false;
  }
}
