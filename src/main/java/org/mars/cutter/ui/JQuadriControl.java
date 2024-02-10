package org.mars.cutter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import lombok.Getter;
import lombok.Setter;
import org.mars.cutter.Quadri;

@Getter
public class JQuadriControl extends JPanel {

  public static final Color SELECTION_COLOR = new Color(0x30, 0xa0, 0xff);

  @Setter
  private Quadri quadri;
  private boolean selected;

  private final JButton discardButton = new JButton("❌");

  public JQuadriControl(Quadri quadri) {
    super(new BorderLayout());
    this.quadri = quadri;
    setBorder(new LineBorder(quadri.getColor(), 4, true));

    add(new JLabel(quadri.getName()), BorderLayout.CENTER);
    add(discardButton, BorderLayout.EAST);

    addMouseListener(new JQuadriSelector(this));
  }



  public void setSelected(boolean selected) {
    this.selected = selected;
    setBackground(selected ? SELECTION_COLOR : null);
  }

  public void addDeleteListener(ActionListener listener) {
    discardButton.addActionListener(listener);
  }
}
