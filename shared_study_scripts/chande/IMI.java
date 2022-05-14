package com.motivewave.platform.study.chande;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Intraday Momemtum Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="IMI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_IMI",
    label="LBL_IMI",
    desc="DESC_IMI",
    menu="MENU_TUSCHARD_CHANDE",
    overlay=false,
    studyOverlay=true,
    signals=true)
public class IMI extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { IMI, GAIN, LOSS, SIGNAL }
	enum Signals { IMI_TOP, IMI_BOTTOM }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA));
    inputs.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 6, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_IMI"), defaults.getLineColor(), 1.0f, null);
    path.setSupportsShowAsBars(true);
    path.setBarCenter(50);
    path.setColorPolicies(Enums.ColorPolicy.values());
    lines.addRow(path);
    var signal = new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, true);
    signal.setSupportsShowAsBars(true);
    signal.setBarCenter(50);
    signal.setColorPolicies(Enums.ColorPolicy.values());
    lines.addRow(signal);
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), defaults.getRed(), null, false, false, true));

    tab = sd.addTab(get("TAB_ADVANCED"));
    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 70, 1, 100, 1, true));
    var gd = new GuideDescriptor(Inputs.TOP_GUIDE2, get("LBL_TOP_GUIDE2"), 60, 1, 100, 1, true);
    gd.setEnabled(false);
    guides.addRow(gd);
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    gd = new GuideDescriptor(Inputs.BOTTOM_GUIDE2, get("LBL_BOTTOM_GUIDE2"), 40, 1, 100, 1, true);
    gd.setEnabled(false);
    guides.addRow(gd);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 30, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 6, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.SIGNAL_PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.SIGNAL_PERIOD, Inputs.METHOD);
    desc.exportValue(new ValueDescriptor(Values.IMI, get("LBL_IMI"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("LBL_IMI_SIGNAL"), new String[] {Inputs.PERIOD, Inputs.SIGNAL_PERIOD, Inputs.METHOD}));
    desc.declarePath(Values.IMI, Inputs.PATH);
    desc.declareIndicator(Values.IMI, Inputs.IND);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    desc.setMaxBottomValue(15);
    desc.setMinTopValue(85);
    desc.setRangeKeys(Values.IMI);
    desc.declareSignal(Signals.IMI_TOP, get("IMI_TOP"));
    desc.declareSignal(Signals.IMI_BOTTOM, get("IMI_BOTTOM"));
    desc.setMinTick(0.1);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    var series = ctx.getDataSeries();
    float open = series.getOpen(index);
    float close = series.getClose(index);
    double gain = 0, loss = 0;
    if (close > open) {
      gain = close - open;
      Double pGain = series.getDouble(index-1, Values.GAIN);
      if (pGain != null) gain += pGain;
    }
    else if (close < open) {
      loss = open - close;
      Double pLoss = series.getDouble(index-1, Values.LOSS);
      if (pLoss != null) loss += pLoss;
    }
    
    series.setDouble(index, Values.GAIN, gain);
    series.setDouble(index, Values.LOSS, loss);
    double gains = series.sum(index, period, Values.GAIN);
    double losses = series.sum(index, period, Values.LOSS);
    double imi = 100*(gains / (gains+losses));
    series.setDouble(index, Values.IMI, imi);
    
    int maPeriod = getSettings().getInteger(Inputs.SIGNAL_PERIOD, 6);
    if (index >= maPeriod) {
      Double signal = series.ma(getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA), index, maPeriod, Values.IMI);
      series.setDouble(index, Values.SIGNAL, signal);
    }
    
    // Do we need to generate a signal?
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    if (crossedAbove(series, index, Values.IMI, topGuide.getValue())) {
      series.setBoolean(index, Signals.IMI_TOP, true);
      ctx.signal(index, Signals.IMI_TOP, get("SIGNAL_IMI_TOP", topGuide.getValue(), round(imi)), round(imi));
    }
    else if (crossedBelow(series, index, Values.IMI, bottomGuide.getValue())) {
      series.setBoolean(index, Signals.IMI_BOTTOM, true);
      ctx.signal(index, Signals.IMI_BOTTOM, get("SIGNAL_IMI_BOTTOM", bottomGuide.getValue(), round(imi)), round(imi));
    }
    
    series.setComplete(index, series.isBarComplete(index));
  }  
}
