package org.mars.cutter.ui;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JPanel;
import org.mars.cutter.Quadri;

public class JQuadrisPanel extends JPanel {

  public JQuadrisPanel() {
    super(new GridLayout(16, 1));
  }

  public JQuadriControl add(Quadri quadri) {
    JQuadriControl jQuadriControl = new JQuadriControl(quadri);
    add(jQuadriControl);
    return jQuadriControl;
  }

  public List<JQuadriControl> getQuadriControls() {
    List<JQuadriControl> jQuadriControls = new ArrayList<>(getComponentCount());
    for (Component component : getComponents()) {
      if(component instanceof JQuadriControl jQuadriControl) {
        jQuadriControls.add(jQuadriControl);
      }
    }
    return jQuadriControls;
  }

  public Optional<JQuadriControl> getQuadriControl(Quadri quadri) {
    return getQuadriControls().stream().filter(jQuadriControl -> jQuadriControl.getQuadri() == quadri).findAny();
  }

  public JQuadriControl getSelectedQuadriControl() {
    return getQuadriControls().stream().filter(JQuadriControl::isSelected).findAny().orElse(null);
  }

  public void setSelectedQuadriControl(JQuadriControl jQuadriControl) {
    for (Component component : getComponents()) {
      if(component instanceof JQuadriControl rect) {
        rect.setSelected(jQuadriControl == rect);
      }
    }
  }

  public void setSelectedQuadriControl(int r) {
    int i = 0;
    for (Component component : getComponents()) {
      if(component instanceof JQuadriControl jQuadriControl) {
        jQuadriControl.setSelected(i == r);
        i++;
      }
    }
  }

  public void clean() {
    removeAll();
  }
}
