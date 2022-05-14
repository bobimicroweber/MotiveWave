package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Percentage Volume Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="PVO", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_PVO",
    label="LBL_PVO",
    desc="DESC_PVO",
    menu="MENU_VOLUME",
    overlay=false,
    requiresVolume=true,
    helpLink="http://www.motivewave.com/studies/percentage_volume_oscillator.htm")
public class PVO extends com.motivewave.platform.sdk.study.Study 
{
  final static String HIST_IND = "histInd";
	
  enum Values { PVO, SIGNAL, HIST }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new MAMethodDescriptor(Inputs.SIGNAL_METHOD, get("LBL_SIGNAL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD1"), 12, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 26, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_PVO_LINE"), defaults.getLineColor(), 1.5f, null, true, false, true));
    lines.addRow(new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, true));
    var histogram = new PathDescriptor(Inputs.BAR, get("LBL_BAR_COLOR"), defaults.getBarColor(), 1.0f, null, true, false, true);
    histogram.setShowAsBars(true);
    histogram.setSupportsShowAsBars(true);
    histogram.setColorPolicies(Enums.ColorPolicy.values());
    lines.addRow(histogram);

    tab = sd.addTab(get("TAB_ADVANCED"));
    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_PVO_IND"), null, null, false, true, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), defaults.getRed(), null, false, false, true));
    indicators.addRow(new IndicatorDescriptor(HIST_IND, get("LBL_HIST_IND"), defaults.getBarColor(), null, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD, Inputs.SIGNAL_METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD1"), 12, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 26, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.SIGNAL_PATH, Inputs.BAR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.PVO, get("LBL_PVO"), new String[] {Inputs.PERIOD, Inputs.PERIOD2}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("LBL_PVO_SIGNAL"), new String[] {Inputs.SIGNAL_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.HIST, get("LBL_PVO_HIST"), new String[] {Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD}));
    desc.declarePath(Values.PVO, Inputs.PATH);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    desc.declarePath(Values.HIST, Inputs.BAR);
    desc.declareIndicator(Values.PVO, Inputs.IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    desc.declareIndicator(Values.HIST, HIST_IND);
    desc.setRangeKeys(Values.PVO, Values.SIGNAL, Values.HIST);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
    desc.setMinTick(0.1);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period1 = getSettings().getInteger(Inputs.PERIOD);
    int period2 = getSettings().getInteger(Inputs.PERIOD2);
    int period = Util.max(period1, period2);
    if (index <= period) return;

    var method = getSettings().getMAMethod(Inputs.METHOD);
    var series = ctx.getDataSeries();
    Double MA1 = null, MA2 = null;
    
    MA1 = series.ma(method, index, period1, Enums.BarInput.VOLUME);
    MA2 = series.ma(method, index, period2, Enums.BarInput.VOLUME);
    
    if (MA1 == null || MA2 == null) return;

    double PVO = ((MA1 - MA2)/MA2) * 100;
    series.setDouble(index, Values.PVO, PVO);

    int signalPeriod = getSettings().getInteger(Inputs.SIGNAL_PERIOD);
    if (index <= period + signalPeriod) return; // Not enough data yet

    // Calculate moving average of PVO (signal line)
    Double signal = series.ma(getSettings().getMAMethod(Inputs.SIGNAL_METHOD), index, signalPeriod, Values.PVO);
    series.setDouble(index, Values.SIGNAL, signal);
    
    if (signal == null) return;
    
    series.setDouble(index, Values.HIST, PVO - signal);
    series.setComplete(index);
  }  
}
