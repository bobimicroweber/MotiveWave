package com.motivewave.platform.study.overlay;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.motivewave.platform.sdk.common.Bar;
import com.motivewave.platform.sdk.common.BarOperation;
import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.FontInfo;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Tick;
import com.motivewave.platform.sdk.common.TickOperation;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Plots the open, high and previous close values for each day */
@StudyHeader(
    namespace="com.motivewave", 
    id="OHLC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_OHLC",
    desc="DESC_OHLC",
    menu="MENU_OVERLAY",
    overlay=true,
    studyOverlay=false,
    requiresBarUpdates=true)
public class OHLC extends com.motivewave.platform.sdk.study.Study 
{
  enum Values { DHIGH_VAL, DLOW_VAL, DMID_VAL }

  final static String FONT="font", ALIGN="align", OPEN="open", HIGH="high", DHIGH="dhigh", LOW="low", DLOW="dlow", CLOSE="close", 
      POPEN="popen", PLOW="plow", PHIGH="phigh", MID="mid", DMID="dmid", OHIGH="ohigh", OLOW="olow", 
      RHIGH="orHigh", RLOW="orLow", RANGE="range", RANGE_INT="rangeInt", SHOW_ALL="showAll", MAX_PRINTS="maxPrints", RTH="rth", 
      SHORTEN_LATEST="sl", TIMEFRAME="tf";
  
  final static String LEFT="L", RIGHT="R", MIDDLE="M";
  final static String VAL_RTH="R", VAL_EXT="E", VAL_CHART="C";
  final static String RANGE_MIN="MIN", RANGE_SECONDS="SEC"; 
  
