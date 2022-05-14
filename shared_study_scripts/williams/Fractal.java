package com.motivewave.platform.study.williams;

import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.Position;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.SwingPoint;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Fractal (Bill Williams) */
@StudyHeader(
    namespace="com.motivewave", 
    id="FRACTAL", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_FRACTAL", 
    desc="DESC_FRACTAL",
    menu="MENU_OVERLAY",
    menu2="MENU_BILL_WILLIAMS",
    overlay=true,
    helpLink="http://www.motivewave.com/studies/fractal.htm")
public class Fractal extends Study 
{
  final static String SHOW_LABEL = "showLabel", ALT_HIGH_LOW="ahl";
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new IntegerDescriptor(Inputs.STRENGTH, get("LBL_STRENGTH"), 2, 1, 999, 1));
    grp.addRow(new BooleanDescriptor(ALT_HIGH_LOW, get("LBL_ALT_HIGH_LOW"), true, false));

    grp = tab.addGroup(get("LBL_MARKERS"));
    grp.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    grp.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));
    grp.addRow(new BooleanDescriptor(SHOW_LABEL, get("LBL_SHOW_LABEL"), false, false));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.STRENGTH, get("LBL_STRENGTH"), 2, 1, 999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(ALT_HIGH_LOW, Inputs.UP_MARKER, Inputs.DOWN_MARKER, SHOW_LABEL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.STRENGTH);
  }
  
  @Override
  public void onBarOpen(DataContext ctx)
  {
    onBarClose(ctx);
  }

  @Override
  public void onBarClose(DataContext ctx)
  {
    // Optimization: we don't need to recalculate for all bars
    var series = ctx.getDataSeries();
    var swingPoints = series.calcSwingPoints(strength, strength*2 + 1);
    
    // This is tricky.  If we have unconfirmed fractals, either remove them or consider them confirmed
    if (unconfirmedTop != null || unconfirmedBottom != null) {
      if (Util.isEmpty(swingPoints)) {
        removeFigure(unconfirmedTop);
        removeFigure(unconfirmedBottom);
      }
      else {
        boolean foundTop = false, foundBottom = false;
        for(var sp : new ArrayList<>(swingPoints)) {
          if (unconfirmedTop != null && sp.getTime() == unconfirmedTop.getTime()) foundTop = true;
          if (unconfirmedBottom != null && sp.getTime() == unconfirmedBottom.getTime()) foundBottom = true;
          if (foundTop || foundBottom) swingPoints.remove(sp);
        }
        if (!foundTop) removeFigure(unconfirmedTop);
        if (!foundBottom) removeFigure(unconfirmedBottom);
      }
    }
    addMarkers(swingPoints, series, prevUp);
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    if (calculated) return;
    var series = ctx.getDataSeries();
    clearFigures();
    var swingPoints = series.calcSwingPoints(strength);
    addMarkers(swingPoints, series, null);
    calculated = true;
  }

  private void addMarkers(List<SwingPoint> swingPoints, DataSeries series, Boolean pu)
  {
    unconfirmedTop = unconfirmedBottom = null;
    if (Util.isEmpty(swingPoints)) return;
    int ind = series.size() - strength -1;
    var instr = series.getInstrument();
    for(var sp : swingPoints) {
      if (altHighLow && pu != null) {
        if (sp.isTop() && pu) continue;
        if (sp.isBottom() && !pu) continue;
      }
      
      if (sp.isTop() && upMarker.isEnabled()) {
        var marker = new Marker(sp.getCoordinate(), Position.BOTTOM, upMarker);
        marker.setOffsetPixels(calcOffset(upMarker));
        marker.setTextValue(showLabel ? instr.format(sp.getValue()) : null);
        marker.setTextPosition(Position.TOP);
        addFigure(marker);
        if (sp.getIndex() >= ind) {
          unconfirmedTop = marker;
        }
      }
      else if (!sp.isTop() && downMarker.isEnabled()) {
        var marker = new Marker(sp.getCoordinate(), Position.TOP, downMarker);
        marker.setOffsetPixels(calcOffset(downMarker));
        marker.setTextValue(showLabel ? instr.format(sp.getValue()) : null);
        marker.setTextPosition(Position.BOTTOM);
        addFigure(marker);
        if (sp.getIndex() >= ind) {
          unconfirmedBottom = marker;
        }
      }
      pu = sp.isTop();
    }
    prevUp = pu;
  }

  // Hack: We setting the position as TOP when at the bottom of the candle and BOTTOM when at the top of the candle
  // This causes the marker to display in the candle body.  To work around set a negative offset....
  private int calcOffset(MarkerInfo marker)
  {
    switch(marker.getSize()) {
    case VERY_LARGE: return -28;
    case LARGE: return -23;
    case MEDIUM: return -17;
    case SMALL: return -13;
    }
    return -10;
  }
  
  @Override
  public void clearState()
  {
    super.clearState();
    strength = getSettings().getInteger(Inputs.STRENGTH, 2);
    altHighLow = getSettings().getBoolean(ALT_HIGH_LOW, true);
    upMarker = getSettings().getMarker(Inputs.UP_MARKER);
    downMarker = getSettings().getMarker(Inputs.DOWN_MARKER);
    showLabel = getSettings().getBoolean(SHOW_LABEL, false);
    calculated = false;
    unconfirmedTop = unconfirmedBottom = null;
  }

  // Store the last unconfirmed fractals.
  // A fractal may be unconfirmed if its in the range of the last bar and the last bar is not complete
  Marker unconfirmedTop, unconfirmedBottom;
  MarkerInfo upMarker, downMarker;
  boolean calculated = false, altHighLow=true, showLabel = false;
  Boolean prevUp = null;
  int strength;
}
