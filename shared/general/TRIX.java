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
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.StudyHeader;

/** TRIX */
@StudyHeader(
    namespace="com.motivewave", 
    id="TRIX", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_TRIX",
    desc="DESC_TRIX",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/trix.htm")
public class TRIX extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { TRIX, SIGNAL, MA1, MA2, MA3 };
  enum Signals { CROSS_ABOVE, CROSS_BELOW };
	
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_SMOOTHING_PERIOD"), 15, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_DISPLAY"));
    var line = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.5f, null, true, false, true);
    line.setSupportsShowAsBars(true);
    settings.addRow(line);
    var signalLine = new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, true);
    signalLine.setSupportsShowAsBars(true);
    settings.addRow(signalLine);
    settings.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    settings.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), null, null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_SMOOTHING_PERIOD"), 15, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.SIGNAL_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.TRIX, get("VAL_TRIX"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("VAL_TRIX_SIGNAL"), new String[] {Inputs.PERIOD2}));

    desc.declarePath(Values.TRIX, Inputs.PATH);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);

    desc.declareIndicator(Values.TRIX, Inputs.IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);

    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    
    desc.setRangeKeys(Values.TRIX, Values.SIGNAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, null));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 15);
    var series = ctx.getDataSeries();
    
    if (index < period) return;
    Object input = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    Double ma1 = series.ma(Enums.MAMethod.EMA, index, period, input);
    series.setDouble(index, Values.MA1, ma1);
    
    if (index < period*2) return;
    Double ma2 = series.ma(Enums.MAMethod.EMA, index, period, Values.MA1);
    series.setDouble(index, Values.MA2, ma2);
    
    if (index < period*3) return;
    Double ma3 = series.ma(Enums.MAMethod.EMA, index, period, Values.MA2);
    series.setDouble(index, Values.MA3, ma3);
    
    if (index <= period*3) return; // need the the prev MA3
    
    Double prevMA3 = series.getDouble(index-1, Values.MA3);
    if (ma3 == null || prevMA3 == null) return;
    
    double TRIX = 100.0 * ((ma3 - prevMA3)/prevMA3);
    series.setDouble(index, Values.TRIX, TRIX);

    int period2 = getSettings().getInteger(Inputs.PERIOD2, 9);
    if (index <= period*3 + period2) return;
    
    Double signal = series.ma(Enums.MAMethod.EMA, index, period2, Values.TRIX);
    if (signal == null) return;
    series.setDouble(index, Values.SIGNAL, signal);
    
    if (!series.isBarComplete(index)) return;

    // Check for signal events
    var c = new Coordinate(series.getStartTime(index), signal);
    if (crossedAbove(series, index, Values.TRIX, Values.SIGNAL)) {
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("SIGNAL_TRIX_CROSS_ABOVE", TRIX, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
    }
    else if (crossedBelow(series, index, Values.TRIX, Values.SIGNAL)) {
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("SIGNAL_TRIX_CROSS_BELOW", TRIX, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
    }
    
    series.setComplete(index);
  }  
}
