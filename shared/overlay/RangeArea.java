package com.motivewave.platform.study.overlay;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.draw.Figure;

/** Displays an (optionally) shaded range area. */
public class RangeArea extends Figure
{
  public RangeArea(long start, long end, double top, double bottom)
  {
    this.top = top;
    this.bottom = bottom;
    this.start = start;
    this.end = end;
  }
  
  @Override
  public void draw(Graphics2D gc, DrawContext ctx)
  {
    if (!isVisible(ctx)) return;
    Line2D tl = topLine;
    Line2D ml = middleLine;
    Line2D bl = bottomLine;
    Rectangle _area = area;
    
    var fill = ctx.getSettings().getColorInfo(Inputs.FILL);
    if (_area != null && fill.isEnabled()) {
      gc.setColor(fill.getColor());
      gc.fill(_area);
    }
    
    var topInfo = ctx.getSettings().getPath(Inputs.TOP_PATH);
    var middleInfo = ctx.getSettings().getPath(Inputs.MIDDLE_PATH);
    var bottomInfo = ctx.getSettings().getPath(Inputs.BOTTOM_PATH);

    if (tl != null && topInfo.isEnabled()) {
      gc.setColor(topInfo.getColor());
      gc.setStroke(Util.getStroke(topInfo, ctx.isSelected()));
      gc.draw(tl);
    }

    if (ml != null && middleInfo.isEnabled()) {
      gc.setColor(middleInfo.getColor());
      gc.setStroke(Util.getStroke(middleInfo, ctx.isSelected()));
      gc.draw(ml);
    }

    if (bl != null && bottomInfo.isEnabled()) {
      gc.setColor(bottomInfo.getColor());
      gc.setStroke(Util.getStroke(bottomInfo, ctx.isSelected()));
      gc.draw(bl);
    }
    
    var labelInfo = ctx.getSettings().getFont(PrevPeriodRange.LABELS);
    if (!labelInfo.isEnabled()) return;
    
    var instr = ctx.getDataContext().getInstrument();
    gc.setFont(labelInfo.getFont());
    gc.setColor(labelInfo.getColor());
    var fm = gc.getFontMetrics();

    if (tl != null && topInfo.isEnabled()) {
      String topLbl = "H:" + instr.format(top);
      var p = tl.getP2();
      if (p != null) gc.drawString(topLbl, getLX(topLbl, fm, ctx), (int)(p.getY() -3));
    }

    if (ml != null && middleInfo.isEnabled()) {
      String middleLbl = "M:" + instr.format((top + bottom)/2);
      var p = ml.getP2();
      if (p != null) gc.drawString(middleLbl, getLX(middleLbl, fm, ctx), (int)(p.getY() -3));
    }

    if (bl != null && bottomInfo.isEnabled()) {
      String bottomLbl = "L:" + instr.format(bottom);
      var p = bl.getP2();
      if (p != null) gc.drawString(bottomLbl, getLX(bottomLbl, fm, ctx), (int)(p.getY() -3));
    }
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
    return x;
  }

  @Override
  public void layout(DrawContext ctx)
  {
    if (!isVisible(ctx)) return;
    var gb = ctx.getBounds();
    if (gb == null) return;
    int lx = ctx.translateTime(start);
    
    int rx = extendLines ? (int)ctx.getBounds().getMaxX() : ctx.translateTime(end);
    right = rx;
    int ty = ctx.translateValue(top);
    int by = ctx.translateValue(bottom);
    int my = (ty + by)/2;

    topLine = Util.clipLine(lx, ty, rx, ty, gb);
    middleLine = Util.clipLine(lx, my, rx, my, gb);
    bottomLine = Util.clipLine(lx, by, rx, by, gb);
    area = new Rectangle(lx, ty, rx - lx, by - ty);
  }
  
  @Override
  public boolean contains(double x, double y, DrawContext ctx)
  {
    if (!isVisible(ctx)) return false;
    var topInfo = ctx.getSettings().getPath(Inputs.TOP_PATH);
    var middleInfo = ctx.getSettings().getPath(Inputs.MIDDLE_PATH);
    var bottomInfo = ctx.getSettings().getPath(Inputs.BOTTOM_PATH);
    if (topLine != null && topInfo.isEnabled() &&  Util.distanceFromLine(x, y, topLine) < 6) return true;
    if (middleLine != null && middleInfo.isEnabled() && Util.distanceFromLine(x, y, middleLine) < 6) return true;
    if (bottomLine != null && bottomInfo.isEnabled() && Util.distanceFromLine(x, y, bottomLine) < 6) return true;
    return false;
  }

  @Override
  public boolean isVisible(DrawContext ctx)
  {
    var series = ctx.getDataContext().getDataSeries();
    if (series.getEndTime(series.getEndIndex()) < start || series.getStartTime(series.getStartIndex()) > end) return false;
    return true;
  }
  
  public boolean isExtendLines() { return extendLines; }
  public void setExtendLines(boolean b) { extendLines=b; }
  
  public double getHigh() { return top; }
  public double getLow() { return bottom; }
  public double getMiddle() { return (top + bottom)/2; }
  public long getStart() { return start; }
  public long getEnd() { return end; }
  
  private long start, end;
  private int right;
  private double top, bottom;
  private Line2D topLine, middleLine, bottomLine;
  private Rectangle area;
  private boolean extendLines=false;
}

