package com.motivewave.platform.study.overlay;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Label;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Zig Zag */
@StudyHeader(
    namespace="com.motivewave", 
    id="ZIG_ZAG", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ZIG_ZAG", 
    desc="DESC_ZIG_ZAG",
    menu="MENU_OVERLAY",
    overlay=true,
    studyOverlay=true,
    supportsBarUpdates=false,
    helpLink="http://www.motivewave.com/studies/zig_zag.htm")
public class ZigZag extends Study 
{
  enum Values { DELTA }
  final static String HIGH_INPUT = "highInput", LOW_INPUT = "lowInput", REVERSAL = "reversal", REVERSAL_TICKS = "reversalTicks", USE_TICKS="useTicks";
  final static String PRICE_MOVEMENTS = "priceMovements", PRICE_LABELS = "priceLabels", RETRACE_LINE = "retraceLine", OFFSET="offset";
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    boolean o = isOverlay();
    
    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new InputDescriptor(HIGH_INPUT, get("LBL_HIGH_INPUT"), Enums.BarInput.HIGH));
    grp.addRow(new InputDescriptor(LOW_INPUT, get("LBL_LOW_INPUT"), Enums.BarInput.LOW));
    grp.addRow(new IntegerDescriptor(REVERSAL_TICKS, get("LBL_REVERSAL_TICKS"), 10, 1, 99999, 1), new BooleanDescriptor(USE_TICKS, get("LBL_ENABLED"), false, false));
    grp.addRow(new DoubleDescriptor(REVERSAL, get("LBL_REVERSAL"), 1.0, 0.0001, 99.999, 0.0001));
    grp.addRow(new BooleanDescriptor(PRICE_MOVEMENTS, get("LBL_PRICE_MOVEMENTS"), true));
    grp.addRow(new BooleanDescriptor(PRICE_LABELS, get("LBL_PRICE_LABELS"), true));
    grp.addRow(new IntegerDescriptor(OFFSET, get("LBL_LABEL_OFFSET"), 5, 0, 99, 1));
    grp.addRow(new FontDescriptor(Inputs.FONT, get("LBL_FONT"), defaults.getFont()));
    
    grp = tab.addGroup(get("LBL_DISPLAY"));
    if (!o) {
      var histogram = new PathDescriptor(Inputs.BAR, get("LBL_HISTOGRAM"), defaults.getBarColor(), 1.0f, null, true, false, true);
      histogram.setShowAsBars(true);
      histogram.setSupportsShowAsBars(true);
      histogram.setColorPolicy(Enums.ColorPolicy.POSITIVE_NEGATIVE);
      histogram.setColor(defaults.getBarUpColor());
      histogram.setColor2(defaults.getBarDownColor());
      histogram.setColorPolicies(Enums.ColorPolicy.values());
      grp.addRow(histogram);
    }
    grp.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, true));
    grp.addRow(new PathDescriptor(RETRACE_LINE, get("LBL_RETRACE_LINE"), defaults.getLineColor(), 1.0f, new float[] {3f, 3f}, true, false, true));

    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(HIGH_INPUT, LOW_INPUT, REVERSAL_TICKS, USE_TICKS, REVERSAL, PRICE_MOVEMENTS, PRICE_LABELS, Inputs.FONT, o ? null : Inputs.BAR , Inputs.PATH, RETRACE_LINE);
    sd.rowAlign(REVERSAL_TICKS, USE_TICKS);

    sd.addDependency(new EnabledDependency(USE_TICKS, REVERSAL_TICKS));
    sd.addDependency(new EnabledDependency(false, USE_TICKS, REVERSAL));
    sd.addDependency(new EnabledDependency(PRICE_LABELS, OFFSET));

    var rd = createRD();
    rd.setLabelSettings(HIGH_INPUT, LOW_INPUT, REVERSAL);
    rd.setIDSettings(HIGH_INPUT, LOW_INPUT, REVERSAL, REVERSAL_TICKS);
    rd.exportValue(new ValueDescriptor(Values.DELTA, Enums.ValueType.DOUBLE, get("LBL_DELTA"), null));
    if (!o) {
      rd.declarePath(Values.DELTA, Inputs.BAR);
      rd.setRangeKeys(Values.DELTA);
      rd.setTopInsetPixels(30);
      rd.setBottomInsetPixels(30);
      rd.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
    }
    setMinBars(200);
  }
  
  protected boolean isOverlay() { return true; }
  
  @Override
  public void clearState()
  {
    super.clearState();
    pivotBar = -1;
    pivot = 0;
    unconfirmed.clear();
    prev = prev2 = null;
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var s = getSettings();
    Object highInput = s.getInput(HIGH_INPUT);
    Object lowInput = s.getInput(LOW_INPUT);
    double reversal = s.getDouble(REVERSAL, 1.0)/100.0;
    int reversalTicks = s.getInteger(REVERSAL_TICKS, 10);
    boolean useTicks = s.getBoolean(USE_TICKS, false);
    boolean movements = s.getBoolean(PRICE_MOVEMENTS, true);
    boolean priceLabels = s.getBoolean(PRICE_LABELS, true);
    int offset = s.getInteger(OFFSET, 5);
    var line = s.getPath(Inputs.PATH);
    var retraceLine = s.getPath(RETRACE_LINE);
    var defaults = ctx.getDefaults();
    var fi = s.getFont(Inputs.FONT);
    Font f = fi == null ? defaults.getFont() : fi.getFont();
    Color bgColor = defaults.getBackgroundColor();
    Color txtColor = line.getColor();
    
    var series = ctx.getDataSeries();
    var instr = ctx.getInstrument();
    double tickAmount = reversalTicks * instr.getTickSize();

    if (pivotBar < 0) {
      // Initialize. 
      double high = series.getDouble(0, highInput, 0);
      double low = series.getDouble(0, lowInput, 0);
      double val = series.getDouble(1, highInput, 0);
      if (val > high) up = true;
      else up = false;
      pivotBar = 0;
      pivot = up ? low : high;    
    }
    
    List<PPoint> points = new ArrayList();
    for(int i = pivotBar+1; i < series.size(); i++) {
      if (!series.isBarComplete(i)) break;
      Double high = series.getDouble(i, highInput);
      Double low = series.getDouble(i, lowInput);
      if (high == null || low == null) continue;
      
      if (up) {
        if (useTicks ? high - pivot >= tickAmount : (1.0-reversal)*high >= pivot) {
          // confirmed previous low
          points.add(new PPoint(new Coordinate(series.getStartTime(pivotBar), series.getLow(pivotBar)), pivotBar, false));
          pivot = high;
          pivotBar = i;
          up=false;
        }
        else if (low < pivot) {
          pivot = low;
          pivotBar = i;
        }
      }
      else {
        if (useTicks ? pivot - low >= tickAmount : (1.0+reversal)*low <= pivot) {
          // confirmed previous max
          points.add(new PPoint(new Coordinate(series.getStartTime(pivotBar), series.getHigh(pivotBar)), pivotBar, true));
          pivot = low;
          pivotBar = i;
          up=true;
        }
        else if (high > pivot) {
          pivot = high;
          pivotBar = i;             
        }
      }
    }
    
    // Build the ZigZag lines
    // For efficiency reasons, only build the delta
    beginFigureUpdate();
    
    for(var p : points) {
      // Zig Zag Lines
      var c = p.coord;
      var pc = prev == null ? null : prev.coord;
      long time = c.getTime();
      double value = isOverlay() ? c.getValue() : p.delta;
      if (pc != null) {
        // Fill in delta values
        for(int i = prev.ind+1; i <= p.ind; i++) {
          series.setFloat(i, Values.DELTA, p.top ? series.getHigh(i) - series.getLow(prev.ind) : series.getLow(i) - series.getHigh(prev.ind));
        }
        p.delta = series.getFloat(p.ind, Values.DELTA, 0f);
        if (!isOverlay()) value = p.delta;
        if (line.isEnabled()) {
          var l = new Line(pc.getTime(), isOverlay() ? pc.getValue() : prev.delta,  time, value, line);
          addFigure(0, l);
          if (movements) {
            l.setText(instr.format(Math.abs(c.getValue() - pc.getValue())), f);
            l.getText().setBackground(bgColor);
          }
        }
      }
      
      // Retracements
      if (retraceLine != null && retraceLine.isEnabled() && prev2 != null && pc != null) {
        var pc2 = prev2.coord;
        var l = new Line(pc2.getTime(), isOverlay() ? pc2.getValue() : prev2.delta,  time, value, retraceLine);
        double l1 = Math.abs(c.getValue() - pc.getValue());
        double l2 = Math.abs(pc2.getValue() - pc.getValue());
        double rt = l1/l2;
        l.setText(Util.round(rt*100, 1)+"%", f);
        addFigure(0, l);
      }
      if (priceLabels) {
        var lbl = new Label(instr.format(c.getValue()), f, txtColor, bgColor);
        lbl.setLocation(c.getTime(), isOverlay() ? c.getValue() : p.delta);
        lbl.setPosition(p.top ? Enums.Position.TOP : Enums.Position.BOTTOM);
        lbl.setShowLine(false);
        lbl.setOffset(offset);
        addFigure(lbl);
      }
      prev2 = prev;
      prev = p;
    }
    

    var tmp = new ArrayList(unconfirmed);
    unconfirmed.clear();

    var last = new Coordinate(series.getStartTime(pivotBar), up ? series.getLow(pivotBar) : series.getHigh(pivotBar));
    if (prev != null && !last.equals(prev.coord)) {
      long time = last.getTime();
      double value = last.getValue();
      // TODO: this code should be refactored with the code above
      // Fill in delta values
      for(int i = prev.ind+1; i <= pivotBar; i++) {
        series.setFloat(i, Values.DELTA, up ? series.getLow(i) - series.getHigh(prev.ind) : series.getHigh(i) - series.getLow(prev.ind));
      }
      if (!isOverlay()) value = series.getFloat(pivotBar, Values.DELTA, 0f);
      var pc = prev.coord;
      if (line.isEnabled()) {
        var l = new Line(pc.getTime(), isOverlay() ? pc.getValue() : prev.delta,  time, value, line);
        unconfirmed.add(l);
        addFigure(0, l);
        if (movements) {
          l.setText(instr.format(Math.abs(last.getValue() - prev.coord.getValue())), f);
          l.getText().setBackground(bgColor);
        }
      }
      // Retracements
      if (retraceLine != null && retraceLine.isEnabled() && prev2 != null) {
        var pc2 = prev2.coord;
        var l = new Line(pc2.getTime(), isOverlay() ? pc2.getValue() : prev2.delta,  time, value, retraceLine);
        double l1 = Math.abs(last.getValue() - prev.coord.getValue());
        double l2 = Math.abs(prev2.coord.getValue() - prev.coord.getValue());
        double rt = l1/l2;
        l.setText(Util.round(rt*100, 1)+"%", f);
        unconfirmed.add(l);
        addFigure(0, l);
      }
      if (priceLabels) {
        var lbl = new Label(instr.format(last.getValue()), f, txtColor, bgColor);
        lbl.setPosition(up ? Enums.Position.BOTTOM : Enums.Position.TOP);
        lbl.setLocation(time, value);
        lbl.setShowLine(false);
        lbl.setOffset(offset);
        unconfirmed.add(lbl);
        addFigure(lbl);
      }
      removeFigures(tmp);
    }
    
    endFigureUpdate();
  }
  
  private double pivot=0;
  private int pivotBar = -1;
  private boolean up;
  private PPoint prev = null, prev2 = null;
  private List<Figure> unconfirmed = new ArrayList<>();
  
  private class PPoint
  {
    PPoint(Coordinate c, int ind, boolean top)
    {
      coord = c;
      this.top = top;
      this.ind = ind;
    }
    Coordinate coord;
    boolean top;
    int ind;
    float delta;
  }
}