  final static BarSize DAY = BarSize.getBarSize(BarSizeType.LINEAR, 1440);
  final static BarSize MIN = BarSize.getBarSize(BarSizeType.LINEAR, 1);
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    List<NVP> aligns = new ArrayList();
    aligns.add(new NVP(get("LBL_LEFT"), LEFT));
    aligns.add(new NVP(get("LBL_MIDDLE"), MIDDLE));
    aligns.add(new NVP(get("LBL_RIGHT"), RIGHT));
    List<NVP> rthOptions = new ArrayList();
    rthOptions.add(new NVP(get("LBL_RTH"), VAL_RTH));
    rthOptions.add(new NVP(get("LBL_EXT"), VAL_EXT));
    rthOptions.add(new NVP(get("LBL_CHART"), VAL_CHART));
    List<NVP> rangeIntervals = new ArrayList();
    rangeIntervals.add(new NVP(get("LBL_MINUTES"), RANGE_MIN));
    rangeIntervals.add(new NVP(get("LBL_SECONDS"), RANGE_SECONDS));
    List<NVP> timeIntervals = new ArrayList();
    timeIntervals.add(new NVP(get("LBL_DAILY"), "D"));
    timeIntervals.add(new NVP(get("LBL_WEEKLY"), "W1"));
    timeIntervals.add(new NVP(get("LBL_MONTHLY"), "M1"));
    timeIntervals.add(new NVP(get("LBL_YEARLY"), "Y1"));
    
    
    var grp = tab.addGroup("", false);
    grp.addRow(new DiscreteDescriptor(TIMEFRAME, get("LBL_TIMEFRAME"), "D", timeIntervals));
    grp.addRow(new IntegerDescriptor(MAX_PRINTS, get("LBL_MAX_PRINTS"), 5, 1, 999, 1), new BooleanDescriptor(SHOW_ALL, get("LBL_SHOW_ALL"), false, false),
        new BooleanDescriptor(SHORTEN_LATEST, get("LBL_SHORTEN_LATEST"), false, false));
    grp.addRow(new DiscreteDescriptor(RTH, get("LBL_RTH_DATA"), VAL_EXT, rthOptions));
    grp.addRow(new FontDescriptor(FONT, get("LBL_FONT"), defaults.getFont(), X11Colors.BLACK, false, true, true));
    grp.addRow(new DiscreteDescriptor(ALIGN, get("LBL_LABEL_ALIGN"), RIGHT, aligns));
    grp.addRow(new IntegerDescriptor(RANGE, get("LBL_OPEN_RANGE_MIN"), 5, 1, 1440, 1), new DiscreteDescriptor(RANGE_INT, null, RANGE_MIN, rangeIntervals));
    grp.addRow(new PathDescriptor(RHIGH, get("LBL_OPEN_RANGE_HIGH_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(RLOW, get("LBL_OPEN_RANGE_LOW_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    // Turn off the shading option
    for(var descriptors : grp.getRows()) {
      for(var desc : descriptors) {
        if (desc instanceof PathDescriptor) ((PathDescriptor)desc).setSupportsShadeType(false);
      }
    }
    
    tab = sd.addTab(get("TAB_LINES"));

    grp = tab.addGroup("", false);
    grp.addRow(new PathDescriptor(OPEN, get("LBL_OPEN_LINE"), defaults.getYellowLine(), 1f, null, true, false, true));
    grp.addRow(new PathDescriptor(HIGH, get("LBL_HIGH_LINE"), defaults.getGreenLine(), 1f, null, true, false, true));
    var path = new PathDescriptor(DHIGH, get("LBL_DHIGH_LINE"), defaults.getGreenLine(), 1f, null, false, false, true);
    path.setOverrideTagDisplay(true);
    path.setContinuous(false);
    grp.addRow(path);
    grp.addRow(new PathDescriptor(MID, get("LBL_MID_LINE"), defaults.getOrange(), 1f, null, false, false, true));
    path = new PathDescriptor(DMID, get("LBL_DMID_LINE"), defaults.getOrange(), 1f, null, false, false, true);
    path.setOverrideTagDisplay(true);
    path.setContinuous(false);
    grp.addRow(path);
    grp.addRow(new PathDescriptor(LOW, get("LBL_LOW_LINE"), defaults.getRedLine(), 1f, null, true, false, true));
    path = new PathDescriptor(DLOW, get("LBL_DLOW_LINE"), defaults.getRedLine(), 1f, null, false, false, true);
    path.setOverrideTagDisplay(true);
    path.setContinuous(false);
    grp.addRow(path);
    grp.addRow(new PathDescriptor(CLOSE, get("LBL_PREV_CLOSE_LINE"), defaults.getBlueLine(), 1f, null, true, false, true));
    grp.addRow(new PathDescriptor(POPEN, get("LBL_PREV_OPEN_LINE"), defaults.getYellowLine(), 1f, null, false, false, true));
    grp.addRow(new PathDescriptor(PHIGH, get("LBL_PREV_HIGH_LINE"), defaults.getGreenLine(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(PLOW, get("LBL_PREV_LOW_LINE"), defaults.getRedLine(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(OHIGH, get("LBL_OVERNIGHT_HIGH_LINE"), defaults.getGreenLine(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(OLOW, get("LBL_OVERNIGHT_LOW_LINE"), defaults.getRedLine(), 1f, new float[] {3, 3}, false, false, true));

    // Turn off the shading option
    for(var descriptors : grp.getRows()) {
      for(var desc : descriptors) {
        if (desc instanceof PathDescriptor) ((PathDescriptor)desc).setSupportsShadeType(false);
      }
    }

    sd.addDependency(new EnabledDependency(false, SHOW_ALL, MAX_PRINTS));
    sd.addDependency(new EnabledDependency(FONT, ALIGN));
    var d = new EnabledDependency(RHIGH, RANGE, RANGE_INT);
    d.setSource2(RLOW);
    d.setOrCompare(true);
    sd.addDependency(d);

    sd.addQuickSettings(TIMEFRAME, RTH, FONT, ALIGN, OPEN, HIGH, DHIGH, MID, DMID, LOW, DLOW, CLOSE, POPEN, PHIGH, PLOW, OHIGH, OLOW, RANGE, RANGE_INT, RHIGH, RLOW);
    sd.rowAlign(RANGE, RANGE_INT);
    
    var desc = createRD();
    desc.setLabelSettings(TIMEFRAME, RTH);
    desc.declarePath(Values.DHIGH_VAL, DHIGH);
    desc.declarePath(Values.DMID_VAL, DMID);
    desc.declarePath(Values.DLOW_VAL, DLOW);
  }

  @Override
  public void clearState()
  {
    super.clearState();
    lines.clear(); visibleLines.clear();
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    var bs = ctx.getDataSeries().getBarSize();
    timeframe = getSettings().getString(TIMEFRAME, "D");
    boolean clear = Util.compare(timeframe, "D") && bs.getIntervalMinutes() >= 1440;
    clear = clear ||Util.compare(timeframe, "W1") && bs.getIntervalMinutes() >= 7*1440;
    clear = clear ||Util.compare(timeframe, "M1") && bs.getIntervalMinutes() >= 30*1440;
    clear = clear ||Util.compare(timeframe, "Y1") && bs.getIntervalMinutes() >= 365*1440;
    
    if (clear || !series.hasData()) {
      lines.clear();
      visibleLines.clear();
      return;
    }
    if (!Util.isEmpty(lines) || calcInProgress) return; // already calculated.  Rely on onTick() to compute from here on out
    try {
      calcInProgress = true;
      var s = getSettings();
      var instr = series.getInstrument();
      int maxPrints = s.getInteger(MAX_PRINTS, 10);
      boolean showAll = s.getBoolean(SHOW_ALL, false);
      shortenLatest = s.getBoolean(SHORTEN_LATEST, false);
      font = s.getFont(FONT);
      openPath = s.getPath(OPEN);
      midPath = s.getPath(MID);
      dMidPath = s.getPath(DMID);
      highPath = s.getPath(HIGH);
      dHighPath = s.getPath(DHIGH);
      lowPath = s.getPath(LOW);
      dLowPath = s.getPath(DLOW);
      closePath = s.getPath(CLOSE);
      pOpenPath = s.getPath(POPEN);
      pHighPath = s.getPath(PHIGH);
      pLowPath = s.getPath(PLOW);
      oHighPath = s.getPath(OHIGH);
      oLowPath = s.getPath(OLOW);
      rHighPath = s.getPath(RHIGH);
      rLowPath = s.getPath(RLOW);
      align = s.getString(ALIGN, RIGHT);
      var r = s.getInteger(RANGE, 5);
      if (Util.compare(s.getString(RANGE_INT, RANGE_MIN), RANGE_MIN)) r *= Util.MILLIS_IN_MINUTE;
      else r *= 1000;
      range = r;
      String rthData = s.getString(RTH, VAL_EXT);
      if (Util.compare(rthData, VAL_RTH)) rth = true;
      else if (Util.compare(rthData, VAL_CHART) && ctx.isRTH()) rth = true;
      else rth = false;
  
      addFigure(figure);
  
      var ls = Util.isEmpty(lines) ? null : lines.get(lines.size()-1);
      if (ls != null && ls.eod <= ctx.getCurrentTime()) {
        long sod = getStartOfNextPeriod(ls.sod, instr, false);
        ls = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr));
        lines.add(ls);
      }
      else if (ls == null){
        long eod = getStartOfPeriod(series.getStartTime(series.size()-1), instr, false);
        long sod = getStartOfPeriod(series.getStartTime(0), instr, false);
        // Hack: we need the previous close, so we should have at least 2 days here
        if (eod - sod <= Util.MILLIS_IN_DAY) sod = Util.getStartOfPrevDay(sod, instr, false);
        if (!showAll) {
          // This is tricky. Need to account for weekends here.  Note: we need an extra day here to pick up the prev close etc
          long _s = getStartOfPeriod(series.getStartTime(), instr, false);
          for(int i=0; i < maxPrints; i++) {
            _s = getStartOfPrevPeriod(_s, instr, false);
          }
          if (_s > sod) sod = _s;
        }
        
        ls = new LineSet(null, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr));
        lines.add(ls);
      }
      
      try {
        boolean _rth = rth;
        if (showOLow() || showOHigh()) _rth = false;
        if (range % Util.MILLIS_IN_MINUTE == 0) {
          instr.forEachBar(ls.sod, ctx.getCurrentTime() + Util.MILLIS_IN_MINUTE, MIN, _rth, new BarCalculator(instr, ls, series));
        }
        else {
          instr.forEachTick(ls.sod, ctx.getCurrentTime() + Util.MILLIS_IN_MINUTE, _rth, new TickCalculator(instr, ls, series));
        }
      }
      catch(Exception exc) {
        exc.printStackTrace();
      }
      
      Collections.sort(lines); // This should already be in order
      if (!showAll && lines.size() > maxPrints) {
        while(lines.size() > maxPrints) lines.remove(0);
      }
    }
    finally {
      calcInProgress = false;
    }
  }
  
  @Override
  public void onTick(DataContext ctx, Tick tick)
  {
    if (calcInProgress || Util.isEmpty(lines)) return;
    var instr = ctx.getInstrument();
    var ls = lines.get(lines.size()-1);
    float p = tick.getPrice();
    long time = tick.getTime();
    if ((showOLow() || showOHigh()) && time < ls.sodRth) {
      if (p < ls.olow) ls.olow = p;
      if (p > ls.ohigh) ls.ohigh = p;
    }
    
    if (rth && !instr.isInsideTradingHours(time, rth)) return;
    
    // This code should really be refactored with the Tick Calculator below
    var series = ctx.getDataSeries();
    if (time >= ls.eod) {
      series.setPathBreak(series.size()-1, Values.DHIGH_VAL, true);
      series.setPathBreak(series.size()-1, Values.DLOW_VAL, true);
      series.setPathBreak(series.size()-1, Values.DMID_VAL, true);
      long sod = getStartOfNextPeriod(ls.sod, instr, false);
      var nextLs = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr));
      nextLs.low = nextLs.high = nextLs.open = nextLs.close = nextLs.olow = nextLs.ohigh = p;
      if (time >= nextLs.sodRth && time < nextLs.sodRth + range) {
        nextLs.rLow = nextLs.rHigh = p;
      }
      nextLs.mid = (nextLs.low + nextLs.high)/2;
      if (!lines.contains(nextLs)) lines.add(nextLs);
      ls = nextLs;
    }
    else {
      if (p < ls.low) ls.low = p;
      if (p > ls.high) ls.high = p;
      if (time < ls.sodRth) {
        if (p < ls.olow) ls.olow = p;
        if (p > ls.ohigh) ls.ohigh = p;
      }
      if (time >= ls.sodRth && time < ls.sodRth + range) {
        if (p < ls.rLow) ls.rLow = p;
        if (p > ls.rHigh) ls.rHigh = p;
      }
      ls.mid = (ls.low + ls.high)/2;
      ls.close = p;
      if (ls.open == Float.MIN_VALUE) ls.open = p;
    }
    series.setFloat(Values.DHIGH_VAL, ls.high);
    series.setFloat(Values.DMID_VAL, ls.mid);
    series.setFloat(Values.DLOW_VAL, ls.low);
  }

  private long getStartOfPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      long sow = instr.getStartOfWeek(time, rth);
      return sow <= time ? sow : instr.getStartOfWeek(time-3*Util.MILLIS_IN_DAY, rth);
    case "M1":
      long som = instr.getStartOfMonth(time, rth);
      return som <= time ? som : instr.getStartOfMonth(time-7*Util.MILLIS_IN_DAY, rth);
    case "Y1":
      var soy = instr.getStartOfYear(time, true);
      return soy >= time ? soy : instr.getStartOfYear(time+20*Util.MILLIS_IN_DAY, true);
    }
    long sod = instr.getStartOfDay(time, rth);
    return sod <= time ? sod : instr.getStartOfDay(time-Util.MILLIS_IN_DAY, rth);
  }

  private long getStartOfNextPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      return Util.getStartOfNextWeek(time, instr, rth);
    case "M1":
      return Util.getStartOfNextMonth(time, instr, rth);
    case "Y1":
      return Util.getStartOfNextYear(time, instr, rth);
    }
    return Util.getStartOfNextDay(time, instr, rth);
  }

  private long getStartOfPrevPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      return Util.getStartOfPrevWeek(time, instr, rth);
    case "M1":
      return Util.getStartOfPrevMonth(time, instr, rth);
    case "Y1":
      return Util.getStartOfPrevYear(time, instr, rth);
    }
    return Util.getStartOfPrevDay(time, instr, rth);
  }

  private long getEndOfPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      long eow = instr.getEndOfWeek(time, rth);
      return eow >= time ? eow : instr.getEndOfWeek(time+5*Util.MILLIS_IN_DAY, rth);
    case "M1":
      long eom = instr.getEndOfMonth(time, rth);
      return eom >= time ? eom : instr.getEndOfMonth(time+15*Util.MILLIS_IN_DAY, rth);
    case "Y1":
      long eoy = instr.getEndOfYear(time, true);
      return eoy >= time ? eoy : instr.getStartOfYear(time+20*Util.MILLIS_IN_DAY, true);
    }
    long eod = instr.getEndOfDay(time, rth);
    return eod >= time ? eod : instr.getEndOfDay(time+Util.MILLIS_IN_DAY, rth);
  }
  
  private long getStartOfPeriodRTH(long sopExt, Instrument instr) {
    switch(timeframe) {
    case "W1":
      var sowRth = instr.getStartOfWeek(sopExt, true);
      return sowRth >= sopExt ? sowRth : instr.getStartOfWeek(sopExt + 2*Util.MILLIS_IN_DAY, true);
    case "M1":
      var somRth = instr.getStartOfMonth(sopExt, true);
      return somRth >= sopExt ? somRth : instr.getStartOfMonth(sopExt + 7*Util.MILLIS_IN_DAY, true);
    case "Y1":
      long soyRth = instr.getStartOfYear(sopExt, true);
      return soyRth >= sopExt ? soyRth : instr.getStartOfYear(sopExt+20*Util.MILLIS_IN_DAY, true);
    }
    var sodRth = instr.getStartOfDay(sopExt, true);
    return sodRth >= sopExt ? sodRth : instr.getStartOfDay(sopExt+Util.MILLIS_IN_DAY, true);
  }

  private boolean showDHigh() { return dHighPath != null && dHighPath.isEnabled(); }
  private boolean showDLow() { return dLowPath != null && dLowPath.isEnabled(); }
  private boolean showDMid() { return dMidPath != null && dMidPath.isEnabled(); }
  private boolean showOLow() { return oLowPath != null && oLowPath.isEnabled(); }
  private boolean showOHigh() { return oHighPath != null && oHighPath.isEnabled(); }
  
  // Keep track of the latest lines
  private List<LineSet> lines = new ArrayList();
  private List<LineSet> visibleLines = new ArrayList();
  private PathInfo openPath, highPath, dHighPath, lowPath, dLowPath, closePath, midPath, dMidPath, pOpenPath, pLowPath, pHighPath, oHighPath, oLowPath, rHighPath, rLowPath;
  private String timeframe, align;
  private FontInfo font;
  private long range;
  private boolean calcInProgress=false, rth, shortenLatest=false;
  private Lines figure = new Lines();
  
  
  // This figure draws all of the lines  
  class Lines extends Figure
  {
    @Override
    public boolean contains(double x, double y, DrawContext ctx)
    {
      for(var ls : visibleLines) {
        if (ls.contains(x, y)) return true;
      }
      return false;
    }

    @Override
    public void layout(DrawContext ctx)
    {
      setBounds(ctx.getBounds());
      var series = ctx.getDataContext().getDataSeries();
      long start = series.getVisibleStartTime();
      long end = series.getVisibleEndTime();
      setBounds(ctx.getBounds());

      // Filter visible lines
      List<LineSet> _lines = new ArrayList<>();
      synchronized(lines) {
        for(var l : lines) {
          if (l.eod <= start || l.sod >= end) continue;
          _lines.add(l);
        }
      }
      
      for(var l : _lines) l.layout(ctx);
      visibleLines = _lines;
    }

    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      if (Util.isEmpty(visibleLines)) return;
      for(var l : visibleLines) l.draw(gc, ctx);
    }
  }
  
  class LineSet implements Comparable<LineSet>
  {
    LineSet(LineSet prev, long sod, long eod, long sodRth)
    {
      this.sod = sod; this.eod = eod; this.sodRth = sodRth;
      if (prev != null) {
        prevClose = prev.close;
        phigh = prev.high;
        plow = prev.low;
        popen = prev.open;
      }
    }

    boolean contains(double x, double y)
    {
      if (contains(x, y, openPath, oy)) return true;
      if (contains(x, y, highPath, hy)) return true;
      if (contains(x, y, lowPath, ly)) return true;
      if (contains(x, y, closePath, cy)) return true;
      if (contains(x, y, pOpenPath, poly)) return true;
      if (contains(x, y, pLowPath, ply)) return true;
      if (contains(x, y, pHighPath, phy)) return true;
      if (contains(x, y, oHighPath, ply)) return true;
      if (contains(x, y, oLowPath, phy)) return true;
      if (contains(x, y, midPath, my)) return true;
      if (contains(x, y, rLowPath, rly)) return true;
      if (contains(x, y, rHighPath, rhy)) return true;
      return false;
    }
    
    void layout(DrawContext ctx)
    {
      var series = ctx.getDataContext().getDataSeries();
      lx = ctx.translateTime(sod);
      rx = ctx.translateTime(eod);
      xrth = ctx.translateTime(sodRth);
      
      if (isLatest() && shortenLatest) {
        int _rx = ctx.translateTime(series.getEndTime());
        if (font != null && font.isEnabled()) {
          float max = Util.maxFloat(open, high, low, prevClose, popen, plow, phigh, olow, mid, rLow, rHigh);
          int width = Util.strWidth("XXX: " + series.getInstrument().format(max), font.getFont());
          _rx += width + 5;
        }
        if (_rx < rx) rx = _rx;
        if (_rx < xrth) xrth = _rx;
      }
      
      cx = ctx.translateTime(series.getStartTime());
      if (openPath.isEnabled()) oy = ctx.translateValue(open);
      if (highPath.isEnabled() || showDHigh()) hy = ctx.translateValue(high);
      if (lowPath.isEnabled() || showDLow()) ly = ctx.translateValue(low);
      if (closePath.isEnabled() && prevClose != 0) cy = ctx.translateValue(prevClose);
      if (pOpenPath.isEnabled() && popen != 0) poly = ctx.translateValue(popen);
      if (pLowPath.isEnabled() && plow != 0) ply = ctx.translateValue(plow);
      if (pHighPath.isEnabled() && phigh != 0) phy = ctx.translateValue(phigh);
      if (showOLow() && olow != 0) oly = ctx.translateValue(olow);
      if (showOHigh() && ohigh != 0) ohy = ctx.translateValue(ohigh);
      if (midPath.isEnabled() || showDMid()) my = ctx.translateValue(mid);
      if (rLowPath.isEnabled() && rLow != Float.MAX_VALUE) rly = ctx.translateValue(rLow);
      if (rHighPath.isEnabled() && rHigh != Float.MIN_VALUE) rhy = ctx.translateValue(rHigh);
    }
    
    void draw(Graphics2D gc, DrawContext ctx)
    {
      int x = rth ? xrth : lx;
      drawLine(gc, ctx, openPath, x, oy, open, "O:");
      drawLine(gc, ctx, highPath, x, hy, high, "H:");
      drawLine(gc, ctx, lowPath, x, ly, low, "L:");
      if (cy != -999) drawLine(gc, ctx, closePath, x, cy, prevClose, "C:");
      if (poly != -999) drawLine(gc, ctx, pOpenPath, x, poly, popen, "PO:");
      if (ply != -999) drawLine(gc, ctx, pLowPath, x, ply, plow, "PL:");
      if (phy != -999) drawLine(gc, ctx, pHighPath, x, phy, phigh, "PH:");
      if (oly != -999) drawLine(gc, ctx, oLowPath, x, oly, olow, "OL:");
      if (ohy != -999) drawLine(gc, ctx, oHighPath, x, ohy, ohigh, "OH:");
      drawLine(gc, ctx, midPath, x, my, (low + high)/2, "M:");
      drawLine(gc, ctx, rLowPath, xrth, rly, rLow, "ORL:");
      drawLine(gc, ctx, rHighPath, xrth, rhy, rHigh, "ORH:");
      
      if (isLatest()) {
        if (!highPath.isEnabled() && showDHigh()) drawLine(gc, ctx, dHighPath, cx, hy, high, "H:");
        if (!lowPath.isEnabled() && showDLow()) drawLine(gc, ctx, dLowPath, cx, ly, low, "L:");
        if (!midPath.isEnabled() && showDMid()) drawLine(gc, ctx, dMidPath, cx, my, mid, "M:");
      }
    }
    
    boolean isLatest() { return !lines.isEmpty() && lines.get(lines.size()-1) == this; } 
     
    boolean contains(double x, double y, PathInfo path, int py)
    {
      if (path == null || !path.isEnabled()) return false;
      if (x < lx || x > rx) return false;
      return Math.abs(y - py) <= 6;
    }
    
    void drawLine(Graphics2D gc, DrawContext ctx, PathInfo path, int x, int y, float value, String prefix)
    {
      if (!path.isEnabled() || value == Float.MAX_VALUE || value == Float.MIN_VALUE) return;
      gc.setStroke(ctx.isSelected() ? path.getSelectedStroke() : path.getStroke());
      gc.setColor(path.getColor());
      var gb = ctx.getBounds();
      if (x < gb.x) x = gb.x;
      int x2 = rx > gb.x + gb.width ? gb.x+gb.width : rx;

      if (font != null && font.isEnabled()) {
        var f = font.getFont();
        String valFmt = ctx.format(value);
        var color = path.getColor();
        
        String lbl = prefix + valFmt;
        if (path.isShowTag()) {
          if (path.getTagFont() != null) f = path.getTagFont();
          lbl = path.getTag();
          if (lbl == null) lbl = "";
          if (path.isShowTagValue()) lbl += " " + valFmt;
          if (path.getTagTextColor() != null) color = path.getTagTextColor();
        }
        
        gc.setFont(f);
        gc.setColor(color);
        var fm = gc.getFontMetrics();
        int w = fm.stringWidth(lbl);
        switch(align) {
        case RIGHT:
          //if (x2 - (w+10) < x) gc.drawLine(x, y, x2, y);
          //else {
            gc.drawLine(x, y, x2-w-5, y);
            gc.drawString(lbl, x2 - w, y+fm.getAscent()/2);
          //}
          break;
        case LEFT:
          if (x2 - x < w + 5) gc.drawLine(x, y, x2, y);
          else {
            gc.drawLine(x+w+5, y, x2, y);
            gc.drawString(lbl, x, y+fm.getAscent()/2);
          }
          break;
        case MIDDLE:
          int _cx = (x + x2)/2;
          if (_cx < x + 5) gc.drawLine(x, y, x2, y);
          else {
            gc.drawLine(x, y, _cx-w/2 - 2, y);
            gc.drawString(lbl, _cx-w/2, y+fm.getAscent()/2);
            gc.drawLine(_cx+w/2+2, y, x2, y);
          }
          break;
        }
      }
      else {
        gc.drawLine(x, y, x2, y);
      }
    }
    
    @Override
    public boolean equals(Object o)
    {
      if (o == null) return false;
      if (o == this) return true;
      return ((LineSet)o).sod == sod;
    }

    @Override
    public int compareTo(LineSet o) { return Long.compare(sod, o.sod); }

    long sod, eod, sodRth;
    float open=Float.MIN_VALUE, close=Float.MIN_VALUE, prevClose=Float.MIN_VALUE, high=Float.MIN_VALUE, low=Float.MAX_VALUE, 
        popen=Float.MAX_VALUE, plow=Float.MAX_VALUE, phigh=Float.MIN_VALUE, olow=Float.MAX_VALUE, ohigh=Float.MIN_VALUE, rHigh=Float.MIN_VALUE, rLow=Float.MAX_VALUE, mid = Float.MAX_VALUE;
    
    // Layout information
    int oy, hy, ly, cy=-999, poly=-999, ply=-999, phy=-999, oly=-999, ohy=-999, my, rhy, rly;
    int lx, rx, xrth, cx;
  }
  
  class BarCalculator implements BarOperation
  {
    BarCalculator(Instrument instr, LineSet ls, DataSeries series)
    {
      this.instr = instr;
      this.ls = ls;
      this.series = series;
    }
    
    @Override
    public void onBar(Bar bar)
    {
      long barStart = bar.getStartTime();
      if (barIndex < 0 || series.getEndTime(barIndex) <= bar.getStartTime()) {
        barIndex = series.findIndex(barStart);
      }
      
      float barLow = bar.getLow(), barHigh = bar.getHigh();
      if (barStart >= ls.eod) { // roll over to the next day
        series.setPathBreak(barIndex, Values.DHIGH_VAL, true);
        series.setPathBreak(barIndex, Values.DLOW_VAL, true);
        series.setPathBreak(barIndex, Values.DMID_VAL, true);
        long sod = getStartOfNextPeriod(ls.sod, instr, false);
        var nextLs = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr));
        if (!rth || barStart >= nextLs.sodRth) {
          nextLs.low = barLow;
          nextLs.high = barHigh;
          nextLs.mid = (nextLs.low + nextLs.high)/2;
          nextLs.open = bar.getOpen();
          nextLs.close = bar.getClose();
        }
        nextLs.olow = barLow;
        nextLs.ohigh = barHigh;
        if (barStart >= nextLs.sodRth && barStart < nextLs.sodRth + range) {
          nextLs.rLow = barLow;
          nextLs.rHigh = barHigh;
        }
        if (!lines.contains(nextLs)) lines.add(nextLs);
        ls = nextLs;
      }
      else {
        if (!rth || barStart >= ls.sodRth) {
          if (barLow < ls.low) ls.low = barLow;
          if (barHigh > ls.high) ls.high = barHigh;
          if (ls.open == Float.MIN_VALUE) ls.open = bar.getOpen();
          ls.close = bar.getClose();
          ls.mid = (ls.low + ls.high)/2;
        }
        if (barStart < ls.sodRth) {
          if (barLow < ls.olow) ls.olow = barLow;
          if (barHigh > ls.ohigh) ls.ohigh = barHigh;
        }
        if (barStart >= ls.sodRth && barStart < ls.sodRth + range) {
          if (barLow < ls.rLow) ls.rLow = barLow;
          if (barHigh > ls.rHigh) ls.rHigh = barHigh;
        }
      }
      
      if (rth && barStart < ls.sodRth) return;
      
      // Developing values
      series.setFloat(barIndex, Values.DHIGH_VAL, ls.high);
      series.setFloat(barIndex, Values.DLOW_VAL, ls.low);
      series.setFloat(barIndex, Values.DMID_VAL, ls.mid);
      
      // Hack for non-linear bars, we can have a gap in the data.  If this is the case, fill in the gap (accounting for RTH data)
      if (lastIndex != -1 && barIndex - lastIndex > 1) {
        for(int i = lastIndex+1; i < barIndex; i++) {
          long start = series.getStartTime(i);
          if (rth && start < ls.sodRth) break;
          series.setFloat(i, Values.DHIGH_VAL, series.getFloat(i-1, Values.DHIGH_VAL));
          series.setFloat(i, Values.DLOW_VAL, series.getFloat(i-1, Values.DLOW_VAL));
          series.setFloat(i, Values.DMID_VAL, series.getFloat(i-1, Values.DMID_VAL));
        }
      }
      lastIndex = barIndex;
    }
    
    int barIndex = -1, lastIndex = -1;
    Instrument instr;
    LineSet ls;
    DataSeries series;
  }
  
  // TODO: this should be refactored with onTick above
  class TickCalculator implements TickOperation
  {
    TickCalculator(Instrument instr, LineSet ls, DataSeries series)
    {
      this.instr = instr;
      this.ls = ls;
      this.series = series;
    }
    
    @Override
    public void onTick(Tick tick)
    {
      long time = tick.getTime();
      float price = tick.getPrice();
      if (barIndex < 0 || series.getEndTime(barIndex) <= tick.getTime()) {
        barIndex = series.findIndex(tick.getTime());
      }
      if (time >= ls.eod) { // roll over to the next day
        series.setPathBreak(barIndex, Values.DHIGH_VAL, true);
        series.setPathBreak(barIndex, Values.DLOW_VAL, true);
        series.setPathBreak(barIndex, Values.DMID_VAL, true);
        long sod = getStartOfNextPeriod(ls.sod, instr, false);
        var nextLs = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr));
        if (!rth || time >= nextLs.sodRth) {
          nextLs.low = nextLs.high = nextLs.open = nextLs.close = nextLs.ohigh = nextLs.olow = price;
          nextLs.mid = (nextLs.low + nextLs.high)/2;
        }
        nextLs.ohigh = nextLs.olow = price;
        if (time >= nextLs.sodRth && time < nextLs.sodRth + range) {
          nextLs.rLow = price;
          nextLs.rHigh = price;
        }
        if (!lines.contains(nextLs)) lines.add(nextLs);
        ls = nextLs;
      }
      else {
        if (!rth || time >= ls.sodRth) {
          if (price < ls.low) ls.low = price;
          if (price > ls.high) ls.high = price;
          if (ls.open == Float.MIN_VALUE) ls.open = price;
          ls.close = price;
          ls.mid = (ls.low + ls.high)/2;
        }
        if (time < ls.sodRth) {
          if (price < ls.olow) ls.olow = price;
          if (price > ls.ohigh) ls.ohigh = price;
        }
        if (time >= ls.sodRth && time < ls.sodRth + range) {
          if (price < ls.rLow) ls.rLow = price;
          if (price > ls.rHigh) ls.rHigh = price;
        }
      }
      if (rth && time < ls.sodRth) return;
      // Developing values
      series.setFloat(barIndex, Values.DHIGH_VAL, ls.high);
      series.setFloat(barIndex, Values.DLOW_VAL, ls.low);
      series.setFloat(barIndex, Values.DMID_VAL, ls.mid);
    }
    
    Instrument instr;
    LineSet ls;
    DataSeries series;
    int barIndex = -1;
  }
}
