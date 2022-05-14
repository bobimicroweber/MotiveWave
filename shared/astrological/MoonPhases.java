package com.motivewave.platform.study.astrological;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Text;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Plots phases of the moon. */
@StudyHeader(
    namespace="com.motivewave", 
    id="MoonPhases", 
    rb="com.motivewave.platform.study.nls.strings",
    menu="MENU_ASTRO",
    name="TITLE_MOON_PHASES", 
    desc="DESC_MOON_PHASES",
    overlay=true,
    helpLink="http://www.motivewave.com/studies/moon_phases.htm")
public class MoonPhases extends com.motivewave.platform.sdk.study.Study 
{
  // Settings
  final static String NEW_MOON = "newMoon", QUARTER_MOON = "quarterMoon", CRESENT_MOON = "cresentMoon";
  final static String SHOW_TIME = "showTime", SHOW_24_HOUR = "show24Hr", SHOW_TOP = "showTop", SHOW_BOTTOM = "showBottom";
  final static String FULL_LINE = "fullLine", NEW_LINE = "newLine", QUARTER_LINE = "quarterLine", CRESENT_LINE = "cresentLine";

  final static BasicStroke SOLID_LINE = new BasicStroke(1.0f);
  final static Font TIMF_FONT = new Font("Arial", Font.PLAIN, 10);
  
  enum Phase { NEW, WAXING_CRESENT, QUARTER, WAXING_GIBBOUS, FULL, WANING_GIBBOUS, LAST_QUARTER, WANING_CRESENT } 

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var inputs = tab.addGroup(get("LBL_PHASES"));
    inputs.addRow(new BooleanDescriptor(NEW_MOON, get("LBL_SHOW_NEW_MOONS"), false));
    inputs.addRow(new BooleanDescriptor(QUARTER_MOON, get("LBL_SHOW_QUARTER_MOONS"), false));

