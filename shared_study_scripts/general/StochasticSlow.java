package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Fast Stochastics (Same as Full with a Slow Period of 1). */
@StudyHeader(
    namespace="com.motivewave", 
    id="STO_SLOW", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_STO_SLOW",
    label="LBL_STO_SLOW",
    desc="DESC_STO_SLOW",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/stochastic_slow.htm")
public class StochasticSlow extends StochasticFull
{
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_K_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD3, get("LBL_D_PERIOD"), 3, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(K_LINE, get("LBL_K_SLOW_LINE"), defaults.getLineColor(), 1.5f, null, true, false, true));
    lines.addRow(new PathDescriptor(D_LINE, get("LBL_D_SLOW_LINE"), defaults.getRed(), 1.0f, null, true, false, true));
    lines.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    lines.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(K_IND, get("LBL_K_SLOW_IND"), null, null, false, true, true));
    indicators.addRow(new IndicatorDescriptor(D_IND, get("LBL_D_SLOW_IND"), defaults.getRed(), null, false, false, true));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 80, 1, 100, 1, true));
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 20, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_K_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD3, get("LBL_D_PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(K_LINE, D_LINE);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD3);
    desc.exportValue(new ValueDescriptor(Values.P_K, get("LBL_K_SLOW"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.P_D, get("LBL_D_SLOW"), new String[] {Inputs.PERIOD, Inputs.PERIOD3}));
    desc.declarePath(Values.P_K, K_LINE);
    desc.declarePath(Values.P_D, D_LINE);
    desc.declareIndicator(Values.P_K, K_IND);
    desc.declareIndicator(Values.P_D, D_IND);
    
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    desc.declareSignal(Signals.PK_TOP, get("LBL_PK_TOP"));
    desc.declareSignal(Signals.PK_BOTTOM, get("LBL_PK_BOTTOM"));

    desc.setRangeKeys(Values.P_K, Values.P_D);
    desc.setMaxBottomValue(10);
    desc.setMinTopValue(90);
    desc.setMinTick(0.1);
  }

  @Override
  protected int getKPPeriod() { return 3; }

}
