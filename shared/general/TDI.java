package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Traders Dynamic Index
    https://www.earnforex.com/metatrader-indicators/Traders-Dynamic-Index/ */
@StudyHeader(
    namespace="com.motivewave", 
    id="TDI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_TDI",
    label="LBL_TDI",
    desc="DESC_TDI",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="https://www.earnforex.com/metatrader-indicators/Traders-Dynamic-Index/")
public class TDI extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { RSI, UP, DOWN, S1, S2, VOL_TOP, VOL_MID, VOL_BOT }
  enum Signals { CROSS_ABOVE, CROSS_BELOW }
  
	final static String RSI_INPUT="rsiInput", RSI_PERIOD = "rsiPeriod", RSI_METHOD = "rsiMethod";
	final static String S1_METHOD ="s1Method", S1_PERIOD = "s1Period", S1_LINE = "s1Line", S1_IND = "s1Ind";
  final static String S2_METHOD ="s2Method", S2_PERIOD = "s2Period", S2_LINE = "s2Line", S2_IND = "s2Ind";
  final static String VOL_PERIOD ="volPeriod", STDDEV = "stdDev", TOP_LINE = "topLine", TOP_IND = "topInd",
      MID_LINE = "midLine", MID_IND = "midInd", BOT_LINE = "botLine", BOT_IND = "botInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_INPUTS"));

    var inputs = tab.addGroup("");
    inputs.addRow(width(new InputDescriptor(RSI_INPUT, get("LBL_RSI"), Enums.BarInput.CLOSE), 2),
        new MAMethodDescriptor(RSI_METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA),
        new IntegerDescriptor(RSI_PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new MAMethodDescriptor(S1_METHOD, get("LBL_TDI_PRICE"), Enums.MAMethod.SMA),
        width(new IntegerDescriptor(S1_PERIOD, get("LBL_PERIOD"), 2, 1, 9999, 1), 2));
    inputs.addRow(new MAMethodDescriptor(S2_METHOD, get("LBL_TDI_TRADE_SIGNAL"), Enums.MAMethod.SMA),
        width(new IntegerDescriptor(S2_PERIOD, get("LBL_PERIOD"), 7, 1, 9999, 1), 2));
    inputs.addRow(new IntegerDescriptor(VOL_PERIOD, get("LBL_TDI_VOLATILITY_BAND"), 34, 1, 9999, 1),
        width(new DoubleDescriptor(STDDEV, get("LBL_TDI_STD"), 1.6185, 0.0001, 999, 0.0001), 2));

    tab = sd.addTab(get("TAB_DISPLAY"));

    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(S1_LINE, get("LBL_TDI_PRICE_LINE"), defaults.getGreenLine(), 2.0f, null));
    lines.addRow(new IndicatorDescriptor(S1_IND, get("LBL_TDI_PRICE_IND"), defaults.getGreenLine(), null, false, true, true));
    lines.addRow(new PathDescriptor(S2_LINE, get("LBL_TDI_TRADE_SIGNAL"), defaults.getRedLine(), 2.0f, null));
    lines.addRow(new IndicatorDescriptor(S2_IND, get("LBL_TDI_TRADE_IND"), defaults.getRedLine(), null, false, true, true));
    lines.addRow(new PathDescriptor(TOP_LINE, get("LBL_TDI_TOP_LINE"), defaults.getBlueLine(), 1.0f, null));
    lines.addRow(new IndicatorDescriptor(TOP_IND, get("LBL_TDI_TOP_IND"), defaults.getBlueLine(), null, false, true, true));
    lines.addRow(new PathDescriptor(MID_LINE, get("LBL_TDI_MID_LINE"), defaults.getYellowLine(), 2.0f, null));
    lines.addRow(new IndicatorDescriptor(MID_IND, get("LBL_TDI_MID_IND"), defaults.getYellowLine(), null, false, true, true));
    lines.addRow(new PathDescriptor(BOT_LINE, get("LBL_TDI_BOT_LINE"), defaults.getBlueLine(), 1.0f, null));
    lines.addRow(new IndicatorDescriptor(BOT_IND, get("LBL_TDI_BOT_IND"), defaults.getBlueLine(), null, false, true, true));

    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 70, 1, 100, 1, true));
    var gd = new GuideDescriptor(Inputs.TOP_GUIDE2, get("LBL_TOP_GUIDE2"), 60, 1, 100, 1, true);
    gd.setEnabled(false);
    guides.addRow(gd);
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    gd = new GuideDescriptor(Inputs.BOTTOM_GUIDE2, get("LBL_BOTTOM_GUIDE2"), 40, 1, 100, 1, true);
    gd.setEnabled(false);
    guides.addRow(gd);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 30, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(RSI_INPUT, RSI_METHOD);
    sd.addQuickSettings(new SliderDescriptor(RSI_PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(S1_METHOD);
    sd.addQuickSettings(new SliderDescriptor(S1_PERIOD, get("LBL_PERIOD"), 2, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(S2_METHOD);
    sd.addQuickSettings(new SliderDescriptor(S2_PERIOD, get("LBL_PERIOD"), 7, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(VOL_PERIOD, get("LBL_TDI_VOLATILITY_BAND"), 34, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(STDDEV, S1_LINE, S2_LINE, TOP_LINE, MID_LINE, BOT_LINE);

    var rdesc = createRD();
    rdesc.setLabelSettings(RSI_INPUT, RSI_METHOD, RSI_PERIOD, S1_METHOD, S1_PERIOD, S2_METHOD, S2_PERIOD, VOL_PERIOD, STDDEV);
    rdesc.exportValue(new ValueDescriptor(Values.S1, get("LBL_TDI_PRICE_LINE"), new String[] {S1_METHOD, S1_PERIOD}));
    rdesc.exportValue(new ValueDescriptor(Values.S2, get("LBL_TDI_TRADE_SIGNAL"), new String[] {S2_METHOD, S2_PERIOD}));
    rdesc.exportValue(new ValueDescriptor(Values.VOL_TOP, get("LBL_TDI_VOL_TOP"), new String[] {VOL_PERIOD, STDDEV}));
    rdesc.exportValue(new ValueDescriptor(Values.VOL_MID, get("LBL_TDI_VOL_MID"), new String[] {VOL_PERIOD, STDDEV}));
    rdesc.exportValue(new ValueDescriptor(Values.VOL_BOT, get("LBL_TDI_VOL_BOT"), new String[] {VOL_PERIOD, STDDEV}));
    rdesc.exportValue(new ValueDescriptor(Signals.CROSS_ABOVE, Enums.ValueType.BOOLEAN, get("LBL_TDI_CROSS_ABOVE"), null));
    rdesc.exportValue(new ValueDescriptor(Signals.CROSS_BELOW, Enums.ValueType.BOOLEAN, get("LBL_TDI_CROSS_BELOW"), null));
    rdesc.declarePath(Values.S1, S1_LINE);
    rdesc.declareIndicator(Values.S1, S1_IND);
    rdesc.declarePath(Values.S2, S2_LINE);
    rdesc.declareIndicator(Values.S2, S2_IND);
    rdesc.declarePath(Values.VOL_TOP, TOP_LINE);
    rdesc.declareIndicator(Values.VOL_TOP, TOP_IND);
    rdesc.declarePath(Values.VOL_MID, MID_LINE);
    rdesc.declareIndicator(Values.VOL_MID, MID_IND);
    rdesc.declarePath(Values.VOL_BOT, BOT_LINE);
    rdesc.declareIndicator(Values.VOL_BOT, BOT_IND);
    rdesc.setMaxBottomValue(15);
    rdesc.setMinTopValue(85);
    rdesc.setRangeKeys(Values.S1, Values.S2, Values.VOL_TOP, Values.VOL_BOT);
    rdesc.declareSignal(Signals.CROSS_ABOVE, get("LBL_TDI_CROSS_ABOVE"));
    rdesc.declareSignal(Signals.CROSS_BELOW, get("LBL_TDI_CROSS_BELOW"));
    rdesc.setMinTick(0.01);
  }
  
  private SettingDescriptor width(SettingDescriptor desc, int w)
  {
    desc.setGridWidth(w);
    return desc;
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var s = getSettings();
    int rsiPeriod = s.getInteger(RSI_PERIOD, 14);
    if (index < 1) return; // not enough data
    var series = ctx.getDataSeries();
    Object input = s.getInput(RSI_INPUT);
    
    double diff = series.getDouble(index, input) - series.getDouble(index-1, input);
    double up = 0, down = 0;
    if (diff > 0) up = diff;
    else down = diff;
    
    series.setDouble(index, Values.UP, up);
    series.setDouble(index, Values.DOWN, Math.abs(down));
    
    if (index < rsiPeriod +1) return;
    
    var method = s.getMAMethod(RSI_METHOD);
    Double avgUp = series.ma(method, index,  rsiPeriod, Values.UP);
    Double avgDown = series.ma(method, index,  rsiPeriod, Values.DOWN);
    if (avgUp == null || avgDown == null) return;
    double RS = avgUp / avgDown;
    double RSI = 100.0 - ( 100.0 / (1.0 + RS));

    series.setDouble(index, Values.RSI, RSI);
    
    // Compute Price Line
    int s1Period = s.getInteger(S1_PERIOD, 2);
    if (index < rsiPeriod + s1Period) return;
    Double fastMA = series.ma(s.getMAMethod(S1_METHOD), index, s1Period, Values.RSI);
    series.setDouble(index, Values.S1, fastMA);

    // Compute Trade Signal Line
    int s2Period = s.getInteger(S2_PERIOD, 7);
    if (index < rsiPeriod + s2Period) return;
    Double slowMA = series.ma(s.getMAMethod(S2_METHOD), index, s2Period, Values.RSI);
    series.setDouble(index, Values.S2, slowMA);

    // Volatility Bands
    int volPeriod = s.getInteger(VOL_PERIOD, 34);
    double mult = s.getDouble(STDDEV, 1.6185);
    if (index < rsiPeriod + volPeriod) return;
    
    Double stdDev = series.std(index, volPeriod, Values.RSI);
    Double ma = series.ma(method, index, volPeriod, Values.RSI);
    if (stdDev == null || ma == null) return;
    series.setDouble(index, Values.VOL_TOP, ma + stdDev*mult);
    series.setDouble(index, Values.VOL_MID, ma);
    series.setDouble(index, Values.VOL_BOT, ma - stdDev*mult);
    boolean latest = index == series.size()-1;
    
    // Check to see if a cross occurred and raise signal.
    var c = new Coordinate(series.getStartTime(index), slowMA);
    if (crossedAbove(series, index, Values.S1, Values.S2)) {
      boolean crossed = series.getBoolean(index, Signals.CROSS_ABOVE, false) && latest;
      series.setBoolean(index, Signals.CROSS_ABOVE, true);
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("SIGNAL_TDI_CROSS_ABOVE", format(fastMA), format(slowMA), format(series.getClose(index)));
      if (!crossed && marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, round(fastMA));
    }
    else if (crossedBelow(series, index, Values.S1, Values.S2)) {
      boolean crossed = series.getBoolean(index, Signals.CROSS_BELOW, false) && latest;
      series.setBoolean(index, Signals.CROSS_BELOW, true);
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("SIGNAL_TDI_CROSS_BELOW", format(fastMA), format(slowMA), format(series.getClose(index)));
      if (!crossed && marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, round(fastMA));
    }

    series.setComplete(index, series.isBarComplete(index));
  }  
 
}
