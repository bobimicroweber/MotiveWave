package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Price Channel */
@StudyHeader(
    namespace="com.motivewave", 
    id="PRICE_CHANNEL", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_PRICE_CHANNEL", 
    menu="MENU_OVERLAY",
    desc="DESC_PRICE_CHANNEL",
    label="LBL_PRICE_CHANNEL",
    overlay=true,
    helpLink="http://www.motivewave.com/studies/price_channel.htm")
public class PriceChannel extends Study 
{
  final static String TOP_PERIOD = "topPeriod", BOTTOM_PERIOD = "bottomPeriod";
  
  enum Values { TOP, BOTTOM }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(TOP_PERIOD, get("LBL_TOP_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(BOTTOM_PERIOD, get("LBL_BOTTOM_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.TOP_PATH, get("LBL_TOP_LINE"), defaults.getBlue(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(Inputs.BOTTOM_PATH, get("LBL_BOTTOM_LINE"), defaults.getBlue(), 1.0f, null, true, true, true));
    lines.addRow(new ShadeDescriptor(Inputs.FILL, get("LBL_FILL_COLOR"), Inputs.TOP_PATH, Inputs.BOTTOM_PATH, Enums.ShadeType.BOTH, defaults.getFillColor(), false, true));
    lines.addRow(new IndicatorDescriptor(Inputs.TOP_IND, get("LBL_TOP_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    lines.addRow(new IndicatorDescriptor(Inputs.BOTTOM_IND, get("LBL_BOTTOM_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(TOP_PERIOD, get("LBL_TOP_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(BOTTOM_PERIOD, get("LBL_BOTTOM_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(Inputs.TOP_PATH, Inputs.BOTTOM_PATH, Inputs.FILL);

    var desc = createRD();
    desc.setLabelSettings(TOP_PERIOD, BOTTOM_PERIOD, Inputs.SHIFT);
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_PRICE_CHANNEL_TOP"), new String[] {TOP_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_PRICE_CHANNEL_BOTTOM"), new String[] {BOTTOM_PERIOD}));
    desc.declarePath(Values.TOP, Inputs.TOP_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.BOTTOM_PATH);
    desc.declareIndicator(Values.TOP, Inputs.TOP_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.BOTTOM_IND);
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
  }

  @Override
  public int getMinBars()
  {
    int shift = getSettings().getInteger(Inputs.SHIFT, 0);
    int topPeriod = getSettings().getInteger(TOP_PERIOD);
    int bottomPeriod = getSettings().getInteger(BOTTOM_PERIOD);

    return Math.max(topPeriod, bottomPeriod) + (shift > 0 ? shift : 0);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int topPeriod = getSettings().getInteger(TOP_PERIOD);
    int bottomPeriod = getSettings().getInteger(BOTTOM_PERIOD);
    int shift = getSettings().getInteger(Inputs.SHIFT, 0);
    var series = ctx.getDataSeries();

    if (index < topPeriod || index < bottomPeriod) return;
    
    series.setDouble(index+shift, Values.TOP, series.highest(index, topPeriod, Enums.BarInput.HIGH));
    series.setDouble(index+shift, Values.BOTTOM, series.lowest(index, bottomPeriod, Enums.BarInput.LOW));
    series.setComplete(index, index >= 0 && index< series.size()-1); // latest bar is not complete
  }
}
