package com.motivewave.platform.study.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.SwingPoint;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.LabelDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Auto Trend Line */
@StudyHeader(
    namespace="com.motivewave", 
    id="TREND_LINE", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_TREND_LINE", 
    desc="DESC_TREND_LINE",
    menu="MENU_OVERLAY",
    overlay=true,
    supportsBarUpdates=false,
    helpLink="http://www.motivewave.com/studies/auto_trend_line.htm")
public class AutoTrendLine extends Study 
{
  final static String EXT_BARS = "extBars", MULTIPLE_LINES = "multipleLines", HISTORICAL_LINES = "historicalLines";
  final static String TOP_LINES = "topLines", BOTTOM_LINES = "bottomLines";
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.TOP_STRENGTH, get("LBL_TOP_STRENGTH"), 8, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.BOTTOM_STRENGTH, get("LBL_BOTTOM_STRENGTH"), 8, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(EXT_BARS, get("LBL_EXT_BARS"), 20, 1, 9999, 1));
    inputs.addRow(new LabelDescriptor(""), new BooleanDescriptor(MULTIPLE_LINES, get("LBL_MULTIPLE_LINES"), true, false));
    inputs.addRow(new LabelDescriptor(""), new BooleanDescriptor(HISTORICAL_LINES, get("LBL_HISTORICAL_LINES"), false, false));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(TOP_LINES, get("LBL_TOP_LINES"), defaults.getRed(), 1.0f, null, true, false, true));
    colors.addRow(new PathDescriptor(BOTTOM_LINES, get("LBL_BOTTOM_LINES"), defaults.getGreen(), 1.0f, null, true, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.TOP_STRENGTH, get("LBL_TOP_STRENGTH"), 8, 1, 9999, true));
    sd.addQuickSettings(new SliderDescriptor(Inputs.BOTTOM_STRENGTH, get("LBL_BOTTOM_STRENGTH"), 8, 1, 9999, true));
    sd.addQuickSettings(new SliderDescriptor(EXT_BARS, get("LBL_EXT_BARS"), 20, 1, 9999, true, () -> Enums.Icon.ARROW_RIGHT.get()));
    sd.addQuickSettings(MULTIPLE_LINES, HISTORICAL_LINES);
    sd.addQuickSettings(TOP_LINES, BOTTOM_LINES);

    var desc = createRD();
    desc.setLabelSettings(Inputs.TOP_STRENGTH, Inputs.BOTTOM_STRENGTH, EXT_BARS);
  }
  
  @Override
  public void clearState()
  {
    super.clearState();
    topPoints = bottomPoints = null;
    lineMap.clear();
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var settings = getSettings();
    int topStrength = settings.getInteger(Inputs.TOP_STRENGTH, 8);
    int bottomStrength = settings.getInteger(Inputs.BOTTOM_STRENGTH, 8);
    int extBars = settings.getInteger(EXT_BARS, 20);
    boolean multiple = settings.getBoolean(MULTIPLE_LINES, true);
    boolean historical = settings.getBoolean(HISTORICAL_LINES, false);
    var topLines = settings.getPath(TOP_LINES);
    var bottomLines = settings.getPath(BOTTOM_LINES);
    var series = ctx.getDataSeries();
    
    // Keep track of the new lines that we have created and add them if appropriate (see below)
    List<Figure> lines = new ArrayList<>();
    
    if (topLines.isEnabled()) { // Calculate Trend Lines from the top swing points
      // Optimization: reuse the swing points from the last calculation (if available).
      // The swing points calculation can be very expensive if there is lot of data.
      var points = topPoints;
      if (!Util.isEmpty(points)) {
        var swingPoints = series.calcSwingPoints(true, topStrength, topStrength*2 + 1);
        var last = points.get(points.size()-1);
        for(var sp : swingPoints) { // add the new swing points (if any)
          if (sp.getIndex() > last.getIndex()) points.add(sp);
        }
      }
      else points = series.calcSwingPoints(true, topStrength);
      topPoints = points; // Save for the next time we need to calculate values.
      
      if (points != null) { // create the lines from the swing points
        int lc = 0; // line count
        for(int i = points.size()-1; i >= 0; i--) {
          // Note: there may be multiple trend lines off of every point (see Multiple Lines attribute)
          var p1 = points.get(i);
          double val = Double.NEGATIVE_INFINITY;
          for(int j = i+1; j < points.size(); j++) {
            var p2 = points.get(j);
            if (p2.getValue() > p1.getValue()) break; // downward trend is broken
            if (p2.getValue() > val) {
              // Does it exist already?
              if (!lineMap.containsKey(p1.getIndex() + "T:" + p2.getIndex())) {
                // Create the line and calculate out by the forward bars
                var line = new Line(p1.getTime(), p1.getValue(), p2.getTime(), p2.getValue());
                line.setStroke(topLines.getStroke());
                line.setColor(topLines.getColor());
                line.setExtendRight(extBars);
                lines.add(line);
                lineMap.put(p1.getIndex() + "T:" + p2.getIndex(), line);
              }
              val = p2.getValue();
              lc++;
              if (!multiple) break;
            }
          }
          
          if (!historical && lc > 0) break;
        }
      }
    }
    
    if (bottomLines.isEnabled()) { // Calculate Trend Lines from the bottom swing points
      // Optimization: reuse the swing points from the last calculation (if available).
      // The swing points calculation can be very expensive if there is lot of data.
      var points = bottomPoints;
      if (!Util.isEmpty(points)) {
        var swingPoints = series.calcSwingPoints(false, bottomStrength, bottomStrength*2 + 1);
        var last = points.get(points.size()-1);
        for(var sp : swingPoints) {
          if (sp.getIndex() > last.getIndex()) points.add(sp);
        }
      }
      else points = series.calcSwingPoints(false, bottomStrength);
      bottomPoints = points; // Save for the next time we need to calculate values.

      if (points != null) {
        int lc = 0;
        for(int i = points.size()-1; i >= 0; i--) {
          // Note: there may be multiple trend lines off of every point (see Multiple Lines attribute)
          var p1 = points.get(i);
          double val = Double.POSITIVE_INFINITY;
          for(int j = i+1; j < points.size(); j++) {
            var p2 = points.get(j);
            if (p2.getValue() < p1.getValue()) break; // downward trend is broken
            if (p2.getValue() < val) {
              // calculate out by the forward bars
              if (!lineMap.containsKey(p1.getIndex() + "B:" + p2.getIndex())) {
                var line = new Line(p1.getTime(), p1.getValue(), p2.getTime(), p2.getValue());
                line.setStroke(bottomLines.getStroke());
                line.setColor(bottomLines.getColor());
                line.setExtendRight(extBars);
                lines.add(line);
                lineMap.put(p1.getIndex() + "B:" + p2.getIndex(), line);
              }
              val = p2.getValue();
              lc++;
              if (!multiple) break;
            }
          }
          
          if (!historical && lc > 0) break;
        }
      }
    }
    
    if (Util.isEmpty(lines)) return;
    
    // Add the new lines to the default plot
    beginFigureUpdate();
    addFigures(lines);
    endFigureUpdate();
  }

  // Optimization: keep the last set of top and bottom points to avoid having to recalculate on every new bar.
  // We need to throw these away if the bar size changes...
  private List<SwingPoint> topPoints, bottomPoints;
  private Map<String, Line> lineMap = new HashMap<>();
}
