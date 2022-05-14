package com.motivewave.platform.study.general;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Fast & Slow Kurtosis 089 */@StudyHeader(  namespace="com.motivewave",  id="ID_FSKT",  rb="com.motivewave.platform.study.nls.strings2",  name="NAME_FAST_SLOW_KURTOSIS",  desc="DESC_FSKT",  label="LBL_FSKT",  helpLink="http://www.motivewave.com/studies/fast_and_slow_kurtosis.htm",  signals=true,  overlay=false,  studyOverlay=true)public class FastSlowKurtosis extends Study{  enum Values { MOM, FSK, SIG };  enum Signals { SELL, BUY };  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 3, 1, 999, 1));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.WMA));    var settings=tab.addGroup(get("PATHS"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_FSKT"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new PathDescriptor(Inputs.PATH2, get("SIGNAL"), defaults.getRed(), 1.0f, null, true, false, true));    settings=tab.addGroup(get("INDICATORS"));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_FSKT"), defaults.getLineColor(), null, false, true, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND2, get("SIGNAL"), defaults.getRed(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));    var guides=tab.addGroup(get("GUIDE"));    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -1.01, 1.01, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    settings=tab.addGroup(get("SHADING"));    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.METHOD, Inputs.PATH, Inputs.PATH2);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.METHOD);    desc.exportValue(new ValueDescriptor(Values.FSK, get("LBL_FSKT"), new String[] { Inputs.INPUT, Inputs.PERIOD, Inputs.METHOD }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declarePath(Values.FSK, Inputs.PATH);    desc.declarePath(Values.SIG, Inputs.PATH2);    desc.declareIndicator(Values.FSK, Inputs.IND);    desc.declareIndicator(Values.SIG, Inputs.IND2);    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.setRangeKeys(Values.FSK, Values.SIG);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars((p1 * 2) + 1);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int period=getSettings().getInteger(Inputs.PERIOD);    if (index < period) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    var method=getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.WMA);    var series=ctx.getDataSeries();    double price=series.getDouble(index, key, 0);    double mom=Utility.momentum(ctx, index, period, key);    series.setDouble(index, Values.MOM, mom);    if (index < period + 1) return;    double prevMom=series.getDouble(index - 1, Values.MOM);    double prevFsk=series.getDouble(index - 1, Values.FSK, 0.0); // returns 0.0 on first try    double fsk=(.03 * (mom - prevMom)) + ((1 - .03) * prevFsk);    series.setDouble(index, Values.FSK, fsk);    if (index < (period * 2) + 1) return;    Double sig=series.ma(method, index, period, Values.FSK);    series.setDouble(index, Values.SIG, sig);    // Check for signal events    boolean buy=crossedAbove(series, index, Values.FSK, Values.SIG);    boolean sell=crossedBelow(series, index, Values.FSK, Values.SIG);    series.setBoolean(index, Signals.SELL, sell);    series.setBoolean(index, Signals.BUY, buy);    if (sell) {      var c=new Coordinate(series.getStartTime(index), fsk);      var marker=getSettings().getMarker(Inputs.DOWN_MARKER);      String msg = get("SELL_PRICE_FSK", Util.round(price, 2), Util.round(fsk, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, price);    }    if (buy) {      var c=new Coordinate(series.getStartTime(index), fsk);      var marker=getSettings().getMarker(Inputs.UP_MARKER);      String msg = get("BUY_PRICE_FSK", Util.round(price, 2), Util.round(fsk, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));      ctx.signal(index, Signals.BUY, msg, price);    }    series.setComplete(index);  }}