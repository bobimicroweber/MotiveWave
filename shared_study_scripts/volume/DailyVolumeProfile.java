package com.motivewave.platform.study.volume;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.ColorInfo;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Implements the Volume Profile study.*/
@StudyHeader(
    namespace="com.motivewave", 
    id="VOLUME_DAILY_PROFILE", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_DAILY_VOLUME_PROFILE", 
    desc="DESC_DAILY_VOLUME_PROFILE",
    //menu="MENU_OVERLAY",
    //menu2="MENU_VOLUME",
    overlay=true, 
    requiresVolume=true,
    supportsBarUpdates=false,
    helpLink="http://www.motivewave.com/studies/daily_volume_profile.htm")
public class DailyVolumeProfile extends Study 
{
  final static String TIMEFRAME = "timeframe";
  final static String WIDTH = "width";
  final static String ALIGN = "align";
  final static String SHOW_RANGE = "showRange";
  final static String PERCENT_RANGE = "perRange";
  final static String RANGE_FILL = "rangeFill";
  final static String RANGE_LINE = "rangeLine";
  final static String MIXED_COLORS = "mixedColors";
  final static String BAR_COLOR = "barColor";
  final static String HISTORICAL = "historical";
  final static String HIGH_VOLUME_COLOR = "highVolColor";
  
  // Alignment
  final static String RIGHT = "RIGHT";
  final static String LEFT = "LEFT";

  // Alignment
  final static String DAILY = "DAILY";
  final static String WEEKLY = "WEEKLY";
  final static String BIWEEKLY = "BIWEEKLY";
  final static String MONTHLY = "MONTHLY";
  final static String MAX_PROFILES = "MAX_PROFILES";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    List<NVP> timeFrames = new ArrayList();
    timeFrames.add(new NVP(get("LBL_DAILY"), DAILY));
    timeFrames.add(new NVP(get("LBL_WEEKLY"), WEEKLY));
    timeFrames.add(new NVP(get("LBL_BIWEEKLY"), BIWEEKLY));
    timeFrames.add(new NVP(get("LBL_MONTHLY"), MONTHLY));
    
    var ddesc = new DiscreteDescriptor(TIMEFRAME, get("LBL_TIMEFRAME"), DAILY, timeFrames);
    ddesc.setGridWidth(2);
    inputs.addRow(ddesc);
    inputs.addRow(new IntegerDescriptor(Inputs.BARS, get("LBL_BARS"), 20, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(WIDTH, get("LBL_WIDTH"), 100, 25, 9999, 1));
    inputs.addRow(new IntegerDescriptor(MAX_PROFILES, get("LBL_MAX_PROFILES"), 5, 1, 999, 1));
    List<NVP> types = new ArrayList();
    types.add(new NVP(get("LBL_LEFT"), LEFT));
    types.add(new NVP(get("LBL_RIGHT"), RIGHT));
    inputs.addRow(new DiscreteDescriptor(ALIGN, get("LBL_ALIGN"), RIGHT, types));
    inputs.addRow(new DoubleDescriptor(PERCENT_RANGE, get("LBL_PERCENT_RANGE"), 70, 1, 100, 0.1),
        new BooleanDescriptor(SHOW_RANGE, get("LBL_SHOW_RANGE"), false, false) );
    var bdesc = new BooleanDescriptor(MIXED_COLORS, get("LBL_MIXED_COLORS"), false, false);
    bdesc.setGridWidth(4);
    inputs.addRow(bdesc);
    bdesc = new BooleanDescriptor(HISTORICAL, get("LBL_HISTORICAL_BARS"), false, false);
    bdesc.setGridWidth(4);
    inputs.addRow(bdesc);
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new ColorDescriptor(BAR_COLOR, get("LBL_SOLID_COLOR"), Util.getAlphaFill(defaults.getBlue(), 160)));
    colors.addRow(new PathDescriptor(RANGE_LINE, get("LBL_RANGE_LINE"), defaults.getLineColor(), 1.0f, new float[] {3f, 3f}, true, false, true));
    colors.addRow(new ColorDescriptor(RANGE_FILL, get("LBL_RANGE_FILL"), Util.getAlphaFill(defaults.getGrey()), true, true));
    colors.addRow(new ColorDescriptor(HIGH_VOLUME_COLOR, get("LBL_HIGH_VOLUME_COLOR"), Util.getAlphaFill(defaults.getOrange(), 160), true, true));

