package com.motivewave.platform.study.chaikin;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Chaikin Money Flow */
@StudyHeader(
    namespace="com.motivewave", 
    id="CKN_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_CKN_OSC",
    label="LBL_CKN_OSC",
    tabName="TAB_CKN_OSC",
    desc="DESC_CKN_OSC",
    menu="MENU_MARC_CHAIKIN",
    overlay=false,
    requiresVolume=true,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/chaikin_oscillator.htm")
public class ChaikinOscillator extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { VAL, ADL }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 3, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 10, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false);
    path.setSupportsShowAsBars(true);
    settings.addRow(path);
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.VAL, get("TAB_CKN_OSC"), new String[] {Inputs.PERIOD, Inputs.PERIOD2}));
    desc.declarePath(Values.VAL, Inputs.PATH);
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.setRangeKeys(Values.VAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    int period1 = getSettings().getInteger(Inputs.PERIOD);
    int period2 = getSettings().getInteger(Inputs.PERIOD2);
    var series = ctx.getDataSeries();
    // Calculate the OBV
    Double prev = series.getDouble(index-1, Values.ADL);
    if (prev == null) prev = 0.0;
    double adl = prev + series.getVolume(index)*series.mfm(index);
    series.setDouble(index, Values.ADL, adl);
    
    if (index < Util.maxInt(period1, period2)-1) return;

    Double ema1 = series.ema(index, period1, Values.ADL);
    Double ema2 = series.ema(index, period2, Values.ADL);
    
    if (ema1 == null || ema2 == null) return;
  
    series.setDouble(index, Values.VAL, ema1 - ema2);
    series.setComplete(index);
  }  
}
