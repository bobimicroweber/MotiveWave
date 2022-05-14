package com.motivewave.platform.study.general2;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.DataSeries;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.GuideInfo;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.MarkerInfo;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Inverse Fisher Transform John Ehlers 145 */@StudyHeader(  namespace="com.motivewave",  id="ID_IFISH",  rb="com.motivewave.platform.study.nls.strings2",  name="NAME_INVERSE_FISHER_TRANSFORM",  label="LBL_IFISH",  desc="DESC_IFISH",  menu="MENU_JOHN_EHLERS",  helpLink="http://www.motivewave.com/studies/inverse_fisher_transform.htm",  signals=true,  overlay=false,  studyOverlay=true)public class InverseFisherTrans extends Study{  enum Values { VALUE1, IFISH, UP, DOWN }  protected enum Signals { BUY, SELL }  double highSell=Double.NEGATIVE_INFINITY;  double lowBuy=Double.MAX_VALUE;  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("RSI_PERIOD"), 5, 10, 999, 1));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("RSI_METHOD"), Enums.MAMethod.EMA));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("AVERAGE_PERIOD"), 9, 1, 999, 1));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD2, get("AVERAGE_METHOD"), Enums.MAMethod.WMA));    var settings=tab.addGroup(get("PATH_INDICATOR"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_IFISH"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var guides=tab.addGroup(get("GUIDES"));    var topDesc=new GuideDescriptor(Inputs.TOP_GUIDE, get("TOP_GUIDE"), .5, 0, 9.01, .01, true);    topDesc.setLineColor(defaults.getRed());    guides.addRow(topDesc);    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -9.01, 9.01, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    var bottomDesc=new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("BOTTOM_GUIDE"), -.5, -9.01, 0, .01, true);    bottomDesc.setLineColor(defaults.getGreen());    guides.addRow(bottomDesc);    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));    var shade=tab.addGroup(get("SHADING"));    shade.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    shade.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("RSI_PERIOD"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.METHOD);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("AVERAGE_PERIOD"), 9, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.METHOD2);    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2, Inputs.METHOD2);    desc.exportValue(new ValueDescriptor(Values.IFISH, get("LBL_IFISH"), new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2 }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.declarePath(Values.IFISH, Inputs.PATH);    desc.declareIndicator(Values.IFISH, Inputs.IND);    desc.setRangeKeys(Values.IFISH);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    int p2=getSettings().getInteger(Inputs.PERIOD2);    setMinBars(p1 + p2);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    int p2=getSettings().getInteger(Inputs.PERIOD2);    if (index < 1) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    Enums.MAMethod rsiMethod=getSettings().getMAMethod(Inputs.METHOD);    Enums.MAMethod avMethod=getSettings().getMAMethod(Inputs.METHOD2);    DataSeries series=ctx.getDataSeries();    double price=series.getDouble(index, key, 0);    double diff=series.getDouble(index, key) - series.getDouble(index - 1, key);    double up=0, down=0;    if (diff > 0) up=diff;    else down=diff;    series.setDouble(index, Values.UP, up);    series.setDouble(index, Values.DOWN, Math.abs(down));    if (index < p1 + 1) return;    Double avgUp=series.ma(rsiMethod, index, p1, Values.UP);    if (avgUp == null) return;    Double avgDown=series.ma(rsiMethod, index, p1, Values.DOWN);    if (avgDown == null) return;    double rs=avgUp / avgDown;    double rsi=100.0 - (100.0 / (1.0 + rs));    double value1=.1 * (rsi - 50);    series.setDouble(index, Values.VALUE1, value1);    if (index < p1 + p2 + 1) return;    Double av=series.ma(avMethod, index, p2, Values.VALUE1);    if (av == null) return;    double ifish=(Math.exp(2 * av) - 1) / (Math.exp(2 * av) + 1);    series.setDouble(index, Values.IFISH, ifish);    // Check for signal events    GuideInfo topGuide=getSettings().getGuide(Inputs.TOP_GUIDE);    double topG=topGuide.getValue();    GuideInfo bottomGuide=getSettings().getGuide(Inputs.BOTTOM_GUIDE);    double bottG=bottomGuide.getValue();    double prevI=series.getDouble(index - 1, Values.IFISH, 0.0);    boolean sell=ifish > topG && prevI > ifish && (ifish > highSell); // peaked above topG    boolean buy=ifish < bottG && prevI < ifish && (ifish < lowBuy); // trough below bottG    series.setBoolean(index, Signals.SELL, sell);    series.setBoolean(index, Signals.BUY, buy);    if (sell) {      lowBuy=Double.MAX_VALUE;      highSell=ifish;      Coordinate c=new Coordinate(series.getStartTime(index), ifish);      MarkerInfo marker=getSettings().getMarker(Inputs.DOWN_MARKER);      String msg = get("SELL_INVERSE_FISHER_TRANSFORM_PRICE_IFISH", Util.round(price, 2), Util.round(ifish, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, price);    }    if (buy) {      highSell=Double.NEGATIVE_INFINITY;      lowBuy=ifish;      Coordinate c=new Coordinate(series.getStartTime(index), ifish);      MarkerInfo marker=getSettings().getMarker(Inputs.UP_MARKER);      String msg = get("BUY_INVERSE_FISHER_TRANSFORM_PRICE_IFISH", Util.round(price, 2), Util.round(ifish, 3));       if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));      ctx.signal(index, Signals.BUY, msg, price);    }    series.setComplete(index);  }}