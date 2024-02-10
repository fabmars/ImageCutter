package org.mars.cutter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import org.mars.cutter.Quadri;
import org.mars.cutter.domain.Rotation;

public class JRotate extends JPanel {

  public static final Color SELECTION_COLOR = new Color(0x30, 0xa0, 0xff);
  private static final EmptyBorder BORDER = new EmptyBorder(3, 3, 3, 3);

  @Getter
  private final Quadri quadri;

  private final Map<Rotation, JButton> map = LinkedHashMap.newLinkedHashMap(4);

  public JRotate(Quadri quadri) {
    setOpaque(false);
    this.quadri = quadri;

    map.put(Rotation.UP, new JButton("⬆\uFE0F"));
    map.put(Rotation.ANTI, new JButton("⤴\uFE0F"));
    map.put(Rotation.CLOCK, new JButton("⤵\uFE0F"));
    map.put(Rotation.DOWN, new JButton("\uD83D\uDD03"));

    map.forEach((rotation, jButton) -> {
      jButton.setBorder(BORDER);
      jButton.addActionListener(e -> select(e, rotation));
      add(jButton);
    });

    Rotation rotation = quadri.getRotation();
    select(new ActionEvent(map.get(rotation), 0, null), rotation);
  }

  private void select(ActionEvent e, Rotation rotation) {
    this.quadri.setRotation(rotation);

    JButton source = (JButton)e.getSource();
    for (Component component : getComponents()) {
      if(component instanceof JButton button && button != source)  {
        button.setBackground(null);
      }
    }
    source.setBackground(SELECTION_COLOR);
  }
}
