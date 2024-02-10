package org.mars.cutter.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JQuadriSelector extends MouseAdapter {

  private final JQuadriControl jQuadriControl;

  public JQuadriSelector(JQuadriControl jQuadriControl) {
    this.jQuadriControl = jQuadriControl;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    JQuadriControl source = (JQuadriControl) e.getSource();
    JQuadrisPanel rectanglesPanel = (JQuadrisPanel) source.getParent();
    rectanglesPanel.setSelectedQuadriControl(jQuadriControl);
  }
}
