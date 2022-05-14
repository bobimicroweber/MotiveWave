package com.motivewave.platform.study.overlay;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.FontInfo;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Text;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** StockMarketExchanges 171 */
@StudyHeader(
  namespace="com.motivewave",
  id="STOCK_MARKET_EXCHANGES",
  rb="com.motivewave.platform.study.nls.strings2",
  name="NAME_STOCK_MARKET_EXCHANGES",
  label="LBL_STMKEX",
  desc="DESC_STMKEX",
  menu="MENU_OVERLAYS",
  requiresVolume=false,
  signals=false,
  overlay=true,
  studyOverlay=true)
public class StockMarketExchanges extends Study
{
  final static String SHOW_STATE = "showState", SHOW_CALL_LETTERS = "showCall";
  
  private StockEx exArray[] = new StockEx[45];
  private String[] ex = new String[exArray.length];
  
  @Override
  public void initialize(Defaults defaults)
  {
    exArray[0] = new StockEx("00", "New Zealand SM+12", "NZSX", "Pacific/Auckland", 9, 5, "10:00", "17:00");
    exArray[1] = new StockEx("01","Australian SE+10", "ASX", "Australia/Sydney",10, 5, "9:50", "16:12");
    exArray[2] = new StockEx("02", "Tokyo SE+9", "TSE", "Japan", 0, 0, "9:00", "15:00");
    exArray[3] = new StockEx("03", "Korea SE+9", "KRX", "Asia/Soeul", 0, 0, "9:00", "15:00");
    exArray[4] = new StockEx("04", "Bursa Malaysia+8", "MYX", "Asia/Brunei", 0, 0, "9:00", "17:00");
    exArray[5] = new StockEx("05", "Singapore E+8", "SGX", "Asia/Singapore", 0, 0, "9:00", "17:00");
    exArray[6] = new StockEx("06", "Taiwan SE+8", "TSE", "Asia/Taipei", 0, 0, "9:00", "13:30");
    exArray[7] = new StockEx("07", "Hong Kong F+8", "HKFE", "Asia/Hong_Kong", 0, 0, "9:15", "16:00");
    exArray[8] = new StockEx("08", "Hong Kong SE+8", "HKEX", "Asia/Hong_Kong", 0, 0, "9:00", "16:00");
    exArray[9] = new StockEx("09", "Shanghai SE+8", "SSE", "Asia/Shanghai", 0, 0, "9:30", "15:00");
    exArray[10] = new StockEx("10", "Shenzhen SE+8", "SZSE", "Asia/Shanghia", 0, 0, "9:00", "15:00");
    exArray[11] = new StockEx("11", "Philippine SE+8", "PSE", "Asia/Manila", 0, 0, "9:30", "15:30");
    exArray[12] = new StockEx("12", "Indonesia SE+7", "IDX", "Asia/Bangkok", 0, 0, "9:30", "16:00");
    exArray[13] = new StockEx("13", "Thialand+7", "SET","Asia/Bangkok", 0, 0, "10:00", "16:30");
    exArray[14] = new StockEx("14", "Bombay+5.5", "BSE", "Asia/Colombo", 0, 0, "9:15", "15:30");
    exArray[15] = new StockEx("15", "India+5.5", "NSE","Asia/Kolkata", 0, 0, "9:15", "15:30");
    exArray[16] = new StockEx("16", "Colombo SE+5.5", "CSE", "Asia/Colombo", 0, 0, "9:30", "14:30");
    exArray[17] = new StockEx("17", "Moscow ICE+4", "MICEX", "Europe/Moscow", 0, 0, "10:00", "19:00");
    exArray[18] = new StockEx("18", "Saudi SE+3", "TADAWUL", "Asia/Riyadh", 0, 0, "11:00", "15:30");
    exArray[19] = new StockEx("19", "Johannesburg SE+2", "JSE", "Africa/Johannesburg", 0, 0, "9:00", "17:00");
    exArray[20] = new StockEx("20", "Frankfurt SE+1", "FSX", "Europe/Berlin", 3, 10, "8:00", "22:00");
    exArray[21] = new StockEx("21","Istanbul SE+2", "ITSE", "Asia/Istanbul", 3, 10, "9:30", "17:30");
    exArray[22] = new StockEx("22", "Wiener Borse+1", "AG", "Europe/Vienna", 3, 10, "8:55", "17:35");
    exArray[23] = new StockEx("23", "Ukrainian E+2", "UX", "EET", 0, 0, "10:00", "17:30");
    exArray[24] = new StockEx("24", "Amman SE+2", "ASE", "Asia/Amman", 3, 10, "10:00", "12:00");
    exArray[25] = new StockEx("25", "Euronext Paris+1", "EPA", "Europe/Paris", 3, 10, "9:00", "17:30");
    exArray[26] = new StockEx("26", "Swiss E+1", "SIX", "Europe/Amsterdam", 3, 10, "9:00", "17:30");
    exArray[27] = new StockEx("27", "Berne E+1", "BX", "Europe/Berlin", 3, 10, "9:00", "16:30");
    exArray[28] = new StockEx("28", "Spanish SE+1", "BME", "Europe/Madrid", 3, 10, "9:00", "17:30");
    exArray[29] = new StockEx("29", "Milan SE+1", "MTA", "Europe/Rome", 3, 10, "9:00", "17:25");
    exArray[30] = new StockEx("30", "Euronext Amsterdam+1", "AMS", "Europe/Amsterdam", 3, 10, "9:00", "17:40");
    exArray[31] = new StockEx("31", "Helsinki SE+2", "OMX", "Europe/Helsinki", 3, 10, "10:00", "18:30");
    exArray[32] = new StockEx("32", "Stockholm SE+1", "OMX", "Europe/Stockholm", 3, 10, "9:00", "17:30");
    exArray[33] = new StockEx("33", "Oslo SE+1", "OSE", "Europe/Oslo", 3, 10, "9:00", "17:30");
    exArray[34] = new StockEx("34", "Copenhagen SE+1", "CSE", "Europe/Copenhagen", 3, 10, "9:00", "17:00");
    exArray[35] = new StockEx("35", "Riga SE+2", "OMXR", "Europe/Riga", 3, 10, "10:00", "16:00");
    exArray[36] = new StockEx("36", "Warsaw SE+1", "GPW", "Europe/Warsaw", 3, 10, "9:00", "17:30");
    exArray[37] = new StockEx("37", "Nigerian SE+1", "NSE", "Africa/Logos", 0, 0, "10:00", "16:00");
    exArray[38] = new StockEx("38", "London SE+0", "LSE", "Europe/London", 3, 10, "8:00", "16:30");
    exArray[39] = new StockEx("39", "Irish SE+0", "ISE", "Europe/London", 3, 10, "8:00", "16:30");
    exArray[40] = new StockEx("40", "Bolsa de Valores-3", "Bovespa", "America/Argentina/Buenos_Aires", 10, 2, "10:00", "17:00");
    exArray[41] = new StockEx("41", "New York SE-5", "NYSE", "America/New_York", 3, 11, "9:30", "16:00");
    exArray[42] = new StockEx("42", "NASDAQ-5", "NASDAQ", "America/New_York", 3, 11, "9:30", "16:00");
    exArray[43] = new StockEx("43", "Toronto SE-5", "TSX", "America/Toronto", 3, 11, "9:30", "16:00");
    exArray[44] = new StockEx("44", "Mexican SE-6", "BMV", "America/Mexico_City", 4, 10, "8:30", "15:00");
//    exArray[45] = new StockEx("45", "Hypothetical GMT+11", "HYP", "Etc/GMT+11", 4, 10, "10:00", "17:00");
     
    //build array ex for user selection
    for (int i = 0; i < exArray.length; i++ ){
      ex[i] = exArray[i].name + " " + exArray[i].letters + " " + exArray[i].recNumber;
    }
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));

    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_EXCHANGE"), ex, ex[0]));
    inputs.addRow(new BooleanDescriptor(SHOW_STATE, get("LBL_SHOW_STATE"), true));
    inputs.addRow(new BooleanDescriptor(SHOW_CALL_LETTERS, get("LBL_SHOW_CALL_LETTERS"), true));
    inputs.addRow(new FontDescriptor(Inputs.FONT, get("FONT"), defaults.getFont()));

    var settings = tab.addGroup(get("LINE"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("Open"), defaults.getGreen(), 1.0f, new float[] {3.0f, 3.0f}, true, false, true));
    settings.addRow(new PathDescriptor(Inputs.PATH2, get("Close"), defaults.getRed(), 1.0f, new float[] {3.0f, 3.0f}, true, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, SHOW_STATE, SHOW_CALL_LETTERS);
    sd.addQuickSettings(Inputs.PATH, Inputs.PATH2);

    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, SHOW_STATE, SHOW_CALL_LETTERS);
    setMinBars(20);
  }
  
  @Override  
  public void onBarUpdate(DataContext ctx){
    calculateValues(ctx);
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    int barMin = 0;
    BarSize bar = series.getBarSize();
    if (bar.getType() == Enums.BarSizeType.LINEAR) barMin=bar.getIntervalMinutes();
    else return;
    double minPerDay = 60*24; //minutes/day
    if (minPerDay/barMin <= 1.0) return;
    
    boolean showState = getSettings().getBoolean(SHOW_STATE);
    boolean showCall = getSettings().getBoolean(SHOW_CALL_LETTERS);
    FontInfo fi = getSettings().getFont(Inputs.FONT);
    Font f = fi.getFont();
    String lett = (String) getSettings().getInput(Inputs.INPUT, ex[0]);
    lett = lett.substring(lett.lastIndexOf(" ") +1); //record number
    int ind = -1;
    //Get user selection index ind 
    for (int i = 0; i < exArray.length; i++ ){
        if (lett.equals(exArray[i].recNumber)){
        ind = i;
        break;
      }
    }
    clearFigures();
    long sDate1 = 0, sDate2 = 0;
    String shLett = exArray[ind].letters;
    TimeZone tz = exArray[ind].timeZone;
    TimeZone defTz = TimeZone.getDefault();
    int openingHr = exArray[ind].hrOpen;    
    int openingMin = exArray[ind].minOpen;
    int closingHr = exArray[ind].hrClose;    
    int closingMin = exArray[ind].minClose;
    String sOpen = exArray[ind].strOpen;
    String sClose = exArray[ind].strClose;

//  String[] id =TimeZone.getAvailableIDs(55*60*60*100);
//  for (int i = 0; i < id.length; i++){info("id[i] " + id[i]);}   
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeZone(tz);
    cal.set(Calendar.HOUR_OF_DAY, openingHr);
    cal.set(Calendar.MINUTE, openingMin);
    cal.set(Calendar.SECOND, 0);
    cal.getTimeInMillis();
    
    int dayInO = cal.get(Calendar.DAY_OF_YEAR);
    cal.setTimeZone(defTz);
    int dayOutO = cal.get(Calendar.DAY_OF_YEAR);
   
    int openHr = cal.get(Calendar.HOUR_OF_DAY);
    //remote stock exchange opens at openMin(minutes of day) in this time zone   
    int openMin = (60*openHr) + cal.get(Calendar.MINUTE); 
    cal.setTimeZone(tz);
    cal.set(Calendar.HOUR_OF_DAY, closingHr);
    cal.set(Calendar.MINUTE, closingMin);
    cal.set(Calendar.SECOND, 0);
    cal.getTimeInMillis();
    
    int dayInC = cal.get(Calendar.DAY_OF_YEAR);
    cal.setTimeZone(defTz);
    int dayOutC = cal.get(Calendar.DAY_OF_YEAR);
    
    int closeHr = cal.get(Calendar.HOUR_OF_DAY);
    //remote stock exchange closes at closeMin(minutes of day) in this time zone  
    int closeMin = (closeHr * 60) + cal.get(Calendar.MINUTE);
   // info("openMin " + openMin + " closeMin " + closeMin);

    GregorianCalendar calBar1 = new GregorianCalendar();
    GregorianCalendar calBar2 = new GregorianCalendar();

    int cHr = 0, cMin = 0, prevHr = 0, prevMin = 0;
    long xtime = 0;
    int endIndex = series.getEndIndex();
    PathInfo path = null;
    for (int i = 1; (i <= endIndex); i++) {
      sDate1 =  series.getStartTime(i);      
      sDate2 =  series.getStartTime(i-1);
      calBar1.setTimeInMillis(sDate1);
      calBar2.setTimeInMillis(sDate2);
           
      cHr = calBar1.get(Calendar.HOUR_OF_DAY);
      cMin = (cHr * 60) + calBar1.get(Calendar.MINUTE); //minute of day for current bar
      prevHr = calBar2.get(Calendar.HOUR_OF_DAY);
      prevMin = (prevHr * 60) + calBar2.get(Calendar.MINUTE);// minute of day for previous bar     

      int day = calBar1.get(Calendar.DAY_OF_WEEK);
      boolean wkDay = (day != Calendar.SUNDAY && day!= Calendar.SATURDAY);
      boolean mondayOpenDC =  (day == Calendar.FRIDAY && dayInO != dayOutO);
      boolean fridayCloseDC =  (day == Calendar.MONDAY && dayInC != dayOutC);
      
      
      if (prevMin < openMin && cMin >= openMin && wkDay && !mondayOpenDC) {
        if (getSettings().getPath(Inputs.PATH).isEnabled()){
          path = getSettings().getPath(Inputs.PATH);
          xtime = series.getStartTime(i);
          addFigure(new StockExFigures(xtime, shLett, path, f, "Open "+ sOpen, showState, showCall));
        }
      }
      if (prevMin < closeMin && cMin >= closeMin && wkDay && !fridayCloseDC) {
        if (getSettings().getPath(Inputs.PATH).isEnabled()){
          path = getSettings().getPath(Inputs.PATH2);
          xtime = series.getStartTime(i);
          addFigure(new StockExFigures(xtime, shLett, path, f, "Close "+ sClose, showState, showCall));         
        }
      }
    }
  }

  private class StockEx
  {
    String recNumber = "";
    String name="";
    String letters="";
    TimeZone timeZone = null;
    String strOpen = "";
    int hrOpen=0;
    int minOpen = 0;
    String strClose = "";
    int hrClose=0;
    int minClose=0;  
    //Called from initialize each instance stores record for a particular stock exchange
    public StockEx(String irecN, String iname, String iletters, String timeZ, int idstStart, int idstEnd,  String iopen, String iclose)
    {
      recNumber = irecN;
      name = iname;
      letters = iletters;
      timeZone = TimeZone.getTimeZone(timeZ);
      strOpen = iopen;
      strClose = iclose;
      String hOpen = iopen.substring(0, iopen.indexOf(":"));
      String mOpen = iopen.substring(iopen.indexOf(":")+1);
      String hClose = iclose.substring(0, iclose.indexOf(":"));
      String mClose = iclose.substring(iclose.indexOf(":")+1);
      hrOpen = Integer.parseInt(hOpen); 
      minOpen = Integer.parseInt(mOpen);
      hrClose = Integer.parseInt(hClose);
      minClose = Integer.parseInt(mClose);
    }
  }

  private static class StockExFigures extends Figure
  {
    long time = 0;
    Line2D line = null;
    String callLetters = "";
    String state = "";
    PathInfo path = null;
    Font font = null;
    Text topLbl = null;
    Text bottomLbl = null;
    boolean showState = false;
    boolean showCall = false;
    
    StockExFigures(long itime, String icallLetters, PathInfo ipath, Font ifont, String istate, boolean ishowState, boolean ishowCall)
    {
      time = itime; 
      callLetters = icallLetters;
      path = ipath;
      font = ifont;
      state = istate;
      showState = ishowState;
      showCall = ishowCall;
    }
    @Override
    public boolean isVisible(DrawContext ctx)
    {
      DataSeries series = ctx.getDataContext().getDataSeries();
      if (series.isLatestData()) return true;
      long startT = series.getVisibleStartTime();
      long endT = series.getVisibleEndTime();
      return (time >= startT && time <= endT);
    }    
    
    @Override
    public void layout(DrawContext ctx)
    {

      Point2D p = ctx.translate(time, 0);
      Rectangle bounds = ctx.getBounds();
      if (p.getX() > bounds.getMaxX()) {
        line = null;
        return;
      }
      
      line = new Line2D.Double(p.getX(), bounds.y, p.getX(), bounds.getMaxY());
      topLbl = new Text(state, font, new Insets(0, 0, 0, 0), true);
      topLbl.setLocation(p.getX(), bounds.getY() + 2);

      bottomLbl = new Text(callLetters, font, new Insets(0, 0, 0, 0), true);
      bottomLbl.setLocation(p.getX(), bounds.getMaxY() - bottomLbl.getHeight()-2);
    }
    
    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      gc.setStroke(path.getStroke());
      gc.setColor(path.getColor());
      gc.draw(line);
      if (showState) topLbl.draw(gc);
      if (showCall) bottomLbl.draw(gc);
    }
  }
}
