package com.motivewave.platform.study.general;

import java.awt.Color;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Plot;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Rahul Mohinder Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="RMO", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_RMO",
    label="LBL_RMO",
    desc="DESC_RMO",
    menu="MENU_GENERAL",
    overlay=false)
public class RMO extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { RMO, MA1, MA2, MA3, MA4, MA5, MA6, MA7, MA8, MA9, MA10, S1, S2, S3 }
  enum Signals { BUY, SELL }
  final static String PAINT_BARS = "paintBars";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var lines = tab.addGroup(get("LBL_SETTINGS"));
    lines.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));
    lines.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));
    lines.addRow(new ColorDescriptor(Inputs.NEUTRAL_COLOR, get("LBL_NEUTRAL_COLOR"), defaults.getBlue()));
    lines.addRow(new BooleanDescriptor(PAINT_BARS, get("LBL_PAINT_PRICE_BARS"), false, true));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_RMO_ST2"), defaults.getGreen(), 1, null, false, false, true));
    lines.addRow(new PathDescriptor(Inputs.PATH2, get("LBL_RMO_ST3"), defaults.getRed(), 1, null, false, false, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.ARROW, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.ARROW, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.UP_COLOR, Inputs.DOWN_COLOR, Inputs.NEUTRAL_COLOR, PAINT_BARS, Inputs.PATH, Inputs.PATH2);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.RMO, get("LBL_RMO"), new String[] {}));
    desc.declareBars(Values.RMO);
    desc.declareIndicator(Values.RMO, Inputs.IND);
    desc.setRangeKeys(Values.RMO);
    desc.declarePath(Values.S2, Inputs.PATH);
    desc.declarePath(Values.S3, Inputs.PATH2);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
    desc.setPricePlotRequired(true);
    desc.declareSignal(Signals.BUY, get("LBL_BUY"));
    desc.declareSignal(Signals.SELL, get("LBL_SELL"));
    
    setMinBars(1000);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index <= 2) return;
    var series = ctx.getDataSeries();
    Double ma1 = series.sma(index, 2, Enums.BarInput.CLOSE);
    series.setDouble(index, Values.MA1, ma1);
    Double ma2 = series.sma(index, 2, Values.MA1);
    if (ma2 == null) return; // not enough data
    series.setDouble(index, Values.MA2, ma2);
    Double ma3 = series.sma(index, 2, Values.MA2);
    if (ma3 == null) return;
    series.setDouble(index, Values.MA3, ma3);
    Double ma4 = series.sma(index, 2, Values.MA3);
    if (ma4 == null) return;
    series.setDouble(index, Values.MA4, ma4);
    Double ma5 = series.sma(index, 2, Values.MA4);
    if (ma5 == null) return;
    series.setDouble(index, Values.MA5, ma5);
    Double ma6 = series.sma(index, 2, Values.MA5);
    if (ma6 == null) return;
    series.setDouble(index, Values.MA6, ma6);
    Double ma7 = series.sma(index, 2, Values.MA6);
    if (ma7 == null) return;
    series.setDouble(index, Values.MA7, ma7);
    Double ma8 = series.sma(index, 2, Values.MA7);
    if (ma8 == null) return;
    series.setDouble(index, Values.MA8, ma8);
    Double ma9 = series.sma(index, 2, Values.MA8);
    if (ma9 == null) return;
    series.setDouble(index, Values.MA9, ma9);
    Double ma10 = series.sma(index, 2, Values.MA9);
    if (ma10 == null) return;
    series.setDouble(index, Values.MA10, ma10);

    double diff = series.highest(index, 10, Enums.BarInput.CLOSE)-series.lowest(index, 10, Enums.BarInput.CLOSE);
    if (diff == 0) return;    
    double s1 = 100 * (series.getClose(index) - (ma1+ma2+ma3+ma4+ma5+ma6+ma7+ma8+ma9+ma10)/10)/diff;
    series.setDouble(index, Values.S1, s1);
    Double s2 = series.ema(index, 30, Values.S1);
    if (s2 == null) return;
    series.setDouble(index, Values.S2, s2);
    Double s3 = series.ema(index, 30, Values.S2);
    if (s3 == null) return;
    series.setDouble(index, Values.S3, s3);
    Double rmo = series.ema(index, 81, Values.S1);
    if (rmo == null) return;
    series.setDouble(index, Values.RMO, rmo);
    
    boolean bull = rmo > 0;
    boolean impulseUp = s2 > 0;
    boolean impulseDown = s2 < 0;
        
    Color upColor = getSettings().getColor(Inputs.UP_COLOR);
    Color downColor = getSettings().getColor(Inputs.DOWN_COLOR);
    Color neutralColor = getSettings().getColor(Inputs.NEUTRAL_COLOR);
    
    var bc = impulseUp ? upColor : (impulseDown ? downColor : (bull ? upColor : neutralColor));

    series.setBarColor(index, Values.RMO, bc);
    if (getSettings().getBoolean(PAINT_BARS, false)) series.setPriceBarColor(index, bc);
    
    if (!series.isBarComplete(index)) return;

    // Check to see if a cross occurred and raise signal.
    if (crossedAbove(series, index, Values.S2, Values.S3)) {
      var c = new Coordinate(series.getStartTime(index), series.getLow(index));
      series.setBoolean(index, Signals.BUY, true);
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("SIGNAL_BUY", format(s2), format(s3), format(series.getClose(index)));
      if (marker.isEnabled()) addFigure(Plot.PRICE, new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.BUY, msg, round(s2));
    }
    else if (crossedBelow(series, index, Values.S2, Values.S3)) {
      var c = new Coordinate(series.getStartTime(index), series.getHigh(index));
      series.setBoolean(index, Signals.SELL, true);
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("SIGNAL_SELL", format(s2), format(s3), format(series.getClose(index)));
      if (marker.isEnabled()) addFigure(Plot.PRICE, new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.SELL, msg, round(s2));
    }
    
    series.setComplete(index);
  }
}