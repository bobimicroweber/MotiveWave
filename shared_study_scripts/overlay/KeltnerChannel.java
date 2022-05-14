package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Keltner Channel */
@StudyHeader(
    namespace="com.motivewave", 
    id="KELTNER_CHANNEL", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_KC", 
    desc="DESC_KC",
    label="LBL_KC",
    menu="MENU_OVERLAY",
    overlay=true,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/keltner_channel.htm")
public class KeltnerChannel extends Study 
{
  final static String MIDDLE_PERIOD = "middlePeriod", ATR_PERIOD = "atrPeriod", UPPER_RANGE = "upperRange", LOWER_RANGE = "lowerRange";
  
  enum Values { TOP, MIDDLE, BOTTOM }
  enum Signals { CROSS_ABOVE_TOP_BAND, CROSS_BELOW_BOTTOM_BAND }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(MIDDLE_PERIOD, get("LBL_MIDDLE_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(ATR_PERIOD, get("LBL_ATR_PERIOD"), 10, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(UPPER_RANGE, get("LBL_UPPER_RANGE"), 2.0, 0.1, 999, 0.1));
    inputs.addRow(new DoubleDescriptor(LOWER_RANGE, get("LBL_LOWER_RANGE"), 2.0, 0.1, 999, 0.1));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), X11Colors.CADET_BLUE, 1.0f, null, true, true, false));
    colors.addRow(new PathDescriptor(Inputs.MIDDLE_PATH, get("LBL_MIDDLE_LINE"), X11Colors.DARK_SLATE_GRAY, 1.0f, null, true, true, true));
    colors.addRow(new ShadeDescriptor(Inputs.FILL, get("LBL_FILL_COLOR"), Inputs.PATH, Inputs.PATH, Enums.ShadeType.BOTH, defaults.getFillColor(), false, true));
    
    tab = sd.addTab(get("TAB_ADVANCED"));
    
    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_CROSS_TOP_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_CROSS_BOTTOM_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_TOP_BOTTOM_IND"), X11Colors.CADET_BLUE, X11Colors.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.MIDDLE_IND, get("LBL_MIDDLE_IND"), X11Colors.DARK_SLATE_GRAY, X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(MIDDLE_PERIOD, get("LBL_MIDDLE_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(ATR_PERIOD, get("LBL_ATR_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(UPPER_RANGE, LOWER_RANGE, Inputs.PATH, Inputs.MIDDLE_PATH, Inputs.FILL);

    var desc = createRD();
    desc.setLabelSettings(MIDDLE_PERIOD, ATR_PERIOD, UPPER_RANGE, LOWER_RANGE);
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_KC_TOP"), new String[] {MIDDLE_PERIOD, ATR_PERIOD, UPPER_RANGE}));
    desc.exportValue(new ValueDescriptor(Values.MIDDLE, get("LBL_KC_MID"), new String[] {MIDDLE_PERIOD, ATR_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_KC_BOTTOM"), new String[] {MIDDLE_PERIOD, ATR_PERIOD, LOWER_RANGE}));
    desc.declarePath(Values.TOP, Inputs.PATH);
    desc.declarePath(Values.MIDDLE, Inputs.MIDDLE_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.PATH);
    
    desc.declareIndicator(Values.TOP, Inputs.IND);
    desc.declareIndicator(Values.MIDDLE, Inputs.MIDDLE_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.IND);
    
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
    desc.declareSignal(Signals.CROSS_ABOVE_TOP_BAND, get("LBL_CROSS_TOP_BAND"));
    desc.declareSignal(Signals.CROSS_BELOW_BOTTOM_BAND, get("LBL_CROSS_BOTTOM_BAND"));
  }
  
  @Override
  public int getMinBars()
  {
    int middlePeriod = getSettings().getInteger(MIDDLE_PERIOD);
    int atrPeriod = getSettings().getInteger(ATR_PERIOD);
    return Math.max(middlePeriod, atrPeriod)*2; // Using an EMA to calculate here so add a bit more data
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int middlePeriod = getSettings().getInteger(MIDDLE_PERIOD);
    int atrPeriod = getSettings().getInteger(ATR_PERIOD);
    if (index < Math.max(middlePeriod, atrPeriod)) return;
    
    double upperRange = getSettings().getDouble(UPPER_RANGE);
    double lowerRange = getSettings().getDouble(LOWER_RANGE);
    Object input = getSettings().getInput(Inputs.INPUT);
    var series = ctx.getDataSeries();
    
    Double value = series.ma(Enums.MAMethod.EMA, index, middlePeriod, input);
    Double atr = series.atr(index, atrPeriod);
    if (value == null || atr == null) return;
    series.setDouble(index, Values.MIDDLE, value);
    series.setDouble(index, Values.TOP, value + (upperRange *atr));
    series.setDouble(index, Values.BOTTOM, value - (lowerRange *atr));
    checkTopBand(ctx, index);
    checkBottomBand(ctx, index);
    series.setComplete(index);
  }
  
  private void checkTopBand(DataContext ctx, int i)  
  {
    var series = ctx.getDataSeries();
    Double top = series.getDouble(i, Values.TOP);
    if (top == null) return;
    var c = new Coordinate(series.getStartTime(i), top);
    if (crossedAbove(series, i, Enums.BarInput.CLOSE, Values.TOP) && !series.getBoolean(i, Signals.CROSS_ABOVE_TOP_BAND, false)) {
      series.setBoolean(i, Signals.CROSS_ABOVE_TOP_BAND, true);
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("CROSS_ABOVE_TOP_BAND", format(series.getClose(i)), format(top));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(i, Signals.CROSS_ABOVE_TOP_BAND, msg, round(series.getClose(i)));
    }
  }

  private void checkBottomBand(DataContext ctx, int i)  
  {
    var series = ctx.getDataSeries();
    Double bottom = series.getDouble(i, Values.BOTTOM);
    if (bottom == null) return;
    var c = new Coordinate(series.getStartTime(i), bottom);
    if (crossedBelow(series, i, Enums.BarInput.CLOSE, Values.BOTTOM) && !series.getBoolean(i, Signals.CROSS_BELOW_BOTTOM_BAND, false)) {
      series.setBoolean(i, Signals.CROSS_BELOW_BOTTOM_BAND, true);
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("CROSS_BELOW_BOTTOM_BAND", format(series.getClose(i)), format(bottom));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(i, Signals.CROSS_BELOW_BOTTOM_BAND, msg, round(series.getClose(i)));
    }
  }
}