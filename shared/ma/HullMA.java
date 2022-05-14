package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/**
 * Hull Moving Average Study see: http://www.justdata.com.au/Journals/AlanHull/hull_ma.htm
 * Calculation is as follows: HMA[i] = MA( (2*MA(input, period/2) - MA(input, period)), SQRT(period))
 */
@StudyHeader(
    namespace="com.motivewave", 
    id="HMA", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_HMA", 
    desc="DESC_HMA",
    label="LBL_HMA",
    menu="MENU_MOVING_AVERAGE",
    overlay=true,
    signals=true,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/hull_moving_average.htm")
public class HullMA extends Study 
{
  enum Values { MA }
  enum Signals { CROSS_ABOVE, CROSS_BELOW }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
  
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.WMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1),
        new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), null, 1.0f, null, true, true, false));
    colors.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var barColors = tab.addGroup(get("LBL_BAR_COLOR"));
    barColors.addRow(new BooleanDescriptor(Inputs.ENABLE_BAR_COLOR, get("LBL_ENABLE_BAR_COLOR"), false));
    barColors.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreenLine()));
    barColors.addRow(new ColorDescriptor(Inputs.NEUTRAL_COLOR, get("LBL_NEUTRAL_COLOR"), defaults.getLineColor()));
    barColors.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRedLine()));

    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));

    sd.addDependency(new EnabledDependency(Inputs.ENABLE_BAR_COLOR, Inputs.UP_COLOR, Inputs.NEUTRAL_COLOR, Inputs.DOWN_COLOR ));

    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()),
        new SliderDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.SHIFT);
    desc.exportValue(new ValueDescriptor(Values.MA, get("LBL_HMA"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.SHIFT}));
    desc.declarePath(Values.MA, Inputs.PATH);
    desc.declareIndicator(Values.MA, Inputs.IND);
    // Signals
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_PRICE_CROSS_ABOVE"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_PRICE_CROSS_BELOW"));
  }

  @Override
  public void clearState()
  {
    super.clearState();
    lastSize = -1;
  }

  @Override
  public void onBarUpdate(DataContext ctx)
  {
    Object input = getSettings().getInput(Inputs.INPUT);
    int shift = getSettings().getInteger(Inputs.SHIFT);
    int period = getSettings().getInteger(Inputs.PERIOD);
    int sqp = (int)Math.sqrt(period);
    var method = getSettings().getMAMethod(Inputs.METHOD);
    var series = ctx.getDataSeries();
    int last = series.size()-1;
    if (series.size() <= 1) return;

    Double ma1 = series.ma(method, last, period/2, input);
    if (ma1 == null) return;
    ma1 = 2*ma1;
    Double ma2 = series.ma(method, last, period, input);
    if (ma2 == null) return;
    series.setDouble(last, "MA_TMP", ma1 - ma2);

    Double value = null;
    if (shift >=0) value = series.ma(method, last, sqp, "MA_TMP");
    else value = series.ma(method, last-shift, sqp, "MA_TMP"); // future 'unfinished' ma
    if (value == null) return;
    if (shift >= 0) series.setDouble(last+shift, Values.MA, value);
    else series.setDouble(last, Values.MA, value); // future 'unfinished' ma
    
    var upMarker = getSettings().getMarker(Inputs.UP_MARKER);
    var downMarker = getSettings().getMarker(Inputs.DOWN_MARKER);
    boolean doBarColor = getSettings().getBoolean(Inputs.ENABLE_BAR_COLOR, false);
    boolean upEnabled = upMarker != null && upMarker.isEnabled();
    boolean downEnabled = downMarker != null && downMarker.isEnabled();
    
    if (!doBarColor && !upEnabled && !downEnabled && !ctx.isSignalEnabled(Signals.CROSS_ABOVE) &&
        !ctx.isSignalEnabled(Signals.CROSS_BELOW)) return;

    Double val = series.getDouble(Values.MA);
    Double prevVal = series.getDouble(series.size()-2, Values.MA);
    if (val == null || prevVal == null) return;

    if (doBarColor) {
      if (val < prevVal) series.setPriceBarColor(getSettings().getColor(Inputs.DOWN_COLOR));
      else if (val == prevVal) series.setPriceBarColor(getSettings().getColor(Inputs.NEUTRAL_COLOR));
      else series.setPriceBarColor(getSettings().getColor(Inputs.UP_COLOR));
    }
    
    double v = val;
    double pv = prevVal;
    float ma = series.getInstrument().round((float)v);
    float pma = series.getInstrument().round((float)pv);
    float close = series.getClose();
    float lastClose = series.getClose(series.size()-2);
    int i = series.size()-1;
    
    var c = new Coordinate(series.getStartTime(i), ma);
    if (upEnabled || ctx.isSignalEnabled(Signals.CROSS_ABOVE)) {
      if (lastClose <= pma && close > ma) {
        //series.setBoolean(i, Signals.CROSS_ABOVE, true);
        String msg = get("SIGNAL_PRICE_CROSS_ABOVE", format(close), format(ma));
        if (upEnabled && !series.getBoolean("UP_MARKER_ADDED", false)) {
          addFigure(new Marker(c, Enums.Position.BOTTOM, upMarker, msg));
          series.setBoolean("UP_MARKER_ADDED", true);
        }
        ctx.signal(i, Signals.CROSS_ABOVE, msg, round(close));
      }
    }

    if (downEnabled || ctx.isSignalEnabled(Signals.CROSS_BELOW)) {
      //series.setBoolean(i, Signals.CROSS_BELOW, true);
      if (lastClose >= pma && close < ma) {
        String msg = get("SIGNAL_PRICE_CROSS_BELOW", format(close), format(ma));
        if (downEnabled && !series.getBoolean("DOWN_MARKER_ADDED", false)) {
          addFigure(new Marker(c, Enums.Position.TOP, downMarker, msg));
          series.setBoolean("DOWN_MARKER_ADDED", true);
        }
        ctx.signal(i, Signals.CROSS_BELOW, msg, round(close));
      }
    }
  }

  @Override
  public void onLoad(Defaults defaults)
  {
    setMinBars(getSettings().getInteger(Inputs.PERIOD));
  }

  @Override
  protected synchronized void calculateValues(DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    int sqp = (int)Math.sqrt(period);
    Enums.MAMethod method = getSettings().getMAMethod(Inputs.METHOD);
    int shift = getSettings().getInteger(Inputs.SHIFT);
    Object input = getSettings().getInput(Inputs.INPUT);
    DataSeries series = ctx.getDataSeries();
    // Optimization: calculate from the last complete index
    int start = period;
    if (lastSize > series.size()) lastSize = -1;
    if (series.size() - lastSize == 1) {
      for(int i = series.size()-1; i >= 0; i--) {
        if (series.isComplete(i)) {
          start = i+1; 
          break;
        }
      }
    }
    
    for(int i = start; i < series.size(); i++) {
      if (series.isComplete(i)) continue;
      Double ma1 = series.ma(method, i, period/2, input);
      if (ma1 == null) continue;
      ma1 = ma1*2;
      Double ma2 = series.ma(method, i, period, input);
      if (ma2 == null) continue;
      series.setDouble(i, "MA_TMP", ma1 - ma2);
    }

    int end = series.size()-1;
    //if (shift < 0) end -= shift; // calculate future values
    if (start <= period+sqp) start = period + sqp;
    
    int lastComplete = series.size()-1;
    if (shift < 0) lastComplete += shift;
    
    for(int i = start; i <= end; i++) {
      if (series.isComplete(i+shift)) continue;
      Double value = series.ma(method, i, sqp, "MA_TMP");
      if (value == null) continue;
      series.setDouble(i+shift, Values.MA, value);
      series.setComplete(i+shift, i >= 0 && i < lastComplete); // latest bar is not complete
    }

    lastSize = series.size();

    MarkerInfo upMarker = getSettings().getMarker(Inputs.UP_MARKER);
    MarkerInfo downMarker = getSettings().getMarker(Inputs.DOWN_MARKER);
    boolean doBarColor = getSettings().getBoolean(Inputs.ENABLE_BAR_COLOR, false);
    boolean upEnabled = upMarker != null && upMarker.isEnabled();
    boolean downEnabled = downMarker != null && downMarker.isEnabled();
    
    if (!doBarColor && !upEnabled && !downEnabled && !ctx.isSignalEnabled(Signals.CROSS_ABOVE) &&
        !ctx.isSignalEnabled(Signals.CROSS_BELOW)) return;
    
    clearFigures();
    for(int i = 1; i < series.size(); i++) {
      Double ma = series.getDouble(i, Values.MA);
      if (ma == null) continue;
      if (doBarColor) {
        Double prevMa = series.getDouble(i-1, Values.MA);
        if (prevMa == null) continue;
        if (ma < prevMa) series.setPriceBarColor(i, getSettings().getColor(Inputs.DOWN_COLOR));
        else if (ma == prevMa) series.setPriceBarColor(i, getSettings().getColor(Inputs.NEUTRAL_COLOR));
        else series.setPriceBarColor(i, getSettings().getColor(Inputs.UP_COLOR));
      }
      
      // Check to see if a cross occurred and raise signal.
      Coordinate c = new Coordinate(series.getStartTime(i), ma);
      float close = series.getClose(i);
      if (crossedAbove(series, i, Enums.BarInput.CLOSE, Values.MA)) {
        series.setBoolean(i, Signals.CROSS_ABOVE, true);
        String msg = get("SIGNAL_PRICE_CROSS_ABOVE", format(close), format(ma));
        if (upEnabled) addFigure(new Marker(c, Enums.Position.BOTTOM, upMarker, msg));
        ctx.signal(i, Signals.CROSS_ABOVE, msg, round(close));
      }
      else if (crossedBelow(series, i, Enums.BarInput.CLOSE, Values.MA)) {
        series.setBoolean(i, Signals.CROSS_BELOW, true);
        String msg = get("SIGNAL_PRICE_CROSS_BELOW", format(close), format(ma));
        if (downEnabled) addFigure(new Marker(c, Enums.Position.TOP, downMarker, msg));
        ctx.signal(i, Signals.CROSS_BELOW, msg, round(close));
      }
    }
  }
  private int lastSize = -1;
}
