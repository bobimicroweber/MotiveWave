package com.motivewave.platform.study.general2;import java.awt.Color;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.DataSeries;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.GuideInfo;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.MarkerInfo;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;import com.motivewave.platform.sdk.common.desc.ColorDescriptor;import com.motivewave.platform.sdk.common.desc.EnabledDependency;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** RSI Divergence 171 */@StudyHeader(  namespace="com.motivewave",  id="ID_RSI_DIVERGENCE",  rb="com.motivewave.platform.study.nls.strings2",  name="NAME_RSI_DIVERGENCE",  label="LBL_RSIDIV",  desc="DESC_RSIDIV",  menu="MENU_OSCILLATORS",  helpLink="http://www.motivewave.com/studies/rsi_divergence.htm",  signals=true,  overlay=false,  requiresVolume=false,  studyOverlay=true)public class RsiDivergence extends Study{  final static String BEARS="Bears";  final static String BULLS="Bulls";  final static String USE_METHOD="UseMethod";  final static String NEUTRAL="Neutral";  final static String NEUTRAL_ON="NeutralOn";  final static String[] TREND= { "Close", "High-Low", "Open", "Midpoint", "Typical", "Weighted" };  enum Values { RSI, MA1, MA2, UP, DOWN }  protected enum Signals { SELL, BUY }  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("RSI_INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("RSI_PERIOD"), 14, 1, 999, 1));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("RSI_METHOD"), Enums.MAMethod.EMA));    inputs.addRow(new InputDescriptor(Inputs.INPUT2, get("TREND_INPUT"), TREND, TREND[0]));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("TREND_PERIOD"), 4, 1, 999, 1));    inputs.addRow(new BooleanDescriptor(USE_METHOD, get("USE_TREND_METHOD"), false));    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD2, get("TREND_METHOD"), Enums.MAMethod.SMA));    inputs.addRow(new BooleanDescriptor(NEUTRAL_ON, get("NEUTRAL_COLOR_ON"), true));    var settings=tab.addGroup(get("PATH_INDICATOR"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));    settings=tab.addGroup(get("SHADING"));    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    settings=tab.addGroup(get("COLORS"));    settings.addRow(new ColorDescriptor(BEARS, get("BEARISH_DIVERGENCE"), defaults.getRed()));    settings.addRow(new ColorDescriptor(BULLS, get("BULLISH_DIVERGENCE"), defaults.getGreen()));    settings.addRow(new ColorDescriptor(NEUTRAL, get("NEUTRAL_BAR_COLOR"), defaults.getGrey()));    var guides=tab.addGroup(get("GUIDES"));    var topDesc=new GuideDescriptor(Inputs.TOP_GUIDE, get("TOP_GUIDE"), 70, 0, 999.1, .1, true);    topDesc.setLineColor(defaults.getRed());    guides.addRow(topDesc);    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 50, 0, 999.1, .1, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    var bottomDesc=new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("BOTTOM_GUIDE"), 30, 0, 999.1, .1, true);    bottomDesc.setLineColor(defaults.getGreen());    guides.addRow(bottomDesc);    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));    sd.addDependency(new EnabledDependency(USE_METHOD, Inputs.METHOD2));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("RSI_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.METHOD);    sd.addQuickSettings(Inputs.INPUT2);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("TREND_PERIOD"), 4, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.METHOD, Inputs.INPUT2, Inputs.PERIOD2, USE_METHOD, Inputs.METHOD2);    desc.exportValue(new ValueDescriptor(Values.RSI, get("LBL_RSIDIV"), new String[] { Inputs.INPUT, Inputs.PERIOD }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.declarePath(Values.RSI, Inputs.PATH);    desc.declareIndicator(Values.RSI, Inputs.IND);    desc.setRangeKeys(Values.RSI);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    int p2=getSettings().getInteger(Inputs.PERIOD2);    setMinBars(Math.max(p1, p2) * 3);  }  @Override  protected void calculate(int index, DataContext ctx)  {    if (index < 1) return;    int rsiP=getSettings().getInteger(Inputs.PERIOD);    int trendP=getSettings().getInteger(Inputs.PERIOD2);    double price=0, priorPrice=0, low=0, priorLow=0, high=0, priorHigh=0;    Object input=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    String trend=(String) getSettings().getInput(Inputs.INPUT2, TREND[0]);    Enums.MAMethod rsiMethod=getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA);    boolean useTM=getSettings().getBoolean(USE_METHOD);    Enums.MAMethod tMethod=getSettings().getMAMethod(Inputs.METHOD2, Enums.MAMethod.SMA);    boolean neutralOn=getSettings().getBoolean(NEUTRAL_ON);    GuideInfo topGuide=getSettings().getGuide(Inputs.TOP_GUIDE);    double topG=topGuide.getValue();    GuideInfo bottGuide=getSettings().getGuide(Inputs.BOTTOM_GUIDE);    double bottG=bottGuide.getValue();    Object tInput1=null;    Object tInput2=Enums.BarInput.LOW; // used in High-Low    if (trend == "Close") {      tInput1=Enums.BarInput.CLOSE;    }    if (trend == "High-Low") {      tInput1=Enums.BarInput.HIGH;    } // tInput1 for high, tInput2 for low    if (trend == "Open") {      tInput1=Enums.BarInput.OPEN;    }    if (trend == "Midpoint") {      tInput1=Enums.BarInput.MIDPOINT;    }    if (trend == "Typical") {      tInput1=Enums.BarInput.TP;    }    if (trend == "Weighted") {      tInput1=Enums.BarInput.WP;    }    DataSeries series=ctx.getDataSeries();    double diff=series.getDouble(index, input) - series.getDouble(index - 1, input);    double up=0, down=0;    if (diff > 0) up=diff;    else down=diff;    series.setDouble(index, Values.UP, up);    series.setDouble(index, Values.DOWN, Math.abs(down));    if (index < Math.max(rsiP, trendP) + 1) return;    Double avgUp=series.ma(rsiMethod, index, rsiP, Values.UP);    if (avgUp == null) return;    Double avgDown=series.ma(rsiMethod, index, rsiP, Values.DOWN);    if (avgDown == null) return;    double rs=avgUp / avgDown;    double rsi=100.0 - (100.0 / (1.0 + rs));    series.setDouble(index, Values.RSI, rsi);    if (useTM) { // Trend method option      Double ma1=series.ma(tMethod, index, trendP, tInput1);      if (ma1 == null) return;      Double ma2=series.ma(tMethod, index, trendP, tInput2); // used for High-Low      if (ma2 == null) return;      series.setDouble(index, Values.MA1, ma1);      series.setDouble(index, Values.MA2, ma2); // used for High-Low    }    if (index < Math.max(rsiP, trendP) + trendP + 1) return;    if (useTM) { // Trend Method      price=series.getDouble(index, Values.MA1, 0);      priorPrice=series.getDouble(index - trendP, Values.MA1, 0);    }    else {      price=series.getDouble(index, tInput1, 0);      priorPrice=series.getDouble(index - trendP, tInput1, 0);    }    double priorRsi=series.getDouble(index - trendP, Values.RSI, rsi);    boolean bearDiv=(price > priorPrice && rsi < priorRsi && rsi >= topG);    boolean bullDiv=(price < priorPrice && rsi > priorRsi && rsi <= bottG);    if (trend == "High-Low") { // 2 prices are needed to overwrite bearDivergence and bullDivergence      high=price;      priorHigh=priorPrice;      if (useTM) { // If Trend Method use moving average data        low=series.getDouble(index, Values.MA2, 0);        priorLow=series.getDouble(index - trendP, Values.MA2, 0);      }      else { // else use raw data for trend        low=series.getDouble(index, tInput2, 0);        priorLow=series.getDouble(index - trendP, tInput2, 0);      }      bearDiv=(high > priorHigh && rsi < priorRsi && rsi >= topG);      bullDiv=(low < priorLow && rsi > priorRsi && rsi <= bottG);    }    Color bearC=getSettings().getColor(BEARS);    Color bullC=getSettings().getColor(BULLS);    Color neutralC=getSettings().getColor(NEUTRAL);    if (neutralOn) series.setPriceBarColor(index, neutralC);    if (bearDiv) series.setPriceBarColor(index, bearC);    if (bullDiv) series.setPriceBarColor(index, bullC);    // signals    boolean sell=bearDiv;    boolean buy=bullDiv;    series.setBoolean(index, Signals.BUY, buy);    series.setBoolean(index, Signals.SELL, sell);    if (sell) {      Coordinate c=new Coordinate(series.getStartTime(index), rsi);      MarkerInfo marker=getSettings().getMarker(Inputs.DOWN_MARKER);      String msg = get("SELL_PRICE_RSI", Util.round(price, 2), Util.round(rsi, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, price);    }    if (buy) {      Coordinate c=new Coordinate(series.getStartTime(index), rsi);      MarkerInfo marker=getSettings().getMarker(Inputs.UP_MARKER);      String msg = get("BUY_PRICE_RSI", Util.round(price, 2), Util.round(rsi, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));      ctx.signal(index, Signals.BUY, msg, price);    }    series.setComplete(index);  }}