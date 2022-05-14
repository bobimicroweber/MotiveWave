package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Williams %R */
@StudyHeader(
    namespace="com.motivewave", 
    id="WILL_R", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_WILL_R",
    tabName="TAB_WILL_R",
    desc="DESC_WILL_R",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/williams_r.htm")
public class WilliamsR extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { R }
  enum Signals { R_TOP, R_BOTTOM }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_COLORS"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null);
    path.setSupportsShowAsBars(true);
    path.setBarCenter(-50);
    lines.addRow(path);
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), -20, -100, 0, 1, true));
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), -50, -100, 0, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), -80, -100, 0, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.R, get("VAL_WILL_R"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.R, Inputs.PATH);
    desc.declareIndicator(Values.R, Inputs.IND);
    desc.setRangeKeys(Values.R);
    desc.setMaxBottomValue(-90);
    desc.setMinTopValue(-10);
    desc.setMinTick(0.1);
    desc.declareSignal(Signals.R_TOP, get("LBL_R_TOP"));
    desc.declareSignal(Signals.R_BOTTOM, get("LBL_R_BOTTOM"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period) return;
  
    // Find the highest high and lowest low over the period
    double high = series.highest(index, period, Enums.BarInput.HIGH), low = series.lowest(index, period, Enums.BarInput.LOW);
    double R = ( (high - series.getClose(index)) / (high - low) ) * (-100.0);
    series.setDouble(index, Values.R, R);
    series.setComplete(index);
    
    if (!series.isBarComplete(index)) return;
    
    // Do we need to generate a signal?
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    
    R = round(R);
    if (crossedAbove(series, index, Values.R, topGuide.getValue())) {
      ctx.signal(index, Signals.R_TOP, get("SIGNAL_R_TOP", R, topGuide.getValue()), R);
    }
    else if (crossedBelow(series, index, Values.R, bottomGuide.getValue())) {
      ctx.signal(index, Signals.R_BOTTOM, get("SIGNAL_R_BOTTOM", R, bottomGuide.getValue()), R);
    }
  }
}
