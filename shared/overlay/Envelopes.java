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
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Envelopes */
@StudyHeader(
    namespace="com.motivewave", 
    id="ENVELOPES", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ENVELOPES", 
    desc="DESC_ENVELOPES",
    label="LBL_ENVELOPES",
    menu="MENU_OVERLAY",
    overlay=true,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/envelopes.htm")
public class Envelopes extends Study 
{
  final static String DEVIATION = "deviation";
  
  enum Values { TOP, BOTTOM }
  enum Signals { CROSS_ABOVE_TOP_BAND, CROSS_BELOW_BOTTOM_BAND }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1), new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new DoubleDescriptor(DEVIATION, get("LBL_DEVIATION"), 1, 0.01, 999, 0.01));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.TOP_PATH, get("LBL_TOP_LINE"), defaults.getBlue(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(Inputs.BOTTOM_PATH, get("LBL_BOTTOM_LINE"), defaults.getBlue(), 1.0f, null, true, true, true));
    lines.addRow(new ShadeDescriptor(Inputs.FILL, get("LBL_FILL_COLOR"), Inputs.TOP_PATH, Inputs.BOTTOM_PATH, Enums.ShadeType.BOTH, defaults.getFillColor(), false, true));
    
    tab = sd.addTab(get("TAB_ADVANCED"));
    
    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_CROSS_TOP_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_CROSS_BOTTOM_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.TOP_IND, get("LBL_TOP_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.BOTTOM_IND, get("LBL_BOTTOM_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(DEVIATION, Inputs.TOP_PATH, Inputs.BOTTOM_PATH, Inputs.FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.SHIFT, DEVIATION);
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_ENV_TOP"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.SHIFT, DEVIATION}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_ENV_BOTTOM"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.SHIFT, DEVIATION}));
    desc.declarePath(Values.TOP, Inputs.TOP_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.BOTTOM_PATH);
    desc.declareIndicator(Values.TOP, Inputs.TOP_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.BOTTOM_IND);
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
    
    // Signals
    desc.declareSignal(Signals.CROSS_ABOVE_TOP_BAND, get("LBL_CROSS_TOP_BAND"));
    desc.declareSignal(Signals.CROSS_BELOW_BOTTOM_BAND, get("LBL_CROSS_BOTTOM_BAND"));
  }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    int shift = getSettings().getInteger(Inputs.SHIFT);
    var series = ctx.getDataSeries();
    int latest = series.size()-1;

    int end = latest;
    if (shift < 0) end -= shift; 
    
    double deviation = getSettings().getDouble(DEVIATION)/100.0;
    
    // Calculate top and middle lines
    boolean updates = getSettings().isBarUpdates();
    for(int i = period; i <= end; i++) {
      if (series.isComplete(i+shift)) continue;
      if (!updates && !series.isBarComplete(i)) continue;
      Double ma = series.ma(getSettings().getMAMethod(Inputs.METHOD), i, period, getSettings().getInput(Inputs.INPUT));
      if (ma == null) continue;
      series.setDouble(i+shift, Values.TOP, ma*(1+deviation));
      series.setDouble(i+shift, Values.BOTTOM, ma*(1-deviation));
      series.setComplete(i+shift, i >= 0 && i < latest);
      checkTopBand(ctx, i);
      checkBottomBand(ctx, i);
    }    
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
