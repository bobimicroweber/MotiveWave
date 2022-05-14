package com.motivewave.platform.study.overlay;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.FontInfo;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.draw.Figure;

public class PivotSet extends Figure
{
  public final static String PIVOT_TYPE = "pivotType";
  public final static String SHOW_ALL_PIVOTS = "showAllPivots";
  public final static String SHOW_LABELS = "showLabels";
  public final static String SHOW_PRICES = "showPrices";
  public final static String EXTEND_RIGHT = "extendRight";
  
  public final static String CLASSIC = "Classic";
  public final static String STANDARD = "Standard"; // see: https://school.stockcharts.com/doku.php?id=technical_indicators:pivot_points

  public final static String WOODIE = "Woodie";
  public final static String CAMARILLA = "Camarilla";
  public final static String FIBONACCI = "Fibonacci";
  public final static String FIB_ZONE = "Fib Zone";
  public final static String FIB_ZONE2 = "FibZone2";

  public PivotSet(long start, long end, double P, double R1, double R2, double S1, double S2)
  {
    p = P;
    r1 = R1;
    r2 = R2;
    s1 = S1;
    s2 = S2;
    lineCount = 5;
    this.start = start;
    this.end = end;
  }
  
  public PivotSet(long start, long end, double P, double R1, double R2, double R3, double S1, double S2, double S3)
  {
    p = P;
    r1 = R1;
    r2 = R2;
    r3 = R3;
    s1 = S1;
    s2 = S2;
    s3 = S3;
    lineCount = 7;
    this.start = start;
    this.end = end;
  }
  
  public PivotSet(long start, long end, double P, double R1, double R2, double R3, double R4, double S1, double S2, double S3, double S4)
  {
    p = P;
    r1 = R1;
    r2 = R2;
    r3 = R3;
    r4 = R4;
    s1 = S1;
    s2 = S2;
    s3 = S3;
    s4 = S4;
    lineCount = 9;
    this.start = start;
    this.end = end;
  }

  public PivotSet(long start, long end, double P, double R1, double R2, double R3, double R4, double R5, double R6, double R7, double R8, 
      double S1, double S2, double S3, double S4, double S5, double S6, double S7, double S8)
  {
    p = P;
    r1 = R1;
    r2 = R2;
    r3 = R3;
    r4 = R4;
    r5 = R5;
    r6 = R6;
    r7 = R7;
    r8 = R8;
    s1 = S1;
    s2 = S2;
    s3 = S3;
    s4 = S4;
    s5 = S5;
    s6 = S6;
    s7 = S7;
    s8 = S8;
    lineCount = 17;
    this.start = start;
    this.end = end;
  }

  public void addZone(double top, double bottom)
  {
    if (top > bottom) zones.add(new Zone(top, bottom));
    else zones.add(new Zone(bottom, top));
  }
  
