package com.motivewave.platform.study.chande;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Chande Momentum Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="CHANDE_MOMENTUM", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_CHANDE_MOMENTUM",
    label="LBL_CHANDE_MOMENTUM",
    tabName="TAB_CHANDE_MOMENTUM",
    desc="DESC_CHANDE_MOMENTUM",
    menu="MENU_TUSCHARD_CHANDE",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/chande_momentum_oscillator.htm")
public class ChandeMomentum extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { CMO, CMO1, CMO2 }
  enum Signals { CMO_TOP, CMO_BOTTOM }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null);
    path.setSupportsShowAsBars(true);
    lines.addRow(path);
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));
    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 50, -100, 100, 1, true));
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), -50, -100, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.CMO, get("TAB_CHANDE_MOMENTUM"), new String[] {Inputs.INPUT, Inputs.PERIOD}));
    desc.declarePath(Values.CMO, Inputs.PATH);
    desc.declareIndicator(Values.CMO, Inputs.IND);
    desc.setRangeKeys(Values.CMO);
    desc.setMaxBottomValue(-90);
    desc.setMinTopValue(90);
    desc.setMinTick(0.1);
    desc.declareSignal(Signals.CMO_TOP, get("LBL_CMO_TOP"));
    desc.declareSignal(Signals.CMO_BOTTOM, get("LBL_CMO_BOTTOM"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    var series = ctx.getDataSeries();
    Object input = getSettings().getInput(Inputs.INPUT);

    Double d1 = series.getDouble(index,  input);
    Double d2 = series.getDouble(index-1,  input);
    if (d1 == null || d2 == null) return;
    
    double diff = d1 - d2;
    double cmo1=0, cmo2=0;
    if (diff > 0) cmo1 = diff;
    if (diff < 0) cmo2 = -diff;
    
    series.setDouble(index, Values.CMO1, cmo1);
    series.setDouble(index, Values.CMO2, cmo2);

    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period-1) return;
    
    double sum1 = series.sum(index, period, Values.CMO1);
    double sum2 = series.sum(index, period, Values.CMO2);
    
    double cmo = ((sum1-sum2)/(sum1+sum2)) * 100.0;
    
    series.setDouble(index,  Values.CMO, cmo);

    if (!series.isBarComplete(index)) return;
    
    // Do we need to generate a signal?
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    cmo = round(cmo);
    if (crossedAbove(series, index, Values.CMO, topGuide.getValue())) {
      ctx.signal(index, Signals.CMO_TOP, get("SIGNAL_CMO_TOP", cmo, topGuide.getValue()), cmo);
    }
    else if (crossedBelow(series, index, Values.CMO, bottomGuide.getValue())) {
      ctx.signal(index, Signals.CMO_BOTTOM, get("SIGNAL_CMO_BOTTOM", cmo, bottomGuide.getValue()), cmo);
    }
    
    series.setComplete(index);
  }
}
