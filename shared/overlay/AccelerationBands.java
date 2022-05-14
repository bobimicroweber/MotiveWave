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
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Price Headley Acceleration Bands */
@StudyHeader(
    namespace="com.motivewave", 
    id="ACCEL_BANDS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ACCEL_BANDS", 
    menu="MENU_OVERLAY",
    desc="DESC_ACCEL_BANDS",
    label="LBL_ACCEL_BANDS",
    overlay=true,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/acceleration_bands.htm")
public class AccelerationBands extends Study 
{
  final static String ACCEL_FACTOR = "accelFactor";
  
  enum Values { TOP, MIDDLE, BOTTOM, UB, LB }
  enum Signals { CROSS_ABOVE_TOP_BAND, CROSS_BELOW_BOTTOM_BAND }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(ACCEL_FACTOR, get("LBL_ACCEL_FACTOR"), 0.001, 0.0001, 10000, 0.0001));

    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_BAND_LINES"), X11Colors.CADET_BLUE, 1.0f, null, true, true, false));
    colors.addRow(new PathDescriptor(Inputs.MIDDLE_PATH, get("LBL_MIDDLE_LINE"), X11Colors.DARK_SLATE_GRAY, 1.0f, new float[] {3f, 3f}, true, true, true));
    
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
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(ACCEL_FACTOR, Inputs.PATH, Inputs.MIDDLE_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, ACCEL_FACTOR);
    
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_ACCEL_TOP"), new String[] {Inputs.INPUT, Inputs.PERIOD, ACCEL_FACTOR}));
    desc.exportValue(new ValueDescriptor(Values.MIDDLE, get("LBL_ACCEL_MID"), new String[] {Inputs.INPUT, Inputs.PERIOD, ACCEL_FACTOR}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_ACCEL_BOTTOM"), new String[] {Inputs.INPUT, Inputs.PERIOD, ACCEL_FACTOR}));
    
    desc.declarePath(Values.TOP, Inputs.PATH);
    desc.declarePath(Values.MIDDLE, Inputs.MIDDLE_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.PATH);
    
    desc.declareIndicator(Values.TOP, Inputs.IND);
    desc.declareIndicator(Values.MIDDLE, Inputs.MIDDLE_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.IND);

    // Signals
    desc.declareSignal(Signals.CROSS_ABOVE_TOP_BAND, get("LBL_CROSS_TOP_BAND"));
    desc.declareSignal(Signals.CROSS_BELOW_BOTTOM_BAND, get("LBL_CROSS_BOTTOM_BAND"));

    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
  }
  
  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    double factor = getSettings().getDouble(ACCEL_FACTOR);
    Object input = getSettings().getInput(Inputs.INPUT);
    var series = ctx.getDataSeries();

    double H = series.getHigh(index);
    double L = series.getLow(index);
    
    double ub = (H*(1+2*((((H-L)/((H+L)/2))*1000)*factor)));
    double lb = (L*(1-2*((((H-L)/((H+L)/2))*1000)*factor)));
    series.setDouble(index, Values.UB, ub);
    series.setDouble(index, Values.LB, lb);

    if (index < period) return;

    Double top = series.sma(index, period, Values.UB);
    Double bottom = series.sma(index, period, Values.LB);
    Double middle = series.sma(index, period, input);
    
    series.setDouble(index, Values.TOP, top);
    series.setDouble(index, Values.MIDDLE, middle);
    series.setDouble(index, Values.BOTTOM, bottom);
    series.setComplete(index); 
    
    checkTopBand(ctx, index);
    checkBottomBand(ctx, index);
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
