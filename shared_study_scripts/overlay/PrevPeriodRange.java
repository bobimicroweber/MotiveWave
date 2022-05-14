package com.motivewave.platform.study.overlay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Previous Period Range.  Displays the range of the previous period as a shaded area and/or top and bottom lines
    By default this study will show the range of the previous day. */
@StudyHeader(
    namespace="com.motivewave", 
    id="PREV_PERIOD_RANGE", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_PREV_PERIOD_RANGE", 
    label="LBL_PREV_PERIOD_RANGE",
    desc="DESC_PREV_PERIOD_RANGE",
    menu="MENU_OVERLAY",
    overlay=true,
    supportsBarUpdates=false,
    helpLink="http://www.motivewave.com/studies/previous_period_range.htm")
public class PrevPeriodRange extends Study 
{
  final static String RANGE = "range", SHOW_ALL = "showAll", EXTEND_RIGHT = "extendRight";
  final static String LABELS = "labels", HIGH_IND = "highInd", LOW_IND = "lowInd", MID_IND = "midInd";
  
  enum Values { HIGH, LOW, MID }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new BarSizeDescriptor(Inputs.BARSIZE, get("LBL_TIMEFRAME"), BarSize.getBarSize(BarSizeType.LINEAR, 1440)));
    inputs.addRow(new DoubleDescriptor(RANGE, get("LBL_RANGE_PER"), 100, 0.1, 999, 0.1));
    inputs.addRow(new BooleanDescriptor(SHOW_ALL, get("LBL_SHOW_ALL"), false, false));
    inputs.addRow(new BooleanDescriptor(EXTEND_RIGHT, get("LBL_EXTEND_RIGHT"), true, false));
    
    var colors = tab.addGroup(get("LBL_DISPLAY"));
    colors.addRow(new PathDescriptor(Inputs.TOP_PATH, get("LBL_TOP_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false, true));
    colors.addRow(new PathDescriptor(Inputs.MIDDLE_PATH, get("LBL_MIDDLE_LINE"), defaults.getLineColor(), 1.0f, null, false, false, false, true));
    colors.addRow(new PathDescriptor(Inputs.BOTTOM_PATH, get("LBL_BOTTOM_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false, true));
    colors.addRow(new ColorDescriptor(Inputs.FILL, get("LBL_FILL"), defaults.getFillColor(), false, true));
    colors.addRow(new FontDescriptor(LABELS, get("LBL_LABELS"), defaults.getFont(), defaults.getTextColor(), true, false, true));
    colors.addRow(new IndicatorDescriptor(HIGH_IND, get("LBL_HIGH_IND"), defaults.getBlue(), null, false, true, true));
    colors.addRow(new IndicatorDescriptor(MID_IND, get("LBL_MID_IND"), defaults.getGreen(), null, false, false, true));
    colors.addRow(new IndicatorDescriptor(LOW_IND, get("LBL_LOW_IND"), defaults.getRed(), null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.BARSIZE, RANGE, SHOW_ALL, EXTEND_RIGHT);
    sd.addQuickSettings(Inputs.TOP_PATH, Inputs.MIDDLE_PATH, Inputs.BOTTOM_PATH, Inputs.FILL, LABELS);

    var desc = createRD();
    desc.setLabelSettings(Inputs.BARSIZE, RANGE);
    desc.declareIndicator(Values.HIGH, HIGH_IND);
    desc.declareIndicator(Values.LOW, LOW_IND);
    desc.declareIndicator(Values.MID, MID_IND);
  }
  
  @Override
  public void clearState()
  {
    super.clearState();
    rangeSets.clear();
  }
  
  @Override
  public void onBarClose(DataContext ctx) { calculateValues(ctx); }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    var barSize = getSettings().getBarSize(Inputs.BARSIZE);
    var series2 = ctx.getDataSeries(barSize);
    if (series2.size() < 2) return;
    
    boolean showPrevRanges = getSettings().getBoolean(SHOW_ALL, false);
    boolean extendRight = getSettings().getBoolean(EXTEND_RIGHT, false);
    int start = 1;
    if (!showPrevRanges) {
      start = series2.size()-1;
      rangeSets.clear();
      clearFigures();
    }
    
    var instr = series2.getInstrument();
    double range = getSettings().getDouble(RANGE);
    range = Util.round(range/100.0, 3);
    
    for(int i = start; i < series2.size(); i++) {
      long time = series2.getStartTime(i);
      if (barSize.isLinear() && barSize.getIntervalMinutes() >= 1440) {
        time = instr.getStartOfDay(time, ctx.isRTH());
      }
      if (rangeSets.containsKey(time)) {
        continue;
      }
      
      double H = series2.getHigh(i-1);
      double L = series2.getLow(i-1);
      double R = H - L;
      double top = H;
      double bottom = L;
      
      if (range > 1.0) {
        top += ((range-1.0)/2) * R;
        bottom -= ((range-1.0)/2) * R;
      }
      else if (range < 1.0) {
        top -= ((1.0-range)/2) * R;
        bottom += ((1.0-range)/2) * R;
      }

      long endTime = series2.getEndTime(i);
      if (barSize.isLinear() && barSize.getIntervalMinutes() >= 1440) {
        endTime = instr.getEndOfDay(endTime - 6*Util.MILLIS_IN_HOUR, ctx.isRTH());
      }

      var area = new RangeArea(time, endTime, top, bottom);
      area.setExtendLines(extendRight && i == series2.size()-1);
      rangeSets.put(time, area);
      addFigure(area);
    }

    // Update the indicator values for the last pivot
    RangeArea area = null;
    for(RangeArea _area : rangeSets.values()) {
      if (area == null) area = _area;
      else {
        if (_area.getStart() > area.getStart()) area = _area;
      }
    }

    if (area == null) return;
    
    var series = ctx.getDataSeries();
    series.setDouble(Values.HIGH, area.getHigh());
    series.setDouble(Values.LOW, area.getLow());
    series.setDouble(Values.MID, area.getMiddle());
  }
  
  protected Map<Long, RangeArea> rangeSets = Collections.synchronizedMap(new HashMap());
}
