package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.Position;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.SwingPoint;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Draws lines between Swing Points and optionally shows labels */
@StudyHeader(
    namespace="com.motivewave", 
    id="SWING_POINTS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_SWING_POINTS", 
    desc="DESC_SWING_POINTS",
    menu="MENU_OVERLAY",
    supportsBarUpdates=false,
    overlay=true,
    helpLink="http://www.motivewave.com/studies/swing_points.htm")
public class SwingPoints extends Study 
{
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.STRENGTH, get("LBL_STRENGTH"), 10, 1, 9999, 1));
    
    var display = tab.addGroup(get("LBL_DISPLAY"));
    var line = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getRed(), 1.0f, null, true, true, false, true); 
    line.setColor2(defaults.getBlue());
    display.addRow(line);
    display.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    display.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.STRENGTH, get("LBL_STRENGTH"), 2, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.UP_MARKER, Inputs.DOWN_MARKER);

    var desc = createRD();
    desc.setLabelSettings(Inputs.STRENGTH);
  }
  
  @Override
  public void onBarClose(DataContext ctx)
  {
    calculateValues(ctx);
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    int strength = getSettings().getInteger(Inputs.STRENGTH);
    var lineInfo = getSettings().getPath(Inputs.PATH);
    var series = ctx.getDataSeries();
    clearFigures();
    var swingPoints = series.calcSwingPoints(strength);
    
    // Add the markers
    var upMarker = getSettings().getMarker(Inputs.UP_MARKER);
    var downMarker = getSettings().getMarker(Inputs.DOWN_MARKER);
    for(var sp : swingPoints) {
      if (sp.isTop() && downMarker.isEnabled()) {
        addFigure(new Marker(sp.getCoordinate(), Position.TOP, downMarker));
      }
      else if (!sp.isTop() && upMarker.isEnabled()) {
        addFigure(new Marker(sp.getCoordinate(), Position.BOTTOM, upMarker));
      }
    }
    
    if (!lineInfo.isEnabled()) return;
    
    int si=-1;
    int li=-1;
    for(int i = 0; i < swingPoints.size(); i++) {
      if (si == -1) {
        si = i;
        continue;
      }

      var sp = swingPoints.get(i);
      var start = swingPoints.get(si);
      
      if (start.isTop() && sp.isTop()) {
        SwingPoint bottom = null;
        int bi = -1;
        for(int j = si; j < i; j++) {
          var tmp = swingPoints.get(j);
          if (tmp.isTop()) continue;
          if (bottom == null) {
            bottom = tmp;
            bi = j;
          }
          else if (tmp.getValue() < bottom.getValue()) {
            bottom = tmp;
            bi = j;
          }
        }

        if (bottom == null) {
          // all top points, choose the highest point
          for(int j = si; j <= i; j++) {
            if (swingPoints.get(j).getValue() > swingPoints.get(si).getValue()) si = j;
          }
          continue;
        }

        // Add a line
        var line = new Line(start.getCoordinate(), bottom.getCoordinate(), lineInfo);
        if (li != -1) {
          var prev = swingPoints.get(li);
          if (prev.getValue() < start.getValue() && prev.getValue() > bottom.getValue()) {
            line.setColorBelow(prev.getValue(), lineInfo.getColor2());
          }
        }
        addFigure(line);

        li = si;
        si = bi;
      }
      else if (start.isBottom() && sp.isBottom()) {
        SwingPoint top = null;
        int ti = -1;
        for(int j = si; j < i; j++) {
          var tmp = swingPoints.get(j);
          if (tmp.isBottom()) continue;
          if (top == null) {
            top = tmp;
            ti = j;
          }
          else if (tmp.getValue() > top.getValue()) {
            top = tmp;
            ti = j;
          }
        }

        if (top == null) {
          // all bottom points, choose the lowest point
          for(int j = si; j <= i; j++) {
            if (swingPoints.get(j).getValue() < swingPoints.get(si).getValue()) si = j;
          }
          continue;
        }
        
        // Add a line
        Line line = new Line(start.getCoordinate(), top.getCoordinate(), lineInfo);
        if (li != -1) {
          var prev = swingPoints.get(li);
          if (prev.getValue() > start.getValue() && prev.getValue() < top.getValue()) {
            line.setColorAbove(prev.getValue(), lineInfo.getColor2());
          }
        }
        addFigure(line);
        
        li = si;
        si = ti;
      }
    }

    // Add the final line (if required)
    if (si != -1) {
      var start = swingPoints.get(si);
      SwingPoint end = null;
      for(int i = si+1; i < swingPoints.size(); i++) {
        var sp = swingPoints.get(i);
        if (start.isTop() && sp.isBottom()) {
          if (end == null) end = sp;
          else if (sp.getValue() < end.getValue()) end = sp;
        }
        else if (start.isBottom() && sp.isTop()) {
          if (end == null) end = sp;
          else if (sp.getValue() > end.getValue()) end = sp;
        }
      }
      
      if (end == null) return;
      
      // Add a line
      Line line = new Line(start.getCoordinate(), end.getCoordinate(), lineInfo);
      if (li != -1) {
        var prev = swingPoints.get(li);
        if (end.isTop()) {
          if (prev.getValue() > start.getValue() && prev.getValue() < end.getValue()) {
            line.setColorAbove(prev.getValue(), lineInfo.getColor2());
          }
        }
        else {
          if (prev.getValue() < start.getValue() && prev.getValue() > end.getValue()) {
            line.setColorBelow(prev.getValue(), lineInfo.getColor2());
          }
        }
        
      }
      addFigure(line);
    }
    
  }
}
