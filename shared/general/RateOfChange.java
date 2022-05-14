package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Rate Of Change */
@StudyHeader(
    namespace="com.motivewave", 
    id="ROC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ROC",
    tabName="TAB_ROC",
    desc="DESC_ROC",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/rate_of_change.htm")
public class RateOfChange extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { ROC, SIGNAL };
  enum Signals { CROSS_ABOVE, CROSS_BELOW };
	
	final static String COMP_PERIOD = "compPeriod", MA_PERIOD = "maPeriod", ROC_LINE = "rocLine", MA_LINE = "maLine", ROC_IND = "rocInd", MA_IND = "maInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new IntegerDescriptor(COMP_PERIOD, get("LBL_COMP_PERIOD"), 12, 1, 9999, 1));
    grp.addRow(new IntegerDescriptor(MA_PERIOD, get("LBL_SIGNAL_PERIOD"), 20, 1, 9999, 1), new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    
    grp = tab.addGroup(get("LBL_DISPLAY"));
    var roc = new PathDescriptor(ROC_LINE, get("LBL_ROC_LINE"), defaults.getLineColor(), 1.5f, null, true, false, false);
    roc.setSupportsShowAsBars(true);
    grp.addRow(roc);
    grp.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), ROC_LINE, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), false, true));
    grp.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), ROC_LINE, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), false, true));
    var maLine = new PathDescriptor(MA_LINE, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, false);
    maLine.setSupportsShowAsBars(true);
    grp.addRow(maLine);
    grp.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    grp.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    grp.addRow(new IndicatorDescriptor(ROC_IND, get("LBL_ROC_IND"), null, null, false, true, true));
    grp.addRow(new IndicatorDescriptor(MA_IND, get("LBL_SIGNAL_IND"), null, null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(COMP_PERIOD, get("LBL_COMP_PERIOD"), 12, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(MA_PERIOD, get("LBL_SIGNAL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(ROC_LINE, MA_LINE, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.METHOD, COMP_PERIOD, MA_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.ROC, get("VAL_ROC"), new String[] {COMP_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("VAL_ROC_MA"), new String[] {Inputs.METHOD, MA_PERIOD}));
    desc.declarePath(Values.ROC, ROC_LINE);
    desc.declarePath(Values.SIGNAL, MA_LINE);
    desc.declareIndicator(Values.ROC, ROC_IND);
    desc.declareIndicator(Values.SIGNAL, MA_IND);
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    desc.setRangeKeys(Values.ROC, Values.SIGNAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, null));
  }

  /** Gets the minimum number of bars required to display this study.
  @param ctx data context
  @return minimum number of bars required to display the study */
  @Override
  public int getMinBars(DataContext ctx) 
  { 
    int p1 = getSettings().getInteger(COMP_PERIOD, 12);
    int p2 = getSettings().getInteger(MA_PERIOD, 20);
    return p1 + p2*2;
  }
  
  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(COMP_PERIOD, 12);
    if (index < period) return;
    
    var series = ctx.getDataSeries();
    Double ROC = series.roc(index,  period, Enums.BarInput.CLOSE)*100;
    series.setDouble(index, Values.ROC, ROC);

    int maPeriod = getSettings().getInteger(MA_PERIOD, 20);
    if (index < maPeriod) return;
    
    Double signal = series.ma(getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA), index, maPeriod, Values.ROC);
    if (signal == null) return;
    series.setDouble(index, Values.SIGNAL, signal);

    if (!series.isBarComplete(index)) return;

    // Check for signal events
    var c = new Coordinate(series.getStartTime(index), signal);
    if (crossedAbove(series, index, Values.ROC, Values.SIGNAL)) {
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("SIGNAL_ROC_CROSS_ABOVE", ROC, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
    }
    else if (crossedBelow(series, index, Values.ROC, Values.SIGNAL)) {
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("SIGNAL_ROC_CROSS_BELOW", ROC, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
    }
    
    series.setComplete(index);
  }  
}