  @Override
  public void draw(Graphics2D gc, DrawContext ctx)
  {
    Rectangle bounds = ctx.getBounds();
    if (!isVisible(ctx) || Util.isEmpty(lines) || bounds == null) return;
    
    PathInfo midPath = ctx.getSettings().getPath(Inputs.PATH4);
    if (midLines != null && midPath != null && midPath.isEnabled()) {
      List<Line2D> _lines = new ArrayList(midLines);
      gc.setColor(midPath.getColor());
      gc.setStroke(Util.getStroke(midPath, ctx.isSelected()));
      for(Line2D line : _lines) {
        if (line == null) continue;
        gc.draw(Util.clipLine(line, bounds));
      }
    }
    
    List<Line2D> _lines = new ArrayList(lines);
    int lc = lineCount;
    
    PathInfo resistancePath = ctx.getSettings().getPath(Inputs.PATH);
    PathInfo pivotPath = ctx.getSettings().getPath(Inputs.PATH2);
    PathInfo supportPath = ctx.getSettings().getPath(Inputs.PATH3);
    if (resistancePath.isEnabled()) {
      for(Rectangle r : new ArrayList<>(topZones)) {
        gc.setColor(Util.getAlphaFill(resistancePath.getColor()));
        gc.fill(bounds.intersection(r));
      }
  
      // Resistance Lines
      gc.setColor(resistancePath.getColor());
      gc.setStroke(Util.getStroke(resistancePath, ctx.isSelected()));
      drawLine(gc, _lines.get(0), bounds);
      drawLine(gc, _lines.get(1), bounds);
      if (lc > 5) drawLine(gc, _lines.get(2), bounds);
      if (lc > 7) drawLine(gc, _lines.get(3), bounds);
      if (lc > 9) {
        drawLine(gc, _lines.get(4), bounds);
        drawLine(gc, _lines.get(5), bounds);
        drawLine(gc, _lines.get(6), bounds);
        drawLine(gc, _lines.get(7), bounds);
      }
    }
    int s = 2;
    if (lc > 5) s = 3;
    if (lc > 7) s = 4;
    if (lc > 9) s = 8;
    
    // Pivot Line
    if (pivotPath.isEnabled()) {
      gc.setColor(pivotPath.getColor());
      gc.setStroke(Util.getStroke(pivotPath, ctx.isSelected()));
      drawLine(gc, _lines.get(s), bounds);
    }

    if (supportPath.isEnabled()) {
      for(Rectangle r : new ArrayList<>(bottomZones)) {
        gc.setColor(Util.getAlphaFill(supportPath.getColor()));
        gc.fill(bounds.intersection(r));
      }
  
      // Support Lines
      gc.setColor(supportPath.getColor());
      gc.setStroke(Util.getStroke(supportPath, ctx.isSelected()));
      drawLine(gc, _lines.get(s+1), bounds);
      drawLine(gc, _lines.get(s+2), bounds);
      if (lc > 5) drawLine(gc, _lines.get(s+3), bounds);
      if (lc > 7) drawLine(gc, _lines.get(s+4), bounds);
      if (lc > 9) {
        drawLine(gc, _lines.get(s+5), bounds);
        drawLine(gc, _lines.get(s+6), bounds);
        drawLine(gc, _lines.get(s+7), bounds);
        drawLine(gc, _lines.get(s+8), bounds);
      }
    }
    drawLabels(gc, ctx);
  }

  private void drawLine(Graphics2D gc, Line2D line, Rectangle bounds)
  {
    if (line == null) return;
    gc.draw(Util.clipLine(line, bounds));
  }
  
  private void drawLabel(Graphics2D gc, Line2D line, String lbl, double price, FontMetrics fm, DrawContext ctx, int offset)
  {
    if (line == null) return;
    boolean drawPrices = ctx.getSettings().getBoolean(SHOW_PRICES, false);
    if (drawPrices) {
      lbl += "(" + ctx.getDataContext().getInstrument().format(price) + ")";
    }

    gc.drawString(lbl, getLX(lbl, fm, ctx), (int)(line.getP2().getY() + offset));
  }
  
  private int getLX(String lbl, FontMetrics fm, DrawContext ctx)
  {
    int x = right;
    int w = fm.stringWidth(lbl);
    x -= w;
    int gr = (int)ctx.getBounds().getMaxX();
    if (x + w > gr) {
      x = gr - w - 5; 
    }
    if (extendLines) x -= 3;
    return x;
  }
  
