package com.motivewave.platform.study.overlay;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Pivot Points.  This study implements several types of Pivot Points:
    Classic
    Woodie
    Camarilla
    Fibonacci
    Fib Zone */
@StudyHeader(
    namespace="com.motivewave", 
    id="PIVOT_POINTS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_PIVOT_POINTS", 
    desc="DESC_PIVOT_POINTS",
    menu="MENU_OVERLAY",
    overlay=true,
    helpLink="http://www.motivewave.com/studies/pivot_points.htm")
public class PivotPoints extends Study 
{
  final static String RESIST_IND = "resistInd", PIVOT_IND = "pivotInd", SUPPORT_IND = "supportInd";

  enum Values { P, R1, R2, R3, R4, R5, R6, R7, R8, S1, S2, S3, S4, S5, S6, S7, S8 }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    List<NVP> types = List.of(new NVP(get("LBL_CLASSIC"), PivotSet.CLASSIC),
        new NVP(get("LBL_STANDARD"), PivotSet.STANDARD),
        new NVP(get("LBL_WOODIE"), PivotSet.WOODIE),
        new NVP(get("LBL_CAMARILLA"), PivotSet.CAMARILLA),
        new NVP(get("LBL_FIBONACCI"), PivotSet.FIBONACCI),
        new NVP(get("LBL_FIB_ZONE"), PivotSet.FIB_ZONE),
        new NVP(get("LBL_FIB_ZONE2"), PivotSet.FIB_ZONE2));

    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new BarSizeDescriptor(Inputs.BARSIZE, get("LBL_TIMEFRAME"), BarSize.getBarSize(BarSizeType.LINEAR, 1440)));
    grp.addRow(new DiscreteDescriptor(PivotSet.PIVOT_TYPE, get("LBL_PIVOT_TYPE"), PivotSet.CLASSIC, types));
    grp.addRow(new BooleanDescriptor(PivotSet.SHOW_ALL_PIVOTS, get("LBL_SHOW_ALL_PIVOTS"), false, false));
    grp.addRow(new BooleanDescriptor(PivotSet.EXTEND_RIGHT, get("LBL_EXTEND_RIGHT"), false, false));
    grp.addRow(new BooleanDescriptor(PivotSet.SHOW_LABELS, get("LBL_SHOW_PIVOT_LABELS"), true, false));
    grp.addRow(new BooleanDescriptor(PivotSet.SHOW_PRICES, get("LBL_SHOW_PIVOT_PRICES"), false, false));
    
    tab = sd.addTab(get("TAB_DISPLAY"));

    grp = tab.addGroup(get("LBL_DISPLAY"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_RESISTANCE_LINE"), defaults.getGreenLine(), 2.0f, null, true, false, false, true);
    path.setSupportsShadeType(false);
    path.setSupportsColorPolicy(false);
    path.setSupportsShowPoints(false);
    path.setSupportsShowAsBars(false);
    path.setSupportsTag(false);
    grp.addRow(path);

    path = new PathDescriptor(Inputs.PATH2, get("LBL_PIVOT_LINE"), defaults.getLineColor(), 2.0f, null, true, false, false, true);
    path.setSupportsShadeType(false);
    path.setSupportsColorPolicy(false);
    path.setSupportsShowPoints(false);
    path.setSupportsShowAsBars(false);
    path.setSupportsTag(false);
    grp.addRow(path);

    path = new PathDescriptor(Inputs.PATH3, get("LBL_SUPPORT_LINE"), defaults.getRedLine(), 2.0f, null, true, false, false, true);
    path.setSupportsShadeType(false);
    path.setSupportsColorPolicy(false);
    path.setSupportsShowPoints(false);
    path.setSupportsShowAsBars(false);
    path.setSupportsTag(false);
    grp.addRow(path);

    path = new PathDescriptor(Inputs.PATH4, get("LBL_MIDDLE_LINES"), defaults.getGrey(), 1.0f, null, false, false, false, true);
    path.setSupportsColorPolicy(false);
    path.setSupportsShadeType(false);
    path.setSupportsShowPoints(false);
    path.setSupportsShowAsBars(false);
    path.setSupportsTag(false);
    grp.addRow(path);

    grp.addRow(new FontDescriptor(Inputs.FONT, get("LBL_FONT"), new Font("Arial", Font.BOLD, 12)));
    grp.addRow(new IndicatorDescriptor(RESIST_IND, get("LBL_RESIST_IND"), defaults.getGreen(), null, false, false, true));
    grp.addRow(new IndicatorDescriptor(PIVOT_IND, get("LBL_PIVOT_IND"), null, null, false, false, true));
    grp.addRow(new IndicatorDescriptor(SUPPORT_IND, get("LBL_SUPPORT_IND"), defaults.getRed(), null, false, false, true));

    sd.addDependency(new EnabledDependency(PivotSet.SHOW_LABELS, PivotSet.SHOW_PRICES));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.BARSIZE, PivotSet.PIVOT_TYPE, PivotSet.SHOW_ALL_PIVOTS, PivotSet.EXTEND_RIGHT, PivotSet.SHOW_LABELS, PivotSet.SHOW_PRICES);
    sd.addQuickSettings(Inputs.PATH, Inputs.PATH2, Inputs.PATH3, Inputs.PATH4);

    var desc = createRD();
    desc.setLabelSettings(Inputs.BARSIZE, PivotSet.PIVOT_TYPE);
    desc.declareIndicator(Values.P, PIVOT_IND);
    desc.declareIndicator(Values.R1, RESIST_IND);
    desc.declareIndicator(Values.R2, RESIST_IND);
    desc.declareIndicator(Values.R3, RESIST_IND);
    desc.declareIndicator(Values.R4, RESIST_IND);
    desc.declareIndicator(Values.R5, RESIST_IND);
    desc.declareIndicator(Values.R6, RESIST_IND);
    desc.declareIndicator(Values.R7, RESIST_IND);
    desc.declareIndicator(Values.R8, RESIST_IND);
    desc.declareIndicator(Values.S1, SUPPORT_IND);
    desc.declareIndicator(Values.S2, SUPPORT_IND);
    desc.declareIndicator(Values.S3, SUPPORT_IND);
    desc.declareIndicator(Values.S4, SUPPORT_IND);
    desc.declareIndicator(Values.S5, SUPPORT_IND);
    desc.declareIndicator(Values.S6, SUPPORT_IND);
    desc.declareIndicator(Values.S7, SUPPORT_IND);
    desc.declareIndicator(Values.S8, SUPPORT_IND);
    setMinBars(2);
  }
  
  @Override
  public void clearState()
  {
    super.clearState();
    pivotSets.clear();
    lastPivot = null;
  }
  
  @Override
  public void onBarClose(DataContext ctx) { calculateValues(ctx); }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    var barSize = getSettings().getBarSize(Inputs.BARSIZE);
    var series2 = ctx.getDataSeries(barSize);
    if (series2.size() < 2) return;
    if (lastPivot != null && lastPivot.end > ctx.getCurrentTime()) return;
    //System.err.println("Calculating pivots: " + Util.formatMMDDYYYYHHMMSS(ctx.getCurrentTime()));
    boolean showOlderPivots = getSettings().getBoolean(PivotSet.SHOW_ALL_PIVOTS, false);
    boolean extendRight = getSettings().getBoolean(PivotSet.EXTEND_RIGHT, false);
    var pivotType = getSettings().getString(PivotSet.PIVOT_TYPE, PivotSet.CLASSIC);
    int start = 0;
    if (!showOlderPivots) start = series2.size()-1;
    pivotSets.clear();
    List<Figure> figures = new ArrayList<>();
    
    var instr = series2.getInstrument();
    for(int i = start; i < series2.size(); i++) {
      long time = series2.getStartTime(i);
      long end = series2.getEndTime(i);
      if (barSize.isLinear() && barSize.getIntervalMinutes() >= 1440) {
        // This is a bit of a hack.
        // If the trading hours are off by a little bit, its possible that the time from the daily bar 
        // is actually on the previous day.  Add a bit of time here to work around
    	  time = instr.getStartOfDay(time + 6*Util.MILLIS_IN_HOUR, ctx.isRTH());
    	  if (ctx.isRTH() && time < series2.getStartTime(i) - 6*Util.MILLIS_IN_HOUR) time += Util.MILLIS_IN_DAY;
        end = Util.getEndOfBar(time, time+barSize.getSizeMillis(), instr, barSize, ctx.isRTH());
      }
      if (pivotSets.containsKey(time)) continue; // already created the pivot set for this time period
      double H = series2.getHigh(i-1);
      double L = series2.getLow(i-1);
      double C = series2.getClose(i-1);
      double TO = series2.getOpen(i-1);
      double P = (H + L + C)/3;
      double range = H - L;
      double R1 = 0;
      double R2 = 0;
      double R3 = 0;
      double R4 = 0;
      double R5 = 0;
      double R6 = 0;
      double R7 = 0;
      double R8 = 0;
      double S1 = 0;
      double S2 = 0;
      double S3 = 0;
      double S4 = 0;
      double S5 = 0;
      double S6 = 0;
      double S7 = 0;
      double S8 = 0;
      PivotSet ps = null;
      
      if (Util.compare(pivotType, PivotSet.WOODIE)) {
        P = (H + L + C + TO)/4;
        R1 = (2 * P) - L;
        R2 = P + range;
        R3 = H + 2 * (P - L);
        R4 = R3 + range;
        S1 = (2 * P) - H;
        S2 = P - range;
        S3 = L - 2 * (H - P);
        S4 = S3 - range;
        ps = new PivotSet(time, end, P, R1, R2, R3, R4, S1, S2, S3, S4);
      }
      else if (Util.compare(pivotType, PivotSet.CAMARILLA)) { // accidently misspelled in first release
        R4 = C + range * 1.1/2;
        R3 = C + range * 1.1/4;
        R2 = C + range * 1.1/6;
        R1 = C + range * 1.1/12;
        S1 = C - range * 1.1/12;
        S2 = C - range * 1.1/6;
        S3 = C - range * 1.1/4;
        S4 = C - range * 1.1/2;
        // Looks like the pivot line should be C (not P) in this case
        ps = new PivotSet(time, end, C, R1, R2, R3, R4, S1, S2, S3, S4);
      }
      else if (Util.compare(pivotType, PivotSet.FIBONACCI)) {
        R4 = P + 1.618 * range;
        R3 = P + 1.000 * range;
        R2 = P + 0.618 * range;
        R1 = P + 0.382 * range; 
        S1 = P - 0.382 * range; 
        S2 = P - 0.618 * range; 
        S3 = P - 1.000 * range;
        S4 = P - 1.618 * range;

        ps = new PivotSet(time, end, P, R1, R2, R3, R4, S1, S2, S3, S4);
        ps.R1 = "+0.382";
        ps.R2 = "+0.618";
        ps.R3 = "+1.00";
        ps.R4 = "+1.618";
        ps.P = "BP";
        ps.S1 = "-0.382";
        ps.S2 = "-0.618";
        ps.S3 = "-1.00";
        ps.S4 = "-1.618";
      }
      else if (Util.compare(pivotType, PivotSet.FIB_ZONE)) {
        R4 = P + 1.382 * range;
        R3 = P + range;
        R2 = P + 0.618 * range; 
        R1 = P + 0.5 * range; 
        S1 = P - 0.5 * range; 
        S2 = P - 0.618 * range; 
        S3 = P - range; 
        S4 = P - 1.382 * range;
        ps = new PivotSet(time, end, P, R1, R2, R3, R4, S1, S2, S3, S4);
        ps.addZone(R4, R3);
        ps.addZone(R2, R1);
        ps.addZone(S1, S2);
        ps.addZone(S3, S4);
        ps.R1 = "+0.5";
        ps.R2 = "+0.618";
        ps.R3 = "+1.00";
        ps.R4 = "+1.382";
        ps.P = "BP";
        ps.S1 = "-0.5";
        ps.S2 = "-0.618";
        ps.S3 = "-1.00";
        ps.S4 = "-1.382";
      }
      else if (Util.compare(pivotType, PivotSet.FIB_ZONE2)) {
        R8 = P + 2.618 * range;
        R7 = P + 1.618 * range;
        R6 = P + 1.27 * range;
        R5 = P + 1.13 * range;
        R4 = P + 0.886 * range;
        R3 = P + 0.786 * range;
        R2 = P + 0.618 * range; 
        R1 = P + 0.382 * range; 
        S1 = P - 0.382 * range; 
        S2 = P - 0.618 * range; 
        S3 = P - 0.786 * range; 
        S4 = P - 0.886 * range; 
        S5 = P - 1.13 * range;
        S6 = P - 1.27 * range;
        S7 = P - 1.618 * range;
        S8 = P - 2.618 * range;
        ps = new PivotSet(time, end, P, R1, R2, R3, R4, R5, R6, R7, R8, S1, S2, S3, S4, S5, S6, S7, S8);
        ps.addZone(R7, R8);
        ps.addZone(R5, R6);
        ps.addZone(R4, R3);
        ps.addZone(R2, R1);
        ps.addZone(S1, S2);
        ps.addZone(S3, S4);
        ps.addZone(S5, S6);
        ps.addZone(S7, S8);
        ps.R1 = "+0.382";
        ps.R2 = "+0.618";
        ps.R3 = "+0.786";
        ps.R4 = "+0.886";
        ps.R5 = "+1.13";
        ps.R6 = "+1.27";
        ps.R7 = "+1.618";
        ps.R8 = "+2.618";
        ps.P = "BP";
        ps.S1 = "-0.382";
        ps.S2 = "-0.618";
        ps.S3 = "-0.786";
        ps.S4 = "-0.886";
        ps.S5 = "-1.13";
        ps.S6 = "-1.27";
        ps.S7 = "-1.618";
        ps.S8 = "-2.618";
      }
      else if (Util.compare(pivotType, PivotSet.STANDARD)) {
        R1 = (P * 2) - L; 
        S1 = (P * 2) - H; 
        R2 = P + (H - L); 
        S2 = P - (H - L); 
        ps = new PivotSet(time, end, P, R1, R2, S1, S2);
      }
      else {
        // Classical Pivot
        R1 = (P * 2) - L; 
        S1 = (P * 2) - H; 
        R2 = P - S1 + R1; 
        S2 = P - (R1 - S1); 
        R3 = 2*(P - L) + H;
        S3 = L - (2*( H-P ));
        ps = new PivotSet(time, end, P, R1, R2, R3, S1, S2, S3);
      }
      ps.setExtendLines(extendRight && i == series2.size()-1);

      pivotSets.put(time, ps);
      figures.add(ps);
    }
    
    // Update the indicator values for the last pivot
    PivotSet ps = null;
    for(PivotSet _ps : pivotSets.values()) {
      if (ps == null) ps = _ps;
      else {
        if (_ps.start > ps.start) ps = _ps;
      }
    }
    
    if (ps != null) {
      var series = ctx.getDataSeries();
      series.setDouble(Values.P, ps.getP() == 0 ? null : ps.getP());
      series.setDouble(Values.R1, ps.getR1() == 0 ? null : ps.getR1());
      series.setDouble(Values.R2, ps.getR2() == 0 ? null : ps.getR2());
      series.setDouble(Values.R3, ps.getR3() == 0 ? null : ps.getR3());
      series.setDouble(Values.R4, ps.getR4() == 0 ? null : ps.getR4());
      series.setDouble(Values.R5, ps.getR5() == 0 ? null : ps.getR5());
      series.setDouble(Values.R6, ps.getR6() == 0 ? null : ps.getR6());
      series.setDouble(Values.R7, ps.getR7() == 0 ? null : ps.getR7());
      series.setDouble(Values.R8, ps.getR8() == 0 ? null : ps.getR8());
      
      series.setDouble(Values.S1, ps.getS1() == 0 ? null : ps.getS1());
      series.setDouble(Values.S2, ps.getS2() == 0 ? null : ps.getS2());
      series.setDouble(Values.S3, ps.getS3() == 0 ? null : ps.getS3());
      series.setDouble(Values.S4, ps.getS4() == 0 ? null : ps.getS4());
      series.setDouble(Values.S5, ps.getS5() == 0 ? null : ps.getS5());
      series.setDouble(Values.S6, ps.getS6() == 0 ? null : ps.getS6());
      series.setDouble(Values.S7, ps.getS7() == 0 ? null : ps.getS7());
      series.setDouble(Values.S8, ps.getS8() == 0 ? null : ps.getS8());
    }

    lastPivot = ps;
    //System.err.println("last pivot: " + Util.formatMMDDYYYYHHMM(lastPivot.start) + " - " + Util.formatMMDDYYYYHHMM(lastPivot.end));
    beginFigureUpdate();
    setFigures(figures);
    endFigureUpdate();
  }
  
  private PivotSet lastPivot;
  protected Map<Long, PivotSet> pivotSets = Collections.synchronizedMap(new HashMap());
}
