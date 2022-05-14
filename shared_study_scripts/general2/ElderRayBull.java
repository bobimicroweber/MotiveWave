package com.motivewave.platform.study.general2;import java.awt.Color;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.DataSeries;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.GuideInfo;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.MarkerInfo;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.ColorDescriptor;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Elder Ray Bull Power 156 */@StudyHeader(  namespace="com.motivewave",  id="ID_ERBULL",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_ERBULL",  name="NAME_ELDER_RAY_BULL_POWER",  desc="DESC_ERBULL",  menu="MENU_ALEXANDER_ELDER",  helpLink="http://www.motivewave.com/studies/elder_ray_bull_power.htm",  signals=false,  overlay=false,  studyOverlay=true)public class ElderRayBull extends Study{  enum Values { BULL, BEAR, MA }  protected enum Signals { BUY, SELL }  double highSell=Double.NEGATIVE_INFINITY;  double lowBuy=Double.MAX_VALUE;  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.MIDPOINT));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.EMA));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 13, 1, 999, 1));    var settings=tab.addGroup(get("COLORS"));    settings.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));    settings.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var guides=tab.addGroup(get("GUIDE"));    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -9.1, 9.1, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));        // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 13, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.UP_COLOR, Inputs.DOWN_COLOR);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.BULL, get("LBL_ERBULL"), new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.declareBars(Values.BULL);    desc.declareIndicator(Values.BULL, Inputs.IND);    desc.setRangeKeys(Values.BULL);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int period=getSettings().getInteger(Inputs.PERIOD);    if (index < period) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    Enums.MAMethod method=getSettings().getMAMethod(Inputs.METHOD);    DataSeries series=ctx.getDataSeries();    double high=series.getDouble(index, Enums.BarInput.HIGH, 0);    double low=series.getDouble(index, Enums.BarInput.LOW, 0);    Double ma=series.ma(method, index, period, key);    if (ma == null) return;    double bull=high - ma;    double bear=low - ma;    series.setDouble(index, Values.BULL, bull);    series.setDouble(index, Values.MA, ma);    series.setDouble(index, Values.BEAR, bear);    // Check for signal events    GuideInfo midGuide=getSettings().getGuide(Inputs.MIDDLE_GUIDE);    double midG=midGuide.getValue();    Color upC=getSettings().getColor(Inputs.UP_COLOR);    Color dnC=getSettings().getColor(Inputs.DOWN_COLOR);    if (bull > midG) series.setBarColor(index, Values.BULL, upC);    else series.setBarColor(index, Values.BULL, dnC);    double prevBull=series.getDouble(index - 1, Values.BULL, bull);    double prevBear=series.getDouble(index - 1, Values.BEAR, bear);    double prevMa=series.getDouble(index - 1, Values.MA, ma);    boolean sell=bull > midG && bull < prevBull && ma < prevMa && bull > highSell;    boolean buy=bear < midG && bear > prevBear && ma > prevMa && bear < lowBuy;    series.setBoolean(index, Signals.SELL, sell);    series.setBoolean(index, Signals.BUY, buy);    if (buy) highSell=Double.NEGATIVE_INFINITY;    if (sell) {      highSell=bull;      Coordinate c=new Coordinate(series.getStartTime(index), bull);      MarkerInfo marker=getSettings().getMarker(Inputs.DOWN_MARKER);      String msg = get("SELL_HIGH_BULL", Util.round(high, 2), Util.round(bull, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, high);    }    series.setComplete(index);  }}