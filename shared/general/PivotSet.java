package com.motivewave.platform.study.general;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.draw.Figure;

public class PivotSet extends Figure 
{
  public final static String PIVOT_TYPE = "pivotType", SHOW_ALL_PIVOTS = "showAllPivots", SHOW_LABELS = "showLabels", EXTEND_RIGHT = "extendRight";
  public final static String LINE = "line", LINE2 = "line2", LINE3 = "line3", FONT = "font";
  public final static String CLASSIC = "Classic", WOODIE = "Woodie", CAMARILLA = "Camarilla",
                             FIBONACCI = "Fibonacci", FIB_ZONE = "Fib Zone", FIB_ZONE2 = "FibZone2";

  public PivotSet(long xstart, long xend, double xP, double xR1, double xR2, double xS1, double xS2)
  {
    p = xP;
    r1 = xR1;
    r2 = xR2;
    s1 = xS1;
    s2 = xS2;
    LC = 5;
    start = xstart;
    end = xend;
  }
  
  public PivotSet(long xstart, long xend, double xP, double xR1, double xR2, double xR3, double xS1, double xS2, double xS3)
  {
    p = xP;
    r1 = xR1;
    r2 = xR2;
    r3 = xR3;
    s1 = xS1;
    s2 = xS2;
    s3 = xS3;
    LC = 7;
    start = xstart;
    end = xend;
  }
  
  public PivotSet(long xstart, long xend, double xP, double xR1, double xR2, double xR3, double xR4, double xS1, double xS2, double xS3, double xS4)
  {
    p = xP;
    r1 = xR1;
    r2 = xR2;
    r3 = xR3;
    r4 = xR4;
    s1 = xS1;
    s2 = xS2;
    s3 = xS3;
    s4 = xS4;
    LC = 9;
    start = xstart;
    end = xend;
  }

  public PivotSet(long xstart, long xend, double xP, double xR1, double xR2, double xR3, double xR4, double xR5, double xR6, double xR7, double xR8, 
      double xS1, double xS2, double xS3, double xS4, double xS5, double xS6, double xS7, double xS8)
  {
    p = xP;
    r1 = xR1;
    r2 = xR2;
    r3 = xR3;
    r4 = xR4;
    r5 = xR5;
    r6 = xR6;
    r7 = xR7;
    r8 = xR8;
    s1 = xS1;
    s2 = xS2;
    s3 = xS3;
    s4 = xS4;
    s5 = xS5;
    s6 = xS6;
    s7 = xS7;
    s8 = xS8;
    LC = 17;
    start = xstart;
    end = xend;
  }

  public void addZone(double top, double bottom)
  {
    if (top > bottom) zones.add(new Zone(top, bottom));
    else zones.add(new Zone(bottom, top));
  }
  
  @Override
  public void draw(Graphics2D gc, DrawContext ctx)
  {
    if (!isVisible(ctx) || Util.isEmpty(lines)) return;
    List<Line2D> _lines = new ArrayList(lines);
    int lc = LC;
    
    var resistancePath = ctx.getSettings().getPath(LINE);
    var pivotPath = ctx.getSettings().getPath(LINE2);
    var supportPath = ctx.getSettings().getPath(LINE3);
    for(var r : topZones) {
      gc.setColor(Util.getAlphaFill(resistancePath.getColor()));
      gc.fill(r);
    }

    // Resistance Lines
    gc.setColor(resistancePath.getColor());
    gc.setStroke(Util.getStroke(resistancePath, ctx.isSelected()));
    drawLine(gc, _lines.get(0));
    drawLine(gc, _lines.get(1));
    if (lc > 5) drawLine(gc, _lines.get(2));
    if (lc > 7) drawLine(gc, _lines.get(3));
    if (lc > 9) {
      drawLine(gc, _lines.get(4));
      drawLine(gc, _lines.get(5));
      drawLine(gc, _lines.get(6));
      drawLine(gc, _lines.get(7));
    }
    int s = 2;
    if (lc > 5) s = 3;
    if (lc > 7) s = 4;
    if (lc > 9) s = 8;
    
    // Pivot Line
    gc.setColor(pivotPath.getColor());
    gc.setStroke(Util.getStroke(pivotPath, ctx.isSelected()));
    drawLine(gc, _lines.get(s));

    for(var r : bottomZones) {
      gc.setColor(Util.getAlphaFill(supportPath.getColor()));
      gc.fill(r);
    }

    // Support Lines
    gc.setColor(supportPath.getColor());
    gc.setStroke(Util.getStroke(supportPath, ctx.isSelected()));
    drawLine(gc, _lines.get(s+1));
    drawLine(gc, _lines.get(s+2));
    if (lc > 5) drawLine(gc, _lines.get(s+3));
    if (lc > 7) drawLine(gc, _lines.get(s+4));
    if (lc > 9) {
      drawLine(gc, _lines.get(s+5));
      drawLine(gc, _lines.get(s+6));
      drawLine(gc, _lines.get(s+7));
      drawLine(gc, _lines.get(s+8));
    }
    drawLabels(gc, ctx);
  }

