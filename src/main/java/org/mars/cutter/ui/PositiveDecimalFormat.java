package org.mars.cutter.ui;

import java.text.DecimalFormat;
import java.text.ParseException;

public class PositiveDecimalFormat extends DecimalFormat {

  public PositiveDecimalFormat(String pattern) {
    super(pattern);
  }

  @Override
  public Object parseObject(String source) throws ParseException {
    Number num = (Number)super.parseObject(source);
    if(num.doubleValue() < 0.0) {
      throw new NumberFormatException("Negative: " + source);
    } else {
      return num;
    }
  }
}
