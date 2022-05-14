package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Relative Vigor Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="REL_VIGOR", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_REL_VIGOR",
    tabName="TAB_REL_VIGOR",
    desc="DESC_REL_VIGOR",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/relative_vigor_index.htm")
public class RelativeVigorIndex extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { VAL, SIGNAL, RVI }
  enum Signals { CROSS_ABOVE, CROSS_BELOW }
	
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_SIGNAL_PERIOD"), 4, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_DISPLAY"));
    var line = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.5f, null, true, false, false);
    line.setSupportsShowAsBars(true);
    settings.addRow(line);
    var signalLine = new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, false);
    signalLine.setSupportsShowAsBars(true);
    settings.addRow(signalLine);
    settings.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    settings.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), null, null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_SIGNAL_PERIOD"), 4, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.SIGNAL_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.VAL, get("VAL_REL_VIGOR"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("VAL_REL_VIGOR_SIGNAL"), new String[] {Inputs.PERIOD2}));
    
    desc.declarePath(Values.VAL, Inputs.PATH);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);

    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    
    desc.setRangeKeys(Values.VAL, Values.SIGNAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, null));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 10);
    int period2 = getSettings().getInteger(Inputs.PERIOD2, 4);
    var series = ctx.getDataSeries();
    double rvi = (series.getClose(index) - series.getOpen(index)) / (series.getHigh(index) - series.getLow(index));
    series.setDouble(index, Values.RVI, rvi);
    
    if (index < period) return;

    Double vigor = series.ma(Enums.MAMethod.SMA, index, period, Values.RVI);
    series.setDouble(index, Values.VAL, vigor);

    if (index < period + period2) return;

    // Calculate the signal (requires 4 previous values)
    Double signal = series.ma(Enums.MAMethod.WMA, index, period2, Values.VAL);
    if (signal == null) return;
    series.setDouble(index, Values.SIGNAL, signal);
    
    if (!series.isBarComplete(index)) return;

    // Check for signal events
    var c = new Coordinate(series.getStartTime(index), signal);
    if (crossedAbove(series, index, Values.VAL, Values.SIGNAL)) {
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("SIGNAL_VIGOR_CROSS_ABOVE", vigor, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
    }
    else if (crossedBelow(series, index, Values.VAL, Values.SIGNAL)) {
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("SIGNAL_VIGOR_CROSS_BELOW", vigor, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
    }
    
    series.setComplete(index);
  }  
}
