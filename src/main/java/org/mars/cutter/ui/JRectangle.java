package org.mars.cutter.ui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JRectangle extends JPanel {

  public JRectangle(String label) {
    super(new BorderLayout());
    add(new JLabel(label), BorderLayout.CENTER);
    JPanel options = new JPanel();
    JButton discardButton = new JButton("❌");
    options.add(discardButton);
    add(options, BorderLayout.EAST);
  }
}
