//Notes
//year - 1 rotation around sun
// Earth 365.256 days / yr
// Mercury 88 earth days
// Venus 224.62 earth days
// Mars 687 earth days
// Jupiter - 4332.6 days or 11.9 years
// Saturn - 74520 or 29.46
// Uranus - 30685.4  or 84.07 tr
// Neptune - 30200 or 164.8 yr
// Pluto - 90465 or 247.7 yr
//
//Planet conjunction dates
// Mercury - Inferior Conjunction 4 March 2013 04hr, 13min GMT  also 1 Nov 2013 20, 00
// Venus  - Inferior Conjunction 6 June 2012 01hr, 00min GMT
// Mars - Conjunction 18 April 2012 00hr, 00min GMT
//Jupiter - Conjunction 19 June 2012 15hr, 00min GMT 
//Saturn Conjunction 6 Nov 2012 11hr, 00min GMT
//Uranus  Conjunction 29 March 2012 00hr, 00min GMT
//Neptune   Conjunction 21 Feb 2013, 07hr, 00min GMT
//Pluto Conjunction 30 Dec 2012, 00, 00 GMT
//
//Synodic Periods Days - Time between alignment of planet with earth and sun
//Mercury 115.88
//Venus 583.9
//Mars 779.9
//Jupiter 398.9
//Saturn 378.1
//Uranus 369.7
//Neptune 367.5
//Pluto 366.7
//
//Moon 29.5306
//sun 27.3
package com.motivewave.platform.study.general;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Text;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/**Planets  206 */
@StudyHeader(
    namespace="com.motivewave", 
    id="ID_PLANETS", 
    rb="com.motivewave.platform.study.nls.strings2",
    menu="MENU_ASTROLOGICAL",
    name="NAME_PLANETS", 
    desc="DESC_PLANETS",
    helpLink= "",
    supportsBarUpdates=false,
    overlay=true)
public class Planets extends Study 
{
  static Planets t = null;
  static Font timeFont = new Font("Arial", Font.PLAIN, 10);
  static long millSecPerDay = (24 * 60 * 60 *1000);
  
  static long marsSP =(long) (779.9 * millSecPerDay);  //Synodic Period in millisec
  static long venusSP =(long) (583.9 * millSecPerDay);  
  static long mercurySP =(long) (115.88 * millSecPerDay);
  static long jupiterSP =(long) (398.9 * millSecPerDay);
  static long saturnSP =(long) (378.1 * millSecPerDay);
  static long uranusSP =(long) (369.7 * millSecPerDay);
  static long neptuneSP =(long) (367.5 * millSecPerDay);
  static long plutoSP =(long) (366.7 * millSecPerDay);
  

  enum Planet {MARS, MARS_0, MARS_60, MARS_90, MARS_120, MARS_180, MARS_240, MARS_270, MARS_300,
    MERCURY, MERCURY_0, MERCURY_60, MERCURY_90, MERCURY_120, MERCURY_180, MERCURY_240, MERCURY_270, MERCURY_300,
    VENUS, VENUS_0, VENUS_60, VENUS_90, VENUS_120, VENUS_180, VENUS_240, VENUS_270, VENUS_300,
    JUPITER, JUPITER_0, JUPITER_60, JUPITER_90, JUPITER_120, JUPITER_180, JUPITER_240, JUPITER_270, JUPITER_300,
    SATURN, SATURN_0, SATURN_60, SATURN_90, SATURN_120, SATURN_180, SATURN_240, SATURN_270, SATURN_300, 
    URANUS, URANUS_0, URANUS_60, URANUS_90, URANUS_120, URANUS_180, URANUS_240, URANUS_270, URANUS_300,
    NEPTUNE, NEPTUNE_0, NEPTUNE_60, NEPTUNE_90, NEPTUNE_120, NEPTUNE_180, NEPTUNE_240, NEPTUNE_270, NEPTUNE_300,
    PLUTO, PLUTO_0, PLUTO_60, PLUTO_90, PLUTO_120, PLUTO_180, PLUTO_240, PLUTO_270, PLUTO_300};
    
