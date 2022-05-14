package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
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
    id="SMOOTH_ROC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_SMOOTH_ROC",
    label="LABEL_SMOOTH_ROC",
    tabName="TAB_SMOOTH_ROC",
    desc="DESC_SMOOTH_ROC",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/rate_of_change.htm")
public class SmoothedRateOfChange extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { MA, ROC, SIGNAL };
  enum Signals { CROSS_ABOVE, CROSS_BELOW };
	
  final static String SMOOTH_METHOD = "smoothMethod";
  final static String SMOOTH_PERIOD = "smoothPeriod";
	final static String COMP_PERIOD = "compPeriod";
  final static String SIGNAL_PERIOD = "signalPeriod";
  final static String SIGNAL_METHOD = "signalMethod";
  final static String ROC_LINE = "rocLine";
  final static String MA_LINE = "maLine";
  final static String ROC_IND = "rocInd";
  final static String MA_IND = "maInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(SMOOTH_PERIOD, get("LBL_SMOOTH_PERIOD"), 10, 1, 9999, 1), new MAMethodDescriptor(SMOOTH_METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(COMP_PERIOD, get("LBL_COMP_PERIOD"), 12, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 20, 1, 9999, 1), new MAMethodDescriptor(SIGNAL_METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    
    var settings = tab.addGroup(get("LBL_DISPLAY"));
    var roc = new PathDescriptor(ROC_LINE, get("LBL_ROC_LINE"), defaults.getLineColor(), 1.5f, null, true, false, false);
    roc.setSupportsShowAsBars(true);
    settings.addRow(roc);
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), ROC_LINE, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), false, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), ROC_LINE, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), false, true));
    var maLine = new PathDescriptor(MA_LINE, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, false);
    maLine.setSupportsShowAsBars(true);
    settings.addRow(maLine);
    settings.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    settings.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    settings.addRow(new IndicatorDescriptor(ROC_IND, get("LBL_ROC_IND"), null, null, false, true, true));
    settings.addRow(new IndicatorDescriptor(MA_IND, get("LBL_SIGNAL_IND"), null, null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(SMOOTH_PERIOD, get("LBL_SMOOTH_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(SMOOTH_METHOD);
    sd.addQuickSettings(new SliderDescriptor(COMP_PERIOD, get("LBL_COMP_PERIOD"), 12, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(SIGNAL_METHOD, ROC_LINE, MA_LINE, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(SMOOTH_PERIOD, SMOOTH_METHOD, COMP_PERIOD, SIGNAL_PERIOD, SIGNAL_METHOD);
    desc.exportValue(new ValueDescriptor(Values.ROC, get("VAL_ROC"), new String[] {SMOOTH_METHOD, SMOOTH_PERIOD, COMP_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("VAL_ROC_MA"), new String[] {SIGNAL_METHOD, SIGNAL_PERIOD}));
    desc.declarePath(Values.ROC, ROC_LINE);
    desc.declarePath(Values.SIGNAL, MA_LINE);
    desc.declareIndicator(Values.ROC, ROC_IND);
    desc.declareIndicator(Values.SIGNAL, MA_IND);
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    desc.setRangeKeys(Values.ROC, Values.SIGNAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, null));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    int smooth_period = getSettings().getInteger(SMOOTH_PERIOD, 10);
    if (index < smooth_period) return;
    
    Double ma = series.ma(getSettings().getMAMethod(SMOOTH_METHOD, Enums.MAMethod.SMA), index, smooth_period, getSettings().getInput(Inputs.INPUT));
    if (ma == null) return;
    
    series.setDouble(index, Values.MA, ma);
    
    int period = getSettings().getInteger(COMP_PERIOD, 12);
    if (index < smooth_period + period) return;
    
    Double ROC = series.roc(index,  period, Values.MA)*100;
    series.setDouble(index, Values.ROC, ROC);

    int signalPeriod = getSettings().getInteger(SIGNAL_PERIOD, 20);
    if (index < smooth_period + period + signalPeriod) return;
    
    Double signal = series.ma(getSettings().getMAMethod(SIGNAL_METHOD, Enums.MAMethod.EMA), index, signalPeriod, Values.ROC);
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