  private void drawLine(Graphics2D gc, Line2D line)
  {
    if (line == null) return;
    gc.draw(line);
  }
  
  private void drawLabel(Graphics2D gc, Line2D line, String lbl, FontMetrics fm, DrawContext ctx, int offset)
  {
    if (line == null) return;
    gc.drawString(lbl, getLX(lbl, fm, ctx), (int)(line.getP2().getY() + offset));
  }
  
  private int getLX(String lbl, FontMetrics fm, DrawContext ctx)
  {
    int x = right;
    int w = fm.stringWidth(lbl);
    x -= w;
    int gr = (int)ctx.getBounds().getMaxX();
    if (x + w > gr) return gr - w - 5; 
    return x;
  }
  
  private void drawLabels(Graphics2D gc, DrawContext ctx)
  {
    if (!isVisible(ctx)) return;
    if (Util.isEmpty(lines)) return;
    if (!ctx.getSettings().getBoolean(SHOW_LABELS, true)) return;

    List<Line2D> _lines = new ArrayList(lines);
    int lc = LC;

    var fi = ctx.getSettings().getFont(FONT); 
    gc.setFont(fi.getFont());
    gc.setColor(ctx.getDefaults().getTextColor());
    var fm = gc.getFontMetrics();
    int offset = -3;
    int l = 0;
    if (lc > 9) {
      drawLabel(gc, _lines.get(l), R8, fm, ctx, offset); l++;
      drawLabel(gc, _lines.get(l), R7, fm, ctx, offset); l++;
      drawLabel(gc, _lines.get(l), R6, fm, ctx, offset); l++;
      drawLabel(gc, _lines.get(l), R5, fm, ctx, offset); l++;
    }
    if (lc > 7) { drawLabel(gc, _lines.get(l), R4, fm, ctx, offset); l++; }
    if (lc > 5) { drawLabel(gc, _lines.get(l), R3, fm, ctx, offset); l++; }
    
    drawLabel(gc, _lines.get(l), R2, fm, ctx, offset); l++;
    drawLabel(gc, _lines.get(l), R1, fm, ctx, offset); l++;
    drawLabel(gc, _lines.get(l), P, fm, ctx, offset); l++;
    
    offset = fm.getHeight()-3;
    
    drawLabel(gc, _lines.get(l), S1, fm, ctx, offset); l++;
    drawLabel(gc, _lines.get(l), S2, fm, ctx, offset); l++;
    if (lc > 5) { drawLabel(gc, _lines.get(l), S3, fm, ctx, offset); l++; }
    if (lc > 7) { drawLabel(gc, _lines.get(l), S4, fm, ctx, offset); l++; }
    if (lc > 9) {
      drawLabel(gc, _lines.get(l), S5, fm, ctx, offset); l++;
      drawLabel(gc, _lines.get(l), S6, fm, ctx, offset); l++;
      drawLabel(gc, _lines.get(l), S7, fm, ctx, offset); l++;
      drawLabel(gc, _lines.get(l), S8, fm, ctx, offset); l++;
    }
  }

