package org.mars.cutter.ui;

import java.text.DecimalFormat;
import javax.swing.JFormattedTextField;

public class DoubleField extends JFormattedTextField {

  public DoubleField(String format, int columns, double value) {

    super(new PositiveNumberFormatter(new DecimalFormat(format)));
    setColumns(columns);
    setValue(value);
  }
}