  static PlanetInfo p = null;
  static TimeZone gmtTz = TimeZone.getTimeZone("GMT");
  private static GregorianCalendar cal = new GregorianCalendar();
  
  public final static String SHOW_TOP = "showTop";
  public final static String SHOW_BOTTOM = "showBottom";
  public final static String INTERVAL_60 = "interval60";
  public final static String INTERVAL_90 = "interval90";

  public final static String MERCURY_COLOR = "mercuryColor";
  public final static String VENUS_COLOR = "venusColor";
  public final static String MARS_COLOR = "marsColor";
  public final static String JUPITER_COLOR = "jupiterColor";
  public final static String SATURN_COLOR = "saturnColor";
  public final static String URANUS_COLOR = "uranusColor";
  public final static String NEPTUNE_COLOR = "neptuneColor";
  public final static String PLUTO_COLOR = "plutoColor";
  
  public final static String SHOW_MERCURY = "showMercury";
  public final static String SHOW_VENUS = "showVenus";
  public final static String SHOW_MARS = "showMars";
  public final static String SHOW_JUPITER = "showJupiter";
  public final static String SHOW_SATURN = "showSaturn";
  public final static String SHOW_URANUS = "showUranus";
  public final static String SHOW_NEPTUNE = "showNeptune";
  public final static String SHOW_PLUTO = "showPluto";  
  public final static String DEGREE_COLOR = "degreeColor";

  public final static String POSITION = "position";

  public final static String LINES = "lines";
  static Color marsColor = null, venusColor = null, mercuryColor = null, jupiterColor = null, 
      saturnColor = null, uranusColor = null, neptuneColor = null, plutoColor = null, degreeColor = null, lColor = null;
  
  static boolean showMars = false, showMercury = false, showVenus = false, showJupiter = false,
      showSaturn = false, showUranus = false, showNeptune = false, showPluto = false,
      showTime = false, showTop = false, showBottom = false,
      show60 = false, show90 = false;
  
  static PathInfo lPath = null;
  static int maxYear = 0;
  