  @Override
  public void layout(DrawContext ctx)
  {
    if (!isVisible(ctx)) return;
    int lc = LC;
    List<Line2D> _lines = new ArrayList();
    Rectangle gb = ctx.getBounds();
    if (gb == null) return;
    int lx = ctx.translateTime(start);
    int rx = extendLines ? (int)ctx.getBounds().getMaxX() : ctx.translateTime(end);
    right = rx;
    
    if (lc > 9) {
      _lines.add(Util.clipLine(lx, ctx.translateValue(r8), rx, ctx.translateValue(r8), gb));
      _lines.add(Util.clipLine(lx, ctx.translateValue(r7), rx, ctx.translateValue(r7), gb));
      _lines.add(Util.clipLine(lx, ctx.translateValue(r6), rx, ctx.translateValue(r6), gb));
      _lines.add(Util.clipLine(lx, ctx.translateValue(r5), rx, ctx.translateValue(r5), gb));
    }
    if (lc > 7) _lines.add(Util.clipLine(lx, ctx.translateValue(r4), rx, ctx.translateValue(r4), gb));
    if (lc > 5) _lines.add(Util.clipLine(lx, ctx.translateValue(r3), rx, ctx.translateValue(r3), gb));
    _lines.add(Util.clipLine(lx, ctx.translateValue(r2), rx, ctx.translateValue(r2), gb));
    _lines.add(Util.clipLine(lx, ctx.translateValue(r1), rx, ctx.translateValue(r1), gb));
    _lines.add(Util.clipLine(lx, ctx.translateValue(p), rx, ctx.translateValue(p), gb));

    _lines.add(Util.clipLine(lx, ctx.translateValue(s1), rx, ctx.translateValue(s1), gb));
    _lines.add(Util.clipLine(lx, ctx.translateValue(s2), rx, ctx.translateValue(s2), gb));

    if (lc > 5) _lines.add(Util.clipLine(lx, ctx.translateValue(s3), rx, ctx.translateValue(s3), gb));
    if (lc > 7) _lines.add(Util.clipLine(lx, ctx.translateValue(s4), rx, ctx.translateValue(s4), gb));
    if (lc > 9) {
      _lines.add(Util.clipLine(lx, ctx.translateValue(s5), rx, ctx.translateValue(s5), gb));
      _lines.add(Util.clipLine(lx, ctx.translateValue(s6), rx, ctx.translateValue(s6), gb));
      _lines.add(Util.clipLine(lx, ctx.translateValue(s7), rx, ctx.translateValue(s7), gb));
      _lines.add(Util.clipLine(lx, ctx.translateValue(s8), rx, ctx.translateValue(s8), gb));
    }
    
    bottomZones.clear();
    topZones.clear();
    for(var z : zones) {
      var tl = new Point2D.Double(lx, ctx.translateValue(z.TOP));
      var br = new Point2D.Double(rx, ctx.translateValue(z.BOTTOM));
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
  }
  
  @Override
  public boolean contains(double x, double y, DrawContext ctx)
  {
    if (!isVisible(ctx)) return false;
    if (Util.isEmpty(lines)) return false;
    for(var line : lines) {
      if (line == null) continue;
      if (Util.distanceFromLine(x, y, line) < 6) return true;
    }
    return false;
  }

  @Override
  public boolean isVisible(DrawContext ctx)
  {
    var series = ctx.getDataContext().getDataSeries();
    if (series.getEndTime(series.getEndIndex()) < start || series.getStartTime(series.getStartIndex()) > end) return false;
    return true;
  }
  
  public double getMaxValue() { return r1; }
  public double getMinValue() { return s1; }
  
  public long getStart() { return start; }
  
  public boolean isExtendLines() { return extendLines; }
  public void setExtendLines(boolean b) { extendLines=b; }

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

  private int LC;
  private int right;
  private boolean extendLines=false;
  private double p;
  private double r1, r2, r3, r4, r5, r6, r7, r8;
  private double s1, s2, s3, s4, s5, s6, s7, s8;
  private List<Zone> zones = new ArrayList();    
  private List<Rectangle> topZones = new ArrayList();    
  private List<Rectangle> bottomZones = new ArrayList();    
  private List<Line2D> lines;    
  
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

