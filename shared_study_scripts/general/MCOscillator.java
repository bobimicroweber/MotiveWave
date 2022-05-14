package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.InstrumentDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Breadth indicator derived from Net Advances.
    This study requires access to Advances and Declines instruments. */
@StudyHeader(
    namespace="com.motivewave", 
    id="MC_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MC_OSC",
    desc="DESC_MC_OSC",
    menu="MENU_GENERAL",
    overlay=false,
    signals=true,
    label="NAME_MC_OSC",
    multipleInstrument=true)
public class MCOscillator extends com.motivewave.platform.sdk.study.Study 
{
  final static String ADV="adv", DEC="dec", SLOW_PATH="slowPath", FAST_PATH="fastPath", 
      SLOW_IND="slowInd", FAST_IND="fastInd", HIST_IND="histInd", RATIO_ADJ="radj";
  enum Values { NET_ADV, FAST, SLOW, HIST };
  enum Signals { CROSS_ABOVE, CROSS_BELOW };
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new InstrumentDescriptor(ADV, get("LBL_MC_ADVANCING")));
    grp.addRow(new InstrumentDescriptor(DEC, get("LBL_MC_DECLINING")));
    grp.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    grp.addRow(new BooleanDescriptor(RATIO_ADJ, get("LBL_MC_RATIO_ADJUSTED"), true));
    grp.addRow(new IntegerDescriptor(Inputs.FAST_PERIOD, get("LBL_MC_FAST_PERIOD"), 19, 1, 999, 1));
    grp.addRow(new IntegerDescriptor(Inputs.SLOW_PERIOD, get("LBL_MC_SLOW_PERIOD"), 39, 1, 999, 1));

    grp = tab.addGroup(get("LBL_DISPLAY"));
    grp.addRow(new PathDescriptor(FAST_PATH, get("LBL_MC_FAST_MA"), defaults.getRedLine(), 1f, null));
    grp.addRow(new PathDescriptor(SLOW_PATH, get("LBL_MC_SLOW_MA"), defaults.getBlueLine(), 1f, null));
    var histogram = new PathDescriptor(Inputs.BAR, get("LBL_HISTOGRAM"), defaults.getBarColor(), 1.0f, null, true, false, true);
    histogram.setShowAsBars(true);
    histogram.setSupportsShowAsBars(true);
    histogram.setColorPolicies(Enums.ColorPolicy.values());
    grp.addRow(histogram);
    grp.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    grp.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    grp = tab.addGroup(get("LBL_INDICATORS"));
    grp.addRow(new IndicatorDescriptor(SLOW_IND, get("LBL_MC_SLOW_IND"), null, null, false, true, true));
    grp.addRow(new IndicatorDescriptor(FAST_IND, get("LBL_MC_FAST_IND"), defaults.getRed(), null, false, false, true));
    grp.addRow(new IndicatorDescriptor(HIST_IND, get("LBL_MC_HIST_IND"), defaults.getBarColor(), null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(ADV, DEC, Inputs.INPUT, RATIO_ADJ);
    sd.addQuickSettings(new SliderDescriptor(Inputs.FAST_PERIOD, get("LBL_MC_FAST_PERIOD"), 19, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SLOW_PERIOD, get("LBL_MC_SLOW_PERIOD"), 39, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(FAST_PATH, SLOW_PATH);

    var desc = createRD();
    desc.setLabelSettings(ADV, DEC, Inputs.INPUT, Inputs.FAST_PERIOD, Inputs.SLOW_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.FAST, get("LBL_MC_FAST_MA"), new String[] {Inputs.FAST_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SLOW, get("LBL_MC_SLOW_MA"), new String[] {Inputs.SLOW_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.HIST, get("LBL_MC_HIST"), new String[] {Inputs.FAST_PERIOD, Inputs.SLOW_PERIOD}));
    desc.declarePath(Values.SLOW, SLOW_PATH);
    desc.declarePath(Values.FAST, FAST_PATH);
    desc.declarePath(Values.HIST, Inputs.BAR);
    desc.declareIndicator(Values.SLOW, SLOW_IND);
    desc.declareIndicator(Values.FAST, FAST_IND);
    desc.declareIndicator(Values.HIST, HIST_IND);
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    desc.setRangeKeys(Values.SLOW, Values.FAST, Values.HIST);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
    desc.setMinTick(0.01d);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var adv = getSettings().getInstrument(ADV);
    var dec = getSettings().getInstrument(DEC);
    var series = ctx.getDataSeries();
    if (series == null || adv == null || dec == null) return;
    
    var input = (Enums.BarInput)getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    var a = series.getDouble(index, input, adv);
    var d = series.getDouble(index, input, dec);
    if (a == null || d == null) return;

    var net = adv.round(a - d);
    if (getSettings().getBoolean(RATIO_ADJ, true)) net = 1000*net/(a + d);
    
    series.setDouble(index, Values.NET_ADV, net);
    int fp = getSettings().getInteger(Inputs.FAST_PERIOD, 19);
    if (index > fp) series.setDouble(index, Values.FAST, series.ema(index, fp, Values.NET_ADV));
    int sp = getSettings().getInteger(Inputs.SLOW_PERIOD, 39);
    if (index > sp) series.setDouble(index, Values.SLOW, series.ema(index, sp, Values.NET_ADV));
    
    var f = series.getDouble(index, Values.FAST);
    var s = series.getDouble(index, Values.SLOW);
    if (f == null || s == null) return;
    series.setDouble(index, Values.HIST, f - s);
    
    if (!series.isBarComplete(index)) return;
    
    // Check for signal events
    var c = new Coordinate(series.getStartTime(index), s);
    if (crossedAbove(series, index, Values.FAST, Values.SLOW)) {
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      var msg = get("SIGNAL_MC_CROSS_ABOVE", f, s);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, s);
    }
    else if (crossedBelow(series, index, Values.FAST, Values.SLOW)) {
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      var msg = get("SIGNAL_MC_CROSS_BELOW", f, s);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, s);
    }
    series.setComplete(index);
  }
}