  @Override
  public void initialize(Defaults defaults) 
  {
    var sd = createSD();
    var tab = sd.addTab(get("INPUTS1"));

    var grp = tab.addGroup(get("INPUTS"));
    grp.addRow(new BooleanDescriptor(SHOW_MERCURY, get("MERCURY"), true), new ColorDescriptor(MERCURY_COLOR, "", X11Colors.PURPLE));
    grp.addRow(new BooleanDescriptor(SHOW_VENUS, get("VENUS"), true), new ColorDescriptor(VENUS_COLOR, "", X11Colors.BLUE));
    grp.addRow(new BooleanDescriptor(SHOW_MARS, get("MARS"), true), new ColorDescriptor(MARS_COLOR, "", X11Colors.RED));
    grp.addRow(new BooleanDescriptor(SHOW_JUPITER, get("JUPITER"), true), new ColorDescriptor(JUPITER_COLOR, "", X11Colors.AMETHYST));
    grp.addRow(new BooleanDescriptor(SHOW_SATURN, get("SATURN"), true), new ColorDescriptor(SATURN_COLOR, "", X11Colors.DARK_SLATE_GRAY));
    grp.addRow(new BooleanDescriptor(SHOW_URANUS, get("URANUS"), true), new ColorDescriptor(URANUS_COLOR, "", X11Colors.CHARTREUSE));
    grp.addRow(new BooleanDescriptor(SHOW_NEPTUNE, get("NEPTUNE"), true), new ColorDescriptor(NEPTUNE_COLOR, "", X11Colors.GREEN));
    grp.addRow(new BooleanDescriptor(SHOW_PLUTO, get("PLUTO"), true), new ColorDescriptor(PLUTO_COLOR, "", X11Colors.TOMATO));
    
    sd.addDependency(new EnabledDependency(SHOW_MERCURY, MERCURY_COLOR));
    sd.addDependency(new EnabledDependency(SHOW_VENUS, VENUS_COLOR));
    sd.addDependency(new EnabledDependency(SHOW_MARS, MARS_COLOR));
    sd.addDependency(new EnabledDependency(SHOW_JUPITER, JUPITER_COLOR));
    sd.addDependency(new EnabledDependency(SHOW_SATURN, SATURN_COLOR));
    sd.addDependency(new EnabledDependency(SHOW_URANUS, URANUS_COLOR));
    sd.addDependency(new EnabledDependency(SHOW_NEPTUNE, NEPTUNE_COLOR));
    sd.addDependency(new EnabledDependency(SHOW_PLUTO, PLUTO_COLOR));
    
    tab = sd.addTab(get("INPUTS2"));
 
    grp = tab.addGroup(get("INPUTS"));
    grp.addRow(new ColorDescriptor(DEGREE_COLOR, get("DEGREE_COLOR"), X11Colors.IVORY));
    grp.addRow(new PathDescriptor(LINES, get("LINES"), defaults.getLineColor(), 1.0f, new float[] {3.0f, 3.0f}, true, false, true));
    var disp = new ArrayList<NVP>();
    disp.add(new NVP("Both", "Both"));
    disp.add(new NVP("Top", "Top"));
    disp.add(new NVP("Bottom", "Bottom"));
    grp.addRow(new DiscreteDescriptor(POSITION, get("POSITION"),"Both", disp));
    grp.addRow(new BooleanDescriptor(INTERVAL_60, get("SHOW_60D_INTERVALS"), false));
    grp.addRow(new BooleanDescriptor(INTERVAL_90, get("SHOW_90D_INTERVALS"), false));

    var desc = createRD();
    desc.setBottomInsetPixels(10);
    desc.setTopInsetPixels(10);
  }
  
  @Override
  protected void calculateValues(DataContext ctx) 
  {
    //t = this;
    PlanetInfo.pList.clear();
    DataSeries series = ctx.getDataSeries();
    Calendar c = Calendar.getInstance();
    int futureYrs = 2;
    maxYear = c.get(Calendar.YEAR) + futureYrs;
    
    mercuryColor = getSettings().getColor(MERCURY_COLOR);
    venusColor = getSettings().getColor(VENUS_COLOR);
    marsColor = getSettings().getColor(MARS_COLOR);
    jupiterColor = getSettings().getColor(JUPITER_COLOR);
    saturnColor = getSettings().getColor(SATURN_COLOR);
    uranusColor = getSettings().getColor(URANUS_COLOR);
    neptuneColor = getSettings().getColor(NEPTUNE_COLOR);
    plutoColor = getSettings().getColor(PLUTO_COLOR);
    
    showMercury = getSettings().getBoolean(SHOW_MERCURY);
    showVenus = getSettings().getBoolean(SHOW_VENUS);
    showMars = getSettings().getBoolean(SHOW_MARS);
    showJupiter = getSettings().getBoolean(SHOW_JUPITER);
    showSaturn = getSettings().getBoolean(SHOW_SATURN);
    showUranus = getSettings().getBoolean(SHOW_URANUS);
    showNeptune = getSettings().getBoolean(SHOW_NEPTUNE);
    showPluto = getSettings().getBoolean(SHOW_PLUTO);
    
    show60 = getSettings().getBoolean(INTERVAL_60);
    show90 = getSettings().getBoolean(INTERVAL_90);
    degreeColor = getSettings().getColor(DEGREE_COLOR);

    String pos = getSettings().getString(POSITION);
    showTop = pos.equals("Both") || pos.equals("Top");
    showBottom = pos.equals("Both") || pos.equals("Bottom");

    lPath = getSettings().getPath(LINES);
    lColor = lPath.getColor();
    cal.setTimeZone(gmtTz);
    if(showMercury)  PlanetInfo.registerMercury(1, 11, 2013, 20, 0);  //date of 1 recent inferior conjunction
    if(showVenus) PlanetInfo.registerVenus(6, 6, 2012, 1, 0); //date of 1 recent inferior conjunction  
    if(showMars) PlanetInfo.registerMars(18, 4, 2013, 0, 0); //date of 1 recent conjunction
    if(showJupiter) PlanetInfo.registerJupiter(19, 6, 2013, 15, 0); //date of 1 recent conjunction
    if(showSaturn) PlanetInfo.registerSaturn(6, 11, 2013, 11, 0); //date of 1 recent conjunction
    if(showUranus) PlanetInfo.registerUranus(29, 3, 2013, 0, 0); //date of 1 recent conjunction
    if(showNeptune) PlanetInfo.registerNeptune(21, 2, 2013, 7, 0); //date of 1 recent conjunction 
    if(showPluto) PlanetInfo.registerPluto(30, 12, 2012, 0, 0); //date of 1 recent conjunction 

    long start = series.getStartTime(0);
    int tIndex = series.size()-1;
    long end = series.getStartTime(tIndex) + (futureYrs * 366 * millSecPerDay);
    clearFigures();
    for(PlanetInfo pi : PlanetInfo.pList) {
      if(pi.time < start || pi.time > end) continue;
      addFigure(pi);
    }
  }

