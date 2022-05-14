package com.motivewave.platform.study.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.TimeFrameDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Highlights a time range in a background color */
@StudyHeader(
    namespace="com.motivewave", 
    id="TIME_FRAME", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_TIME_FRAME", 
    desc="DESC_TIME_FRAME",
    menu="MENU_OVERLAY",
    signals=true,
    overlay=true)
public class TimeFrameStudy extends Study 
{
  final static String TIMEFRAME = "timeframe", SHOW_RANGE = "showRange", EXTEND_RANGE = "extendRange";
  
  enum Signals { CROSS_ABOVE_HIGH, CROSS_BELOW_HIGH, CROSS_ABOVE_LOW, CROSS_BELOW_LOW }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new TimeFrameDescriptor(TIMEFRAME, get("LBL_TIMEFRAME"), 11*60*60*1000, 14*60*60*1000, true, false));
    inputs.addRow(new ColorDescriptor(Inputs.FILL, get("LBL_FILL"), Util.getAlphaFill(defaults.getGrey()), true, true));
    
    inputs = tab.addGroup(get("LBL_RANGE"));
    inputs.addRow(new BooleanDescriptor(SHOW_RANGE, get("LBL_SHOW_RANGE"), false, false));
    inputs.addRow(new BooleanDescriptor(EXTEND_RANGE, get("LBL_EXTEND_RANGE"), true, false));
    inputs.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false, true));
    inputs.addRow(new ColorDescriptor(Inputs.FILL2, get("LBL_RANGE_FILL"), Util.getAlphaFill(defaults.getYellow()), true, true));
    inputs.addRow(new FontDescriptor(Inputs.FONT, get("LBL_FONT"), defaults.getFont(), defaults.getTextColor(), true, true, true));
    inputs.addRow(new FontDescriptor(Inputs.FONT2, get("LBL_RANGE_FONT"), defaults.getFont(), defaults.getTextColor(), true, true, true));

    var markers= tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.MARKER, get("LBL_CROSS_ABOVE_HIGH"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    markers.addRow(new MarkerDescriptor(Inputs.MARKER2, get("LBL_CROSS_BELOW_HIGH"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    markers.addRow(new MarkerDescriptor(Inputs.MARKER4, get("LBL_CROSS_BELOW_LOW"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    markers.addRow(new MarkerDescriptor(Inputs.MARKER3, get("LBL_CROSS_ABOVE_LOW"), Enums.MarkerType.TRIANGLE,
        Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));

    sd.addDependency(new EnabledDependency(SHOW_RANGE, EXTEND_RANGE, Inputs.PATH, Inputs.PATH2, Inputs.FILL2, Inputs.FONT, Inputs.FONT2, Inputs.MARKER, Inputs.MARKER2, Inputs.MARKER3, Inputs.MARKER4));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(TIMEFRAME, Inputs.FILL, SHOW_RANGE, EXTEND_RANGE, Inputs.PATH, Inputs.FILL2);

    var desc = createRD();
    desc.setLabelSettings(TIMEFRAME);
    desc.declareSignal(Signals.CROSS_ABOVE_HIGH, get("LBL_CROSS_ABOVE_HIGH"));
    desc.declareSignal(Signals.CROSS_BELOW_HIGH, get("LBL_CROSS_BELOW_HIGH"));
    desc.declareSignal(Signals.CROSS_ABOVE_LOW, get("LBL_CROSS_ABOVE_LOW"));
    desc.declareSignal(Signals.CROSS_BELOW_LOW, get("LBL_CROSS_BELOW_LOW"));
  }
  
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
    clearFigures();
    var settings = getSettings();
    var series = ctx.getDataSeries();
    long MILLIS_IN_DAY = 24L*60L*60L*1000L;
    long start = series.getStartTime(0);
    long end = ctx.getCurrentTime() + 15*MILLIS_IN_DAY;
    var tf = settings.getTimeFrame(TIMEFRAME);
    boolean showRange = settings.getBoolean(SHOW_RANGE, false);
    Instrument instr = ctx.getInstrument();
    List<Zone> _zones = new ArrayList();
    extendRange = settings.getBoolean(EXTEND_RANGE, true);
    
    if (!series.getBarSize().isIntraday()) {
      zones = _zones;
      return;
    }
    
    // Add zones
    long day = Util.getMidnight(start, ctx.getTimeZone());
    long day2 = day;
    long now = ctx.getCurrentTime();
    Zone prevZone = null;
    while(day < end) {
      int s = tf.getStartTime();
      int e = tf.getEndTime();
      if (s > e) day2 = Util.getNextDayMidnight(day, ctx.getTimeZone());
      else day2 = day;
      
      Zone zone = new Zone(day + s, day2 + e);
      addFigure(zone);
      _zones.add(zone);
      long nextDay = Util.getNextDayMidnight(day, ctx.getTimeZone());

      // Show range lines?
      if (showRange && now > day+s) {
        int si = series.findIndex(day+s);
        int ei = series.findIndex(day2+e-100);
        
        // Make sure this is a valid range
        if (series.getStartTime(si) > day2+e || series.getStartTime(ei) < day+s) {
          day = nextDay;
          continue;
        }
        
        
        Double h = series.highest(ei, ei-si+1, Enums.BarInput.HIGH);
        Double l = series.lowest(ei, ei-si+1, Enums.BarInput.LOW);
        
        if (h != null && l != null) {
          double high = instr.round(h);
          double low = instr.round(l);
          zone.high = high;
          zone.low = low;
          zone.showRange = true;
          if (nextDay + s > now) zone.latest=true;
          zone.rangeEnd = nextDay+s;
        }
      }
      
      if (prevZone != null) prevZone.rangeEnd = zone.start;
      day = nextDay;
      prevZone = zone;
    }

    zones = _zones;
    
    // Clear the crossed flags
    // This is a bit of a hack, go back a couple of bars to clear any flags that may have been set previously
    for(int i=0; i <= series.size(); i++) {
      // Reset these flags since the channel has been recalculated
      series.setBoolean(i, Signals.CROSS_ABOVE_HIGH, false);
      series.setBoolean(i, Signals.CROSS_BELOW_HIGH, false);
      series.setBoolean(i, Signals.CROSS_ABOVE_LOW, false);
      series.setBoolean(i, Signals.CROSS_BELOW_LOW, false);
    }

    for(int i = 0; i < series.size(); i++) {
      checkSignal(ctx, i);
    }
  }
  
  protected Zone findZone(long time)
  {
    for(Zone zone : zones) {
      if (zone.start <= time && zone.rangeEnd > time) return zone;
    }
    return null;
  }
  
  void checkSignal(DataContext ctx, int i)
  {
    var series = ctx.getDataSeries();
    long startTime = series.getStartTime(i);
    Zone zone = findZone(series.getStartTime(i));
    if (zone == null) return;
    if (zone.start <= startTime && zone.end > startTime) return; // Do not calculate signals in the zone
    double top = zone.high;
    double bottom = zone.low;

    //if (!series.isBarComplete(i)) continue;
    float close = series.getClose(i);
    
    if (!series.getBoolean(i, Signals.CROSS_ABOVE_HIGH, false) && crossedAbove(series, i, top)) {
      series.setBoolean(i, Signals.CROSS_ABOVE_HIGH, true);
      var marker = getSettings().getMarker(Inputs.MARKER);
      String msg = get("SIGNAL_CROSS_ABOVE_HIGH", format(close), format(top));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), top), Enums.Position.BOTTOM, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_ABOVE_HIGH, msg, round(top));
    }
    else if (!series.getBoolean(i, Signals.CROSS_BELOW_HIGH, false) && crossedBelow(series, i, top)) {
      series.setBoolean(i, Signals.CROSS_BELOW_HIGH, true);
      var marker = getSettings().getMarker(Inputs.MARKER2);
      String msg = get("SIGNAL_CROSS_BELOW_HIGH", format(close), format(top));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), top), Enums.Position.TOP, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_BELOW_HIGH, msg, round(top));
    }

    if (!series.getBoolean(i, Signals.CROSS_ABOVE_LOW, false) && crossedAbove(series, i, bottom)) {
      series.setBoolean(i, Signals.CROSS_ABOVE_LOW, true);
      var marker = getSettings().getMarker(Inputs.MARKER3);
      String msg = get("SIGNAL_CROSS_ABOVE_LOW", format(close), format(bottom));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), bottom), Enums.Position.BOTTOM, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_ABOVE_LOW, msg, round(bottom));
    }
    else if (!series.getBoolean(i, Signals.CROSS_BELOW_LOW, false) && crossedBelow(series, i, bottom)) {
      series.setBoolean(i, Signals.CROSS_BELOW_LOW, true);
      var marker = getSettings().getMarker(Inputs.MARKER4);
      String msg = get("SIGNAL_CROSS_BELOW_LOW", format(close), format(bottom));
      if (marker.isEnabled()) {
        addFigure(new Marker(new Coordinate(series.getStartTime(i), bottom), Enums.Position.TOP, marker, msg));
      }
      ctx.signal(i, Signals.CROSS_BELOW_LOW, msg, round(bottom));
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
  
  private List<Zone> zones = new ArrayList();
  private boolean extendRange = true;
  
  // Draws the background fill for the time frame
  class Zone extends Figure
  {
    Zone(long start, long end)
    {
      this.start = start;
      this.end = end;
    }
    
    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      var b = ctx.getBounds();
      int x1 = ctx.translateTime(start);

      if (x1 >= (int)b.getMaxX()) return; // not visible
      
      int x2 = ctx.translateTime(end);
      if (x2 < 0 && !showRange) return;
      
      var settings = getSettings();
      var c = settings.getColorInfo(Inputs.FILL);
      var instr = ctx.getDataContext().getInstrument();
      //int bw = ctx.getBarWidth();

      if (c.isEnabled()) {
        var fill = c.getColor();
        // Make sure this is a transparent fill.
        //fill = Util.getAlphaFill(fill);
        gc.setColor(fill);
        gc.fillRect(x1, b.y,  x2 - x1, b.height);
        // Why are we subtracting half a bar width here?
        //gc.fillRect(x1 - bw/2, b.y,  x2 - x1, b.height);
      }
      
      // Display the range?
      if (!showRange) return;

      int x3 = ctx.translateTime(extendRange ? rangeEnd : end);
      if (x3 < 0 && !latest) return;

      var line = settings.getPath(Inputs.PATH);
      var labelFont = settings.getFont(Inputs.FONT);
      var rangeFont = settings.getFont(Inputs.FONT2);
      var rangeFill = settings.getColorInfo(Inputs.FILL2);
      
      if (latest && extendRange) x3 = (int)b.getMaxX();
      
      int hy = ctx.translateValue(high);
      int ly = ctx.translateValue(low);
      if (rangeFill.isEnabled()) {
        Color fill = rangeFill.getColor();
        // Make sure this is a transparent fill.
        //fill = Util.getAlphaFill(fill);
        gc.setColor(fill);
        gc.fillRect(x1, hy,  x3 - x1, ly - hy);
      }
      
      if (line.isEnabled()) {
        gc.setColor(line.getColor());
        gc.setStroke(line.getStroke());
        gc.drawLine(x1, hy, x3, hy);
        gc.drawLine(x1, ly, x3, ly);
      }

      x3 -= 4;
      
      if (labelFont != null && labelFont.isEnabled()) {
        String val1 = instr.format(high);
        String val2 = instr.format(low);
        gc.setFont(labelFont.getFont());
        var fm = gc.getFontMetrics();
        int w1 = fm.stringWidth(val1);
        int w2 = fm.stringWidth(val2);
        
        gc.setColor(ctx.getDefaults().getBackgroundColor());
        gc.fillRect(x3-w1-3, hy - fm.getAscent()-3, w1+5, fm.getAscent()+2);
        gc.fillRect(x3-w2-3, ly+2, w2+5, fm.getAscent()+2);

        gc.setColor(labelFont.getColor());
        gc.drawString(val1, x3 - w1, hy - 3);
        gc.drawString(val2, x3 - w2, ly + fm.getAscent()+2);
      }

      if (rangeFont != null && rangeFont.isEnabled()) {
        double range = high - low;
        String val = instr.format(range);
        if (instr.getType() == Enums.InstrumentType.FOREX) {
          range = range / instr.getPointSize();
          val = Util.round(range, 1) +  "";
        }

        gc.setFont(rangeFont.getFont());
        var fm = gc.getFontMetrics();
        int w = fm.stringWidth(val);

        gc.setColor(line.getColor());
        gc.setStroke(new BasicStroke(1f));
        int x4 = x3 - w/2;
        gc.drawLine(x4, hy, x4, ly);

        gc.fill(Util.createUpTriangle(x4, hy+4, 3));
        gc.fill(Util.createDownTriangle(x4, ly-4, 3));
        
        gc.setColor(ctx.getDefaults().getBackgroundColor());
        gc.fillRect(x3-w-3, (ly + hy)/2 - fm.getAscent()/2 - 1, w+5, fm.getAscent()+4);
        
        gc.setColor(rangeFont.getColor());
        gc.drawString(val, x3 - w, (ly + hy)/2 + fm.getAscent()/2);
        
      }
    }

    long start, end, rangeEnd;
    double high, low;
    boolean showRange=false;
    boolean latest=false;
  }
}
