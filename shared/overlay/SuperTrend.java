package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
  namespace="com.motivewave",
  id="ID_SUPERTREND",
  rb="com.motivewave.platform.study.nls.strings",
  label="LBL_SUPERTREND",
  menu="MENU_GENERAL",
  name="LBL_SUPERTREND",
  desc="DESC_SUPERTREND",
  signals=true,
  overlay=true)
public class SuperTrend extends Study
{
  final static String MULTIPLIER="multiplier";

  protected enum Values { TSL, UP, DOWN, TREND }
  protected enum Signals { BUY, SELL };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));

    var inputs=tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new DoubleDescriptor(MULTIPLIER, get("LBL_MULTIPLIER"), 3, 0.01, 999, 0.01));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_ATR_PERIOD"), 7, 1, 999, 1));

    var settings=tab.addGroup(get("LBL_DISPLAY"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, true);
    path.setSupportsColor(false);
    path.setSupportsShadeType(false);
    settings.addRow(path);
    settings.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreenLine(),  true, false));
    settings.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRedLine(),  true, false));
    settings.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.ARROW, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    settings.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.ARROW, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(MULTIPLIER);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_ATR_PERIOD"), 7, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.UP_COLOR, Inputs.DOWN_COLOR);

    var desc=createRD();
    desc.setLabelSettings(MULTIPLIER, Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.TSL, get("LBL_SUPERTREND"), new String[] {MULTIPLIER, Inputs.PERIOD}));
    desc.declarePath(Values.TSL, Inputs.PATH);
    desc.declareIndicator(Values.TSL, Inputs.IND);
    desc.declareSignal(Signals.BUY, get("LBL_BUY_SIGNAL"));
    desc.declareSignal(Signals.SELL, get("LBL_SELL_SIGNAL"));
    desc.setRangeKeys(Values.TSL);
  }

  @Override
  public int getMinBars()
  {
    return getSettings().getInteger(Inputs.PERIOD);
  }
  
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    double mult = getSettings().getDouble(MULTIPLIER);
    int atrPeriod = getSettings().getInteger(Inputs.PERIOD);
    if (index < atrPeriod) return;
    var series=ctx.getDataSeries();
    Double atr = series.atr(index, atrPeriod);
    if (atr == null) return;
    
    float mid = (series.getHigh(index) + series.getLow(index))/2;
    double up = mid - (mult*atr);
    double down = mid + (mult*atr);

    Double pUp = series.getDouble(index-1, Values.UP);
    Double pDown = series.getDouble(index-1, Values.DOWN);
    Double pTrend = series.getDouble(index-1, Values.TREND);
    float close = series.getClose(index);
    float pClose = series.getClose(index-1);

    if (pUp != null && pClose > pUp) up = Util.max(up, pUp);
    if (pDown != null && pClose < pDown) down = Util.min(down, pDown);
    
    double trend = 1;
    if (pDown != null && close > pDown) trend = 1;
    else if (pUp != null && close < pUp) trend = -1;
    else if (pTrend != null) trend = pTrend;
    
    series.setDouble(index, Values.TREND, trend);
    series.setDouble(index, Values.UP, up);
    series.setDouble(index, Values.DOWN, down);
    boolean trendChange = pTrend != null && trend != pTrend;
    //boolean completeBar = index < series.size()-1;
    boolean completeBar = series.isBarComplete(index);
    
    if (trend > 0) {
      series.setDouble(index, Values.TSL, up);
      series.setPathColor(index, Values.TSL,  getSettings().getColor(Inputs.UP_COLOR, ctx.getDefaults().getGreenLine()));
      if (trendChange) {
        // Hack: when the direction changes make the path from the previous point to the new point "green"
        series.setPathColor(index-1, Values.TSL,  getSettings().getColor(Inputs.UP_COLOR, ctx.getDefaults().getGreenLine()));
        if (completeBar) {
          var marker = getSettings().getMarker(Inputs.UP_MARKER);
          String msg = get("SIGNAL_SUPERTREND_BUY", close);
          if (marker.isEnabled()) addFigure(new Marker(new Coordinate(series.getStartTime(index), series.getLow(index)), Enums.Position.BOTTOM, marker, msg));
          ctx.signal(index, Signals.BUY, msg, close);
        }
      }
    }
    else {
      series.setDouble(index, Values.TSL, down);
      series.setPathColor(index, Values.TSL,  getSettings().getColor(Inputs.DOWN_COLOR, ctx.getDefaults().getRedLine()));
      if (trendChange) {
        // Hack: when the direction changes make the path from the previous point to the new point "red"
        series.setPathColor(index-1, Values.TSL,  getSettings().getColor(Inputs.DOWN_COLOR, ctx.getDefaults().getRedLine()));
        if (completeBar) {
          var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
          String msg = get("SIGNAL_SUPERTREND_SELL", close);
          if (marker.isEnabled()) addFigure(new Marker(new Coordinate(series.getStartTime(index), series.getHigh(index)), Enums.Position.TOP, marker, msg));
          ctx.signal(index, Signals.SELL, msg, close);
        }
      }
    }
    
    series.setComplete(index, completeBar);
  }
}