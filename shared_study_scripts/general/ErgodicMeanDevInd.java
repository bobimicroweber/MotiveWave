package com.motivewave.platform.study.general;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.SettingTab;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Ergodic Mean Deviation Ind 087 Location General */@StudyHeader(  namespace="com.motivewave",  id="ID_EMDI",  rb="com.motivewave.platform.study.nls.strings2",  name="NAME_ERGODIC_MEAN_DEVIATION_IND",  desc="DESC_EMDI",  menu="MENU_WILLIAM_BLAU",  label="LBL_EMDI",  helpLink="http://www.motivewave.com/studies/ergodic_mean_deviation_ind.htm",  signals=true,  overlay=false,  studyOverlay=true)public class ErgodicMeanDevInd extends Study{  enum Signals { SELL, BUY };  enum Values { MA1, MA2, EMDI, SIG };  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    SettingTab tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD1"), 32, 1, 999, 1));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("PERIOD2"), 5, 1, 999, 1));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD3, get("PERIOD3"), 5, 1, 999, 1));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD4, get("SMOOTH_PERIOD"), 5, 1, 999, 1));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD1"), Enums.MAMethod.EMA));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD2, get("METHOD2"), Enums.MAMethod.EMA));    var settings=tab.addGroup(get("PATHS"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_EMDI"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new PathDescriptor(Inputs.PATH2, get("SIGNAL"), defaults.getRed(), 1.0f, null, true, false, true));    tab=sd.addTab(get("TAB_DISPLAY"));    settings=tab.addGroup(get("INDICATORS"));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_EMDI"), defaults.getLineColor(), null, false, true, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND2, get("SIGNAL"), defaults.getRed(), null, false, true, true));    var guides=tab.addGroup(get("GUIDE"));    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -9.01, 9.01, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    settings=tab.addGroup(get("SHADING"));    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD1"), 32, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("PERIOD2"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD3, get("PERIOD3"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD4, get("SMOOTH_PERIOD"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.METHOD, Inputs.METHOD2, Inputs.PATH, Inputs.PATH2, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3, Inputs.PERIOD4, Inputs.METHOD, Inputs.METHOD2);    desc.exportValue(new ValueDescriptor(Values.EMDI, get("LBL_EMDI"), new String[] { Inputs.INPUT, Inputs.PERIOD,        Inputs.PERIOD2, Inputs.PERIOD3, Inputs.PERIOD4, Inputs.METHOD, Inputs.METHOD2 }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.declarePath(Values.EMDI, Inputs.PATH);    desc.declarePath(Values.SIG, Inputs.PATH2);    desc.declareIndicator(Values.EMDI, Inputs.IND);    desc.declareIndicator(Values.SIG, Inputs.IND2);    desc.setRangeKeys(Values.EMDI, Values.SIG);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    int p2=getSettings().getInteger(Inputs.PERIOD2);    int p3=getSettings().getInteger(Inputs.PERIOD3);    int p4=getSettings().getInteger(Inputs.PERIOD4);    setMinBars(p1 + p2 + p3 + p4);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int period1=getSettings().getInteger(Inputs.PERIOD);    if (index < period1) return;    int period2=getSettings().getInteger(Inputs.PERIOD2);    int period3=getSettings().getInteger(Inputs.PERIOD3);    int sPeriod=getSettings().getInteger(Inputs.PERIOD4);    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    var method1=getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA);    var method2=getSettings().getMAMethod(Inputs.METHOD2, Enums.MAMethod.EMA);    var series=ctx.getDataSeries();    Double ma=series.ma(method1, index, period1, key);    if (ma == null) return;    double price=series.getDouble(index, key, 0);    double ma1=price - ma;    series.setDouble(index, Values.MA1, ma1);    if (index < period1 + period2) return;    Double ma2=series.ma(method1, index, period2, Values.MA1);    series.setDouble(index, Values.MA2, ma2);    if (index < period1 + period2 + period3) return;    Double emdi=series.ma(method1, index, period3, Values.MA2);    series.setDouble(index, Values.EMDI, emdi);    if (index < period1 + period2 + period3 + sPeriod) return;    Double sig=series.ma(method2, index, sPeriod, Values.EMDI);    series.setDouble(index, Values.SIG, sig);    // Check for signal events    boolean buy=crossedAbove(series, index, Values.EMDI, Values.SIG);    boolean sell=crossedBelow(series, index, Values.EMDI, Values.SIG);    series.setBoolean(index, Signals.SELL, sell);    series.setBoolean(index, Signals.BUY, buy);    if (sell) {      var c=new Coordinate(series.getStartTime(index), emdi);      var marker=getSettings().getMarker(Inputs.DOWN_MARKER);      String msg = get("SELL_PRICE_EMDI", Util.round(price, 2), Util.round(emdi, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, price);    }    if (buy) {      var c=new Coordinate(series.getStartTime(index), emdi);      var marker=getSettings().getMarker(Inputs.UP_MARKER);      String msg = get("BUY_PRICE_EMDI", Util.round(price, 2), Util.round(emdi, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));      ctx.signal(index, Signals.BUY, msg, price);    }    series.setComplete(index);  }}