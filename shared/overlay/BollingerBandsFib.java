package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Bollinger Bands - Fibonacci Ratios*/
@StudyHeader(
    namespace="com.motivewave", 
    id="BB_FIB", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_BB_FIB", 
    desc="DESC_BB_FIB",
    label="LBL_BB_FIB",
    menu="MENU_OVERLAY",
    overlay=true,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/bollinger_bands_fib_ratios.htm")
public class BollingerBandsFib extends Study 
{
  // Settings
  final static String FIB_RATIO_1 = "fibRatio1", FIB_RATIO_2 = "fibRatio2", FIB_RATIO_3 = "fibRatio3";
  final static String FIB_LINE_1 = "fibLine1", FIB_LINE_2 = "fibLine2", FIB_LINE_3 = "fibLine3";
  
  enum Values { TR, MIDDLE, FIB_TOP_1, FIB_TOP_2, FIB_TOP_3, FIB_BOTTOM_1, FIB_BOTTOM_2, FIB_BOTTOM_3 }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(FIB_RATIO_1, get("LBL_FIB_RATIO_1"), 1.618, 0.1, 99, 0.001));
    inputs.addRow(new DoubleDescriptor(FIB_RATIO_2, get("LBL_FIB_RATIO_2"), 2.618, 0.1, 99, 0.001));
    inputs.addRow(new DoubleDescriptor(FIB_RATIO_3, get("LBL_FIB_RATIO_3"), 4.236, 0.1, 99, 0.001));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.MIDDLE_PATH, get("LBL_MIDDLE_LINE"), X11Colors.DARK_SLATE_GRAY, 1.0f, new float[] {3f, 3f}, true, true, false));
    lines.addRow(new PathDescriptor(FIB_LINE_1, get("LBL_FIB_LINE_1"), X11Colors.CADET_BLUE, 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(FIB_LINE_2, get("LBL_FIB_LINE_2"), X11Colors.DARK_SLATE_GRAY, 1.0f, new float[] {3f, 3f}, true, true, true));
    lines.addRow(new PathDescriptor(FIB_LINE_3, get("LBL_FIB_LINE_3"), defaults.getGreen(), 1.0f, new float[] {3f, 3f}, true, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(FIB_RATIO_1, FIB_RATIO_2, FIB_RATIO_3, Inputs.MIDDLE_PATH, FIB_LINE_1, FIB_LINE_2, FIB_LINE_3);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, FIB_RATIO_1, FIB_RATIO_2, FIB_RATIO_3);
    
    desc.exportValue(new ValueDescriptor(Values.MIDDLE, get("LBL_BB_FIB_MID"), new String[] {Inputs.INPUT, Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.FIB_TOP_1, get("LBL_BB_FIB_TOP_1"), new String[] {Inputs.INPUT, Inputs.PERIOD, FIB_RATIO_1}));
    desc.exportValue(new ValueDescriptor(Values.FIB_BOTTOM_1, get("LBL_BB_FIB_BOTTOM_1"), new String[] {Inputs.INPUT, Inputs.PERIOD, FIB_RATIO_1}));
    desc.exportValue(new ValueDescriptor(Values.FIB_TOP_2, get("LBL_BB_FIB_TOP_2"), new String[] {Inputs.INPUT, Inputs.PERIOD, FIB_RATIO_2}));
    desc.exportValue(new ValueDescriptor(Values.FIB_BOTTOM_2, get("LBL_BB_FIB_BOTTOM_2"), new String[] {Inputs.INPUT, Inputs.PERIOD, FIB_RATIO_2}));
    desc.exportValue(new ValueDescriptor(Values.FIB_TOP_3, get("LBL_BB_FIB_TOP_3"), new String[] {Inputs.INPUT, Inputs.PERIOD, FIB_RATIO_3}));
    desc.exportValue(new ValueDescriptor(Values.FIB_BOTTOM_3, get("LBL_BB_FIB_BOTTOM_3"), new String[] {Inputs.INPUT, Inputs.PERIOD, FIB_RATIO_3}));
    
    desc.declarePath(Values.MIDDLE, Inputs.MIDDLE_PATH);
    desc.declarePath(Values.FIB_TOP_1, FIB_LINE_1);
    desc.declarePath(Values.FIB_TOP_2, FIB_LINE_2);
    desc.declarePath(Values.FIB_TOP_3, FIB_LINE_3);
    desc.declarePath(Values.FIB_BOTTOM_1, FIB_LINE_1);
    desc.declarePath(Values.FIB_BOTTOM_2, FIB_LINE_2);
    desc.declarePath(Values.FIB_BOTTOM_3, FIB_LINE_3);
    
    desc.setRangeKeys(Values.FIB_TOP_3, Values.FIB_BOTTOM_3);
  }
  
  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 20);
    var series = ctx.getDataSeries();

    series.setFloat(index, Values.TR, series.getTrueRange(index));
    
    if (index < period) return;

    // Calculate the ATR using a smoothed MA
    Object input = getSettings().getInput(Inputs.INPUT);
    double fibRatio1 = getSettings().getDouble(FIB_RATIO_1, 1.618);
    double fibRatio2 = getSettings().getDouble(FIB_RATIO_2, 2.618);
    double fibRatio3 = getSettings().getDouble(FIB_RATIO_3, 4.236);
    
    Double ATR = series.smma(index, period, Values.TR);
    Double sma = series.sma(index, period, input);
    if (ATR == null || sma == null) return;
    
    // Calculate the lines
    double r1 = ATR*fibRatio1;
    double r2 = ATR*fibRatio2;
    double r3 = ATR*fibRatio3;

    series.setDouble(index, Values.FIB_TOP_3, sma + r3);
    series.setDouble(index, Values.FIB_TOP_2, sma + r2);
    series.setDouble(index, Values.FIB_TOP_1, sma + r1);
    series.setDouble(index, Values.MIDDLE, sma);
    series.setDouble(index, Values.FIB_BOTTOM_1, sma - r1);
    series.setDouble(index, Values.FIB_BOTTOM_2, sma - r2);
    series.setDouble(index, Values.FIB_BOTTOM_3, sma - r3);

    series.setComplete(index);
  }  
}