    var options = tab.addGroup(get("LBL_OPTIONS"));
    options.addRow(new BooleanDescriptor(SHOW_TIME, get("LBL_SHOW_TIME"), true), new BooleanDescriptor(SHOW_24_HOUR, get("LBL_SHOW_24_HOUR_TIME"), false));
    options.addRow(new BooleanDescriptor(SHOW_TOP, get("LBL_SHOW_TOP"), true), new BooleanDescriptor(SHOW_BOTTOM, get("LBL_SHOW_BOTTOM"), true));

    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(FULL_LINE, get("LBL_FULL_LINES"), defaults.getLineColor(), 1.0f, new float[] {3.0f, 3.0f}, true, false, true));
    lines.addRow(new PathDescriptor(NEW_LINE, get("LBL_NEW_LINES"), defaults.getGreenLine(), 1.0f, new float[] {3.0f, 3.0f}, true, false, true));
    lines.addRow(new PathDescriptor(QUARTER_LINE, get("LBL_QUARTER_LINES"), defaults.getBlueLine(), 1.0f, new float[] {3.0f, 3.0f}, true, false, true));

    sd.addDependency(new EnabledDependency(SHOW_TIME, SHOW_24_HOUR));
    
    sd.addQuickSettings(NEW_MOON, QUARTER_MOON, SHOW_TIME, SHOW_TOP, FULL_LINE, NEW_LINE, QUARTER_LINE);
  }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    DataSeries series = ctx.getDataSeries();
    long start = series.getStartTime(0);
    boolean show24 = getSettings().getBoolean(SHOW_24_HOUR);
    boolean showNewMoons = getSettings().getBoolean(NEW_MOON);
    boolean showQuarterMoons = getSettings().getBoolean(QUARTER_MOON);


    clearFigures();
    for(Long time : new ArrayList<>(allPhases.keySet())) {
      if (time == null) continue;
      if (time < start) continue;
      if (time > ctx.getCurrentTime() + 6*Util.MILLIS_IN_MONTH) break;
      Phase phase = allPhases.get(time);
      if (phase == null) continue;
      if (!showNewMoons && phase == Phase.NEW) continue;
      if (!showQuarterMoons && phase == Phase.LAST_QUARTER) continue;
      if (!showQuarterMoons && phase == Phase.QUARTER) continue;
      if (!showQuarterMoons && phase == Phase.LAST_QUARTER) continue;
      addFigure(new MoonPhase(time, allPhases.get(time), show24));
    }
  }
  
  private static class MoonPhase extends Figure
  {
    MoonPhase(long time, Phase p, boolean show24)
    {
      this.time = time; 
      phase = p;
      SimpleDateFormat fmt = show24 ? fmt24 : fmt12;
      topLbl = new Text(fmt.format(time), TIMF_FONT, new Insets(1, 3, 1, 1), false);
      bottomLbl = new Text(fmt.format(time), TIMF_FONT, new Insets(1, 3, 1, 1), false);
    }
    
    @Override
    public boolean isVisible(DrawContext ctx)
    {
      DataSeries series = ctx.getDataContext().getDataSeries();
      if (time < series.getVisibleStartTime()) return false;
      if (series.isLatestData()) return true;
      return time < series.getVisibleEndTime();
    }

    @Override
    public boolean contains(double x, double y, DrawContext ctx)
    {
      if (!isVisible(ctx)) return false;
      if (Math.abs(xpos - x) < 4) return true;
      
      boolean showTime = ctx.getSettings().getBoolean(SHOW_TIME);
      boolean showTop = ctx.getSettings().getBoolean(SHOW_TOP);
      boolean showBottom = ctx.getSettings().getBoolean(SHOW_BOTTOM);
      if (showTime && showTop) {
        if (topLbl != null && topLbl.contains(x, y)) return true;
      }
      if (showTime && showBottom) {
        if (bottomLbl != null && bottomLbl.contains(x, y)) return true;
      }
      return false;
    }
    
    @Override 
    public void layout(DrawContext ctx)
    {
      Rectangle bounds = ctx.getBounds();
      xpos = (int)ctx.translate(time, 0).getX();
      topLbl.setLocation(xpos + 10, bounds.getY() + 2);
      bottomLbl.setLocation(xpos + 10, bounds.getMaxY() - bottomLbl.getHeight()-2);
    }

    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      Rectangle bounds = ctx.getBounds();
      if (xpos > (bounds.getMaxX()+20) || xpos < (bounds.x - 20)) return;

      boolean showTime = ctx.getSettings().getBoolean(SHOW_TIME);
      boolean showTop = ctx.getSettings().getBoolean(SHOW_TOP);
      boolean showBottom = ctx.getSettings().getBoolean(SHOW_BOTTOM);

      PathInfo newPath = ctx.getSettings().getPath(NEW_LINE);
      PathInfo fullPath = ctx.getSettings().getPath(FULL_LINE);
      PathInfo quarterPath = ctx.getSettings().getPath(QUARTER_LINE);
      
      switch(phase) {
      case FULL:
        if (fullPath.isEnabled()) {
          if (ctx.isSelected()) gc.setStroke(fullPath.getSelectedStroke());
          else gc.setStroke(fullPath.getStroke());
          gc.setColor(fullPath.getColor());
          gc.drawLine(xpos, bounds.y, xpos, (int)bounds.getMaxY());
        }
        if (showBottom) {
          gc.setColor(ctx.getDefaults().getYellow());
          gc.fillOval(xpos-8, (int)bounds.getMaxY() - 18, 16, 16);
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.setStroke(SOLID_LINE);
          gc.drawOval(xpos-8, (int)bounds.getMaxY() - 18, 16, 16);
        }
        if (showTop) {
          gc.setColor(ctx.getDefaults().getYellow());
          gc.fillOval(xpos-8, (int)bounds.getY() +2, 16, 16);
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.setStroke(SOLID_LINE);
          gc.drawOval(xpos-8, (int)bounds.getY() +2, 16, 16);
        }
        if (showTime) {
          if (showTop) topLbl.draw(gc);
          if (showBottom) bottomLbl.draw(gc);
        }
        break;
      case NEW:
        if (newPath.isEnabled()) {
          if (ctx.isSelected()) gc.setStroke(newPath.getSelectedStroke());
          else gc.setStroke(newPath.getStroke());
          gc.setColor(newPath.getColor());
          gc.drawLine(xpos, bounds.y, xpos, (int)bounds.getMaxY());
        }
        if (showBottom) {
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.fillOval(xpos-8, (int)bounds.getMaxY() - 18, 16, 16);
        }
        if (showTop) {
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.fillOval(xpos-8, (int)bounds.getY() +2, 16, 16);
        }
        if (showTime) {
          if (showTop) topLbl.draw(gc);
          if (showBottom) bottomLbl.draw(gc);
        }
        
        break;
      case QUARTER:
      case LAST_QUARTER:
        if (quarterPath.isEnabled()) {
          if (ctx.isSelected()) gc.setStroke(quarterPath.getSelectedStroke());
          else gc.setStroke(quarterPath.getStroke());
          gc.setColor(quarterPath.getColor());
          gc.drawLine(xpos, bounds.y, xpos, (int)bounds.getMaxY());
        }
        if (showTop) {
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.fillOval(xpos-8, (int)bounds.getY()+2, 16, 16);
          gc.setColor(ctx.getDefaults().getYellow());
          if (phase == Phase.LAST_QUARTER) gc.fillArc(xpos-8, (int)bounds.getY()+2, 16, 16, -90, 180);
          else gc.fillArc(xpos-8, (int)bounds.getY()+2, 16, 16, -270, 180);
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.setStroke(SOLID_LINE);
          gc.drawOval(xpos-8, (int)bounds.getY()+2, 16, 16);
        }
        if (showBottom) {
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.fillOval(xpos-8, (int)bounds.getMaxY() - 18, 16, 16);
          gc.setColor(ctx.getDefaults().getYellow());
          if (phase == Phase.LAST_QUARTER) gc.fillArc(xpos-8, (int)bounds.getMaxY() - 18, 16, 16, -90, 180);
          else gc.fillArc(xpos-8, (int)bounds.getMaxY() - 18, 16, 16, -270, 180);
          gc.setColor(ctx.getDefaults().getLineColor());
          gc.setStroke(SOLID_LINE);
          gc.drawOval(xpos-8, (int)bounds.getMaxY() - 18, 16, 16);
        }
        if (showTime) {
          if (showTop) topLbl.draw(gc);
          if (showBottom) bottomLbl.draw(gc);
        }
        break;
      default: break;
      }
    }

    long time;
    Phase phase;
    int xpos;
    Text topLbl;
    Text bottomLbl;
    static SimpleDateFormat fmt12 = new SimpleDateFormat("hh:mm a");
    static SimpleDateFormat fmt24 = new SimpleDateFormat("HH:mm");
  }
  
  
  private static void registerFullMoon(int month, int day, int year, int hour, int minute)
  {
    long time = toGMTDate(day, month-1, year, hour, minute, 0, 0).getTime();
    long cycle = (long)((double)24* 60 * 60 * 1000 * 29.5305882)/2;
    if (lastFullMoon > 0) cycle = (time - lastFullMoon);
    
    //cAllPhases.put(time - 7*cycle/8, Phase.WANING_CRESENT);
    allPhases.put((long)(time - 0.75*cycle), Phase.QUARTER);
    //cAllPhases.put(time - 5*cycle/8, Phase.WANING_GIBBOUS);
    allPhases.put((long)(time - 0.50*cycle), Phase.NEW);
    //cAllPhases.put(time - 3*cycle/8, Phase.WAXING_CRESENT);
    allPhases.put((long)(time - 0.25*cycle), Phase.LAST_QUARTER);
    //cAllPhases.put(time - cycle/8, Phase.WAXING_GIBBOUS);
    allPhases.put(time, Phase.FULL);
    lastFullMoon = time;
  }

  public static java.util.Date toGMTDate(int day, int month, int year, int hour, int min, int sec, int millis) 
  {
    synchronized(GMTCal) {
      GMTCal.set(Calendar.MONTH, month);
      GMTCal.set(Calendar.YEAR, year);
      GMTCal.set(Calendar.DAY_OF_MONTH, day);
      GMTCal.set(Calendar.HOUR_OF_DAY, hour);
      GMTCal.set(Calendar.MINUTE, min);
      GMTCal.set(Calendar.SECOND, sec);
      GMTCal.set(Calendar.MILLISECOND, millis);
      return GMTCal.getTime();
    }
  }

  private static long lastFullMoon=0;
  private static Map<Long, Phase> allPhases = Collections.synchronizedMap(new LinkedHashMap()); 
  private static Calendar GMTCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

  static {
    // From www.timeanddate.com
    // 1990
    registerFullMoon(1, 11, 1990, 4, 57);
    registerFullMoon(2, 9, 1990, 19, 16);
    registerFullMoon(3, 11, 1990, 10, 59);
    registerFullMoon(4, 10, 1990, 3, 19);
    registerFullMoon(5, 9, 1990, 19, 31);
    registerFullMoon(6, 8, 1990, 11, 1);
    registerFullMoon(7, 8, 1990, 1, 24);
    registerFullMoon(8, 6, 1990, 14, 19);
    registerFullMoon(9, 5, 1990, 1, 45);
    registerFullMoon(10, 4, 1990, 12, 2);
    registerFullMoon(11, 2, 1990, 21, 49);
    registerFullMoon(12, 2, 1990, 7, 50);
    registerFullMoon(12, 31, 1990, 18, 35);

    // 1991
    registerFullMoon(1, 30, 1991, 6, 10);
    registerFullMoon(2, 28, 1991, 18, 25);
    registerFullMoon(3, 30, 1991, 7, 18);
    registerFullMoon(4, 28, 1991, 20, 59);
    registerFullMoon(5, 28, 1991, 11, 37);
    registerFullMoon(6, 27, 1991, 2, 58);
    registerFullMoon(7, 26, 1991, 18, 24);
    registerFullMoon(8, 25, 1991, 9, 7);
    registerFullMoon(9, 23, 1991, 20, 40);
    registerFullMoon(10, 23, 1991, 11, 8);
    registerFullMoon(11, 21, 1991, 20, 56);
    registerFullMoon(12, 21, 1991, 10, 24);

    // 1992
    registerFullMoon(1, 19, 1992, 21, 29);
    registerFullMoon(2, 18, 1992, 8, 4);
    registerFullMoon(3, 18, 1992, 18, 18);
    registerFullMoon(4, 17, 1992, 4, 43);
    registerFullMoon(5, 16, 1992, 16, 3);
    registerFullMoon(6, 15, 1992, 4, 50);
    registerFullMoon(7, 14, 1992, 19, 6);
    registerFullMoon(8, 13, 1992, 10, 27);
    registerFullMoon(9, 12, 1992, 2, 17);
    registerFullMoon(10, 11, 1992, 18, 3);
    registerFullMoon(11, 10, 1992, 9, 20);
    registerFullMoon(12, 9, 1992, 23, 41);

    // 1993
    registerFullMoon(1, 8, 1993, 12, 37);
    registerFullMoon(2, 6, 1993, 23, 55);
    registerFullMoon(3, 8, 1993, 9, 46);
    registerFullMoon(4, 6, 1993, 18, 43);
    registerFullMoon(5, 6, 1993, 3, 34);
    registerFullMoon(6, 4, 1993, 13, 2);
    registerFullMoon(7, 3, 1993, 23, 45);
    registerFullMoon(8, 2, 1993, 12, 10);
    registerFullMoon(9, 1, 1993, 2, 33);
    registerFullMoon(9, 30, 1993, 18, 54);
    registerFullMoon(10, 30, 1993, 12, 37);
    registerFullMoon(11, 29, 1993, 6, 31);
    registerFullMoon(12, 28, 1993, 23, 05);

    // 1994
    registerFullMoon(1, 27, 1994, 13, 23);
    registerFullMoon(2, 26, 1994, 1, 15);
    registerFullMoon(3, 27, 1994, 11, 10);
    registerFullMoon(4, 25, 1994, 19, 45);
    registerFullMoon(5, 25, 1994, 3, 39);
    registerFullMoon(6, 23, 1994, 11, 33);
    registerFullMoon(7, 22, 1994, 20, 16);
    registerFullMoon(8, 21, 1994, 6, 47);
    registerFullMoon(9, 19, 1994, 20, 1);
    registerFullMoon(10, 19, 1994, 12, 18);
    registerFullMoon(11, 18, 1994, 6, 57);
    registerFullMoon(12, 18, 1994, 2, 17);

    // 1995
    registerFullMoon(1, 16, 1995, 20, 26);
    registerFullMoon(2, 15, 1995, 12, 15);
    registerFullMoon(3, 17, 1995, 1, 26);
    registerFullMoon(4, 15, 1995, 12, 8);
    registerFullMoon(5, 14, 1995, 20, 48);
    registerFullMoon(6, 13, 1995, 4, 3);
    registerFullMoon(7, 12, 1995, 10, 49);
    registerFullMoon(8, 10, 1995, 18, 16);
    registerFullMoon(9, 9, 1995, 3, 37);
    registerFullMoon(10, 8, 1995, 15, 52);
    registerFullMoon(11, 7, 1995, 7, 21);
    registerFullMoon(12, 7, 1995, 1, 27);

    // 1996
    registerFullMoon(1, 5, 1996, 20, 51);
    registerFullMoon(2, 4, 1996, 15, 58);
    registerFullMoon(3, 5, 1996, 9, 23);
    registerFullMoon(4, 4, 1996, 0, 7);
    registerFullMoon(5, 3, 1996, 11, 48);
    registerFullMoon(6, 1, 1996, 20, 47);
    registerFullMoon(7, 1, 1996, 3, 58);
    registerFullMoon(7, 30, 1996, 3, 21);
    registerFullMoon(8, 28, 1996, 10, 35);
    registerFullMoon(9, 27, 1996, 17, 52);
    registerFullMoon(10, 26, 1996, 2, 51);
    registerFullMoon(11, 25, 1996, 14, 10);
    registerFullMoon(12, 24, 1996, 20, 41);

    // 1997
    registerFullMoon(1, 23, 1997, 15, 11);
    registerFullMoon(2, 22, 1997, 10, 27);
    registerFullMoon(3, 24, 1997, 4, 45);
    registerFullMoon(4, 22, 1997, 20, 34);
    registerFullMoon(5, 22, 1997, 9, 13);
    registerFullMoon(6, 20, 1997, 19, 9);
    registerFullMoon(7, 20, 1997, 3, 21);
    registerFullMoon(8, 18, 1997, 10, 56);
    registerFullMoon(9, 16, 1997, 18, 51);
    registerFullMoon(10, 16, 1997, 3, 46);
    registerFullMoon(11, 14, 1997, 14, 12);
    registerFullMoon(12, 14, 1997, 2, 38);

    // 1998
    registerFullMoon(1, 12, 1998, 17, 24);
    registerFullMoon(2, 11, 1998, 10, 23);
    registerFullMoon(3, 13, 1998, 4, 35);
    registerFullMoon(4, 11, 1998, 20, 24);
    registerFullMoon(5, 11, 1998, 14, 29);
    registerFullMoon(6, 10, 1998, 4, 18);
    registerFullMoon(7, 9, 1998, 16, 1);
    registerFullMoon(8, 8, 1998, 2, 10);
    registerFullMoon(9, 6, 1998, 11, 21);
    registerFullMoon(10, 5, 1998, 20, 12);
    registerFullMoon(11, 4, 1998, 5, 18);
    registerFullMoon(12, 3, 1998, 15, 19);

    // 1999
    registerFullMoon(1, 2, 1999, 2, 50);
    registerFullMoon(1, 31, 1999, 16, 7);
    registerFullMoon(3, 2, 1999, 6, 59);
    registerFullMoon(3, 31, 1999, 22, 49);
    registerFullMoon(4, 30, 1999, 14, 55);
    registerFullMoon(5, 30, 1999, 6, 40);
    registerFullMoon(6, 28, 1999, 21, 38);
    registerFullMoon(7, 28, 1999, 11, 25);
    registerFullMoon(8, 26, 1999, 23, 48);
    registerFullMoon(9, 25, 1999, 10, 51);
    registerFullMoon(10, 24, 1999, 21, 3);
    registerFullMoon(11, 23, 1999, 7, 4);
    registerFullMoon(12, 22, 1999, 17, 31);

    // 2000
    registerFullMoon(1, 21, 2000, 4, 40);
    registerFullMoon(2, 19, 2000, 16, 27);
    registerFullMoon(3, 20, 2000, 4, 45);
    registerFullMoon(4, 18, 2000, 17, 42);
    registerFullMoon(5, 18, 2000, 7, 35);
    registerFullMoon(6, 16, 2000, 22, 27);
    registerFullMoon(7, 16, 2000, 13, 56);
    registerFullMoon(8, 15, 2000, 5, 13);
    registerFullMoon(9, 13, 2000, 19, 37);
    registerFullMoon(10, 13, 2000, 8, 53);
    registerFullMoon(11, 11, 2000, 21, 15);
    registerFullMoon(12, 11, 2000, 9, 3);

    // 2001
    registerFullMoon(1, 9, 2001, 20, 25);
    registerFullMoon(2, 8, 2001, 7, 12);
    registerFullMoon(3, 9, 2001, 17, 23);
    registerFullMoon(4, 8, 2001, 3, 22);
    registerFullMoon(5, 7, 2001, 13, 53);
    registerFullMoon(6, 6, 2001, 1, 40);
    registerFullMoon(7, 5, 2001, 15, 4);
    registerFullMoon(8, 4, 2001, 5, 56);
    registerFullMoon(9, 2, 2001, 21, 43);
    registerFullMoon(10, 2, 2001, 13, 49);
    registerFullMoon(11, 1, 2001, 5, 41);
    registerFullMoon(11, 30, 2001, 20, 49);
    registerFullMoon(12, 30, 2001, 10, 41);

    // 2002
    registerFullMoon(1, 28, 2002, 20, 50);
    registerFullMoon(2, 27, 2002, 9, 16);
    registerFullMoon(3, 28, 2002, 18, 25);
    registerFullMoon(4, 27, 2002, 3, 0);
    registerFullMoon(5, 26, 2002, 11, 52);
    registerFullMoon(6, 24, 2002, 21, 43);
    registerFullMoon(7, 24, 2002, 9, 7);
    registerFullMoon(8, 22, 2002, 22, 29);
    registerFullMoon(9, 21, 2002, 13, 59);
    registerFullMoon(10, 21, 2002, 7, 20);
    registerFullMoon(11, 20, 2002, 1, 34);
    registerFullMoon(12, 19, 2002, 19, 10);

    // 2003
    registerFullMoon(1, 18, 2003, 10, 48);
    registerFullMoon(2, 16, 2003, 23, 51);
    registerFullMoon(3, 18, 2003, 10, 34);
    registerFullMoon(4, 16, 2003, 17, 35);
    registerFullMoon(5, 16, 2003, 3, 36);
    registerFullMoon(6, 14, 2003, 11, 16);
    registerFullMoon(7, 13, 2003, 19, 21);
    registerFullMoon(8, 12, 2003, 4, 48);
    registerFullMoon(9, 10, 2003, 16, 36);
    registerFullMoon(10, 10, 2003, 7, 28);
    registerFullMoon(11, 9, 2003, 1, 14);
    registerFullMoon(12, 8, 2003, 20, 37);

    // 2004
    registerFullMoon(1, 7, 2004, 15, 41);
    registerFullMoon(2, 6, 2004, 8, 47);
    registerFullMoon(3, 6, 2004, 23, 14);
    registerFullMoon(4, 5, 2004, 11, 3);
    registerFullMoon(5, 4, 2004, 20, 33);
    registerFullMoon(6, 3, 2004, 4, 20);
    registerFullMoon(7, 2, 2004, 11, 9);
    registerFullMoon(7, 31, 2004, 18, 5);
    registerFullMoon(8, 30, 2004, 2, 23);
    registerFullMoon(9, 28, 2004, 13, 10);
    registerFullMoon(10, 28, 2004, 3, 8);
    registerFullMoon(11, 26, 2004, 20, 7);
    registerFullMoon(12, 26, 2004, 15, 7);

    // 2005
    registerFullMoon(1, 25, 2005, 10, 33);
    registerFullMoon(2, 24, 2005, 4, 54);
    registerFullMoon(3, 25, 2005, 20, 59);
    registerFullMoon(4, 24, 2005, 10, 7);
    registerFullMoon(5, 23, 2005, 20, 18);
    registerFullMoon(6, 22, 2005, 4, 14);
    registerFullMoon(7, 21, 2005, 11, 00);
    registerFullMoon(8, 19, 2005, 17, 53);
    registerFullMoon(9, 18, 2005, 2, 1);
    registerFullMoon(10, 17, 2005, 12, 14);
    registerFullMoon(11, 16, 2005, 0, 58);
    registerFullMoon(12, 15, 2005, 16, 16);

    // 2006
    registerFullMoon(1, 14, 2006, 9, 48);
    registerFullMoon(2, 13, 2006, 4, 45);
    registerFullMoon(3, 14, 2006, 23, 36);
    registerFullMoon(4, 13, 2006, 16, 40);
    registerFullMoon(5, 13, 2006, 6, 51);
    registerFullMoon(6, 11, 2006, 18, 3);
    registerFullMoon(7, 11, 2006, 3, 2);
    registerFullMoon(8, 9, 2006, 10, 54);
    registerFullMoon(9, 7, 2006, 18, 42);
    registerFullMoon(10, 7, 2006, 3, 13);
    registerFullMoon(11, 5, 2006, 12, 58);
    registerFullMoon(12, 5, 2006, 0, 25);

    // 2007
    registerFullMoon(1, 3, 2007, 13, 57);
    registerFullMoon(2, 2, 2007, 5, 45);
    registerFullMoon(3, 3, 2007, 23, 17);
    registerFullMoon(4, 2, 2007, 17, 15);
    registerFullMoon(5, 2, 2007, 10, 10);
    registerFullMoon(6, 1, 2007, 1, 4);
    registerFullMoon(6, 30, 2007, 13, 49);
    registerFullMoon(7, 30, 2007, 0, 48);
    registerFullMoon(8, 28, 2007, 10, 35);
    registerFullMoon(9, 26, 2007, 19, 45);
    registerFullMoon(10, 26, 2007, 4, 52);
    registerFullMoon(11, 24, 2007, 14, 30);
    registerFullMoon(12, 24, 2007, 1, 16);

    // 2008
    registerFullMoon(1, 22, 2008, 13, 35);
    registerFullMoon(2, 21, 2008, 3, 31);
    registerFullMoon(3, 21, 2008, 18, 40);
    registerFullMoon(4, 20, 2008, 10, 26);
    registerFullMoon(5, 20, 2008, 2, 12);
    registerFullMoon(6, 18, 2008, 17, 31);
    registerFullMoon(7, 18, 2008, 7, 59);
    registerFullMoon(8, 16, 2008, 21, 16);
    registerFullMoon(9, 15, 2008, 9, 13);
    registerFullMoon(10, 14, 2008, 20, 2);
    registerFullMoon(11, 13, 2008, 6, 18);
    registerFullMoon(12, 12, 2008, 16, 38);

    // 2009
    registerFullMoon(1, 11, 2009, 3, 27);
    registerFullMoon(2, 9, 2009, 14, 49);
    registerFullMoon(3, 11, 2009, 2, 38);
    registerFullMoon(4, 9, 2009, 14, 56);
    registerFullMoon(5, 9, 2009, 4, 2);
    registerFullMoon(6, 7, 2009, 18, 12);
    registerFullMoon(7, 7, 2009, 9, 22);
    registerFullMoon(8, 6, 2009, 0, 55);
    registerFullMoon(9, 4, 2009, 16, 2);
    registerFullMoon(10, 4, 2009, 6, 10);
    registerFullMoon(11, 2, 2009, 19, 14);
    registerFullMoon(12, 2, 2009, 7, 31);
    registerFullMoon(12, 31, 2009, 19, 13);


    // 2010
    registerFullMoon(1, 30, 2010, 6, 18);
    registerFullMoon(2, 28, 2010, 16, 38);
    registerFullMoon(3, 30, 2010, 2, 25);
    registerFullMoon(4, 28, 2010, 12, 19);
    registerFullMoon(5, 27, 2010, 23, 8);
    registerFullMoon(6, 26, 2010, 11, 31);
    registerFullMoon(7, 26, 2010, 1, 37);
    registerFullMoon(8, 24, 2010, 17, 5);
    registerFullMoon(9, 23, 2010, 9, 17);
    registerFullMoon(10, 23, 2010, 1, 36);
    registerFullMoon(11, 21, 2010, 17, 27);
    registerFullMoon(12, 21, 2010, 8, 13);

    // 2011
    registerFullMoon(1, 19, 2011, 21, 21);
    registerFullMoon(2, 18, 2011, 8, 36);
    registerFullMoon(3, 19, 2011, 18, 10);
    registerFullMoon(4, 18, 2011, 2, 44);
    registerFullMoon(5, 17, 2011, 11, 9);
    registerFullMoon(6, 15, 2011, 20, 14);
    registerFullMoon(7, 15, 2011, 6, 40);
    registerFullMoon(8, 13, 2011, 18, 58);
    registerFullMoon(9, 12, 2011, 9, 27);
    registerFullMoon(10, 12, 2011, 2, 6);
    registerFullMoon(11, 10, 2011, 20, 16);
    registerFullMoon(12, 10, 2011, 14, 36);

    // 2012
    registerFullMoon(1, 9, 2012, 7, 30);
    registerFullMoon(2, 7, 2012, 21, 54);
    registerFullMoon(3, 8, 2012, 9, 40);
    registerFullMoon(4, 6, 2012, 19, 19);
    registerFullMoon(5, 6, 2012, 3, 35);
    registerFullMoon(6, 4, 2012, 11, 12);
    registerFullMoon(7, 3, 2012, 18, 52);
    registerFullMoon(8, 2, 2012, 3, 28);
    registerFullMoon(8, 31, 2012, 13, 59);
    registerFullMoon(9, 30, 2012, 3, 19);
    registerFullMoon(10, 29, 2012, 19, 50);
    registerFullMoon(11, 28, 2012, 14, 46);
    registerFullMoon(12, 28, 2012, 10, 22);

    // 2013
    registerFullMoon(1, 27, 2013, 4, 39);
    registerFullMoon(2, 25, 2013, 20, 27);
    registerFullMoon(3, 27, 2013, 9, 28);
    registerFullMoon(4, 25, 2013, 19, 58);
    registerFullMoon(5, 25, 2013, 4, 26);
    registerFullMoon(6, 23, 2013, 11, 33);
    registerFullMoon(7, 22, 2013, 18, 16);
    registerFullMoon(8, 21, 2013, 1, 45);
    registerFullMoon(9, 19, 2013, 11, 38);
    registerFullMoon(10, 18, 2013, 23, 38);
    registerFullMoon(11, 17, 2013, 15, 16);
    registerFullMoon(12, 17, 2013, 9, 29);

    // 2014
    registerFullMoon(1, 16, 2014, 4, 53);
    registerFullMoon(2, 14, 2014, 23, 54);
    registerFullMoon(3, 16, 2014, 17, 9);
    registerFullMoon(4, 15, 2014, 7, 43);
    registerFullMoon(5, 14, 2014, 19, 16);
    registerFullMoon(6, 13, 2014, 4, 12);
    registerFullMoon(7, 12, 2014, 11, 25);
    registerFullMoon(8, 10, 2014, 18, 10);
    registerFullMoon(9, 9, 2014, 1, 39);
    registerFullMoon(10, 8, 2014, 22, 51);
    registerFullMoon(11, 6, 2014, 22, 23);
    registerFullMoon(12, 6, 2014, 12, 27);

    // 2015
    registerFullMoon(1, 5, 2015, 4, 54);
    registerFullMoon(2, 3, 2015, 23, 9);
    registerFullMoon(3, 5, 2015, 18, 6);
    registerFullMoon(4, 4, 2015, 12, 6);
    registerFullMoon(5, 4, 2015, 3, 42);
    registerFullMoon(6, 2, 2015, 16, 19);
    registerFullMoon(7, 2, 2015, 2, 20);
    registerFullMoon(7, 31, 2015, 10, 43);
    registerFullMoon(8, 29, 2015, 18, 36);
    registerFullMoon(9, 28, 2015, 2, 51);
    registerFullMoon(10, 27, 2015, 12, 6);
    registerFullMoon(11, 25, 2015, 22, 45);
    registerFullMoon(12, 25, 2015, 11, 12);

    // 2016
    registerFullMoon(1, 24, 2016, 1, 46);
    registerFullMoon(2, 22, 2016, 18, 20);
    registerFullMoon(3, 23, 2016, 12, 2);
    registerFullMoon(4, 22, 2016, 5, 24);
    registerFullMoon(5, 21, 2016, 21, 15);
    registerFullMoon(6, 20, 2016, 11, 3);
    registerFullMoon(7, 19, 2016, 22, 57);
    registerFullMoon(8, 18, 2016, 9, 27);
    registerFullMoon(9, 16, 2016, 19, 6);
    registerFullMoon(10, 16, 2016, 4, 24);
    registerFullMoon(11, 14, 2016, 13, 53);
    registerFullMoon(12, 14, 2016, 0, 6);

    // 2017
    registerFullMoon(1, 12, 2017, 11, 34);
    registerFullMoon(2, 11, 2017, 0, 33);
    registerFullMoon(3, 12, 2017, 14, 54);
    registerFullMoon(4, 11, 2017, 6, 9);
    registerFullMoon(5, 10, 2017, 21, 43);
    registerFullMoon(6, 9, 2017, 13, 10);
    registerFullMoon(7, 9, 2017, 4, 7);
    registerFullMoon(8, 7, 2017, 18, 11);
    registerFullMoon(9, 6, 2017, 7, 3);
    registerFullMoon(10, 5, 2017, 18, 40);
    registerFullMoon(11, 4, 2017, 5, 23);
    registerFullMoon(12, 3, 2017, 15, 47);

    // 2018
    registerFullMoon(1, 2, 2018, 2, 24);
    registerFullMoon(1, 31, 2018, 13, 27);
    registerFullMoon(3, 2, 2018, 0, 52);
    registerFullMoon(3, 31, 2018, 12, 37);
    registerFullMoon(4, 30, 2018, 0, 59);
    registerFullMoon(5, 29, 2018, 14, 20);
    registerFullMoon(6, 28, 2018, 4, 53);
    registerFullMoon(7, 27, 2018, 20, 21);
    registerFullMoon(8, 26, 2018, 11, 57);
    registerFullMoon(9, 25, 2018, 2, 53);
    registerFullMoon(10, 24, 2018, 16, 45);
    registerFullMoon(11, 23, 2018, 5, 39);
    registerFullMoon(12, 22, 2018, 17, 49);

    // 2019
    registerFullMoon(1, 21, 2019, 5, 16);
    registerFullMoon(2, 19, 2019, 15, 53);
    registerFullMoon(3, 21, 2019, 1, 43);
    registerFullMoon(4, 19, 2019, 11, 12);
    registerFullMoon(5, 18, 2019, 21, 12);
    registerFullMoon(6, 17, 2019, 8, 31);
    registerFullMoon(7, 16, 2019, 21, 38);
    registerFullMoon(8, 15, 2019, 12, 30);
    registerFullMoon(9, 14, 2019, 4, 33);
    registerFullMoon(10, 13, 2019, 21, 8);
    registerFullMoon(11, 12, 2019, 13, 35);
    registerFullMoon(12, 12, 2019, 5, 12);

    // 2020
    registerFullMoon(1, 10, 2020, 19, 21);
    registerFullMoon(2, 9, 2020, 7, 33);
    registerFullMoon(3, 9, 2020, 17, 47);
    registerFullMoon(4, 8, 2020, 2, 35);
    registerFullMoon(5, 7, 2020, 10, 45);
    registerFullMoon(6, 5, 2020, 19, 12);
    registerFullMoon(7, 5, 2020, 4, 44);
    registerFullMoon(8, 3, 2020, 15, 59);
    registerFullMoon(9, 2, 2020, 5, 22);
    registerFullMoon(10, 1, 2020, 21, 5);
    registerFullMoon(10, 31, 2020, 14, 49);
    registerFullMoon(11, 30, 2020, 9, 30);
    registerFullMoon(12, 30, 2020, 3, 29);

    // 2021
    registerFullMoon(1, 28, 2021, 19, 16);
    registerFullMoon(2, 27, 2021, 8, 17);
    registerFullMoon(3, 28, 2021, 18, 48);
    registerFullMoon(4, 27, 2021, 3, 32);
    registerFullMoon(5, 26, 2021, 11, 14);
    registerFullMoon(6, 24, 2021, 18, 40);
    registerFullMoon(7, 24, 2021, 2, 37);
    registerFullMoon(8, 22, 2021, 12, 02);
    registerFullMoon(9, 20, 2021, 23, 55);
    registerFullMoon(10, 20, 2021, 14, 57);
    registerFullMoon(11, 19, 2021, 8, 58);
    registerFullMoon(12, 19, 2021, 4, 36);
    
    // 2022
    registerFullMoon(1, 17, 2022, 23, 51);
    registerFullMoon(2, 16, 2022, 16, 59);
    registerFullMoon(3, 18, 2022, 7, 20);
    registerFullMoon(4, 16, 2022, 18, 57);
    registerFullMoon(5, 16, 2022, 4, 15);
    registerFullMoon(6, 14, 2022, 11, 52);
    registerFullMoon(7, 13, 2022, 20, 38);
    registerFullMoon(8, 12, 2022, 1, 36);
    registerFullMoon(9, 10, 2022, 9, 58);
    registerFullMoon(10, 9, 2022, 20, 54);
    registerFullMoon(11, 8, 2022, 11, 2);
    registerFullMoon(12, 8, 2022, 4, 9);

    // 2023
    registerFullMoon(1, 6, 2023, 23, 9);
    registerFullMoon(2, 5, 2023, 18, 30);
    registerFullMoon(3, 7, 2023, 12, 42);
    registerFullMoon(4, 6, 2023, 4, 37);
    registerFullMoon(5, 5, 2023, 17, 36);
    registerFullMoon(6, 4, 2023, 3, 43);
    registerFullMoon(7, 3, 2023, 11, 40);
    registerFullMoon(8, 1, 2023, 18, 33);
    registerFullMoon(8, 31, 2023, 1, 37);
    registerFullMoon(9, 29, 2023, 9, 58);
    registerFullMoon(10, 28, 2023, 20, 24);
    registerFullMoon(11, 27, 2023, 9, 16);
    registerFullMoon(12, 27, 2023, 12, 33);

  }
}