  private static class PlanetInfo extends Figure {
    static List<PlanetInfo> pList = new ArrayList<>();
    long time = 0;
    Planet name = null;
    Planet shortName = null;
    String phaseTxt = "";
    Line2D line;
    Text topLbl;
    Text bottomLbl;
    //Constructor
    PlanetInfo(long time1, Planet sName, Planet name1) {
      name = name1;
      String s = name.toString();
      phaseTxt = s.substring(s.indexOf("_")+1) + "\u00B0";  //degrees + degree symbol
      shortName = sName;
      time = time1;
      pList.add(this);  //record each instance
    }
    
    @Override
    public boolean isVisible(DrawContext ctx) {
      DataSeries series = ctx.getDataContext().getDataSeries();
      if (time < series.getVisibleStartTime()) return false;
      if (series.isLatestData()) return true;
      return true;  //time > series.getVisibleEndTime();
    }
    @Override
    //used to make lines respond to mouse click 
    public boolean contains(double x, double y, DrawContext ctx)
    {
      if (!isVisible(ctx)) return false;
      for(PlanetInfo planet : pList) {
        if (planet.line == null) continue;
         if (Util.distanceFromLine(x, y, planet.line) < 6) return true;
      }
      return false;
    }

    
    @Override 
    public synchronized void layout(DrawContext ctx)
    {
      SimpleDateFormat fmt = new SimpleDateFormat("MMM:dd:hh:mm");  

      Rectangle bounds = ctx.getBounds();
      Point2D p1 = ctx.translate(time, 0);
      if (p1.getX() > bounds.getMaxX()) {
        line = null;
        return;
      }

      line = new Line2D.Double(p1.getX(), bounds.y, p1.getX(), bounds.getMaxY());
      topLbl = new Text(fmt.format(time), timeFont, new Insets(1, 3, 1, 1), false);
      topLbl.setLocation(p1.getX() + 10, bounds.getY() + 2);
      bottomLbl = new Text(fmt.format(time), timeFont, new Insets(1, 3, 1, 1), false);
      bottomLbl.setLocation(p1.getX() + 10, bounds.getMaxY() - bottomLbl.getHeight()-2);
    }

