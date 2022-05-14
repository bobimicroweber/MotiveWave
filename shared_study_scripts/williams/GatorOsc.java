package com.motivewave.platform.study.williams;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Gator Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="GATOR_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_GATOR_OSC",
    label="LBL_GATOR_OSC",
    desc="DESC_GATOR_OSC",
    menu="MENU_BILL_WILLIAMS",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/gator_oscillator.htm")
public class GatorOsc extends com.motivewave.platform.sdk.study.Study 
{
  final static String JAW_PERIOD = "jawPeriod", JAW_SHIFT = "jawShift", TEETH_PERIOD = "teethPeriod", 
      TEETH_SHIFT = "teethShift", LIPS_PERIOD = "lipsPeriod", LIPS_SHIFT = "lipsShift";

  enum Values { TOP, BOTTOM, JAW, TEETH, LIPS }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.MIDPOINT));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA));
    inputs.addRow(new IntegerDescriptor(JAW_PERIOD, get("LBL_JAW_PERIOD"), 13, 1, 9999, 1),
        new IntegerDescriptor(JAW_SHIFT, get("LBL_SHIFT"), 8, -999, 999, 1));
    inputs.addRow(new IntegerDescriptor(TEETH_PERIOD, get("LBL_TEETH_PERIOD"), 8, 1, 9999, 1),
        new IntegerDescriptor(TEETH_SHIFT, get("LBL_SHIFT"), 5, -999, 999, 1));
    inputs.addRow(new IntegerDescriptor(LIPS_PERIOD, get("LBL_LIPS_PERIOD"), 5, 1, 9999, 1),
        new IntegerDescriptor(LIPS_SHIFT, get("LBL_SHIFT"), 3, -999, 999, 1));
    
    var lines = tab.addGroup(get("LBL_SETTINGS"));
    lines.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));
    lines.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));
    lines.addRow(new IndicatorDescriptor(Inputs.TOP_IND, get("LBL_TOP_IND"), null, null, false, true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.BOTTOM_IND, get("LBL_BOTTOM_IND"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(JAW_PERIOD, get("LBL_JAW_PERIOD"), 13, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(JAW_SHIFT, get("LBL_SHIFT"), 8, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(new SliderDescriptor(TEETH_PERIOD, get("LBL_TEETH_PERIOD"), 8, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(TEETH_SHIFT, get("LBL_SHIFT"), 5, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(new SliderDescriptor(LIPS_PERIOD, get("LBL_LIPS_PERIOD"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(LIPS_SHIFT, get("LBL_SHIFT"), 3, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(Inputs.UP_COLOR, Inputs.DOWN_COLOR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, JAW_PERIOD, TEETH_PERIOD, LIPS_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_GATOR_TOP"), new String[] {Inputs.INPUT, Inputs.METHOD, JAW_PERIOD, TEETH_PERIOD,}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_GATOR_BOTTOM"), new String[] {Inputs.INPUT, Inputs.METHOD, TEETH_PERIOD, LIPS_PERIOD}));
    desc.declareBars(Values.TOP, null);
    desc.declareBars(Values.BOTTOM, null);
    desc.declareIndicator(Values.TOP, Inputs.TOP_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.BOTTOM_IND);
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  @Override  
  protected void calculateValues(DataContext ctx)
  {
    boolean updates = getSettings().isBarUpdates();
    Util.calcSeriesMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(JAW_PERIOD), getSettings().getInteger(JAW_SHIFT), Values.JAW, false, updates);
    Util.calcSeriesMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(TEETH_PERIOD), getSettings().getInteger(TEETH_SHIFT), Values.TEETH, false, updates);
    Util.calcSeriesMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(LIPS_PERIOD), getSettings().getInteger(LIPS_SHIFT), Values.LIPS, false, updates);
    // Calculate top and middle lines
    int maxShift = Util.maxInt(getSettings().getInteger(JAW_SHIFT), getSettings().getInteger(TEETH_SHIFT), getSettings().getInteger(LIPS_SHIFT));
    var series = ctx.getDataSeries();
    for(int i = 0; i < series.size() + maxShift; i++) {
      if (series.isComplete(i)) continue;
      if (!updates && !series.isBarComplete(i)) continue;
      calculate(i, ctx);
    }
    super.calculateValues(ctx);
  }  

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    Color upColor = getSettings().getColor(Inputs.UP_COLOR);
    Color downColor = getSettings().getColor(Inputs.DOWN_COLOR);
    Double jaw = series.getDouble(index, Values.JAW);
    Double teeth = series.getDouble(index, Values.TEETH);
    Double lips = series.getDouble(index, Values.LIPS);
    
    boolean complete = false;
    
    if (jaw != null && teeth != null) {
      double top = Math.abs(jaw-teeth);
      Double prev = series.getDouble(index-1, Values.TOP);
      series.setDouble(index, Values.TOP, top);
      series.setBarColor(index, Values.TOP, prev == null || top > prev ? upColor : downColor);
      complete = series.isComplete(index,  Values.JAW) && series.isComplete(index, Values.TEETH);
    }
    if (teeth != null && lips != null) {
      double bottom = -Math.abs(teeth-lips);
      Double prev = series.getDouble(index-1, Values.BOTTOM);
      series.setDouble(index, Values.BOTTOM, bottom);
      series.setBarColor(index, Values.BOTTOM, prev == null || bottom < prev ? upColor : downColor);
      complete = complete && series.isComplete(index,  Values.TEETH) && series.isComplete(index, Values.LIPS);
    }
    else complete = false;
    
    series.setComplete(index, complete);
  }
}
