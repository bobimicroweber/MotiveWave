package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.ShadeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
  namespace="com.motivewave",
  id="VPCI",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_VPCI",
  name="TITLE_VPCI",
  desc="DESC_VPCI",
  overlay=false,
  menu="MENU_VOLUME_BASED",
  requiresVolume=true,
  studyOverlay=true,
  signals=true)
public class Vpci extends Study
{
  enum Values { VPCI, SIGNAL }
  enum Signals { BULLISH_CROSS, BEARISH_CROSS }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));

    var inputGroup=tab.addGroup(get("LBL_INPUTS"));
    inputGroup.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputGroup.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_SHORT_PERIOD"), 7, 1, 9999, 1));
    inputGroup.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_LONG_PERIOD"), 28, 1, 9999, 1));
    inputGroup.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 20, 1, 9999, 1));

    var colorsGroup=tab.addGroup(get("LBL_COLORS"));
    colorsGroup.addRow(new PathDescriptor(Inputs.PATH, get("LBL_VPCI_PATH"), defaults.getLineColor(), 1.5f, null, true, false, true));
    colorsGroup.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0.0, ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    colorsGroup.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0.0, ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    colorsGroup.addRow(new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_PATH"), defaults.getRed(), 1.0f, null, true, false, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var markersGroup=tab.addGroup(get("LBL_MARKERS"));
    markersGroup.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_BULLISH_CROSS"), Enums.MarkerType.TRIANGLE,
        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markersGroup.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_BEARISH_CROSS"), Enums.MarkerType.TRIANGLE,
        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    var indicatorsGroup=tab.addGroup(get("LBL_INDICATORS"));
    indicatorsGroup.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_VPCI_IND"), null, null, false, true, true));
    indicatorsGroup.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), defaults.getRed(), null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_SHORT_PERIOD"), 7, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_LONG_PERIOD"), 28, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL, Inputs.SIGNAL_PATH);

    var desc=createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.VPCI, get("LBL_VPCI"), new String[] { Inputs.PERIOD, Inputs.PERIOD2 }));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("LBL_VPCI_SIGNAL"), new String[] { Inputs.SIGNAL_PERIOD }));
    desc.declarePath(Values.VPCI, Inputs.PATH);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    desc.declareIndicator(Values.VPCI, Inputs.IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    desc.setRangeKeys(Values.VPCI);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] { 3, 3 }));
    desc.declareSignal(Signals.BULLISH_CROSS, get("LBL_BULLISH_CROSS"));
    desc.declareSignal(Signals.BEARISH_CROSS, get("LBL_BEARISH_CROSS"));
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    Object input=getSettings().getInput(Inputs.INPUT);
    int shortPeriod=getSettings().getInteger(Inputs.PERIOD);
    int longPeriod=getSettings().getInteger(Inputs.PERIOD2);
    int signalPeriod=getSettings().getInteger(Inputs.SIGNAL_PERIOD);
    if (index < longPeriod + signalPeriod) return;

    var series=ctx.getDataSeries();

    try {
      Double longVwma=series.vwma(index, longPeriod, input);
      Double longSma=series.sma(index, longPeriod, input);
      if (null == longVwma || null == longSma) return;
  
      double theVpc=longVwma - longSma;
  
      // Volume Price Ratio
      // VPR is calculated by dividing the short-term VWMA by the short-term SMA
      Double shortVwma=series.vwma(index, shortPeriod, input);
      Double shortSma=series.sma(index, shortPeriod, input);
      if (null == shortVwma || null == shortSma) return;
  
      double theVpr=shortVwma / shortSma;
  
      // Volume Multiplier
      // VM is calculated by dividing the short-term average volume
      // by the long-term average volume.
      Double shortTermVolume=series.sma(index, shortPeriod, Enums.BarInput.VOLUME); // calculateShortTermAvgVolume();
      Double longTermVolume=series.sma(index, longPeriod, Enums.BarInput.VOLUME); // calculateLongTermAvgVolume();
  
      double theVm=shortTermVolume / longTermVolume;
  
      // Volume Price Confirmation Indicator
      // VPCI is calculated by multiplying the VPC by the VPR
      // and then multiplying the result by the VM
      double theVpci=theVpc * theVpr * theVm;
  
      // Calculate moving average of the VPCI (signal path)
      Double theSignal=series.sma(index, signalPeriod, Values.VPCI);
  
      // Calculated values are stored in the data series using a key.
      // Notice that in the initialize method we declared a path using this key.
      // Values.VPCI represents the VPCI indicator
      // Set the value for the VPCI
      series.setDouble(index, Values.VPCI, theVpci);
  
      // Values.SIGNAL represents the moving average of the VPCI (signal path)
      // Set the value for the signal path
      series.setDouble(index, Values.SIGNAL, theSignal);
  
      // Add Markers and raise signal if appropriate
      checkBullishCross(index, ctx);
      checkBearishCross(index, ctx);
    }
    finally {
      series.setComplete(index);
    }
  }

  /** Checks to see if VPCI crossed above the signal line for this bar. If so, sets a marker on the plot, and raises a
   * signal.
   * @param index - index in the data series
   * @param ctx - Data Context */
  private void checkBullishCross(int index, DataContext ctx)
  {
    var series=ctx.getDataSeries();
    Double theSignal=series.getDouble(index, Values.SIGNAL);
    var theInstrument=ctx.getInstrument();

    if (null == theSignal) return;

    var coordinate=new Coordinate(series.getStartTime(index), theSignal);
    if (crossedAbove(series, index, Values.VPCI, Values.SIGNAL)
        && !series.getBoolean(index, Signals.BULLISH_CROSS, false)) {
      series.setBoolean(index, Signals.BULLISH_CROSS, true);
      var markerInfo=getSettings().getMarker(Inputs.UP_MARKER);
      if (markerInfo.isEnabled()) addFigure(new Marker(coordinate, Enums.Position.BOTTOM, markerInfo));
      ctx.signal(index, Signals.BULLISH_CROSS, get("LBL_BULLISH_CROSS"), theInstrument.round(series.getClose(index)));
    }
  }

  /** Checks to see if VPCI crossed below the signal line for this bar. If so, sets a marker on the plot, and raises a
   * signal.
   * @param index - index in the data series
   * @param ctx - Data Context */
  private void checkBearishCross(int index, DataContext ctx)
  {
    var series=ctx.getDataSeries();
    Double theSignal=series.getDouble(index, Values.SIGNAL);
    var theInstrument=ctx.getInstrument();

    if (null == theSignal) return;

    var coordinate=new Coordinate(series.getStartTime(index), theSignal);
    if (crossedBelow(series, index, Values.VPCI, Values.SIGNAL)
        && !series.getBoolean(index, Signals.BEARISH_CROSS, false)) {
      series.setBoolean(index, Signals.BEARISH_CROSS, true);
      var markerInfo=getSettings().getMarker(Inputs.DOWN_MARKER);
      if (markerInfo.isEnabled()) addFigure(new Marker(coordinate, Enums.Position.TOP, markerInfo));
      ctx.signal(index, Signals.BEARISH_CROSS, get("LBL_BEARISH_CROSS"), theInstrument.round(series.getClose(index)));
    }
  }
}
