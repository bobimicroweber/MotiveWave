package com.motivewave.platform.study.overlay;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Elder Ray Index 159 */@StudyHeader(  namespace="com.motivewave",  id="ID_ERINDEX",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_ERINDEX",  name="NAME_ELDER_RAY_INDEX",  desc="DESC_ERINDEX",  menu="MENU_ALEXANDER_ELDER",  helpLink="http://www.motivewave.com/studies/elder_ray_index.htm",  signals=true,  overlay=true,  studyOverlay=true)public class ElderRayIndex extends Study{  enum Values { BULL, BEAR, MA }  protected enum Signals { SELL, BUY }  double highSell=Double.NEGATIVE_INFINITY;  double lowBuy=Double.MAX_VALUE;  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.EMA));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 13, 1, 999, 1));    var settings=tab.addGroup(get("PATH_INDICATOR"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var guides=tab.addGroup(get("GUIDE"));    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -9.1, 9.1, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 13, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.PATH);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.MA, get("LBL_EREMA"), new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.declarePath(Values.MA, Inputs.PATH);    desc.declareIndicator(Values.MA, Inputs.IND);    desc.setRangeKeys(Values.MA);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1 + 1);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int period=getSettings().getInteger(Inputs.PERIOD);    if (index < period) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    var method=getSettings().getMAMethod(Inputs.METHOD);    var series=ctx.getDataSeries();    Double ma=series.ma(method, index, period, key);    if (ma == null) return;    double low=series.getDouble(index, Enums.BarInput.LOW, 0);    double high=series.getDouble(index, Enums.BarInput.HIGH, 0);    double bull=high - ma;    double bear=low - ma;    series.setDouble(index, Values.BULL, bull);    series.setDouble(index, Values.BEAR, bear);    series.setDouble(index, Values.MA, ma);    series.setComplete(index);    if (index < period + 1) return;    // Check for signal events    var midGuide=getSettings().getGuide(Inputs.MIDDLE_GUIDE);    double midG=midGuide.getValue();    double prevBear=series.getDouble(index - 1, Values.BEAR, bear);    double prevBull=series.getDouble(index - 1, Values.BULL, bull);    double prevMa=series.getDouble(index - 1, Values.MA, ma);    boolean sell=bull > midG && bull < prevBull && bull > highSell && ma < prevMa;    boolean buy=bear < midG && bear > prevBear && bear < lowBuy && ma > prevMa;    series.setBoolean(index, Signals.BUY, buy);    series.setBoolean(index, Signals.SELL, sell);    if (sell) {      highSell=bull;      lowBuy=Double.MAX_VALUE;      var c=new Coordinate(series.getStartTime(index), high);      var marker=getSettings().getMarker(Inputs.DOWN_MARKER);      String msg = get("SELL_HIGH_BULL", Util.round(high, 2), Util.round(bull, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, high);    }    if (buy) {      lowBuy=bear;      highSell=Double.NEGATIVE_INFINITY;      var c=new Coordinate(series.getStartTime(index), low);      var marker=getSettings().getMarker(Inputs.UP_MARKER);      String msg = get("BUY_LOW_BEAR", Util.round(low, 2), Util.round(bear, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));      ctx.signal(index, Signals.BUY, msg, low);    }  }}