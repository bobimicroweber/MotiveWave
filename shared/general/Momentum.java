package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Momentum */
@StudyHeader(
    namespace="com.motivewave", 
    id="MOMENTUM", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MOMENTUM",
    label="LBL_MOMENTUM",
    desc="DESC_MOMENTUM",
    menu="MENU_GENERAL",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/momentum.htm")
public class Momentum extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { M };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_COLORS"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.M, get("LBL_MOMENTUM"), new String[] {Inputs.INPUT, Inputs.PERIOD}));
    desc.declarePath(Values.M, Inputs.PATH);
    desc.declareIndicator(Values.M, Inputs.IND);
    desc.setRangeKeys(Values.M);
  }

  @Override
  public int getMinBars() { return getSettings().getInteger(Inputs.PERIOD); }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period) return;
    Object input = getSettings().getInput(Inputs.INPUT);

    var series = ctx.getDataSeries();
    Double curVal = series.getDouble(index,  input);
    Double prevVal = series.getDouble(index-period,  input);
    
    if (curVal == null || prevVal == null) return;
    double val = (curVal / prevVal) * 100;
    
    series.setDouble(index, Values.M,  val);
    series.setComplete(index);
  }
}
