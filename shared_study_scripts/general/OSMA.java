package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Moving Average of Oscillator
   Calculates the difference between the oscillator and the oscillator smoothing.
   This uses the MACD line as the oscillator and the signal line as the smoothing.
   (Essentially, this is just the MACD - the lines).
   @see http://www.insidefutures.com/education/courses/tech101.php#osma */
@StudyHeader(
    namespace="com.motivewave", 
    id="OSMA", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_OSMA",
    label="LBL_OSMA",
    desc="DESC_OSMA",
    menu="MENU_GENERAL",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/moving_average_of_oscillator.htm")
public class OSMA extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { MACD, SIGNAL, OSMA };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new MAMethodDescriptor(Inputs.SIGNAL_METHOD, get("LBL_SIGNAL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD1"), 12, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 26, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_SETTINGS"));
    var histogram = new PathDescriptor(Inputs.BAR, get("LBL_BAR_COLOR"), defaults.getBarColor(), 1.0f, null, true, false, true);
    histogram.setShowAsBars(true);
    histogram.setSupportsShowAsBars(true);
    histogram.setColorPolicies(Enums.ColorPolicy.values());
    lines.addRow(histogram);
    //lines.addRow(new BarDescriptor(Inputs.BAR, get("LBL_BAR_COLOR"), defaults.getBarColor(), true, false));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), defaults.getBarColor(), null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD, Inputs.SIGNAL_METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD1"), 12, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 26, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.BAR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.OSMA, get("LBL_OSMA"), new String[] {Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD}));
    desc.declarePath(Values.OSMA, Inputs.BAR);
    desc.declareIndicator(Values.OSMA, Inputs.IND);
    desc.setRangeKeys(Values.OSMA);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period1 = getSettings().getInteger(Inputs.PERIOD, 12);
    int period2 = getSettings().getInteger(Inputs.PERIOD2, 26);
    int period = Util.max(period1, period2);
    if (index <= period) return;

    var method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA);
    var sMethod = getSettings().getMAMethod(Inputs.SIGNAL_METHOD, Enums.MAMethod.SMA);
    Object input = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    var series = ctx.getDataSeries();
    Double MA1 = null, MA2 = null;
    
    MA1 = series.ma(method, index, period1, input);
    MA2 = series.ma(method, index, period2, input);
    if (MA1 == null || MA2 == null) return;
    
    double MACD = MA1 - MA2; 
    series.setDouble(index, Values.MACD, MACD);

    int signalPeriod = getSettings().getInteger(Inputs.SIGNAL_PERIOD, 9);
    if (index <= period + signalPeriod) return; // Not enough data yet

    // Calculate moving average of MACD (signal line)
    Double signal = series.ma(sMethod, index, signalPeriod, Values.MACD);
    series.setDouble(index, Values.SIGNAL, signal);
    if (signal == null) return;
    series.setDouble(index, Values.OSMA, MACD - signal);
    series.setComplete(index, series.isBarComplete(index));
  }
}
