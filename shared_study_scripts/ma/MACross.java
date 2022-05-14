package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Moving Average Cross.  This study consists of two moving averages: 
    Fast MA (shorter period), Slow MA.
    Signals are generated when the Fast MA moves above or below 
    the Slow MA. Markers are also displayed where these crosses occur. */
@StudyHeader(
    namespace="com.motivewave", 
    id="MA_CROSS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MA_CROSS", 
    label="LBL_MA_CROSS",
    desc="DESC_MA_CROSS",
    menu="MENU_MOVING_AVERAGE",
    overlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/moving_average_cross.htm")
public class MACross extends Study 
{
  enum Values { FAST_MA, SLOW_MA }
  enum Signals { CROSS_ABOVE, CROSS_BELOW }

  @Override
  public void initialize(Defaults defaults)
  {
    // User Settings
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    populateGeneralTab(tab);

    tab = sd.addTab(get("TAB_DISPLAY"));

    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_FAST_MA"), defaults.getGreenLine(), 1.0f, null, true, false, true));
    lines.addRow(new PathDescriptor(Inputs.PATH2, get("LBL_SLOW_MA"), defaults.getBlueLine(), 1.0f, null, true, false, true));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, Inputs.PATH2, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), false, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, Inputs.PATH2, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), false, true));
    
    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));
    
    var inds = tab.addGroup(get("LBL_INDICATORS"));
    inds.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_FAST_MA"), defaults.getGreenLine(), X11Colors.WHITE, false, false, true));
    inds.addRow(new IndicatorDescriptor(Inputs.IND2, get("LBL_SLOW_MA"), defaults.getBlueLine(), X11Colors.WHITE, false, false, true));
    
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_FAST_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new InputDescriptor(Inputs.INPUT2, get("LBL_SLOW_INPUT"), Enums.BarInput.CLOSE));
    sd.addQuickSettings(Inputs.INPUT2, Inputs.METHOD2);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_SLOW_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));

    // Runtime Settings
    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.INPUT2, Inputs.METHOD2, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.FAST_MA, get("LBL_FAST_MA"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SLOW_MA, get("LBL_SLOW_MA"), new String[] {Inputs.INPUT2, Inputs.METHOD2, Inputs.PERIOD2}));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_ABOVE, Enums.ValueType.BOOLEAN, get("LBL_CROSS_ABOVE"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_BELOW, Enums.ValueType.BOOLEAN, get("LBL_CROSS_BELOW"), null));
    desc.declarePath(Values.FAST_MA, Inputs.PATH);
    desc.declarePath(Values.SLOW_MA, Inputs.PATH2);
    desc.declareIndicator(Values.FAST_MA, Inputs.IND);
    desc.declareIndicator(Values.SLOW_MA, Inputs.IND2);
    
    // Signals
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW"));
    
    desc.setRangeKeys(Values.FAST_MA, Values.SLOW_MA);
  }

  @Override
  public void onLoad(Defaults defaults)
  {
    int p1 = getSettings().getInteger(Inputs.PERIOD);
    int p2 = getSettings().getInteger(Inputs.PERIOD2);
    setMinBars(Math.max(p1, p2)*2);
  }

  protected void populateGeneralTab(SettingTab tab)
  {
    // Fast MA (shorter period)
    var ma1 = tab.addGroup(get("LBL_FAST_MA"));
    ma1.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_FAST_INPUT"), Enums.BarInput.CLOSE));
    ma1.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_FAST_METHOD"), Enums.MAMethod.EMA));
    ma1.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_FAST_PERIOD"), 10, 1, 9999, 1));
    
    // Slow MA (shorter period)
    var ma2 = tab.addGroup(get("LBL_SLOW_MA"));
    ma2.addRow(new InputDescriptor(Inputs.INPUT2, get("LBL_SLOW_INPUT"), Enums.BarInput.CLOSE));
    ma2.addRow(new MAMethodDescriptor(Inputs.METHOD2, get("LBL_SLOW_METHOD"), Enums.MAMethod.EMA));
    ma2.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_SLOW_PERIOD"), 20, 1, 9999, 1));
  }
  
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int fastPeriod = getSettings().getInteger(Inputs.PERIOD);
    int slowPeriod = getSettings().getInteger(Inputs.PERIOD2);
    if (index < Math.max(fastPeriod, slowPeriod)) return; // not enough data
    var series = ctx.getDataSeries();
 
    // Calculate and store the fast and slow MAs
    Double fastMA = series.ma(getSettings().getMAMethod(Inputs.METHOD), index, 
        fastPeriod, getSettings().getInput(Inputs.INPUT));
    Double slowMA = series.ma(getSettings().getMAMethod(Inputs.METHOD2), index, 
        slowPeriod, getSettings().getInput(Inputs.INPUT2));
    if (fastMA == null || slowMA == null) return;
    
    series.setDouble(index, Values.FAST_MA, fastMA);
    series.setDouble(index, Values.SLOW_MA, slowMA);

    if (!series.isBarComplete(index)) return;
    
    // Check to see if a cross occurred and raise signal.
    var c = new Coordinate(series.getStartTime(index), slowMA);
    if (crossedAbove(series, index, Values.FAST_MA, Values.SLOW_MA)) {
      series.setBoolean(index, Signals.CROSS_ABOVE, true);
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("SIGNAL_CROSS_ABOVE", format(fastMA), format(slowMA), format(series.getClose(index)));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, round(fastMA));
    }
    else if (crossedBelow(series, index, Values.FAST_MA, Values.SLOW_MA)) {
      series.setBoolean(index, Signals.CROSS_BELOW, true);
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("SIGNAL_CROSS_BELOW", format(fastMA), format(slowMA), format(series.getClose(index)));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, round(fastMA));
    }
    
    series.setComplete(index);
  }
}
