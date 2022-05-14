package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Bollinger Bands Width */
@StudyHeader(
    namespace="com.motivewave", 
    id="BB_WIDTH", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_BB_WIDTH",
    label="LBL_BB_WIDTH",
    desc="DESC_BB_WIDTH",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/bollinger_band_width.htm")
public class BBWidth extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { BBW };
  final static String STD_DEV = "stdDev";
	
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(STD_DEV, get("LBL_STD_DEV"), 2, 0.1, 999, 0.1));
    
    var lines = tab.addGroup(get("LBL_COLORS"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(STD_DEV, Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, STD_DEV);
    desc.exportValue(new ValueDescriptor(Values.BBW, get("LBL_BB_WIDTH"), new String[] {Inputs.INPUT, Inputs.PERIOD, STD_DEV}));
    desc.declarePath(Values.BBW, Inputs.PATH);
    desc.declareIndicator(Values.BBW, Inputs.IND);
    desc.setRangeKeys(Values.BBW);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 20);
    if (index < period) return;
    Object input = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    double stdDev = getSettings().getDouble(STD_DEV, 2.0);
    DataSeries series = ctx.getDataSeries();
    double dev = series.std(index, period, input) * stdDev;
    
    series.setDouble(index, Values.BBW, 2*dev );
    series.setComplete(index);
  }  
}
