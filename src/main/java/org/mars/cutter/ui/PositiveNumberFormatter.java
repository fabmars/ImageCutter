package org.mars.cutter.ui;

import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.text.NumberFormatter;

public class PositiveNumberFormatter extends NumberFormatter {

  public PositiveNumberFormatter(DecimalFormat decimalFormat) {
    super(decimalFormat);
  }

  @Override
  public Object stringToValue(String text) throws ParseException {
    if(text.startsWith("-")) {
      throw new ParseException("Negative: " + text, 0);
    } else {
      return super.stringToValue(text);
    }
  }
}