  private void drawLabels(Graphics2D gc, DrawContext ctx)
  {
    if (!isVisible(ctx)) return;
    if (Util.isEmpty(lines)) return;
    
    boolean drawLabels = ctx.getSettings().getBoolean(SHOW_LABELS, true);
    if (!drawLabels) return;

    PathInfo resistancePath = ctx.getSettings().getPath(Inputs.PATH);
    PathInfo pivotPath = ctx.getSettings().getPath(Inputs.PATH2);
    PathInfo supportPath = ctx.getSettings().getPath(Inputs.PATH3);

    List<Line2D> _lines = new ArrayList(lines);
    int lc = lineCount;

    FontInfo font = ctx.getSettings().getFont(Inputs.FONT); 
    if (font == null || font.getFont() == null) gc.setFont(new Font("Arial", Font.BOLD, 12));
    else gc.setFont(font.getFont());

    gc.setColor(ctx.getDefaults().getTextColor());
    FontMetrics fm = gc.getFontMetrics();
    boolean draw = resistancePath.isEnabled();
    
    int offset = -3;
    int l = 0;
    if (lc > 9) {
      if (draw) drawLabel(gc, _lines.get(l), R8, r8, fm, ctx, offset);
      l++;
      if (draw) drawLabel(gc, _lines.get(l), R7, r7, fm, ctx, offset);
      l++;
      if (draw) drawLabel(gc, _lines.get(l), R6, r6, fm, ctx, offset);
      l++;
      if (draw) drawLabel(gc, _lines.get(l), R5, r5, fm, ctx, offset);
      l++;
    }
    if (lc > 7) {
      if (draw) drawLabel(gc, _lines.get(l), R4, r4, fm, ctx, offset);
      l++;
    }
    if (lc > 5) {
      if (draw) drawLabel(gc, _lines.get(l), R3, r3, fm, ctx, offset);
      l++;
    }
    if (draw) drawLabel(gc, _lines.get(l), R2, r2, fm, ctx, offset);
    l++;
    if (draw) drawLabel(gc, _lines.get(l), R1, r1, fm, ctx, offset);
    l++;
    
    draw = pivotPath.isEnabled();
    
    if (draw) drawLabel(gc, _lines.get(l), P, p, fm, ctx, offset);
    l++;
    
    draw = supportPath.isEnabled();
    offset = fm.getHeight()-3;
    
    if (draw) drawLabel(gc, _lines.get(l), S1, s1, fm, ctx, offset);
    l++;
    if (draw) drawLabel(gc, _lines.get(l), S2, s2, fm, ctx, offset);
    l++;
    if (lc > 5) {
      if (draw) drawLabel(gc, _lines.get(l), S3, s3, fm, ctx, offset);
      l++;
    }
    if (lc > 7) {
      if (draw) drawLabel(gc, _lines.get(l), S4, s4, fm, ctx, offset);
      l++;
    }
    if (lc > 9) {
      if (draw) drawLabel(gc, _lines.get(l), S5, s5, fm, ctx, offset);
      l++;
      if (draw) drawLabel(gc, _lines.get(l), S6, s6, fm, ctx, offset);
      l++;
      if (draw) drawLabel(gc, _lines.get(l), S7, s7, fm, ctx, offset);
      l++;
      if (draw) drawLabel(gc, _lines.get(l), S8, s8, fm, ctx, offset);
      l++;
    }
  }

  private Line2D createLine(double val, int lx, int rx, DrawContext ctx)
  {
    int y = ctx.translateValue(val);
    return Util.clipLine(lx, y, rx, y, ctx.getBounds());
  }
  
  @Override
  public void layout(DrawContext ctx)
  {
    if (!isVisible(ctx)) return;
    int lc = lineCount;
    List<Line2D> _lines = new ArrayList();
    List<Line2D> _midLines = new ArrayList();
    Rectangle gb = ctx.getBounds();
    if (gb == null) return;
    int lx = ctx.translateTime(start);
    int rx = extendLines ? (int)ctx.getBounds().getMaxX() : ctx.translateTime(end);
    right = rx;
    
    if (lc > 9) {
      _lines.add(createLine(r8, lx, rx, ctx));
      _lines.add(createLine(r7, lx, rx, ctx));
      _lines.add(createLine(r6, lx, rx, ctx));
      _lines.add(createLine(r5, lx, rx, ctx));

      _midLines.add(createLine((r8+r7)/2, lx, rx, ctx));
      _midLines.add(createLine((r7+r6)/2, lx, rx, ctx));
      _midLines.add(createLine((r6+r5)/2, lx, rx, ctx));
      _midLines.add(createLine((r5+r4)/2, lx, rx, ctx));
    }
    if (lc > 7) {
      _lines.add(createLine(r4, lx, rx, ctx));
      _midLines.add(createLine((r4+r3)/2, lx, rx, ctx));
    }
    if (lc > 5) {
      _lines.add(createLine(r3, lx, rx, ctx));
      _midLines.add(createLine((r3+r2)/2, lx, rx, ctx));
    }
    _lines.add(createLine(r2, lx, rx, ctx));
    _midLines.add(createLine((r2+r1)/2, lx, rx, ctx));
    _lines.add(createLine(r1, lx, rx, ctx));
    _midLines.add(createLine((r1+p)/2, lx, rx, ctx));
    _lines.add(createLine(p, lx, rx, ctx));

    _lines.add(createLine(s1, lx, rx, ctx));
    _midLines.add(createLine((s1+p)/2, lx, rx, ctx));
    _lines.add(createLine(s2, lx, rx, ctx));
    _midLines.add(createLine((s2+s1)/2, lx, rx, ctx));

    if (lc > 5) {
      _lines.add(createLine(s3, lx, rx, ctx));
      _midLines.add(createLine((s3+s2)/2, lx, rx, ctx));
    }
    if (lc > 7) {
      _lines.add(createLine(s4, lx, rx, ctx));
      _midLines.add(createLine((s4+s3)/2, lx, rx, ctx));
    }
    if (lc > 9) {
      _lines.add(createLine(s5, lx, rx, ctx));
      _lines.add(createLine(s6, lx, rx, ctx));
      _lines.add(createLine(s7, lx, rx, ctx));
      _lines.add(createLine(s8, lx, rx, ctx));

      _midLines.add(createLine((s5+s4)/2, lx, rx, ctx));
      _midLines.add(createLine((s6+s5)/2, lx, rx, ctx));
      _midLines.add(createLine((s7+s6)/2, lx, rx, ctx));
      _midLines.add(createLine((s8+s7)/2, lx, rx, ctx));
    }
    
    bottomZones.clear();
    topZones.clear();
    for(Zone z : zones) {
      Point2D tl = new Point2D.Double(lx, ctx.translateValue(z.TOP));
      Point2D br = new Point2D.Double(rx, ctx.translateValue(z.BOTTOM));
      int x = (int)tl.getX();
      int y = (int)tl.getY();
      int x2 = (int)br.getX();
      int y2 = (int)br.getY();
      if (y < gb.y) y = gb.y;
      if (x < gb.x) x = gb.x;
      if (y2 > gb.y + gb.height) y2 = gb.y + gb.height;
      if (x2 > gb.x + gb.width) x2 = gb.x + gb.width;
      if (z.TOP > p) topZones.add(new Rectangle(x, y, x2-x, y2-y));
      else bottomZones.add(new Rectangle(x, y, x2-x, y2-y));
    }
    lines = _lines;
    midLines = _midLines;
  }
  
