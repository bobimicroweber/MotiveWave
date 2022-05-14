package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
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
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Moving Average Convergence/Divergence (MACD) */
@StudyHeader(
    namespace="com.motivewave", 
    id="MACD", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MACD",
    label="LBL_MACD",
    desc="DESC_MACD",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/moving_average_convergence_divergence.htm")
public class MACD extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { MACD, SIGNAL, HIST };
  enum Signals { CROSS_ABOVE, CROSS_BELOW };

	final static String MACD_LINE = "macdLine", MACD_IND = "macdInd", HIST_IND = "histInd";
	
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    grp.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    grp.addRow(new MAMethodDescriptor(Inputs.SIGNAL_METHOD, get("LBL_SIGNAL_METHOD"), Enums.MAMethod.SMA));
    grp.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD1"), 12, 1, 9999, 1));
    grp.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 26, 1, 9999, 1));
    grp.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, 1));
    
    tab = sd.addTab(get("TAB_DISPLAY"));

    grp = tab.addGroup(get("LBL_DISPLAY"));
    grp.addRow(new PathDescriptor(MACD_LINE, get("LBL_MACD_LINE"), defaults.getLineColor(), 1.5f, null, true, false, true));
    grp.addRow(new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, true));
    var histogram = new PathDescriptor(Inputs.BAR, get("LBL_HISTOGRAM"), defaults.getBarColor(), 1.0f, null, true, false, true);
    histogram.setShowAsBars(true);
    histogram.setSupportsShowAsBars(true);
    histogram.setColorPolicies(Enums.ColorPolicy.values());
    grp.addRow(histogram);
    grp.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), MACD_LINE, Inputs.SIGNAL_PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), false, true));
    grp.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), MACD_LINE, Inputs.SIGNAL_PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), false, true));
    
    grp.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    grp.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    grp = tab.addGroup(get("LBL_INDICATORS"));
    grp.addRow(new IndicatorDescriptor(MACD_IND, get("LBL_MACD_IND"), null, null, false, true, true));
    grp.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), defaults.getRed(), null, false, false, true));
    grp.addRow(new IndicatorDescriptor(HIST_IND, get("LBL_MACD_HIST_IND"), defaults.getBarColor(), null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD, Inputs.SIGNAL_METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD1"), 12, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 26, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(MACD_LINE, Inputs.SIGNAL_PATH, Inputs.BAR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.MACD, get("LBL_MACD"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("LBL_MACD_SIGNAL"), new String[] {Inputs.SIGNAL_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.HIST, get("LBL_MACD_HIST"), new String[] {Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD}));
    desc.declarePath(Values.MACD, MACD_LINE);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    desc.declarePath(Values.HIST, Inputs.BAR);
    desc.declareIndicator(Values.MACD, MACD_IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    desc.declareIndicator(Values.HIST, HIST_IND);
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    desc.setRangeKeys(Values.MACD, Values.SIGNAL, Values.HIST);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var settings = getSettings();
    int period1 = settings.getInteger(Inputs.PERIOD, 12);
    int period2 = settings.getInteger(Inputs.PERIOD2, 26);
    int period = Util.max(period1, period2);
    if (index < period) return;

    var method = settings.getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA);
    var input = settings.getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    var series = ctx.getDataSeries();
    Double ma1 = null, ma2 = null;
    
    ma1 = series.ma(method, index, period1, input);
    ma2 = series.ma(method, index, period2, input);    
    
    if (ma1 == null || ma2 == null) return;

    double macd = ma1 - ma2; 
    series.setDouble(index, Values.MACD, macd);

    int signalPeriod = settings.getInteger(Inputs.SIGNAL_PERIOD, 9);
    if (index < period + signalPeriod) return; // Not enough data yet

    // Calculate moving average of MACD (signal line)
    var signal = series.ma(getSettings().getMAMethod(Inputs.SIGNAL_METHOD, Enums.MAMethod.SMA), index, signalPeriod, Values.MACD);
    if (signal == null) return;
    
    series.setDouble(index, Values.SIGNAL, signal);
    series.setDouble(index, Values.HIST, macd - signal);

    if (!series.isBarComplete(index)) return;
    
    // Check for signal events
    var c = new Coordinate(series.getStartTime(index), signal);
    if (crossedAbove(series, index, Values.MACD, Values.SIGNAL)) {
      var marker = settings.getMarker(Inputs.UP_MARKER);
      var msg = get("SIGNAL_MACD_CROSS_ABOVE", macd, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
    }
    else if (crossedBelow(series, index, Values.MACD, Values.SIGNAL)) {
      var marker = settings.getMarker(Inputs.DOWN_MARKER);
      var msg = get("SIGNAL_MACD_CROSS_BELOW", macd, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
    }
    
    series.setComplete(index, series.isBarComplete(index));
  }  
}