    @Override
    public void draw(Graphics2D gc, DrawContext ctx) {
      Rectangle bounds = ctx.getBounds();
      if (line == null || line.getX1() > bounds.getMaxX()) return;
     
      int x1 = 0, y1 = 0;

      if (lPath.isEnabled()) {
        gc.setStroke(lPath.getStroke());
        gc.setColor(lColor);
        gc.draw(line);
      }
      if (showBottom) {
        x1 = (int)line.getX1() - 8;
        y1 = (int)bounds.getMaxY() -28; 
        if(showMars && shortName.equals(Planet.MARS)) drawMars(gc, x1, y1, phaseTxt);
        if(showMercury && shortName.equals(Planet.MERCURY)) drawMercury(gc, x1, y1, phaseTxt);
        if(showVenus && shortName.equals(Planet.VENUS)) drawVenus(gc, x1, y1, phaseTxt);
        if(showJupiter && shortName.equals(Planet.JUPITER)) drawJupiter(gc, x1, y1, phaseTxt);
        if(showSaturn && shortName.equals(Planet.SATURN)) drawSaturn(gc, x1, y1, phaseTxt);
        if(showUranus && shortName.equals(Planet.URANUS)) drawUranus(gc, x1, y1, phaseTxt);
        if(showNeptune && shortName.equals(Planet.NEPTUNE)) drawNeptune(gc, x1, y1, phaseTxt);
        if(showPluto && shortName.equals(Planet.PLUTO)) drawPluto(gc, x1, y1, phaseTxt);
        bottomLbl.draw(gc);
      }
      if (showTop) {
        x1 = (int)line.getX1()-8;
        y1 = (int)bounds.getY()+6;
        if(showMars && shortName.equals(Planet.MARS)) drawMars(gc, x1, y1, phaseTxt);
        if(showMercury && shortName.equals(Planet.MERCURY)) drawMercury(gc, x1, y1, phaseTxt);
        if(showVenus && shortName.equals(Planet.VENUS)) drawVenus(gc, x1, y1, phaseTxt);
        if(showJupiter && shortName.equals(Planet.JUPITER)) drawJupiter(gc, x1, y1, phaseTxt);
        if(showSaturn && shortName.equals(Planet.SATURN)) drawSaturn(gc, x1, y1, phaseTxt);
        if(showUranus && shortName.equals(Planet.URANUS)) drawUranus(gc, x1, y1, phaseTxt);
        if(showNeptune && shortName.equals(Planet.NEPTUNE)) drawNeptune(gc, x1, y1, phaseTxt);
        if(showPluto && shortName.equals(Planet.PLUTO)) drawPluto(gc, x1, y1, phaseTxt);
        topLbl.draw(gc);
      }
    }
    
    private void drawPluto(Graphics2D gc, int x1, int y1, String s) {
      gc.setColor(plutoColor);
      gc.fillRoundRect(x1, y1+4, 22, 10, 4, 4);
           
      gc.setFont(new Font("", Font.BOLD, 36));
      gc.drawString("\u2647", x1-9, y1+22);  // symbol
 
      int offSet = 0;
      if(s.length() == 1) offSet = -6;
      if(s.length() == 2) offSet = -4;
      if(s.length() == 3) offSet = 0;
          
      x1 = x1 - offSet;
      y1 = y1 + 13; 
      gc.setFont(new Font("Arial", Font.PLAIN, 10));
      gc.setColor(degreeColor);
      gc.drawString(s, x1, y1);
    }
    
    private void drawNeptune(Graphics2D gc, int x1, int y1, String s) {
      gc.setColor(neptuneColor);
      gc.fillRoundRect(x1, y1+4, 22, 10, 4, 4);
           
      gc.setFont(new Font("", Font.BOLD, 36));
      gc.drawString("\u2646", x1-9, y1+20);  // symbol
 
      int offSet = 0;
      if(s.length() == 1) offSet = -6;
      if(s.length() == 2) offSet = -4;
      if(s.length() == 3) offSet = 0;
          
      x1 = x1 - offSet;
      y1 = y1 + 13;  //+ 12;
      gc.setFont(new Font("Arial", Font.PLAIN, 10));
      gc.setColor(degreeColor);
      gc.drawString(s, x1, y1);
    }
    
