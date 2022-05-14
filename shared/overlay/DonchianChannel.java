package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Donchian Channel */
@StudyHeader(
    namespace="com.motivewave", 
    id="DON_CHANNEL", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_DON_CHANNEL", 
    menu="MENU_OVERLAY",
    desc="DESC_DON_CHANNEL",
    label="LBL_DON_CHANNEL",
    overlay=true,
    helpLink="http://www.motivewave.com/studies/donchian_channel.htm")
public class DonchianChannel extends Study 
{
  enum Values { TOP, MIDDLE, BOTTOM }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.TOP_PATH, get("LBL_TOP_LINE"), defaults.getLineColor(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(Inputs.MIDDLE_PATH, get("LBL_MIDDLE_LINE"), defaults.getLineColor(), 1.0f, new float[] {3f, 3f}, false, true, true));
    lines.addRow(new PathDescriptor(Inputs.BOTTOM_PATH, get("LBL_BOTTOM_LINE"), defaults.getRed(), 1.0f, null, true, true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.TOP_IND, get("LBL_TOP_IND"), X11Colors.WHITE, X11Colors.BLACK, false, false, true));
    lines.addRow(new IndicatorDescriptor(Inputs.MIDDLE_IND, get("LBL_MIDDLE_IND"), X11Colors.WHITE, X11Colors.BLACK, false, false, true));
    lines.addRow(new IndicatorDescriptor(Inputs.BOTTOM_IND, get("LBL_BOTTOM_IND"), defaults.getRed(), X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, 1, 9999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(Inputs.TOP_PATH, Inputs.MIDDLE_PATH, Inputs.BOTTOM_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.SHIFT);
    
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_DC_TOP"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.MIDDLE, get("LBL_DC_MIDDLE"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_DC_BOTTOM"), new String[] {Inputs.PERIOD}));
    
    desc.declarePath(Values.TOP, Inputs.TOP_PATH);
    desc.declarePath(Values.MIDDLE, Inputs.MIDDLE_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.BOTTOM_PATH);
    
    desc.declareIndicator(Values.TOP, Inputs.TOP_IND);
    desc.declareIndicator(Values.MIDDLE, Inputs.MIDDLE_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.BOTTOM_IND);
    
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
  }
  
  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    var series = ctx.getDataSeries();
    int shift = getSettings().getInteger(Inputs.SHIFT);

    if (index < period) return;

    Double highest = series.highest(index, period, Enums.BarInput.HIGH);
    Double lowest = series.lowest(index, period, Enums.BarInput.LOW);
    Double mid = null;
    if (highest != null && lowest != null) mid = (highest + lowest)/2;
    series.setDouble(index+shift, Values.TOP, highest);
    series.setDouble(index+shift, Values.MIDDLE, mid);
    series.setDouble(index+shift, Values.BOTTOM, lowest);
    series.setComplete(index, index >= 0 &&  series.isBarComplete(index));
  }
}
