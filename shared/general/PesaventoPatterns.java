package com.motivewave.platform.study.general;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.FontInfo;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Label;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

//Pesavento Patterns 204
@StudyHeader(
    namespace="com.motivewave",
    id="PESAVENTO_PATT",
    rb="com.motivewave.platform.study.nls.strings2",
    name="NAME_PESAVENTO_PATTERNS", 
    desc="DESC_PESAVENTO_PATTERNS",
    label="LBL_PP",
    helpLink= "",  //"http://www.motivewave.com/studies/pesavento_patterns.htm",
    overlay=true,
    studyOverlay=true)
public class PesaventoPatterns extends Study 
{
  // Settings
  final static String HIGH_INPUT = "highInput";
  final static String LOW_INPUT = "lowInput";
  final static String REVERSAL = "reversal";
  final static String PRICE_MOVEMENTS = "priceMovements";
  final static String PRICE_LABELS = "priceLabels";
  final static String RETRACE_LINE = "retraceLine";
  final static String ERROR_PERCENT = "errorPercent";
  final static String PESAVENTO_COLOR = "pesaventoColor";
  final static String DISPLAY_PIVOTS = "displayPivots";
  
  protected Map<Long, PivotSet> pivotSets = new HashMap();

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("GENERAL"));
    
    var inputs1 = tab.addGroup(get("INPUTS"));
    inputs1.addRow(new InputDescriptor(HIGH_INPUT, get("HIGH_INPUT"), Enums.BarInput.MIDPOINT));
    inputs1.addRow(new InputDescriptor(LOW_INPUT, get("LOW_INPUT"), Enums.BarInput.MIDPOINT));
    inputs1.addRow(new DoubleDescriptor(REVERSAL, get("REVERSAL"), 1.0, 0.001, 99.999, 0.001));
    inputs1.addRow(new DoubleDescriptor(ERROR_PERCENT, get("ERROR_PERCENT"), 4.0, 0, 99.1, 0.1));
    inputs1.addRow(new BooleanDescriptor(PRICE_MOVEMENTS, get("PRICE_MOVEMENTS"), true));
    inputs1.addRow(new BooleanDescriptor(PRICE_LABELS, get("PRICE_LABELS"), true));
    inputs1.addRow(new FontDescriptor(Inputs.FONT, get("FONT"), defaults.getFont()));
    
    var colors = tab.addGroup(get("COLORS"));
    colors.addRow(new ColorDescriptor(PESAVENTO_COLOR, get("PESAVENTO_LABEL_COLOR"), defaults.getBlue()));
    colors.addRow(new PathDescriptor("pLine", get("LINE"), defaults.getLineColor(), 1.0f, null, true, false, false));
    colors.addRow(new PathDescriptor(RETRACE_LINE, get("RETRACE_LINE"), defaults.getLineColor(), 1.0f, new float[] {3f, 3f}, true, false, true));

    tab = sd.addTab(get("PIVOT_POINTS"));
    
    var inputs2 = tab.addGroup(get("INPUTS"));
    inputs2.addRow(new BooleanDescriptor(DISPLAY_PIVOTS, get("DISPLAY_PIVOTS"), false));
    inputs2.addRow(new BarSizeDescriptor(Inputs.BARSIZE, get("TIMEFRAME"), BarSize.getBarSize(BarSizeType.LINEAR, 1440)));
    List<NVP> types = new ArrayList();
    types.add(new NVP(get("FIB_ZONE"), PivotSet.FIB_ZONE));
    types.add(new NVP(get("FIB_ZONE2"), PivotSet.FIB_ZONE2));
    types.add(new NVP(get("CLASSIC"), PivotSet.CLASSIC));
    types.add(new NVP(get("WOODIE"), PivotSet.WOODIE));
    types.add(new NVP(get("CAMARILLA"), PivotSet.CAMARILLA));
    types.add(new NVP(get("FIBONACCI"), PivotSet.FIBONACCI));
    inputs2.addRow(new DiscreteDescriptor(PivotSet.PIVOT_TYPE, get("PIVOT_TYPE"), PivotSet.FIB_ZONE, types));
    inputs2.addRow(new BooleanDescriptor(PivotSet.SHOW_ALL_PIVOTS, get("SHOW_ALL_PIVOTS"), false));
    inputs2.addRow(new BooleanDescriptor(PivotSet.SHOW_LABELS, get("SHOW_PIVOT_LABELS"), true));
    inputs2.addRow(new BooleanDescriptor(PivotSet.EXTEND_RIGHT, get("EXTEND_RIGHT"), true));
    
    var colors2 = tab.addGroup(get("COLORS"));
    colors2.addRow(new PathDescriptor(Inputs.PATH, get("RESISTANCE_LINE"), defaults.getGreenLine(), 2.0f, null, true, false, false, false));
    colors2.addRow(new PathDescriptor(Inputs.PATH2, get("PIVOT_LINE"), defaults.getLineColor(), 2.0f, null, true, false, false, false));
    colors2.addRow(new PathDescriptor(Inputs.PATH3, get("SUPPORT_LINE"), defaults.getRedLine(), 2.0f, null, true, false, false, false));
    colors2.addRow(new FontDescriptor(PivotSet.FONT, get("FONT"), new Font("Arial", Font.BOLD, 12)));

    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, Inputs.BARSIZE));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, PivotSet.PIVOT_TYPE));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, PivotSet.SHOW_ALL_PIVOTS));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, PivotSet.SHOW_LABELS));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, PivotSet.EXTEND_RIGHT));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, Inputs.PATH));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, Inputs.PATH2));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, Inputs.PATH3));
    sd.addDependency(new EnabledDependency(DISPLAY_PIVOTS, PivotSet.FONT));

    var desc = createRD();
    desc.setLabelSettings(HIGH_INPUT, LOW_INPUT, REVERSAL, ERROR_PERCENT);
  }
  
  @Override
  public void clearState()
  {
    super.clearState();
    pivotSets.clear();
  }
  
  @Override
  public void onBarClose(DataContext ctx) { calculateValues(ctx); }

  @SuppressWarnings("null")
  @Override
  protected void calculateValues(DataContext ctx)
  {
    Object highInput = getSettings().getInput(HIGH_INPUT);
    Object lowInput = getSettings().getInput(LOW_INPUT);
    double reversal = getSettings().getDouble(REVERSAL, 1.0)/100.0;
    double error = getSettings().getDouble(ERROR_PERCENT)/100.0;

    boolean movements = getSettings().getBoolean(PRICE_MOVEMENTS, true);
    boolean priceLabels = getSettings().getBoolean(PRICE_LABELS, true);
    PathInfo line = getSettings().getPath("pLine");
    PathInfo retraceLine = getSettings().getPath(RETRACE_LINE);
    Defaults defaults = ctx.getDefaults();
    FontInfo fi = getSettings().getFont(Inputs.FONT);
    Font f = fi == null ? defaults.getFont() : fi.getFont();
    Color bgColor = defaults.getBackgroundColor();
    Color ptColor = defaults.getBackgroundColor();
    
    Color txtColor = line.getColor();
    Color pColor = getSettings().getColor(PESAVENTO_COLOR);
    DataSeries series = ctx.getDataSeries();
    Instrument instr = ctx.getInstrument();
    clearFigures();

    // Find a local low or high
    double max = series.getDouble(0, highInput);
    double min = series.getDouble(0, lowInput);

    int minBar = 0;
    int maxBar = 0;
    boolean up = false;

    // Determine the initial direction
    double val = series.getDouble(1, highInput);
    if (val > max) {
      up = true;
    }
    else {
      up = false;
    }

    List<Coordinate> points = new ArrayList();
    for(int i = 0; i < series.size()-1; i++) {
      if (up) val = series.getDouble(i, highInput);
      else val = series.getDouble(i, lowInput);

      if (val>max) {
        max = val;
        maxBar = i;
        if (up) {
          min = max;
          minBar = maxBar;
        }
      }
      if (val<min) {
        min = val;
        minBar = i;
        
        if (!up) {
          max = min;
          maxBar = minBar;
        }
      }
      
      // Check to see if we have found a reversal point
      if (up && (min < (1.0-reversal)*max)) {
        points.add(new Coordinate(series.getStartTime(maxBar), series.getHigh(maxBar)));
        max = min;
        maxBar = minBar;
        up=false;
      }
      
      if (!up && (max > (1.0+reversal)*min)) {
        points.add(new Coordinate(series.getStartTime(minBar), series.getLow(minBar)));
        
        min = max;
        minBar = maxBar;
        up=true;
      }
    }
    
    if (up) points.add(new Coordinate(series.getStartTime(maxBar), series.getHigh(maxBar)));
    else points.add(new Coordinate(series.getStartTime(minBar), series.getLow(minBar)));
    
    // Build the ZigZag lines
    Coordinate prev = null, prev2 = null;
   
    for(Coordinate c : points) {
      // Zig Zag Lines
      if (prev != null) {
        Line l = new Line(prev, c, line);
        addFigure(l);
        if (movements) {
          double value = c.getValue();
          boolean isP = isPesavento(value, error);
          Color bC = isP ? pColor : bgColor;
          Color tColor = isP ? ptColor : txtColor;
          l.setText(instr.format(Math.abs(c.getValue() - prev.getValue())), f);
          l.getText().setTextColor(tColor);
          l.getText().setBackground(bC);
        }
      }
      
      // Retracements
      if ((prev != null) && (prev2 != null) && (retraceLine.isEnabled()) ) {
          Line l = new Line(prev2, c, retraceLine);
          double l1 = Math.abs(c.getValue() - prev.getValue());
          double l2 = Math.abs(prev2.getValue() - prev.getValue());
          double rt = l1/l2;
          boolean isP = isPesavento(rt, error);
          Color bC = isP ? pColor : bgColor;
          Color tColor = isP ? ptColor : txtColor;
          l.setText(Double.toString(Util.round(rt, 4)), f);
          l.getText().setTextColor(tColor);
          l.getText().setBackground(bC);
          addFigure(l);
      }
      prev2 = prev;
      prev = c;
      
      // Price Labels
      if (priceLabels) {
        double value = c.getValue();
        boolean isP = isPesavento(value, error);
        Color bC = isP ? pColor : bgColor;
        Color tColor = isP ? ptColor : txtColor;
        Label lbl = new Label(instr.format(value), f, tColor, bC);
        lbl.setLocation(c);
        addFigure(lbl);
      }
    }
    
    //Pivot Points
    if(!getSettings().getBoolean(DISPLAY_PIVOTS)) return;
  
    BarSize barSize = getSettings().getBarSize(Inputs.BARSIZE);
    DataSeries series2 = ctx.getDataSeries(barSize);
    if (series2.size() < 2) {
      return;
    }
    
    boolean showOlderPivots = getSettings().getBoolean(PivotSet.SHOW_ALL_PIVOTS, false);
    boolean extendRight = getSettings().getBoolean(PivotSet.EXTEND_RIGHT, false);
    String pivotType = getSettings().getString(PivotSet.PIVOT_TYPE, PivotSet.CLASSIC);
    int start = 1;
    if (!showOlderPivots) {
      start = series2.size()-1;
      pivotSets.clear();
      //clearFigures();
    }
    
    Instrument instr2 = series2.getInstrument();
    for(int i = start; i < series2.size(); i++) {
      long time = series2.getStartTime(i);
      if (barSize.isLinear() && barSize.getInterval() >= 1440) {
        time = instr2.getStartOfDay(time, ctx.isRTH());
      }
      if (pivotSets.containsKey(time)) {
        continue;
      }
      
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
        ps = new PivotSet(time, series2.getEndTime(i), P, R1, R2, R3, R4, S1, S2, S3, S4);
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
        ps = new PivotSet(time, series2.getEndTime(i), P, R1, R2, R3, R4, S1, S2, S3, S4);
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

        ps = new PivotSet(time, series2.getEndTime(i), P, R1, R2, R3, R4, S1, S2, S3, S4);
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
        ps = new PivotSet(time, series2.getEndTime(i), P, R1, R2, R3, R4, S1, S2, S3, S4);
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
        ps = new PivotSet(time, series2.getEndTime(i), P, R1, R2, R3, R4, R5, R6, R7, R8, S1, S2, S3, S4, S5, S6, S7, S8);
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
      else {
        // Classical Pivot
        R1 = (P * 2) - L; 
        S1 = (P * 2) - H; 
        R2 = P - S1 + R1; 
        S2 = P - (R1 - S1); 
        R3 = 2*(P - L) + H;
        S3 = L - (2*( H-P ));
        ps = new PivotSet(time, series2.getEndTime(i), P, R1, R2, R3, S1, S2, S3);
      }
      ps.setExtendLines(extendRight && i == series2.size()-1);
      pivotSets.put(time, ps);
      addFigure(ps);
    }  
  }
  
  private boolean isPesavento(double value, double error)
  {
    double[] pNumbs = {.382, .618, .786, 1.0, 1.272, 1.618, 2.618, .50, .707, .841, 1.414, 2.0, 1.128, 4.0};
    error = error / 2.0;
    double minFactor = 1.0 - error;
    double maxFactor = 1.0 + error;
    double maxValue = value * maxFactor;
    double minValue = value * minFactor;
    
    for (int i = 0; i < pNumbs.length; i++){
      if (pNumbs[i] <= maxValue && pNumbs[i] >= minValue) return true;
    }
    return false;
  }
}