package com.motivewave.platform.study.custom;

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

/** Delta Moving Average */
@StudyHeader(
    namespace="com.motivewave", 
    id="DELTA_MA", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_DELTA_MA",
    menu="MENU_CUSTOM",
    desc="DESC_DELTA_MA",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/delta_ma.htm")
public class DeltaMA extends com.motivewave.platform.sdk.study.Study 
{
  final static String DELTA_RANGE = "deltaRange";
  
	enum Values { MA, DELTA }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT1"), Enums.BarInput.CLOSE));
    inputs.addRow(new InputDescriptor(Inputs.INPUT2, get("LBL_INPUT2"), Enums.BarInput.OPEN));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(DELTA_RANGE, get("LBL_DELTA_RANGE"), 5, 1, 9999, 1));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));
    colors.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));
    colors.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.INPUT2, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 0, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(DELTA_RANGE, Inputs.UP_COLOR, Inputs.DOWN_COLOR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.INPUT2, Inputs.METHOD, Inputs.PERIOD, DELTA_RANGE);
    desc.exportValue(new ValueDescriptor(Values.MA, get("LBL_DELTA_MA"), new String[] {Inputs.INPUT, Inputs.INPUT2, Inputs.METHOD, Inputs.PERIOD, DELTA_RANGE}));
    desc.declareBars(Values.MA);
    desc.declareIndicator(Values.MA, Inputs.IND);
    desc.setRangeKeys(Values.MA);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int deltaRange = getSettings().getInteger(DELTA_RANGE);
    if (index < deltaRange) return;

    var series = ctx.getDataSeries();
    Double input1 = series.getDouble(index, getSettings().getInput(Inputs.INPUT));
    Double input2 = series.getDouble(index-deltaRange, getSettings().getInput(Inputs.INPUT2));
    if (input1 == null || input2 == null) return;
    
    series.setDouble(index, Values.DELTA, input1 - input2);
    
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period + deltaRange) return;

    Double ma = series.ma(getSettings().getMAMethod(Inputs.METHOD), index, period, Values.DELTA);
    if (ma == null) return;
    series.setDouble(index, Values.MA, ma);
    
    if (ma > 0) series.setBarColor(index,  Values.MA, getSettings().getColor(Inputs.UP_COLOR));
    else series.setBarColor(index,  Values.MA, getSettings().getColor(Inputs.DOWN_COLOR));
    
    series.setComplete(index);
  }
}