    private void drawUranus(Graphics2D gc, int x1, int y1, String s) {
      gc.setColor(uranusColor);
      gc.fillRoundRect(x1, y1+4, 22, 10, 4, 4);
           
      gc.setFont(new Font("", Font.BOLD, 36));
      gc.drawString("\u2645", x1-9, y1+18);  // symbol
 
      int offSet = 0;
      if(s.length() == 1) offSet = -6;
      if(s.length() == 2) offSet = -4;
      if(s.length() == 3) offSet = 0;
          
      x1 = x1 - offSet;
      y1 = y1 + 12;
      gc.setFont(new Font("Arial", Font.PLAIN, 10));
      gc.setColor(degreeColor);
      gc.drawString(s, x1, y1);
    } 
    
    private void drawSaturn(Graphics2D gc, int x1, int y1, String s) {
      gc.setColor(saturnColor);
      gc.fillRoundRect(x1, y1+4, 22, 10, 4, 4);
           
      gc.setFont(new Font("", Font.BOLD, 36));
      gc.drawString("\u2644", x1-9, y1+18);  // saturn symbol
 
      int offSet = 0;
      if(s.length() == 1) offSet = -6;
      if(s.length() == 2) offSet = -4;
      if(s.length() == 3) offSet = 0;
          
      x1 = x1 - offSet;
      y1 = y1 + 13;
      gc.setFont(new Font("Arial", Font.PLAIN, 10));
      gc.setColor(degreeColor);
      gc.drawString(s, x1, y1);
    }

    private void drawJupiter(Graphics2D gc, int x1, int y1, String s) {
      gc.setColor(jupiterColor);
      gc.fillRoundRect(x1, y1+4, 22, 10, 4, 4);
           
      gc.setFont(new Font("", Font.BOLD, 36));
      gc.drawString("\u2643", x1-10, y1+18);  //jupiter symbol
 
      int offSet = 0;
      if(s.length() == 1) offSet = -6;
      if(s.length() == 2) offSet = -4;
      if(s.length() == 3) offSet = 0;
          
      x1 = x1 - offSet;
      y1 = y1 + 13;
      gc.setFont(new Font("Arial", Font.PLAIN, 10));
      gc.setColor(degreeColor);
      gc.drawString(s, x1, y1);
    }

    private void drawMars(Graphics2D gc, int x1, int y1, String s) {
      gc.setColor(marsColor);
      gc.setFont(new Font("", Font.BOLD, 32));
      gc.drawString("\u2642", x1-4, y1+18);  //mars symbol
      gc.fillOval(x1, y1, 18, 18);
 
      int offSet = 0;
      if(s.length() == 1) offSet = -6;
      if(s.length() == 2) offSet = -4;
      if(s.length() == 3) offSet = 0;
          
      x1 = x1 - offSet;
      y1 = y1 + 13;
      gc.setFont(new Font("Arial", Font.PLAIN, 10));
      gc.setColor(degreeColor);
      gc.drawString(s, x1, y1);
    }

    private void drawVenus(Graphics2D gc, int x1, int y1, String s) {
      gc.setColor(venusColor);
      gc.setFont(new Font("", Font.BOLD, 32));
      gc.drawString("\u2640", x1-6, y1+24);  //venus symbol 
      gc.fillOval(x1, y1, 18, 18);
      int offSet = 0;
      if(s.length() == 1) offSet = -6;
      if(s.length() == 2) offSet = -4;
      if(s.length() == 3) offSet = 0;
         
      x1 = x1 - offSet;
      y1 = y1 + 13;
      gc.setFont(new Font("Arial", Font.PLAIN, 10));
      gc.setColor(degreeColor);
      gc.drawString(s, x1, y1);
    }
    