  @Override
  public boolean contains(double x, double y, DrawContext ctx)
  {
    if (!isVisible(ctx)) return false;
    if (Util.isEmpty(lines)) return false;
    List<Line2D> _lines = lines;
    for(Line2D line : _lines) {
      if (line == null) continue;
      if (Util.distanceFromLine(x, y, line) < 6) return true;
    }

    PathInfo midPath = ctx.getSettings().getPath(Inputs.PATH4);
    if (midPath != null && midPath.isEnabled()) {
      _lines = midLines;
      if (_lines != null) {
        for(Line2D line : _lines) {
          if (line == null) continue;
          if (Util.distanceFromLine(x, y, line) < 6) return true;
        }
      }
    }
    
    return false;
  }

  @Override
  public boolean isVisible(DrawContext ctx)
  {
    DataSeries series = ctx.getDataContext().getDataSeries();
    if (series.getEndTime(series.getEndIndex()) < start || series.getStartTime(series.getStartIndex()) > end) { 
      return false;
    }
    return true;
  }
  
  public double getMaxValue() { return r1; }
  public double getMinValue() { return s1; }
  
  public long getStart() { return start; }
  
  public boolean isExtendLines() { return extendLines; }
  public void setExtendLines(boolean b) { extendLines=b; }
  
  public double getP() { return p; }
  public double getR1() { return r1; }
  public double getR2() { return r2; }
  public double getR3() { return r3; }
  public double getR4() { return r4; }
  public double getR5() { return r5; }
  public double getR6() { return r6; }
  public double getR7() { return r7; }
  public double getR8() { return r8; }
  public double getS1() { return s1; }
  public double getS2() { return s2; }
  public double getS3() { return s3; }
  public double getS4() { return s4; }
  public double getS5() { return s5; }
  public double getS6() { return s6; }
  public double getS7() { return s7; }
  public double getS8() { return s8; }

  public String P = "P";
  public String R1 = "R1";
  public String R2 = "R2";
  public String R3 = "R3";
  public String R4 = "R4";
  public String R5 = "R5";
  public String R6 = "R6";
  public String R7 = "R7";
  public String R8 = "R8";
  public String S1 = "S1";
  public String S2 = "S2";
  public String S3 = "S3";
  public String S4 = "S4";
  public String S5 = "S5";
  public String S6 = "S6";
  public String S7 = "S7";
  public String S8 = "S8";

  public long start, end;

  private int lineCount;
  private int right;
  private boolean extendLines=false;
  private double p, r1, r2, r3, r4, r5, r6, r7, r8;
  private double s1, s2, s3, s4, s5, s6, s7, s8;
  private List<Zone> zones = new ArrayList();    
  
  private List<Rectangle> topZones = new ArrayList();    
  private List<Rectangle> bottomZones = new ArrayList();    
  private List<Line2D> lines;    
  private List<Line2D> midLines;    
  
  protected static class Zone
  {
    Zone(double top, double bottom)
    {
      TOP = top;
      BOTTOM = bottom;
    }
    
    double TOP;
    double BOTTOM;
  }
}