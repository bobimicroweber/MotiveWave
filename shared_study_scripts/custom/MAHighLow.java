package com.motivewave.platform.study.custom;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Moving Average (High/Low) */
@StudyHeader(
    namespace="com.motivewave", 
    id="MA_HIGH_LOW", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MA_HIGH_LOW", 
    desc="DESC_MA_HIGH_LOW",
    menu="MENU_OVERLAY",
    menu2="MENU_CUSTOM",
    overlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/ma_high_low.htm")
public class MAHighLow extends Study 
{
  final static String HIGH_PERIOD = "highPeriod", HIGH_MA_PERIOD = "highMAPeriod", HIGH_SHIFT = "highShift";
  final static String LOW_PERIOD = "lowPeriod", LOW_MA_PERIOD = "lowMAPeriod", LOW_SHIFT = "lowShift";
  
  enum Values { TOP, MIDDLE, BOTTOM, HIGHEST, LOWEST }
  enum Signals { CROSS_ABOVE_TOP_BAND, CROSS_BELOW_BOTTOM_BAND }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.WMA));
    inputs.addRow(new IntegerDescriptor(HIGH_PERIOD, get("LBL_HIGH_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(HIGH_MA_PERIOD, get("LBL_HIGH_MA_PERIOD"), 14, 1, 9999, 1), new IntegerDescriptor(HIGH_SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new IntegerDescriptor(LOW_PERIOD, get("LBL_LOW_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(LOW_MA_PERIOD, get("LBL_LOW_MA_PERIOD"), 14, 1, 9999, 1), new IntegerDescriptor(LOW_SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.TOP_PATH, get("LBL_TOP_LINE"), defaults.getBlue(), 1.0f, null, true, false, true));
    lines.addRow(new PathDescriptor(Inputs.MIDDLE_PATH, get("LBL_MIDDLE_LINE"), defaults.getGrey(), 1.0f, null, true, false, true));
    lines.addRow(new PathDescriptor(Inputs.BOTTOM_PATH, get("LBL_BOTTOM_LINE"), defaults.getRed(), 1.0f, null, true, false, true));

    tab = sd.addTab(get("TAB_ADVANCED"));
    
    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_CROSS_TOP_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_CROSS_BOTTOM_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.TOP_IND, get("LBL_TOP_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.MIDDLE_IND, get("LBL_MIDDLE_IND"), defaults.getGrey(), X11Colors.BLACK, false, false, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.BOTTOM_IND, get("LBL_BOTTOM_IND"), defaults.getRed(), X11Colors.WHITE, false, false, true));

    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(HIGH_PERIOD, get("LBL_HIGH_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(HIGH_MA_PERIOD, get("LBL_HIGH_MA_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(HIGH_SHIFT, get("LBL_SHIFT"), 0, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(new SliderDescriptor(LOW_PERIOD, get("LBL_LOW_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(LOW_MA_PERIOD, get("LBL_LOW_MA_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(LOW_SHIFT, get("LBL_SHIFT"), 0, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(Inputs.TOP_PATH, Inputs.MIDDLE_PATH, Inputs.BOTTOM_PATH);
    
    var desc = createRD();
    desc.setLabelSettings(Inputs.METHOD, HIGH_PERIOD, HIGH_MA_PERIOD, HIGH_SHIFT, LOW_PERIOD, LOW_MA_PERIOD, LOW_SHIFT);
    
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_MA_HL_TOP"), new String[] {Inputs.METHOD, HIGH_PERIOD, HIGH_MA_PERIOD, HIGH_SHIFT}));
    desc.exportValue(new ValueDescriptor(Values.MIDDLE, get("LBL_MA_HL_MIDDLE"), new String[] {Inputs.METHOD}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_MA_HL_BOTTOM"), new String[] {Inputs.METHOD, LOW_PERIOD, LOW_MA_PERIOD, LOW_SHIFT}));
    
    desc.declarePath(Values.TOP, Inputs.TOP_PATH);
    desc.declarePath(Values.MIDDLE, Inputs.MIDDLE_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.BOTTOM_PATH);
    
    desc.declareIndicator(Values.TOP, Inputs.TOP_IND);
    desc.declareIndicator(Values.MIDDLE, Inputs.MIDDLE_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.BOTTOM_IND);
    desc.declareSignal(Signals.CROSS_ABOVE_TOP_BAND, get("LBL_CROSS_TOP_BAND"));
    desc.declareSignal(Signals.CROSS_BELOW_BOTTOM_BAND, get("LBL_CROSS_BOTTOM_BAND"));
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
  }
  
  @Override
  protected synchronized void calculate(int index, DataContext ctx)
  {
    int highPeriod = getSettings().getInteger(HIGH_PERIOD);
    int highMAPeriod = getSettings().getInteger(HIGH_MA_PERIOD);
    int highShift = getSettings().getInteger(HIGH_SHIFT);
    DataSeries series = ctx.getDataSeries();
    boolean complete = false;

    if (index >= highPeriod) {
      double highest = series.highest(index,  highPeriod, Enums.BarInput.HIGH);
      series.setDouble(index, Values.HIGHEST, highest);
      if (index >= highPeriod + highMAPeriod) {
        Util.calcMAAt(index, ctx, getSettings().getMAMethod(Inputs.METHOD), Values.HIGHEST, highMAPeriod, highShift, Values.TOP);
        complete = true;
      }
    }

    int lowPeriod = getSettings().getInteger(LOW_PERIOD);
    int lowMAPeriod = getSettings().getInteger(LOW_MA_PERIOD);
    int lowShift = getSettings().getInteger(LOW_SHIFT);

    if (index >= lowPeriod) {
      series.setDouble(index, Values.LOWEST, series.lowest(index, lowPeriod, Enums.BarInput.LOW));
      if (index >= lowPeriod + lowMAPeriod) {
        Util.calcMAAt(index, ctx, getSettings().getMAMethod(Inputs.METHOD), Values.LOWEST, lowMAPeriod, lowShift, Values.BOTTOM);
      }
      else complete = false;
    }
    else complete = false;

    // Calculate the midpoint
    Double top = series.getDouble(index, Values.TOP);
    Double bottom = series.getDouble(index, Values.BOTTOM);
    
    if (top != null && bottom != null) {
      series.setDouble(index, Values.MIDDLE, (top + bottom)/2.0);
    }
    else complete = false;

    checkTopBand(ctx, index);
    checkBottomBand(ctx, index);
    
    series.setComplete(index, complete);
  }
  
  private void checkTopBand(DataContext ctx, int i)  
  {
    DataSeries series = ctx.getDataSeries();
    Double top = series.getDouble(i, Values.TOP);
    if (top == null) return;
    Coordinate c = new Coordinate(series.getStartTime(i), top);
    if (crossedAbove(series, i, Enums.BarInput.CLOSE, Values.TOP) && !series.getBoolean(i, Signals.CROSS_ABOVE_TOP_BAND, false)) {
      boolean wasCross = i == series.size()-1 && series.getBoolean(i, Signals.CROSS_ABOVE_TOP_BAND, false);
      series.setBoolean(i, Signals.CROSS_ABOVE_TOP_BAND, true);
      MarkerInfo marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("CROSS_ABOVE_TOP_BAND", format(series.getClose(i)), format(top)); ;
      if (!wasCross && marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(i, Signals.CROSS_ABOVE_TOP_BAND, msg, round(series.getClose(i)));
    }
  }

  private void checkBottomBand(DataContext ctx, int i)  
  {
    DataSeries series = ctx.getDataSeries();
    Double bottom = series.getDouble(i, Values.BOTTOM);
    if (bottom == null) return;
    Coordinate c = new Coordinate(series.getStartTime(i), bottom);
    if (crossedBelow(series, i, Enums.BarInput.CLOSE, Values.BOTTOM) && !series.getBoolean(i, Signals.CROSS_BELOW_BOTTOM_BAND, false)) {
      boolean wasCross = i == series.size()-1 && series.getBoolean(i, Signals.CROSS_BELOW_BOTTOM_BAND, false);
      series.setBoolean(i, Signals.CROSS_BELOW_BOTTOM_BAND, true);
      MarkerInfo marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("CROSS_BELOW_BOTTOM_BAND", format(series.getClose(i)), format(bottom));
      if (!wasCross && marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(i, Signals.CROSS_BELOW_BOTTOM_BAND, msg, round(series.getClose(i)));
    }
  }
}
