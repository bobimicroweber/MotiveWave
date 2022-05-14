package com.motivewave.platform.study.general;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Moving Average Difference. Calculates the difference between two moving averages and displays it as a graph. */
@StudyHeader(
    namespace="com.motivewave", 
    id="MA_DIFF", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MA_DIFF",
    label="LBL_MA_DIFF",
    desc="DESC_MA_DIFF",
    menu="MENU_GENERAL",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/ma_difference.htm")
public class MADifference extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { DIFF };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_MA1_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_MA1_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_MA1_PERIOD"), 10, 1, 9999, 1),
        new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new InputDescriptor(Inputs.INPUT2, get("LBL_MA2_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD2, get("LBL_MA2_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_MA2_PERIOD"), 5, 1, 9999, 1),
        new IntegerDescriptor(Inputs.SHIFT2, get("LBL_SHIFT"), 0, -999, 999, 1));
    
    var lines = tab.addGroup(get("LBL_COLORS"));
    lines.addRow(new BarDescriptor(Inputs.BAR, get("LBL_UP_COLOR"), defaults.getGreen(), true, false));
    lines.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_MA1_PERIOD"), 21, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(Inputs.INPUT2, Inputs.METHOD2);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_MA2_PERIOD"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SHIFT2, get("LBL_SHIFT"), 0, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(Inputs.BAR, Inputs.DOWN_COLOR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.INPUT2, Inputs.METHOD2, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.DIFF, get("LBL_MA_DIFF"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.INPUT2, Inputs.METHOD2, Inputs.PERIOD2}));
    desc.declareBars(Values.DIFF, Inputs.BAR);
    desc.declareIndicator(Values.DIFF, Inputs.IND);
    desc.setRangeKeys(Values.DIFF);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period1 = getSettings().getInteger(Inputs.PERIOD, 10);
    int period2 = getSettings().getInteger(Inputs.PERIOD2, 5);
    int shift1 = getSettings().getInteger(Inputs.SHIFT, 0);
    int shift2 = getSettings().getInteger(Inputs.SHIFT2, 0);
    if (index <= Util.max(period1-shift1, period2-shift2)) return;

    var method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.SMA);
    Object input = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    var method2 = getSettings().getMAMethod(Inputs.METHOD2, Enums.MAMethod.SMA);
    Object input2 = getSettings().getInput(Inputs.INPUT2, Enums.BarInput.CLOSE);
    var series = ctx.getDataSeries();
    
    Double MA1 = series.ma(method, index+shift1, period1, input);
    Double MA2 = series.ma(method2, index+shift2, period2, input2);
    
    if (MA1 == null || MA2 == null) return;

    Color upColor = getSettings().getBars(Inputs.BAR).getColor();
    Color downColor = getSettings().getColor(Inputs.DOWN_COLOR);
    
    double diff = MA1 - MA2;
    series.setDouble(index,  Values.DIFF, diff);
    series.setBarColor(index, Values.DIFF, diff >= 0 ? upColor : downColor);
    
    series.setComplete(index);
  }
}
