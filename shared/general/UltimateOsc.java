package com.motivewave.platform.study.general;

import com.motivewave.common.Util;
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

/** Ultimate Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="ULT_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ULT_OSC",
    tabName="TAB_ULT_OSC",
    desc="DESC_ULT_OSC",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/ultimate_oscillator.htm")
public class UltimateOsc extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { UO, BP, TR, DIV1, DIV2, DIV3 }
  enum Signals { UO_TOP, UO_BOTTOM }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 7, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 14, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD3, get("LBL_PERIOD3"), 28, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_COLORS"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 70, 1, 100, 1, true));
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 30, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 7, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD3, get("LBL_PERIOD3"), 28, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3);
    desc.exportValue(new ValueDescriptor(Values.UO, get("VAL_ULT_OSC"), new String[] {Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3}));
    desc.declarePath(Values.UO, Inputs.PATH);
    desc.declareIndicator(Values.UO, Inputs.IND);
    desc.setRangeKeys(Values.UO);
    desc.setMaxBottomValue(15);
    desc.setMinTopValue(85);
    desc.setMinTick(0.1);
    desc.declareSignal(Signals.UO_TOP, get("LBL_UO_TOP"));
    desc.declareSignal(Signals.UO_BOTTOM, get("LBL_UO_BOTTOM"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    int period = getSettings().getInteger(Inputs.PERIOD);
    int period2 = getSettings().getInteger(Inputs.PERIOD2);
    int period3 = getSettings().getInteger(Inputs.PERIOD3);
    int maxPeriod = Util.maxInt(period, period2, period3);
    if (index < 1) return;

    double TL = Math.min(series.getLow(index), series.getClose(index-1)); // True Low
    double BP = series.getClose(index) - TL; // Buying Pressure
    double TR = series.getTrueRange(index); // True Range
    series.setDouble(index, Values.BP, BP);
    series.setDouble(index, Values.TR, TR);
    
    if (index <= period) return;

    // Calculate BPSum1 and TRSum1
    double sumBP1 = series.sum(index, period, Values.BP);
    double sumTR1 = series.sum(index, period, Values.TR);
    series.setDouble(index, Values.DIV1, sumBP1/sumTR1);

    if (index <= period2) return;

    // Calculate BPSum2 and TRSum2
    double sumBP2 = series.sum(index, period2, Values.BP);
    double sumTR2 = series.sum(index, period2, Values.TR);
    series.setDouble(index, Values.DIV2, sumBP2/sumTR2);

    if (index <= period3) return;

    // Calculate BPSum2 and TRSum2
    double sumBP3 = series.sum(index, period3, Values.BP);
    double sumTR3 = series.sum(index, period3, Values.TR);
    series.setDouble(index, Values.DIV3, sumBP3/sumTR3);

    if (index <= maxPeriod) return;

    // Now we have enough data to calculate the Ultimate Osc
    Double div1 = series.getDouble(index,  Values.DIV1);
    Double div2 = series.getDouble(index,  Values.DIV2);
    Double div3 = series.getDouble(index,  Values.DIV3);
    if (div1 == null || div2 == null || div3 == null) return;
    
    double RawUO = 4*div1 + 2*div2 + div3;
    double UltOsc = (RawUO / (4 + 2 + 1)) * 100;
    series.setDouble(index,  Values.UO, UltOsc);

    if (!series.isBarComplete(index)) return;

    // Do we need to generate a signal?
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    UltOsc = round(UltOsc);
    if (crossedAbove(series, index, Values.UO, topGuide.getValue())) {
      ctx.signal(index, Signals.UO_TOP, get("SIGNAL_UO_TOP", UltOsc, topGuide.getValue()), UltOsc);
    }
    else if (crossedBelow(series, index, Values.UO, bottomGuide.getValue())) {
      ctx.signal(index, Signals.UO_BOTTOM, get("SIGNAL_UO_BOTTOM", UltOsc, bottomGuide.getValue()), UltOsc);
    }
    
    series.setComplete(index);
  }
}