    sd.addDependency(new EnabledDependency(SHOW_RANGE, PERCENT_RANGE, RANGE_LINE, RANGE_FILL));
    sd.addDependency(new EnabledDependency(false, MIXED_COLORS, BAR_COLOR));

    sd.addQuickSettings(TIMEFRAME, Inputs.BARS, WIDTH, MAX_PROFILES, ALIGN, PERCENT_RANGE, MIXED_COLORS, HISTORICAL, BAR_COLOR, RANGE_LINE, RANGE_FILL, HIGH_VOLUME_COLOR);
    
    var desc = createRD();
    desc.setIDSettings(TIMEFRAME, Inputs.BARS);
    
    // initialize the session colors
    int alpha = 160;
    sessionColors.add(Util.getAlphaFill(defaults.getRed(), alpha));
    sessionColors.add(Util.getAlphaFill(defaults.getYellow(), alpha));
    sessionColors.add(Util.getAlphaFill(defaults.getBlue(), alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.BROWN, alpha));
    sessionColors.add(Util.getAlphaFill(defaults.getGreen(), alpha));
    sessionColors.add(Util.getAlphaFill(defaults.getLineColor(), alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.TEAL, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.PINK, alpha));
    sessionColors.add(Util.getAlphaFill(defaults.getPurple(), alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.GOLD, alpha));
    sessionColors.add(Util.getAlphaFill(defaults.getGrey(), alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.ORANGE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.LIME, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.AQUAMARINE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.LIGHT_CORAL, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.SEA_GREEN, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.GREEN_YELLOW, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.MAGENTA, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.LIGHT_SALMON, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.ROYAL_BLUE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.HOT_PINK, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.LAVENDER, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.PALE_GOLDENROD, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.TOMATO, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.SLATE_BLUE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.PALE_VIOLET_RED, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.WHEAT, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.SIENNA, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.ORCHID, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.PEACH_PUFF, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.SILVER, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.PERU, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.FIRE_BRICK, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.GOLDENROD, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.STEEL_BLUE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.DARK_ORANGE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.TAN, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.TURQUOISE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.SALMON, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.YELLOW_GREEN, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.MAGENTA, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.THISTLE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.MOCCASIN, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.CORAL, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.OLIVE_DRAB, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.AMETHYST, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.SKY_BLUE, alpha));
    sessionColors.add(Util.getAlphaFill(X11Colors.INDIAN_RED, alpha));
  }

  /** Gets the minimum start time (milliseconds since 1970) required to display this study (null if no minimum is required)
  @return minimum start time required to display this study (null if no minimum is required). */
  @Override
  public Long getMinStartTime(DataContext ctx) 
  {
    long periodStart = computePeriodStart(ctx, ctx.getCurrentTime());
    return periodStart - Util.MILLIS_IN_DAY;
  }

  @Override
  public void onDataSeriesMoved(DataContext ctx)
  {
    // When historical is not turned on, show the volume profile for the latest day on the visible data
    // We need recalculate every time the data series is moved in this case
    boolean historical = getSettings().getBoolean(HISTORICAL, false);
    if (!historical) calculateValues(ctx);
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    DataSeries series = ctx.getDataSeries();    
    boolean historical = getSettings().getBoolean(HISTORICAL, false);
    
    clearFigures();
    
    BarSize bs = series.getBarSize();
    if (series.size() == 0 || bs.getIntervalMinutes() >= 1440) return;
    
    // For historical, compute the volume profile for each day
    // Otherwise, compute for the latest visible day
    int seriesEnd = series.getEndIndex();
    if (historical) seriesEnd = series.size()-1;
    int seriesStart = series.getStartIndex();
    if (historical || seriesStart < 0) seriesStart = 0;
    
    // Find the start of the day
    // Allow a few bars to build up here
    long time = series.getStartTime(seriesEnd);
    if (seriesEnd == series.size()-1) {
      time = series.getStartTime(seriesEnd-4);
    }
    
    long periodStart = computePeriodStart(ctx, time);
    long periodEnd = computePeriodEnd(ctx, time);
    
    int startIndex = seriesEnd;
    for(int i = seriesEnd; i >= 0; i--) {
      if (series.getStartTime(i) < periodStart) break;
      startIndex = i;
    }

    // Determine the range
    int endIndex = seriesStart;
    for(int i = seriesStart; i < series.size(); i++) {
      if (series.getStartTime(i) >= periodEnd) break;
      endIndex = i;
    }

    createVolumeBars(ctx, periodStart, periodEnd, startIndex, endIndex);
    int count=1;
    
    if (!historical) return;
    int max = getSettings().getInteger(MAX_PROFILES, 5);
    
    while(startIndex > 5) {
      if (count >= max) break;
      time = series.getStartTime(startIndex-1);
      
      periodStart = computePeriodStart(ctx, time);
      periodEnd = computePeriodEnd(ctx, time);
      
      int end = startIndex-1;
      startIndex--;
      for(int i = end; i >= 0; i--) {
        if (series.getStartTime(i) < periodStart) break;
        startIndex = i;
      }

      // Determine the range
      for(int i = 0; i < end; i++) {
        if (series.getStartTime(i) >= periodEnd) break;
        endIndex = i;
      }
      createVolumeBars(ctx, periodStart, periodEnd, startIndex, endIndex);
      count++;
    }
  }

  private long computePeriodStart(DataContext ctx, long time)
  {
    DataSeries series = ctx.getDataSeries();
    String tf = getSettings().getString(TIMEFRAME);
    if (Util.compare(tf, WEEKLY)) {
      return series.getInstrument().getStartOfWeek(time,  ctx.isRTH());
    }
    if (Util.compare(tf, BIWEEKLY)) {
      Calendar cal = Calendar.getInstance(series.getInstrument().getTimeZone());
      cal.setTimeInMillis(time);
      int week = cal.get(Calendar.WEEK_OF_YEAR);
      long start = series.getInstrument().getStartOfWeek(time,  ctx.isRTH());
      if (week % 2 == 0) {
        start = series.getInstrument().getStartOfWeek(start - 2*Util.MILLIS_IN_DAY,  ctx.isRTH());
      }
      return start;
    }
    if (Util.compare(tf, MONTHLY)) {
      long start = series.getInstrument().getStartOfMonth(time,  ctx.isRTH());
      //System.out.println("Start Of Month: " + Util.formatMMDDYYYY(start));
      return start;
    }
    return series.getInstrument().getStartOfDay(time,  ctx.isRTH());
  }
  
  private long computePeriodEnd(DataContext ctx, long time)
  {
    DataSeries series = ctx.getDataSeries();    
    String tf = getSettings().getString(TIMEFRAME);
    if (Util.compare(tf, WEEKLY)) {
      return series.getInstrument().getEndOfWeek(time,  ctx.isRTH());
    }
    if (Util.compare(tf, BIWEEKLY)) {
      Calendar cal = Calendar.getInstance(series.getInstrument().getTimeZone());
      cal.setTimeInMillis(time);
      int week = cal.get(Calendar.WEEK_OF_YEAR);
      long end = series.getInstrument().getEndOfWeek(time,  ctx.isRTH());
      if (week % 2 != 0) {
        end = series.getInstrument().getEndOfWeek(end + 2*Util.MILLIS_IN_DAY,  ctx.isRTH());
      }
      return end;
    }
    if (Util.compare(tf, MONTHLY)) {
      return series.getInstrument().getEndOfMonth(time,  ctx.isRTH());
    }
    return series.getInstrument().getEndOfDay(time,  ctx.isRTH());
  }
  
  private void createVolumeBars(DataContext ctx, long dayStart, long dayEnd, int startIndex, int endIndex)
  {
    int bars = getSettings().getInteger(Inputs.BARS);
    DataSeries series = ctx.getDataSeries();    
    
    List<VolumeBar> volumeBars = new ArrayList();
    
    BarSize bs = series.getBarSize();
    if (series.size() == 0 || bs.getIntervalMinutes() >= 1440) return;
    
    VolumeLegend legend = new VolumeLegend(dayStart, dayEnd);
    addFigure(legend);
    
    Double top = series.highest(endIndex, endIndex-startIndex-1, Enums.BarInput.HIGH);
    Double bottom = series.lowest(endIndex, endIndex-startIndex-1, Enums.BarInput.LOW);
    if (top == null || bottom == null) return;
    
    double step = (top - bottom)/bars;
    if (step <= 0) return;
    
    // Create the bars
    double t = top;
    for(int i = 0; i < bars; i++) {
      VolumeBar vb = new VolumeBar(t, t-step, dayStart, dayEnd);
      addFigure(vb);
      volumeBars.add(vb);
      t -= step;
    }
    
    for(int i = startIndex; i <= endIndex; i++) {
      long volume = series.getVolume(i);
      if (volume <= 0) continue;
      
      double high = series.getHigh(i);
      double low = series.getLow(i);
      double range = high - low;
      if (range <= 0) continue;
      
      t = top;
      // where does this index start?
      int start = (int)Math.floor((top - high)/step);
      int end = (int)Math.floor((top - low)/step);
      if (start < 0) start = 0;
      if (end >= volumeBars.size()) end = volumeBars.size()-1;
      
      // Determine the color group for this bar
      Color color = getColor((int)(series.getStartTime(i) - dayStart), series.getBarSize());

      for(int j = start; j <= end; j++) {
        VolumeBar vb = volumeBars.get(j);
        double overlap = step;
        if (vb.topRange > high) overlap -= vb.topRange - high;
        if (vb.bottomRange < low) overlap -= low - vb.bottomRange;
        if (overlap <= 0) continue;
        
        // Need to determine which portion of this volume applies to this range
        int vol = (int)(volume * (overlap/range));
        if (vol <= 0) continue;
        VolumeColor vc = vb.volumeColors.get(color);
        if (vc == null) {
          vc = new VolumeColor(color, 0);
          vb.volumeColors.put(color,  vc);
        }
        vc.volume += vol;
        vb.volume += vol;
      }
    }
    
    // Determine what the maximum volume is (for scaling the bars)
    int maxVolume = 0;
    int totalVolume = 0;
    for(VolumeBar vb : volumeBars) {
      if (vb.volume <= 0) continue;
      if (vb.volume > maxVolume) maxVolume = vb.volume;
      totalVolume += vb.volume;
    }
    
    // now assign the max volume
    for(VolumeBar vb : volumeBars) {
      vb.setMaxVolume(maxVolume);
    }

    boolean showRange = getSettings().getBoolean(SHOW_RANGE);
    if (!showRange) return;
    
    double percentRange = getSettings().getDouble(PERCENT_RANGE)/100.0;
    
    // Find the range with the smallest number of bars that fits the percent range
    int start = 0;
    int end = volumeBars.size()-1;
    int count = volumeBars.size();
    int volume = totalVolume;
    
    for(int i = 0; i < volumeBars.size(); i++) {
      int e = i, c = 0, v = 0;
      double per = 0;
      for(int j = i; j < volumeBars.size(); j++) {
        VolumeBar vb = volumeBars.get(j);
        e = j; c++;
        v += vb.volume;
        per = ((double)v/(double)totalVolume);
        if ( per >= percentRange) break;
      }

      if (per < percentRange) break;
      if (c < count || (c==count && v > volume)) {
        start = i;
        end = e;
        count = c;
        volume = v;
      }
    }

    // Assign the bars that are in the range;
    for(int i = 0; i < volumeBars.size(); i++) {
      VolumeBar vb = volumeBars.get(i);
      vb.inRange = (i>= start && i <= end);
      vb.lineTop = (i == start);
      vb.lineBottom = (i == end);
    }
  }
  
  private Color getColor(int timeOffset, BarSize barSize)
  {
    int index = timeOffset/getSessionSize(barSize);
    if (index >= sessionColors.size()) {
      index = index % (sessionColors.size()-1);
    }
    if (index < 0) index = 0;
    
    return sessionColors.get(index);
  }

  private Color getColor(long time, DrawContext ctx)
  {
    DataSeries series = ctx.getDataContext().getDataSeries();
    long dayStart = computePeriodStart(ctx.getDataContext(), time);
    
    int index = (int)((time-dayStart)/getSessionSize(series.getBarSize()));
    if (index >= sessionColors.size()) {
      index = index % (sessionColors.size()-1);
    }
    if (index < 0) return null;
    return sessionColors.get(index);
  }

  private int getSessionSize(BarSize barSize)
  {
    int size = 30*60000; // 30 Min
    if (barSize.getIntervalMinutes() >= 10) size = 60*60000; // 1 hour
    if (barSize.getIntervalMinutes() >= 30) size = 240*60000; // 4 hour
    return size;
  }
  
  List<Color> sessionColors = new ArrayList();
  
  protected class VolumeLegend extends Figure
  {
    VolumeLegend(long dayStart, long dayEnd)
    {
      this.dayStart = dayStart;
      this.dayEnd = dayEnd;
    }
    
    @Override 
    public void layout(DrawContext ctx)
    {
      DataSeries series = ctx.getDataContext().getDataSeries();
      
      long sessionSize = getSessionSize(series.getBarSize());
      if (sessionSize < 30*60000) sessionSize = 30*60000;
      colors.clear();

      long time = dayStart;
      int y = ctx.getBounds().y + ctx.getBounds().height - 4;
      while (time < dayEnd) {
        // Performance enhancement.  Only layout the volume legend if it is visible
        if (time > series.getVisibleEndTime() || time + sessionSize < series.getVisibleStartTime()) {
          time+= sessionSize;
          continue;
        }
        Color c = getColor(time, ctx);
        if (c == null) {
          time += sessionSize;
          continue;
        }
         
        int x = ctx.translateTime(time);
        int x2 = ctx.translateTime(time + sessionSize);
        Rectangle area = new Rectangle(x, y, x2-x, 4);
        colors.add(new ColorLegend(getColor(time, ctx), area));
        time+= sessionSize;
      }
    }

    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      Color barColor = getSettings().getColor(BAR_COLOR);
      PathInfo path = getSettings().getPath(RANGE_LINE);
      boolean mixedColors = getSettings().getBoolean(MIXED_COLORS);
      if (!mixedColors) return; // No point in drawing this legend if it is a solid color
      
      if (path != null) {
        if (ctx.isSelected()) gc.setStroke(path.getSelectedStroke());
        else gc.setStroke(path.getStroke());
      }
      
      for(ColorLegend l : colors) {
        Rectangle area = l.area;
        if (area == null) continue;
        if (mixedColors) gc.setColor(l.color);
        else gc.setColor(barColor);
        gc.fill(area);
      }
    }
    
    private List<ColorLegend> colors = new ArrayList();
    private long dayStart;
    private long dayEnd;
  }
  
  protected class VolumeBar extends Figure
  {
    VolumeBar(double top, double bottom, long dayStart, long dayEnd) 
    {
      topRange = top;
      bottomRange = bottom;
      this.dayEnd = dayEnd;
      this.dayStart = dayStart;
    }

    @Override
    public boolean contains(double x, double y, DrawContext ctx)
    {
      if (barArea != null && barArea.contains(x, y)) return true;
      return false;
    }
    
    public void setMaxVolume(int vol) { maxVolume = vol; }

    @Override
    public String getPopupMessage(double x, double y, DrawContext ctx)
    {
      String volStr = "";
      int totalVol = 0;
      for(VolumeColor vc : volumeColors.values()) {
        volStr += "\n" + Util.formatMK(vc.volume);
        totalVol += vc.volume;
      }
      return get("LBL_DAILY_VOLUME_POPUP", Util.formatMK(totalVol) + volStr);
    }
    
    @Override 
    public void layout(DrawContext ctx)
    {
      int maxWidth = getSettings().getInteger(WIDTH);
      int ex = ctx.translateTime(dayEnd);
      int sx = ctx.translateTime(dayStart);
      int dayWidth = ex - sx;
      if (maxWidth > dayWidth) maxWidth = dayWidth;
      Rectangle gb = ctx.getBounds();
      if (gb == null) return;

      String align = getSettings().getString(ALIGN);
      boolean historical = getSettings().getBoolean(HISTORICAL);
      if (maxWidth > gb.width) maxWidth = gb.width;
      int ty = ctx.translateValue(topRange);
      int by = ctx.translateValue(bottomRange);
      int w = maxVolume == 0 ? 0 : (int)(((double)volume/(double)maxVolume)*maxWidth); // up width
      int gw = gb.width;
      int lx = gb.x;
      int rx = gb.x + gb.width;
      int h = by-ty-1;
      
      // Where do we place these bars?
      if (historical) {
        // Adjust to the end of day
        rx = ex;
        lx = sx;
        gw = rx - lx;
      }

      if (align != null && align.equals(RIGHT)) {
        barArea = gb.intersection(new Rectangle(rx-w, ty, w, h));
        rangeArea = gb.intersection(new Rectangle(lx, ty, gw-w, h+1));
        
        // layout the volume colors
        int x = rx;
        for(Color c : volumeColors.keySet()) {
          VolumeColor vc = volumeColors.get(c);
          int vw = (int)Math.round((((double)vc.volume/(double)maxVolume)*maxWidth));
          x -= vw;
          vc.area = gb.intersection(new Rectangle(x, ty, vw, h));
        }
      }
      else {
        barArea = gb.intersection(new Rectangle(lx, ty, w, h));
        rangeArea = gb.intersection(new Rectangle(lx+w, ty, gw-w, h+1));
        
        // layout the volume colors
        int x = lx;
        for(Color c : volumeColors.keySet()) {
          VolumeColor vc = volumeColors.get(c);
          int vw = (int)Math.round((((double)vc.volume/(double)maxVolume)*maxWidth));
          vc.area = gb.intersection(new Rectangle(x, ty, vw, h));
          x += vw;
        }
      }
    }

    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      ColorInfo rangeColor = getSettings().getColorInfo(RANGE_FILL);
      Color barColor = getSettings().getColor(BAR_COLOR);
      ColorInfo highVolColor = getSettings().getColorInfo(HIGH_VOLUME_COLOR);
      PathInfo path = getSettings().getPath(RANGE_LINE);
      boolean showRange = getSettings().getBoolean(SHOW_RANGE);
      boolean mixedColors = getSettings().getBoolean(MIXED_COLORS);
      if (!getSettings().getBoolean(SHOW_RANGE)) rangeColor = null;
      
      if (path != null) {
        if (ctx.isSelected()) gc.setStroke(path.getSelectedStroke());
        else gc.setStroke(path.getStroke());
      }
      
      if (highVolColor != null && volume == maxVolume && highVolColor.isEnabled()) {
        gc.setColor(highVolColor.getColor());
        if (barArea != null) gc.fill(barArea);
      }
      else if (mixedColors) {
        for(Color c : volumeColors.keySet()) {
          VolumeColor vc = volumeColors.get(c);
          Rectangle area = vc.area;
          if (area == null) continue;
          gc.setColor(vc.color);
          gc.fill(area);
        }
      }
      else if (barArea != null) {
        gc.setColor(barColor);
        gc.fill(barArea);
      }

      if (rangeArea == null) return;
      Rectangle gb = ctx.getBounds();
      if (gb == null) return;
      rangeArea = gb.intersection(rangeArea);
      if (rangeColor != null && inRange && rangeColor.isEnabled()) {
        gc.setColor(rangeColor.getColor());
        gc.fill(rangeArea);
      }

      if (!showRange || path == null || !path.isEnabled()) return;
      
      if (lineTop) {
        gc.setColor(path.getColor());
        gc.drawLine(rangeArea.x, rangeArea.y, (int)rangeArea.getMaxX(), rangeArea.y);
      }
      else if (lineBottom) {
        gc.setColor(path.getColor());
        int y = (int)rangeArea.getMaxY()-1;
        gc.drawLine(rangeArea.x, y, (int)rangeArea.getMaxX(), y);
      }
    }
    
    private double topRange;
    private double bottomRange;
    private Map<Color, VolumeColor> volumeColors = new LinkedHashMap<>();
    private int volume=0;
    private boolean inRange = false;
    private boolean lineTop = false;
    private boolean lineBottom = false;
    private Rectangle barArea;
    private Rectangle rangeArea;
    private int maxVolume;
    private long dayStart;
    private long dayEnd;
  }
  
  static class ColorLegend
  {
    ColorLegend(Color c, Rectangle a) 
    {
      color = c;
      area = a;
    }
    Color color;
    Rectangle area;
  }
  
  static class VolumeColor
  {
    VolumeColor(Color c, int v)
    {
      color = c; volume = v;
    }
    
    int volume;
    Color color;
    Rectangle area;
  }
}
