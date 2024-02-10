package org.mars.cutter.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.mars.cutter.DetectionParams;

public class JControlsPanel extends JPanel {
  private final JLabel fileNameLabel = new JLabel("fileName");

  protected final JButton resetButton;
  protected final JButton skipButton;
  protected final JButton confirmButton;

  private final DoubleField cannyThresholdInput;
  private final DoubleField cannyRatioInput;
  private final IntegerField houghThresholdInput;
  private final DoubleField angleThresholdInput;


  public JControlsPanel(DetectionParams params) {
    JLabel cannyThresholdLabel = new JLabel("C.thresh");
    cannyThresholdInput = new DoubleField("#.###", 5, params.getCannyThreshold());
    JLabel cannyRatioLabel = new JLabel("C.ratio");
    cannyRatioInput = new DoubleField("#.###", 5, params.getCannyRatio());
    JLabel houghThresholdLabel = new JLabel("H.thresh");
    houghThresholdInput = new IntegerField("#", 4, params.getHoughThreshold());
    JLabel angleThresholdLabel = new JLabel("A.thresh");
    angleThresholdInput = new DoubleField("#.################", 8, params.getHvAngleTolerance());

    resetButton = new JButton("Start Over");
    skipButton = new JButton("Skip");
    confirmButton = new JButton("Validate");

    this.setLayout(new GridBagLayout());
    GridBagConstraints westConstraints = westGridBagConstraints();
    GridBagConstraints eastConstraints = eastGridBagConstraints();
    this.add(fileNameLabel, westConstraints);
    this.add(cannyThresholdLabel, eastConstraints);
    this.add(cannyThresholdInput, eastConstraints);
    this.add(cannyRatioLabel, eastConstraints);
    this.add(cannyRatioInput, eastConstraints);
    this.add(houghThresholdLabel, eastConstraints);
    this.add(houghThresholdInput, eastConstraints);
    this.add(angleThresholdLabel, eastConstraints);
    this.add(angleThresholdInput, eastConstraints);
    this.add(resetButton, eastConstraints);
    this.add(skipButton, eastConstraints);
    this.add(confirmButton, eastConstraints);
  }

  public void setFileName(String fileName) {
    fileNameLabel.setText(fileName);
  }

  private static GridBagConstraints westGridBagConstraints() {
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0.8;
    c.anchor = GridBagConstraints.WEST;
    return c;
  }

  private static GridBagConstraints eastGridBagConstraints() {
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0.01;
    c.anchor = GridBagConstraints.EAST;
    return c;
  }

  public DetectionParams getDetectionParams() {
    return new DetectionParams(
      DetectionParams.TEMP_DIMENSION,
      cannyThresholdInput.getDouble(),
      cannyRatioInput.getDouble(),
      DetectionParams.HOUGH_THETA,
      houghThresholdInput.getInt(),
      angleThresholdInput.getDouble()
    );
  }
}
