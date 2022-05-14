package com.motivewave.platform.study.overlay;

import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Standard Deviation Channel */
@StudyHeader(
  namespace="com.motivewave",
  id="ID_STAND_DEV_CHNL",
  rb="com.motivewave.platform.study.nls.strings",
  name="NAME_REGRESSION_CHANNEL",
  label="LBL_REGRESSION_CHANNEL",
  desc="DESC_REGRESSION_CHANNEL",
  menu = "MENU_GENERAL",
  helpLink="http://www.motivewave.com/studies/standard_deviation_channel.htm",
  overlay=true,
  signals=true,
  studyOverlay=true)
public class RegressionChannel extends Study
{
  final static String STAND_DEV="standDev";
  final static String MARKER_CROSS_ABOVE_TOP="crossAboveTopMarker", MARKER_CROSS_BELOW_TOP="crossBelowTopMarker", MARKER_CROSS_ABOVE_MIDDLE="crossAboveMiddleMarker";
  final static String MARKER_CROSS_BELOW_MIDDLE="crossBelowMiddleMarker", MARKER_CROSS_ABOVE_BOTTOM="crossAboveBottomMarker", MARKER_CROSS_BELOW_BOTTOM="crossBelowBottomMarker";
  final static String EXTEND_RIGHT="extendRight", EXTEND_LEFT="extendLeft";
  
  enum Signals { CROSS_ABOVE_TOP, CROSS_BELOW_TOP, CROSS_ABOVE_MIDDLE, CROSS_BELOW_MIDDLE, CROSS_ABOVE_BOTTOM, CROSS_BELOW_BOTTOM }
  enum Values { TOP, MIDDLE, BOTTOM }
  
