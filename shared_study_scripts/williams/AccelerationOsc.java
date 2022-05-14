package com.motivewave.platform.study.williams;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Acceleration Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="ACCEL_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ACCEL_OSC",
    label="LBL_ACCEL_OSC",
    desc="DESC_ACCEL_OSC",
    menu="MENU_BILL_WILLIAMS",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/acceleration_oscillator.htm")
public class AccelerationOsc extends com.motivewave.platform.sdk.study.Study 
{
  final static String ACCEL_PERIOD = "accelPeriod";
	enum Values { VAL, AO }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.MIDPOINT));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.SLOW_PERIOD, get("LBL_SLOW_PERIOD"), 34, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.FAST_PERIOD, get("LBL_FAST_PERIOD"), 5, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(ACCEL_PERIOD, get("LBL_ACCEL_PERIOD"), 5, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_SETTINGS"));
    lines.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));
    lines.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.SLOW_PERIOD, get("LBL_SLOW_PERIOD"), 34, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.FAST_PERIOD, get("LBL_FAST_PERIOD"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(ACCEL_PERIOD, Inputs.UP_COLOR, Inputs.DOWN_COLOR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.SLOW_PERIOD, Inputs.FAST_PERIOD, ACCEL_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.VAL, get("LBL_ACCEL_OSC"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.SLOW_PERIOD, Inputs.FAST_PERIOD, ACCEL_PERIOD}));
    desc.declareBars(Values.VAL);
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.setRangeKeys(Values.VAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    // Calculate the Awesome Oscillator value first
    int slowPeriod = getSettings().getInteger(Inputs.SLOW_PERIOD);
    int fastPeriod = getSettings().getInteger(Inputs.FAST_PERIOD);
    if (index <= slowPeriod) return;

    var method = getSettings().getMAMethod(Inputs.METHOD);
    Object input = getSettings().getInput(Inputs.INPUT);
    var series = ctx.getDataSeries();
    
    Double slowMA = series.ma(method, index, slowPeriod, input);
    Double fastMA = series.ma(method, index, fastPeriod, input);

    if (slowMA == null || fastMA == null) {
      // This should not happen
      return;
    }

    // Awesome oscillator is the difference between the fast and slow moving averages
    double ao = fastMA - slowMA;
    series.setDouble(index,  Values.AO, ao);
    int accelPeriod = getSettings().getInteger(ACCEL_PERIOD);
    
    if (index <= slowPeriod + accelPeriod) return; // not enough data

    Color upColor = getSettings().getColor(Inputs.UP_COLOR);
    Color downColor = getSettings().getColor(Inputs.DOWN_COLOR);
    
    // Moving average of the Awesome Oscillator value
    Double aoMA = series.ma(method, index, accelPeriod, Values.AO);
    if (aoMA == null) return;
    
    // Acceleration Oscillator is the difference between the Awesome Oscillator value and its moving average
    double val = ao - aoMA;
    series.setDouble(index,  Values.VAL, val);
    Double prev = series.getDouble(index-1, Values.VAL);

    Color c = upColor;
    if (prev != null && prev > val) c = downColor;
    series.setBarColor(index, Values.VAL, c);
    
    series.setComplete(index);
  }
}