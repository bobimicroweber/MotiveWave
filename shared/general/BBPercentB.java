package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Bollinger Bands Percent B (%B) */
@StudyHeader(
    namespace="com.motivewave", 
    id="BB_PERCENT", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_BB_PERCENT",
    label="LBL_BB_PERCENT",
    menu="MENU_GENERAL",
    desc="DESC_BB_PERCENT",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/bollinger_bands_b.htm")
public class BBPercentB extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { PB };
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
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 1.0, -100, 100, 0.01, true));
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 0.5, -100, 100, 0.01, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 0.0, -100, 100, 0.01, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(STD_DEV, Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, STD_DEV);
    desc.exportValue(new ValueDescriptor(Values.PB, get("LBL_PB"), new String[] {Inputs.INPUT, Inputs.PERIOD, STD_DEV}));
    desc.declarePath(Values.PB, Inputs.PATH);
    desc.declareIndicator(Values.PB, Inputs.IND);
    desc.setRangeKeys(Values.PB);
    desc.setMinTick(0.01);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 20);
    if (index < period) return;
    Object input = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    double stdDev = getSettings().getDouble(STD_DEV, 2.0);
    DataSeries series = ctx.getDataSeries();

    double value = series.getDouble(index,  input);
    double ma = series.sma(index, period, input);
    Double dev = series.std(index, period, input) * stdDev;
    double upperBand = ma + dev;
    double lowerBand = ma - dev;
    
    series.setDouble(index, Values.PB, (value - lowerBand)/(upperBand-lowerBand) );
    series.setComplete(index);
  }  
}