  final static String D1 = "1";
  final static String D15 = "1.5";
  final static String D2 = "2";
  final static String C50 = "0.674";
  final static String C80 = "1.282";
  final static String C90 = "1.645";
  final static String C95 = "1.96";
  final static String C99 = "2.58";
  final static String C995 = "2.81";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));

    var inputs=tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.BARS, get("LBL_BARS"), 40, 1, 999, 1));
    
    List<NVP> stdDevs = new ArrayList();
    stdDevs.add(new NVP(get("LBL_CONFIDENCE_50"), C50));
    stdDevs.add(new NVP(get("LBL_CONFIDENCE_80"), C80));
    stdDevs.add(new NVP(get("LBL_CONFIDENCE_90"), C90));
    stdDevs.add(new NVP(get("LBL_CONFIDENCE_95"), C95));
    stdDevs.add(new NVP(get("LBL_CONFIDENCE_99"), C99));
    stdDevs.add(new NVP(get("LBL_CONFIDENCE_995"), C995));
    stdDevs.add(new NVP(get("LBL_DEV_1"), D1));
    stdDevs.add(new NVP(get("LBL_DEV_15"), D15));
    stdDevs.add(new NVP(get("LBL_DEV_2"), D2));
    inputs.addRow(new DiscreteDescriptor(STAND_DEV, get("LBL_STANDARD_DEVIATION"), D1, stdDevs));

    inputs.addRow(new BooleanDescriptor(EXTEND_RIGHT, get("LBL_EXTEND_LINES_RIGHT"), true));
    inputs.addRow(new BooleanDescriptor(EXTEND_LEFT, get("LBL_EXTEND_LINES_LEFT"), false));

    tab=sd.addTab(get("TAB_DISPLAY"));

    var colors=tab.addGroup(get("LBL_LINES"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_TOP_LINE"), defaults.getLineColor(), 1.5f, null, true, false, true));
    colors.addRow(new PathDescriptor(Inputs.PATH2, get("LBL_MIDDLE_LINE"), defaults.getLineColor(), 0.75f, null, true, false, true));
    colors.addRow(new PathDescriptor(Inputs.PATH3, get("LBL_BOTTOM_LINE"), defaults.getLineColor(), 1.5f, null, true, false, true));

    var markers=tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(MARKER_CROSS_ABOVE_TOP, get("LBL_CROSS_ABOVE_TOP"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(MARKER_CROSS_BELOW_TOP, get("LBL_CROSS_BELOW_TOP"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(MARKER_CROSS_ABOVE_MIDDLE, get("LBL_CROSS_ABOVE_MIDDLE"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    markers.addRow(new MarkerDescriptor(MARKER_CROSS_BELOW_MIDDLE, get("LBL_CROSS_BELOW_MIDDLE"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    markers.addRow(new MarkerDescriptor(MARKER_CROSS_ABOVE_BOTTOM, get("LBL_CROSS_ABOVE_BOTTOM"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(MARKER_CROSS_BELOW_BOTTOM, get("LBL_CROSS_BELOW_BOTTOM"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(STAND_DEV, EXTEND_RIGHT, EXTEND_LEFT);
    sd.addQuickSettings(Inputs.PATH, Inputs.PATH2, Inputs.PATH3);

    var desc=createRD();
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_TOP"), new String[] { Inputs.INPUT, Inputs.BAR, STAND_DEV }));
    desc.exportValue(new ValueDescriptor(Values.MIDDLE, get("LBL_MIDDLE"), new String[] { Inputs.INPUT, Inputs.BAR, STAND_DEV}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_BOTTOM"), new String[] { Inputs.INPUT, Inputs.BAR, STAND_DEV}));
    
    desc.exportValue(new ValueDescriptor(Signals.CROSS_ABOVE_TOP, Enums.ValueType.BOOLEAN, get("LBL_CROSS_ABOVE_TOP"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_BELOW_TOP, Enums.ValueType.BOOLEAN, get("LBL_CROSS_BELOW_TOP"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_ABOVE_MIDDLE, Enums.ValueType.BOOLEAN, get("LBL_CROSS_ABOVE_MIDDLE"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_BELOW_MIDDLE, Enums.ValueType.BOOLEAN, get("LBL_CROSS_BELOW_MIDDLE"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_ABOVE_BOTTOM, Enums.ValueType.BOOLEAN, get("LBL_CROSS_ABOVE_BOTTOM"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_BELOW_BOTTOM, Enums.ValueType.BOOLEAN, get("LBL_CROSS_BELOW_BOTTOM"), null));

    desc.declareSignal(Signals.CROSS_ABOVE_TOP, get("LBL_CROSS_ABOVE_TOP"));
    desc.declareSignal(Signals.CROSS_BELOW_TOP, get("LBL_CROSS_BELOW_TOP"));
    desc.declareSignal(Signals.CROSS_ABOVE_MIDDLE, get("LBL_CROSS_ABOVE_MIDDLE"));
    desc.declareSignal(Signals.CROSS_BELOW_MIDDLE, get("LBL_CROSS_BELOW_MIDDLE"));
    desc.declareSignal(Signals.CROSS_ABOVE_BOTTOM, get("LBL_CROSS_ABOVE_BOTTOM"));
    desc.declareSignal(Signals.CROSS_BELOW_BOTTOM, get("LBL_CROSS_BELOW_BOTTOM"));
 
    desc.setMinTick(0.1);
    desc.setLabelSettings(Inputs.INPUT, Inputs.BARS, STAND_DEV);
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
  }

  @Override
  public int getMinBars() { return getSettings().getInteger(Inputs.BARS, 40); }
  
  @Override
  public void onBarUpdate(DataContext ctx) 
  {
    // Check to see if one of the lines has been crossed with this price update
    var series = ctx.getDataSeries();
    checkSignal(ctx, series.size()-1);
  }
  
  @Override
  protected void calculateValues(DataContext ctx) 
  {
    var series=ctx.getDataSeries();
    clearFigures();

    int endIndex=series.size()-1;
    int bars=getSettings().getInteger(Inputs.BARS);
    int startIndex=endIndex - bars;
    if (startIndex < 0) startIndex = 0;
    boolean extRight = getSettings().getBoolean(EXTEND_RIGHT);
    boolean extLeft = getSettings().getBoolean(EXTEND_LEFT);
    
    Object input=getSettings().getInput(Inputs.INPUT);
    String devStr = getSettings().getString(STAND_DEV, D1);
    double dev = Util.toDouble(devStr);

    // Do not include the latest bar if it is not complete in the regression calculation
    int ei = endIndex;
    if (!series.isBarComplete(endIndex)) ei--; // only include completed bars

    double[] value=linRegLine(series, ei, bars, input, 0);
    double a=value[0]; // y = a + m*x
    double m=value[1];

    // get the standard deviation
    Double standDev=series.std(ei, bars, input);
    if (standDev == null) return;
    standDev=standDev * dev; // factor in users choice

    // linear regression line y = a + (m * x)
    double startY=a; // + (m * 0)
    double endY=a + (m * bars);
    var middleInfo=getSettings().getPath(Inputs.PATH2);
    var start=new Coordinate(series.getStartTime(startIndex), startY);
    var end=new Coordinate(series.getStartTime(endIndex), endY);

    if (middleInfo.isEnabled()) {
      var line=new Line(start, end, middleInfo);
      line.setExtendLeftBounds(extLeft);
      line.setExtendRightBounds(extRight);
      addFigure(line);
    }
    // top channel
    var topInfo=getSettings().getPath(Inputs.PATH);
    if (topInfo.isEnabled()) {
      startY=a + standDev; // + (m * 0)
      endY=a + standDev + (m * bars);
      start=new Coordinate(series.getStartTime(startIndex), startY);
      end=new Coordinate(series.getStartTime(endIndex), endY);
      var line=new Line(start, end, topInfo);
      line.setExtendLeftBounds(extLeft);
      line.setExtendRightBounds(extRight);
      addFigure(line);
    }

    // bottom channel
    var bottomInfo=getSettings().getPath(Inputs.PATH3);
    if (bottomInfo.isEnabled()) {
      startY=a - standDev; // + (m * 0)
      endY=a - standDev + (m * bars);
      start=new Coordinate(series.getStartTime(startIndex), startY);
      end=new Coordinate(series.getStartTime(endIndex), endY);
      var line=new Line(start, end, bottomInfo);
      line.setExtendLeftBounds(extLeft);
      line.setExtendRightBounds(extRight);
      addFigure(line);
    }
    
    // Clear the crossed flags
    // This is a bit of a hack, go back a couple of bars to clear any flags that may have been set previously
    for(int i=0; i <= endIndex; i++) {
      // Reset these flags since the channel has been recalculated
      series.setBoolean(i, Signals.CROSS_ABOVE_TOP, false);
      series.setBoolean(i, Signals.CROSS_BELOW_TOP, false);
      series.setBoolean(i, Signals.CROSS_ABOVE_MIDDLE, false);
      series.setBoolean(i, Signals.CROSS_BELOW_MIDDLE, false);
      series.setBoolean(i, Signals.CROSS_ABOVE_BOTTOM, false);
      series.setBoolean(i, Signals.CROSS_BELOW_BOTTOM, false);
    }
    
    int bar = 0;
    for(int i = startIndex; i <= endIndex; i++) {
      // Calculate the values for this bar
      double mid = a + (m * bar);
      double top = mid + standDev;
      double bottom = mid - standDev;
      series.setDouble(i, Values.MIDDLE, mid);
      series.setDouble(i, Values.TOP, top);
      series.setDouble(i, Values.BOTTOM, bottom);
      checkSignal(ctx, i);
      bar++;
    }
  }
  
  void checkSignal(DataContext ctx, int i)
  {
    var series = ctx.getDataSeries();
    double top = series.getDouble(i, Values.TOP, 0);
    double mid = series.getDouble(i, Values.MIDDLE, 0);
    double bottom =  series.getDouble(i, Values.BOTTOM, 0);
    //if (!series.isBarComplete(i)) continue;
    float close = series.getClose(i);
    
    if (!series.getBoolean(i, Signals.CROSS_ABOVE_TOP, false) && crossedAbove(series, i, top)) {
      series.setBoolean(i, Signals.CROSS_ABOVE_TOP, true);
      var marker = getSettings().getMarker(MARKER_CROSS_ABOVE_TOP);
      String msg = get("SIGNAL_CROSS_ABOVE_TOP", format(close), format(top));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), top), Enums.Position.BOTTOM, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_ABOVE_TOP, msg, round(top));
    }
    else if (!series.getBoolean(i, Signals.CROSS_BELOW_TOP, false) && crossedBelow(series, i, top)) {
      series.setBoolean(i, Signals.CROSS_BELOW_TOP, true);
      var marker = getSettings().getMarker(MARKER_CROSS_BELOW_TOP);
      String msg = get("SIGNAL_CROSS_BELOW_TOP", format(close), format(top));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), top), Enums.Position.TOP, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_BELOW_TOP, msg, round(top));
    }

    if (!series.getBoolean(i, Signals.CROSS_ABOVE_MIDDLE, false) && crossedAbove(series, i, mid)) {
      series.setBoolean(i, Signals.CROSS_ABOVE_MIDDLE, true);
      var marker = getSettings().getMarker(MARKER_CROSS_ABOVE_MIDDLE);
      String msg = get("SIGNAL_CROSS_ABOVE_MIDDLE", format(close), format(mid));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), mid), Enums.Position.BOTTOM, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_ABOVE_MIDDLE, msg, round(mid));
    }
    else if (!series.getBoolean(i, Signals.CROSS_BELOW_MIDDLE, false) && crossedBelow(series, i, mid)) {
      series.setBoolean(i, Signals.CROSS_BELOW_MIDDLE, true);
      var marker = getSettings().getMarker(MARKER_CROSS_BELOW_MIDDLE);
      String msg = get("SIGNAL_CROSS_BELOW_MIDDLE", format(close), format(mid));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), mid), Enums.Position.TOP, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_BELOW_MIDDLE, msg, round(mid));
    }

    if (!series.getBoolean(i, Signals.CROSS_ABOVE_BOTTOM, false) && crossedAbove(series, i, bottom)) {
      series.setBoolean(i, Signals.CROSS_ABOVE_BOTTOM, true);
      var marker = getSettings().getMarker(MARKER_CROSS_ABOVE_BOTTOM);
      String msg = get("SIGNAL_CROSS_ABOVE_BOTTOM", format(close), format(bottom));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), bottom), Enums.Position.BOTTOM, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_ABOVE_BOTTOM, msg, round(bottom));
    }
    else if (!series.getBoolean(i, Signals.CROSS_BELOW_BOTTOM, false) && crossedBelow(series, i, bottom)) {
      series.setBoolean(i, Signals.CROSS_BELOW_BOTTOM, true);
      var marker = getSettings().getMarker(MARKER_CROSS_BELOW_BOTTOM);
      String msg = get("SIGNAL_CROSS_BELOW_BOTTOM", format(close), format(bottom));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), bottom), Enums.Position.TOP, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_BELOW_BOTTOM, msg, round(bottom));
    }
  }
  
  boolean crossedBelow(DataSeries series, int i, double val)
  {
    double prev = round(series.getClose(i-1));
    double current = round(series.getLow(i));
    return prev >= val && current < val;
  }

  boolean crossedAbove(DataSeries series, int i, double val)
  {
    double prev = round(series.getClose(i-1));
    double current = round(series.getHigh(i));
    return prev <= val && current > val;
  }

  // Linear regression line
  double[] linRegLine(DataSeries series, int index, int period, Object key, int barIndex)
  {
    long sumX=0;
    long sumX2=0;
    double y=0.0;
    double sumY=0.0;
    double sumXy=0.0;
    int x=0;
    for (int i=index - period + 1; i <= index; i++) {
      y=series.getDouble(i, key, 0);
      if (y == 0) continue;
      sumX=sumX + x;
      sumY=sumY + y;
      sumXy=sumXy + (x * y);
      sumX2=sumX2 + (x * x);
      x++;
    }
    // Calculate b and slope y = mx + b
    double m=((period * sumXy) - (sumX * sumY)) / ((period * sumX2) - (sumX * sumX));
    double b=((sumY) - (m * sumX)) / period;
    double line=b + (m * barIndex); // returns value of y at barIndex position.
    double[] value= { line, m };
    return value;
  }  
}