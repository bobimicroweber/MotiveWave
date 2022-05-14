package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Know Sure Thing (KST) */
@StudyHeader(
    namespace="com.motivewave", 
    id="KST", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_KST",
    label="LBL_KST",
    desc="DESC_KST",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/know_sure_thing.htm")
public class KST extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { KST, SIGNAL, ROC1, ROC2, ROC3, ROC4 };
  enum Signals { CROSS_ABOVE, CROSS_BELOW };
	
  final static String MA1 = "ma1";
  final static String MA2 = "ma2";
  final static String MA3 = "ma3";
  final static String MA4 = "ma4";

  final static String R1 = "r1";
  final static String R2 = "r2";
  final static String R3 = "r3";
  final static String R4 = "r4";

  final static String W1 = "w1";
  final static String W2 = "w2";
  final static String W3 = "w3";
  final static String W4 = "w4";
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_KST_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new MAMethodDescriptor(Inputs.SIGNAL_METHOD, get("LBL_SIGNAL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 8, 1, 9999, 1));

    inputs = tab.addGroup(get("LBL_ROC"));
    inputs.addRow(new IntegerDescriptor(MA1, get("LBL_MA1"), 10, 1, 9999, 1),
                  new IntegerDescriptor(R1, get("LBL_R1"), 10, 0, 9999, 1),
                  new IntegerDescriptor(W1, get("LBL_W1"), 1, 0, 9999, 1));
    inputs.addRow(new IntegerDescriptor(MA2, get("LBL_MA2"), 10, 1, 9999, 1),
                  new IntegerDescriptor(R2, get("LBL_R2"), 15, 0, 9999, 1),
                  new IntegerDescriptor(W2, get("LBL_W2"), 2, 0, 9999, 1));
    inputs.addRow(new IntegerDescriptor(MA3, get("LBL_MA3"), 10, 1, 9999, 1),
                  new IntegerDescriptor(R3, get("LBL_R3"), 20, 0, 9999, 1),
                  new IntegerDescriptor(W3, get("LBL_W3"), 3, 0, 9999, 1));
    inputs.addRow(new IntegerDescriptor(MA4, get("LBL_MA4"), 15, 1, 9999, 1),
                  new IntegerDescriptor(R4, get("LBL_R4"), 30, 0, 9999, 1),
                  new IntegerDescriptor(W4, get("LBL_W4"), 4, 0, 9999, 1));
    
    tab = sd.addTab(get("TAB_DISPLAY"));

    var settings = tab.addGroup(get("LBL_LINES"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_KST_LINE"), defaults.getLineColor(), 1.5f, null, true, false, false));
    settings.addRow(new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, false));
    
    settings = tab.addGroup(get("LBL_MARKERS"));
    settings.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    settings.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    
    settings = tab.addGroup(get("LBL_INDICATORS"));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), null, null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD, Inputs.SIGNAL_METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 8, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.SIGNAL_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD);
    desc.exportValue(new ValueDescriptor(Values.KST, get("VAL_KST"), new String[] {Inputs.INPUT, Inputs.METHOD}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("VAL_KST_SIGNAL"), new String[] {Inputs.INPUT, Inputs.SIGNAL_METHOD}));
    
    desc.declarePath(Values.KST, Inputs.PATH);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    
    desc.declareIndicator(Values.KST, Inputs.IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    
    desc.setRangeKeys(Values.KST, Values.SIGNAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, null));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    Object input = getSettings().getInput(Inputs.INPUT);

    int r1 = getSettings().getInteger(R1);
    int r2 = getSettings().getInteger(R2);
    int r3 = getSettings().getInteger(R3);
    int r4 = getSettings().getInteger(R4);

    int maxX = Util.maxInt(r1, r2, r3, r4);
    if (index < maxX) return;
    
    var series = ctx.getDataSeries();
    
    series.setDouble(index, Values.ROC1, series.roc(index, r1, input)*100);
    series.setDouble(index, Values.ROC2, series.roc(index, r2, input)*100);
    series.setDouble(index, Values.ROC3, series.roc(index, r3, input)*100);
    series.setDouble(index, Values.ROC4, series.roc(index, r4, input)*100);

    int ma1 = getSettings().getInteger(MA1);
    int ma2 = getSettings().getInteger(MA2);
    int ma3 = getSettings().getInteger(MA3);
    int ma4 = getSettings().getInteger(MA4);

    int maxAvg = Util.maxInt(ma1, ma2, ma3, ma4);
    if (index < maxX+maxAvg) return;

    var method = getSettings().getMAMethod(Inputs.METHOD);
    int w1 = getSettings().getInteger(W1);
    int w2 = getSettings().getInteger(W2);
    int w3 = getSettings().getInteger(W3);
    int w4 = getSettings().getInteger(W4);

    Double m1 = series.ma(method, index, ma1, Values.ROC1);
    Double m2 = series.ma(method, index, ma2, Values.ROC2);
    Double m3 = series.ma(method, index, ma3, Values.ROC3);
    Double m4 = series.ma(method, index, ma4, Values.ROC4);
    if (m1 == null || m2 == null || m3 == null || m4 == null) return;
    
    double KST = m1*w1 + m2*w2 + m3*w3 + m4*w4;

    series.setDouble(index, Values.KST, KST);

    int signalPeriod = getSettings().getInteger(Inputs.SIGNAL_PERIOD);
    if (index < maxX+maxAvg+signalPeriod) return;

    Double signal = series.ma(getSettings().getMAMethod(Inputs.SIGNAL_METHOD), index, signalPeriod, Values.KST);
    series.setDouble(index, Values.SIGNAL, signal);
    
    if (!series.isBarComplete(index)) return;

    if (signal != null) {
      // Check for signal events
      var c = new Coordinate(series.getStartTime(index), signal);
      if (crossedAbove(series, index, Values.KST, Values.SIGNAL)) {
        var marker = getSettings().getMarker(Inputs.UP_MARKER);
        String msg = get("SIGNAL_KST_CROSS_ABOVE", KST, signal);
        if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
        ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
      }
      else if (crossedBelow(series, index, Values.KST, Values.SIGNAL)) {
        var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
        String msg = get("SIGNAL_KST_CROSS_BELOW", KST, signal);
        if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
        ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
      }
    }
    
    series.setComplete(index);
  }  
}
