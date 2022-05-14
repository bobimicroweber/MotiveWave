package com.motivewave.platform.study.general2;import java.util.Calendar;import java.util.GregorianCalendar;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Halloween Indicator 178 */@StudyHeader(  namespace="com.motivewave",  id="ID_HALLOWEEN",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_HLWNI",  name="NAME_HALLOWEEN_INDICATOR",  desc="DESC_HLWNI",  helpLink="http://www.motivewave.com/studies/halloween_indicator.htm",  signals=true,  overlay=true,  studyOverlay=true)public class HalloweenInd extends Study{  final static String HIGH_LOW="HighLow";  enum Values { HLWNI }  protected enum Signals { SELL, BUY }  double highSell=Double.NEGATIVE_INFINITY;  double lowBuy=Double.MAX_VALUE;  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.SMA));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 50, 1, 9999, 1));    var settings=tab.addGroup(get("PATH_INDICATOR"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("INDICATOR"), defaults.getLineColor(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 50, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.PATH);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.HLWNI, get("LBL_HLWNI"), new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3 }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.declarePath(Values.HLWNI, Inputs.PATH);    desc.declareIndicator(Values.HLWNI, Inputs.IND);    desc.setRangeKeys(Values.HLWNI);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int period=getSettings().getInteger(Inputs.PERIOD);    if (index < period) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    var method=getSettings().getMAMethod(Inputs.METHOD);    var series=ctx.getDataSeries();    double price=series.getDouble(index, key, 0);    Double hlwni=series.ma(method, index, period, key);    if (hlwni == null) return;    series.setDouble(index, Values.HLWNI, hlwni);    // Check for signal events    var cal=new GregorianCalendar();    long curBarTime=series.getStartTime(index);    cal.setTimeInMillis(curBarTime);    int mth=cal.get(Calendar.MONTH);    boolean sell=mth == Calendar.MAY;    boolean buy=price < hlwni && mth == Calendar.OCTOBER;    series.setBoolean(index, Signals.SELL, sell);    series.setBoolean(index, Signals.BUY, buy);    if (sell) {      var c=new Coordinate(series.getStartTime(index), hlwni);      var marker=getSettings().getMarker(Inputs.DOWN_MARKER);       String msg = get("SELL_PRICE_HLWNI", Util.round(price, 2), Util.round(hlwni, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, price);    }    if (buy) {      var c=new Coordinate(series.getStartTime(index), hlwni);      var marker=getSettings().getMarker(Inputs.UP_MARKER);       String msg = get("BUY_PRICE_HLWNI", Util.round(price, 2), Util.round(hlwni, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));      ctx.signal(index, Signals.BUY, msg, price);    }    series.setComplete(index);  }}