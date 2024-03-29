package org.mars.cutter.ui;

import java.text.DecimalFormat;
import javax.swing.JFormattedTextField;

public class IntegerField extends JFormattedTextField {

  public IntegerField(String format, int columns, int value) {
    super(new PositiveNumberFormatter(new DecimalFormat(format)));
    setColumns(columns);
    setValue(value);
  }

  public int getInt() {
    return ((Number)getValue()).intValue();
  }
}
