package com.motivewave.platform.study.ma;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.Tick;
import com.motivewave.platform.sdk.common.TickOperation;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.common.menu.MenuDescriptor;
import com.motivewave.platform.sdk.common.menu.MenuItem;
import com.motivewave.platform.sdk.common.menu.MenuSeparator;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;
import com.motivewave.platform.ui.draw.component.study.DataSeriesImpl;

/** Volume Weighted Average Price */
@StudyHeader(
    namespace="com.motivewave", 
    id="VWAP", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_VWAP", 
    desc="DESC_VWAP",
    label="LBL_VWAP",
    menu="MENU_VOLUME",
    overlay=true, 
    requiresVolume=true,
    requiresBarUpdates=true,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/volume_weighted_average_price.htm")
public class VWAP extends Study 
{
  enum Values { VWAP, TOP1, BOTTOM1, TOP2, BOTTOM2, TOP3, BOTTOM3 }

  final static String USE_HISTORICAL_BARS="useBars", MAX_BARS="maxBars", SHOW_ALL="showAll", USE_SERIES_BS="useSeriesBS",
      BAND_STD1="bandStd1", PATH1="topPath1", TOP_IND1="topInd1", BOTTOM_IND1="bottomInd1", FILL_COLOR1="fillColor1",
      BAND_STD2="bandStd2", PATH2="topPath2", TOP_IND2="topInd2", BOTTOM_IND2="bottomInd2", FILL_COLOR2="fillColor2",
      BAND_STD3="bandStd3", PATH3="topPath3", TOP_IND3="topInd3", BOTTOM_IND3="bottomInd3", FILL_COLOR3="fillColor3", RTH="rth";
  final static String ANCHOR="anchor";
  final static String VAL_RTH="R", VAL_EXT="E", VAL_CHART="C";
 
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    List<NVP> rthOptions = new ArrayList();
    rthOptions.add(new NVP(get("LBL_RTH"), VAL_RTH));
    rthOptions.add(new NVP(get("LBL_EXT"), VAL_EXT));
    rthOptions.add(new NVP(get("LBL_CHART"), VAL_CHART));

    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup("", false);
    // 2021/06/02 - Preparing to remove this options since they are not very useful
    sd.addInvisibleSetting(new IntegerDescriptor(MAX_BARS, get("LBL_MAX_BARS"), 100, 1, 9999, 1));
    sd.addInvisibleSetting(new BooleanDescriptor(SHOW_ALL, get("LBL_SHOW_ALL"), true, false));
    grp.addRow(new DiscreteDescriptor(RTH, get("LBL_RTH_DATA"), VAL_CHART, rthOptions));
    grp.addRow(new BooleanDescriptor(USE_HISTORICAL_BARS, get("LBL_USE_HISTORICAL_BARS"), true, false));
    grp.addRow(new BarSizeDescriptor(Inputs.BARSIZE, get("LBL_TIMEFRAME"), BarSize.getBarSize(1440)), new BooleanDescriptor(USE_SERIES_BS, get("LBL_USE_SERIES_BS"), false, false));
    var pdesc = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getBlue(), 1.0f, null, true, true, false);
    pdesc.setContinuous(false);
    grp.addRow(pdesc);
    grp.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), defaults.getBlue(), X11Colors.WHITE, false, false, true));

    tab = sd.addTab(get("TAB_STD_DEV_BANDS"));
    grp = tab.addGroup(get("GRP_BAND1"), false);
    pdesc = new PathDescriptor(PATH1, get("LBL_LINE"), defaults.getBlue(), 1.0f, null, false, false, true);
    pdesc.setContinuous(false);
    grp.addRow(pdesc);
    grp.addRow(new DoubleDescriptor(BAND_STD1, get("LBL_BAND_STD_DEV"), 2.0, 0.1, 999, 0.1)); 
    grp.addRow(new IndicatorDescriptor(TOP_IND1, get("LBL_TOP_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    grp.addRow(new IndicatorDescriptor(BOTTOM_IND1, get("LBL_BOTTOM_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    grp.addRow(new ShadeDescriptor(FILL_COLOR1, get("LBL_FILL_COLOR"), PATH1, PATH1, Enums.ShadeType.BOTH, defaults.getFillColor(), false, true));

    grp = tab.addGroup(get("GRP_BAND2"), false);
    pdesc = new PathDescriptor(PATH2, get("LBL_LINE"), defaults.getGreen(), 1.0f, null, false, false, true);
    pdesc.setContinuous(false);
    grp.addRow(pdesc);
    grp.addRow(new DoubleDescriptor(BAND_STD2, get("LBL_BAND_STD_DEV"), 3.0, 0.1, 999, 0.1)); 
    grp.addRow(new IndicatorDescriptor(TOP_IND2, get("LBL_TOP_IND"), defaults.getGreen(), X11Colors.WHITE, false, false, true));
    grp.addRow(new IndicatorDescriptor(BOTTOM_IND2, get("LBL_BOTTOM_IND"), defaults.getGreen(), X11Colors.WHITE, false, false, true));
    grp.addRow(new ShadeDescriptor(FILL_COLOR2, get("LBL_FILL_COLOR"), new String[][] { {"TOP1", "TOP2"}, {"BOTTOM1", "BOTTOM2"} }, Util.getAlphaFill(defaults.getGreen()), false, true));

    grp = tab.addGroup(get("GRP_BAND3"), false);
    pdesc = new PathDescriptor(PATH3, get("LBL_LINE"), defaults.getRed(), 1.0f, null, false, false, true);
    pdesc.setContinuous(false);
    grp.addRow(pdesc);
    grp.addRow(new DoubleDescriptor(BAND_STD3, get("LBL_BAND_STD_DEV"), 4.0, 0.1, 999, 0.1)); 
    grp.addRow(new IndicatorDescriptor(TOP_IND3, get("LBL_TOP_IND"), defaults.getRed(), X11Colors.WHITE, false, false, true));
    grp.addRow(new IndicatorDescriptor(BOTTOM_IND3, get("LBL_BOTTOM_IND"), defaults.getRed(), X11Colors.WHITE, false, false, true));
    grp.addRow(new ShadeDescriptor(FILL_COLOR3, get("LBL_FILL_COLOR"), new String[][] { {"TOP2", "TOP3"}, {"BOTTOM2", "BOTTOM3"} }, Util.getAlphaFill(defaults.getRed()), false, true));

    sd.addDependency(new EnabledDependency(false, SHOW_ALL, MAX_BARS));
    sd.addDependency(new EnabledDependency(false, USE_SERIES_BS, Inputs.BARSIZE));
    sd.addDependency(new EnabledDependency(PATH1, PATH2, PATH3, BAND_STD1, TOP_IND1, BOTTOM_IND1, FILL_COLOR1));
    var dep = new EnabledDependency(PATH2, PATH3, BAND_STD2, TOP_IND2, BOTTOM_IND2, FILL_COLOR2);
    dep.setSource2(PATH1);
    sd.addDependency(dep);
    dep = new EnabledDependency(PATH3, BAND_STD3, TOP_IND3, BOTTOM_IND3, FILL_COLOR3);
    dep.setSource2(PATH1);
    dep.setSource3(PATH2);
    sd.addDependency(dep);
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(RTH, USE_HISTORICAL_BARS, Inputs.BARSIZE, USE_SERIES_BS, Inputs.PATH);
    sd.rowAlign(MAX_BARS, SHOW_ALL);
    sd.rowAlign(Inputs.BARSIZE, USE_SERIES_BS);
    
    var desc = createRD();
    desc.setLabelSettings(Inputs.BARSIZE, USE_SERIES_BS, RTH);
    // Hack: Allow the user to create multiple copies of this study if they have different path settings
    // This will allow for multiple VWAPs with different anchors
    desc.setIDSettings(Inputs.BARSIZE, USE_SERIES_BS, Inputs.PATH, RTH);

    desc.exportValue(new ValueDescriptor(Values.VWAP, get("LBL_VWAP"), new String[] { Inputs.BARSIZE, USE_SERIES_BS }));
    desc.exportValue(new ValueDescriptor(Values.TOP1, get("LBL_VWAP_TOP1"), new String[] { BAND_STD1 }));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM1, get("LBL_VWAP_BOTTOM1"), new String[] { BAND_STD1 }));
    desc.exportValue(new ValueDescriptor(Values.TOP2, get("LBL_VWAP_TOP2"), new String[] { BAND_STD2 }));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM2, get("LBL_VWAP_BOTTOM2"), new String[] { BAND_STD2 }));
    desc.exportValue(new ValueDescriptor(Values.TOP3, get("LBL_VWAP_TOP3"), new String[] { BAND_STD3 }));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM3, get("LBL_VWAP_BOTTOM3"), new String[] { BAND_STD3 }));
    desc.declarePath(Values.VWAP, Inputs.PATH);
    desc.declareIndicator(Values.VWAP, Inputs.IND);
    desc.declarePath(Values.TOP1, PATH1);
    desc.declareIndicator(Values.TOP1, TOP_IND1);
    desc.declarePath(Values.BOTTOM1, PATH1);
    desc.declareIndicator(Values.BOTTOM1, BOTTOM_IND1);
    desc.declarePath(Values.TOP2, PATH2);
    desc.declareIndicator(Values.TOP2, TOP_IND2);
    desc.declarePath(Values.BOTTOM2, PATH2);
    desc.declareIndicator(Values.BOTTOM2, BOTTOM_IND2);
    desc.declarePath(Values.TOP3, PATH3);
    desc.declareIndicator(Values.TOP3, TOP_IND3);
    desc.declarePath(Values.BOTTOM3, PATH3);
    desc.declareIndicator(Values.BOTTOM3, BOTTOM_IND3);
    desc.setRangeKeys(Values.VWAP);
  }
  
  // Split/Merge menu items
  @Override
  public MenuDescriptor onMenu(String plotName, Point loc, DrawContext ctx)
  {
    var items = new ArrayList<MenuItem>();
    
    if (anchorPoint != null) {
      items.add(new MenuItem(get("LBL_REMOVE_ANCHOR"), () -> {
        anchorPoint = null;
        getSettings().setLong(ANCHOR, null);
      }));
    }
    
    long time = ctx.translate2Time(loc.getX());
    var series = ctx.getDataContext().getDataSeries();
    int ind = series.findIndex(time);
    if (ind >= 0 && ind < series.size()) {
      items.add(new MenuItem(get("LBL_ANCHOR_AT", Util.formatMMDDYYYYHHMM(series.getStartTime(ind), ctx.getDataContext().getTimeZone())), () -> {
        anchorPoint = series.getStartTime(ind);
        getSettings().setLong(ANCHOR, anchorPoint);
      }));
    }

    if (Util.isEmpty(items)) return null;
    items.add(0, new MenuSeparator());
    return new MenuDescriptor(items, true);
  }

  @Override
  public void clearState()
  {
    super.clearState();
    calculated = false; // need to recalculate the study
    bandCalcInProgress = false;
    lastBandCalc = 0;
  }

  @Override
  public void onTick(DataContext ctx, Tick tick)
  {
    if (calcInProgress || calculator == null) return;
    calculator.onTick(tick);
    // Band calculation be expensive (especially in a fast moving market)
    // Limit this to once per 2 seconds
    if (!band1Enabled || bandCalcInProgress || tick.getTime() - lastBandCalc < 500) return;
    bandCalcInProgress = true;
    Util.schedule(() -> {
      try {
        var series = ctx.getDataSeries();
        calcStd(series.size()-1, series, useSeriesBS ? null : ctx.getDataSeries(barSize));
        lastBandCalc = tick.getTime();
      }
      finally { 
        bandCalcInProgress = false;
      }
    });
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    if (series.size() == 0 || calcInProgress || calculated) return;
    var settings = getSettings();
    anchorPoint = settings.getLong(ANCHOR);
    if (anchorPoint != null && anchorPoint == 0L) anchorPoint = null;
    barSize = settings.getBarSize(Inputs.BARSIZE);
    useSeriesBS = settings.getBoolean(USE_SERIES_BS, true);
    String rthData = settings.getString(RTH, VAL_CHART);
    rth = ctx.isRTH();
    if (Util.compare(rthData, VAL_RTH)) rth = true;
    else if (Util.compare(rthData, VAL_EXT)) rth = false;

    var series2 = ctx.getDataSeries(barSize);
    boolean useSeries2 = series2 != null && !useSeriesBS;
    if (useSeries2 && (series2 == null || series2.size() == 0)) return; // Data not available yet
    
    calculated = true;
    bandStd1 = settings.getDouble(BAND_STD1, 2d);
    bandStd2 = settings.getDouble(BAND_STD2, 3d);
    bandStd3 = settings.getDouble(BAND_STD3, 4d);
    var path1 = settings.getPath(PATH1);
    band1Enabled = path1 != null && path1.isEnabled();
    var path2 = settings.getPath(PATH2);
    band2Enabled = path2 != null && path2.isEnabled() && band1Enabled;
    var path3 = settings.getPath(PATH3);
    band3Enabled = path3 != null && path3.isEnabled() && band2Enabled;
    Util.schedule(() -> calculate(ctx));
  }
  
  private void calculate(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    var settings = getSettings();
    if (series.size() == 0) return;
    var series2 = ctx.getDataSeries(barSize);
    boolean useSeries2 = series2 != null && !useSeriesBS;
    if (useSeries2 && (series2 == null || series2.size() == 0)) return;
    calcInProgress = true;

    var instr = series.getInstrument();
    int maxBars = settings.getInteger(MAX_BARS, 50);
    boolean showAll = settings.getBoolean(SHOW_ALL, false);
    int ind = 0;
    if (!showAll) ind = series.size() - maxBars - 1;
    if (ind < 0) ind = 0;
    
    for(int i = series.size()-1; i >= 0; i--) {
      if (series.isComplete(i)) {
        ind = i+1;
        break;
      }
      if (!showAll && ind < series.size() - maxBars) {
        ind = series.size() - maxBars - 1;
        break;
      }
    }
    long start = anchorPoint != null ? anchorPoint : Util.getStartOfBar(series.getStartTime(ind), series.getEndTime(ind), instr, series.getBarSize(), rth);
    if (anchorPoint == null && series2 != null && useSeries2) start = Util.getStartOfBar(series.getStartTime(ind), series.getEndTime(ind), instr, series2.getBarSize(), rth);
    
    
    int ind2 = 0;
    if (useSeries2 && series2 != null && anchorPoint == null) {
      for(int i = series2.size()-1; i >= 0; i--) {
        long s = Util.getStartOfBar(series2.getStartTime(i), series2.getEndTime(i), instr, series2.getBarSize(), rth);
        if (s <= start) {
          ind2 = i;
          start = s;
          break;
        }
      }
      //System.err.println("ind2: " + ind2 + " size2: " + series2.size() + " " + Util.formatMMDDYYYYHHMM(start));
    }
    if (anchorPoint != null) {
      int i = series.findIndex(start);
      if (i > ind) ind = i;
    }
    else if (!showAll && start < series.getStartTime(ind)) {
      int i = series.findIndex(start);
      if (i >= 0 && i < ind) ind = i;
    }

    calculator = new VWAPCalculator(ind, series, ind2, series2, useSeries2);
    instr.forEachTick(start, ctx.getCurrentTime() + 5*Util.MILLIS_IN_MINUTE, rth, settings.getBoolean(USE_HISTORICAL_BARS, false), calculator);
    // Calculate Top and Bottom bands if applicable
    if (band1Enabled) {
      for(int i=ind; i < series.size(); i++) calcStd(i, series, series2);
    }
    calcInProgress=false; // Let the onTick process incoming ticks.  Note: it is possible that we might miss a couple of ticks in a fast moving market!
    notifyRedraw();
  }

  private void calcStd(int ind, DataSeries series, DataSeries series2)
  {
    Double vwap = series.getDouble(ind, Values.VWAP);
    if (vwap == null || !band1Enabled) return;
    if (anchorPoint == null && (series2 == null || Util.compare(series.getBarSize(), series2.getBarSize()))) return;
    // This is tricky.  We want to restart the band calculation at the beginning of the VWAP period.
    int startIndex = 0; // index of the start of the period 
    if (anchorPoint != null) startIndex = series.findIndex(anchorPoint);
    else {
      long barStart = series.getStartTime(ind);
      var bs = series2.getBarSize();
      int ind2 = series2.findIndex(barStart);
      long start =0;
      // Hack: if this is a daily bar, we cannot count on the start/end times
      var instr = series2.getInstrument();
      if (bs.getIntervalMinutes() == 1440) {
        start = instr.getStartOfDay(barStart, rth);
      }
      else if (bs.getIntervalMinutes() > 1440) {
        var eod = instr.getEndOfDay(series.getEndTime(ind), rth);
        if (eod == barStart) eod += 12+Util.MILLIS_IN_DAY;
        start = Util.getStartOfBar(series.getEndTime(ind), eod, series2.getInstrument(), series2.getBarSize(), rth);
      }
      else {
        long s = series2.getStartTime(ind2);
        long e = series2.getEndTime(ind2);
        start = Util.getStartOfBar(s, e, series2.getInstrument(), series2.getBarSize(), rth);
      }
      startIndex = series.findIndex(start);
    }
    
    if (startIndex < 0) startIndex = 0;
    
    int period = ind - startIndex;
        
    // There are multiple ways to calculate the standard deviation bands
    // The one below comes from the Sierra Chart website: https://www.sierrachart.com/index.php?page=doc/StudiesReference.php&ID=108
    double var=0;
    double totalVol = 0;
    for(int i = ind-period+1; i <= ind; i++) {
      Double _vwap = series.getDouble(i, Values.VWAP);
      if (_vwap == null) continue;
      double diff = (series.getClose(i) + series.getHigh(i) + series.getLow(i))/3 - vwap;
      float vol = series.getVolumeAsFloat(i);
      var += diff*diff*vol;
      totalVol += vol;
    }
    
    double dev = totalVol == 0 ? 0 : Math.sqrt(var/totalVol);

    // This one below comes from a post on TradingView: https://www.tradingview.com/script/g9bbnUBN-Anchored-VWAP-Standard-Deviations/
    /*
    double var =0;
    double psum = 0;
    for(int i = ind-period+1; i <= ind; i++) {
      psum += (series.getClose(i) + series.getHigh(i) + series.getLow(i))/3;
    }

    double mean = psum/period;
    for(int i = ind-period+1; i <= ind; i++) {
      double diff = (series.getClose(i) + series.getHigh(i) + series.getLow(i))/3 - mean;
      var += diff * diff;
    }
    double dev = Math.sqrt(var/(period-1));*/

    if (band1Enabled) {
      series.setDouble(ind, Values.TOP1, vwap + dev*bandStd1);
      series.setDouble(ind, Values.BOTTOM1, vwap - dev*bandStd1);
    }
    if (band2Enabled) {
      series.setDouble(ind, Values.TOP2, vwap + dev*bandStd2);
      series.setDouble(ind, Values.BOTTOM2, vwap - dev*bandStd2);
    }
    if (band3Enabled) {
      series.setDouble(ind, Values.TOP3, vwap + dev*bandStd3);
      series.setDouble(ind, Values.BOTTOM3, vwap - dev*bandStd3);
    }
  }
  
  private boolean calcInProgress = false, bandCalcInProgress = false, calculated=false, band1Enabled=false, band2Enabled=false, band3Enabled=false, useSeriesBS;
  private double bandStd1=2, bandStd2=3, bandStd3=4;
  private boolean rth=false;
  private long lastBandCalc = 0;
  private VWAPCalculator calculator;
  private BarSize barSize;
  private Long anchorPoint;
  
  private class VWAPCalculator implements TickOperation
  {
    VWAPCalculator(int startIndex, DataSeries series, int startIndex2, DataSeries series2, boolean useSeries2)
    {
      ind = startIndex;
      this.series = (DataSeriesImpl)series;
      ind2 = startIndex2;
      this.series2 = (DataSeriesImpl)series2;
      this.useSeries2 = useSeries2;
      updateNextInterval();
    }
    
    @Override
    public void onTick(Tick tick)
    {
      if (tick.getTime() < start) return;

      if (useSeries2 && tick.getTime() >= series.getEndTime(ind) && ind < series.size()-1) {
        series.setComplete(ind); // Note: if this is the current unfinished bar, the bar will not be set to complete
        series.setDouble(ind, Values.VWAP, totalPrice/totalVolume);
        ind = incInd(tick, ind, tick.getTime() < end, series);
      }
      
      if (tick.getTime() >= end) { 
        series.setComplete(ind); // Note: if this is the current unfinished bar, the bar will not be set to complete
        if (tick.getTime() >= nextStart) {
          if (useSeries2) ind2 = incInd(tick, ind2, true, series2);
          else ind = incInd(tick, ind, true, series);
          updateNextInterval();
          totalPrice = totalVolume = 0;
          
          series.setPathBreak(ind, Values.VWAP, true);
          series.setPathBreak(ind, Values.TOP1, true);
          series.setPathBreak(ind, Values.BOTTOM1, true);
          series.setPathBreak(ind, Values.TOP2, true);
          series.setPathBreak(ind, Values.BOTTOM2, true);
          series.setPathBreak(ind, Values.TOP3, true);
          series.setPathBreak(ind, Values.BOTTOM3, true);
          //System.err.println("Path Break: " + ind + " " + Util.formatMMDDYYYYHHMM(series.getStartTime(ind)) + " " + series.isPathBreak(ind, Values.VWAP));
        }
        else return; // this tick is in between the end of the bar and the start of the next bar.  Could be RTH data on a daily timeframe
      }
      totalPrice += tick.getPrice() * tick.getVolumeAsFloat();
      totalVolume += tick.getVolumeAsFloat();
      if (totalVolume == 0) series.setDouble(ind, Values.VWAP, series.getDouble(ind-1, Values.VWAP));
      else series.setDouble(ind, Values.VWAP, totalPrice/totalVolume);
    }
    
    // Its possible that a tick skips over a bar (especially for millisecond bars)
    int incInd(Tick tick, int idx, boolean fill, DataSeries ds)
    {
      //com.motivewave.common.Util.printStackTrace();
      var vwap = series.getDouble(idx, Values.VWAP);
      int i = idx+1;
      long e = ds.getEndTime(i);
      while(e <= tick.getTime() && i < ds.size()-1) {
        if (fill) series.setDouble(i,Values.VWAP, vwap);
        i++;
        e = ds.getEndTime(i);
      }
      return i;
    }
    
    void updateNextInterval()
    {
      if (anchorPoint != null) {
        start = anchorPoint;
        end = Long.MAX_VALUE;
        nextStart = end;
        return;
      }
      var ds = useSeries2 ? series2 : series;
      int i = useSeries2 ? ind2 : ind;
      long s = ds.getStartTime(i);
      long e = ds.getEndTime(i);
      var bs = ds.getBarSize();
      if (bs == null) return; // This should not happen

      start = Util.getStartOfBar(s, e, ds.getInstrument(), bs, rth);
      end = Util.getEndOfBar(s, e, ds.getInstrument(), bs, rth);
      nextStart = end;
      if (!bs.isFixedSize() && i >= ds.size()-1) end = Long.MAX_VALUE; // for non-linear bars we do not know the end time of the latest bar
    }
    
    long start, end, nextStart;
    double totalPrice, totalVolume;
    int ind, ind2;
    DataSeriesImpl series, series2;
    boolean useSeries2;
  }

}