     private void drawMercury(Graphics2D gc, int x1, int y1, String s) {
       gc.setColor(mercuryColor);
       gc.setFont(new Font("", Font.BOLD, 50));
       gc.drawString("\u263F", x1-18, y1+28);  //mercury symbol
       gc.fillOval(x1, y1, 18, 18);
     
       int offSet = 0;
       if(s.length() == 1) offSet = -6;
       if(s.length() == 2) offSet = -4;
       if(s.length() == 3) offSet = 0;
          
       x1 = x1 - offSet;
       y1 = y1 + 13;
       gc.setFont(new Font("Arial", Font.PLAIN, 10));
       gc.setColor(degreeColor);
       gc.drawString(s, x1, y1);
    }

    private static void registerMercury(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, mercurySP);
      int yr = year;
      while (yr <= maxYear){
        p = new PlanetInfo(time, Planet.MERCURY, Planet.MERCURY_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * mercurySP), Planet.MERCURY, Planet.MERCURY_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * mercurySP), Planet.MERCURY, Planet.MERCURY_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * mercurySP), Planet.MERCURY, Planet.MERCURY_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * mercurySP), Planet.MERCURY, Planet.MERCURY_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * mercurySP), Planet.MERCURY, Planet.MERCURY_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * mercurySP), Planet.MERCURY, Planet.MERCURY_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * mercurySP), Planet.MERCURY, Planet.MERCURY_300);
        time = time + mercurySP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }
    
    private static void registerVenus(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, venusSP);
      int yr = year;
      while (yr <= maxYear){
        p = new PlanetInfo(time, Planet.VENUS, Planet.VENUS_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * venusSP), Planet.VENUS, Planet.VENUS_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * venusSP), Planet.VENUS, Planet.VENUS_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * venusSP), Planet.VENUS, Planet.VENUS_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * venusSP), Planet.VENUS, Planet.VENUS_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * venusSP), Planet.VENUS, Planet.VENUS_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * venusSP), Planet.VENUS, Planet.VENUS_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * venusSP), Planet.VENUS, Planet.VENUS_300);
        time = time + venusSP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }
    
    private static void registerMars(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, marsSP);
      int yr = year;
      while (yr <= (maxYear + 1)){
        p = new PlanetInfo(time, Planet.MARS, Planet.MARS_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * marsSP), Planet.MARS, Planet.MARS_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * marsSP), Planet.MARS, Planet.MARS_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * marsSP), Planet.MARS, Planet.MARS_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * marsSP), Planet.MARS, Planet.MARS_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * marsSP), Planet.MARS, Planet.MARS_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * marsSP), Planet.MARS, Planet.MARS_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * marsSP), Planet.MARS, Planet.MARS_300);
        time = time + marsSP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }
    
    private static void registerJupiter(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, jupiterSP);
      int yr = year;
      while (yr <= maxYear){
        p = new PlanetInfo(time, Planet.JUPITER, Planet.JUPITER_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * jupiterSP), Planet.JUPITER, Planet.JUPITER_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * jupiterSP), Planet.JUPITER, Planet.JUPITER_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * jupiterSP), Planet.JUPITER, Planet.JUPITER_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * jupiterSP), Planet.JUPITER, Planet.JUPITER_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * jupiterSP), Planet.JUPITER, Planet.JUPITER_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * jupiterSP), Planet.JUPITER, Planet.JUPITER_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * jupiterSP), Planet.JUPITER, Planet.JUPITER_300);
        time = time + jupiterSP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }
    private static void registerSaturn(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, saturnSP);
      int yr = year;
      while (yr <= maxYear){
        p = new PlanetInfo(time, Planet.SATURN, Planet.SATURN_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * saturnSP), Planet.SATURN, Planet.SATURN_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * saturnSP), Planet.SATURN, Planet.SATURN_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * saturnSP), Planet.SATURN, Planet.SATURN_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * saturnSP), Planet.SATURN, Planet.SATURN_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * saturnSP), Planet.SATURN, Planet.SATURN_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * saturnSP), Planet.SATURN, Planet.SATURN_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * saturnSP), Planet.SATURN, Planet.SATURN_300);
        time = time + saturnSP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }    

    private static void registerUranus(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, uranusSP);
      int yr = year;
      while (yr <= maxYear){
        p = new PlanetInfo(time, Planet.URANUS, Planet.URANUS_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * uranusSP), Planet.URANUS, Planet.URANUS_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * uranusSP), Planet.URANUS, Planet.URANUS_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * uranusSP), Planet.URANUS, Planet.URANUS_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * uranusSP), Planet.URANUS, Planet.URANUS_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * uranusSP), Planet.URANUS, Planet.URANUS_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * uranusSP), Planet.URANUS, Planet.URANUS_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * uranusSP), Planet.URANUS, Planet.URANUS_300);
        time = time + uranusSP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }    
    
    private static void registerNeptune(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, neptuneSP);
      int yr = year;
      while (yr <= maxYear){
        p = new PlanetInfo(time, Planet.NEPTUNE, Planet.NEPTUNE_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * neptuneSP), Planet.NEPTUNE, Planet.NEPTUNE_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * neptuneSP), Planet.NEPTUNE, Planet.NEPTUNE_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * neptuneSP), Planet.NEPTUNE, Planet.NEPTUNE_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * neptuneSP), Planet.NEPTUNE, Planet.NEPTUNE_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * neptuneSP), Planet.NEPTUNE, Planet.NEPTUNE_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * neptuneSP), Planet.NEPTUNE, Planet.NEPTUNE_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * neptuneSP), Planet.NEPTUNE, Planet.NEPTUNE_300);
        time = time + neptuneSP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }

    private static void registerPluto(int day, int month, int year, int hour, int minute) {
      long time = conjStartTime(day, month, year, hour, minute, plutoSP);
      int yr = year;
      while (yr <= maxYear){
        p = new PlanetInfo(time, Planet.PLUTO, Planet.PLUTO_0);
        if(show60) p = new PlanetInfo(time + (long)((60/360.0) * plutoSP), Planet.PLUTO, Planet.PLUTO_60);
        if(show90) p = new PlanetInfo(time + (long)((90/360.0) * plutoSP), Planet.PLUTO, Planet.PLUTO_90);
        if(show60) p = new PlanetInfo(time + (long)((120/360.0) * plutoSP), Planet.PLUTO, Planet.PLUTO_120);
        if(show90 || show60) p = new PlanetInfo(time + (long)((180/360.0) * plutoSP), Planet.PLUTO, Planet.PLUTO_180);
        if(show60) p = new PlanetInfo(time + (long)((240/360.0) * plutoSP), Planet.PLUTO, Planet.PLUTO_240);
        if(show90) p = new PlanetInfo(time + (long)((270/360.0) * plutoSP), Planet.PLUTO, Planet.PLUTO_270);
        if(show60) p = new PlanetInfo(time + (long)((300/360.0) * plutoSP), Planet.PLUTO, Planet.PLUTO_300);
        time = time + plutoSP;
        cal.clear();
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
    }
    //Get early (before year 2000) Conjunction time 
    //The input date may be any conjunction date after the year 2000;
    //for Venus and Mercury use inferior conjunction date
    static long conjStartTime(int day, int month, int yr, int hour, int minute, long synodicPeriod) {
      long time = toGMTDate(day, month-1, yr, hour, minute);
      while (yr >= 2000){
        time = time - synodicPeriod; 
        cal.clear();
        cal.setTimeZone(gmtTz);
        cal.setTimeInMillis(time);
        yr = cal.get(Calendar.YEAR);
      }
      return time;
    }
   
    public static long toGMTDate(int day, int month, int year, int hour, int min) {
      cal.clear();
      cal.setTimeZone(gmtTz);
      cal.set(Calendar.MONTH, month);
      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.DAY_OF_MONTH, day);
      cal.set(Calendar.HOUR_OF_DAY, hour);
      cal.set(Calendar.MINUTE, min);
      return cal.getTimeInMillis();
    }   
  }
}