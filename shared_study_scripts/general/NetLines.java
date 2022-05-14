package com.motivewave.platform.study.general;
import java.awt.Color;
import java.awt.Font;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Label;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

//Net Lines 205
/** Net Lines is a reversal pattern indicator. After 3 consecutive up/down bars a
Net Line is drawn at the high/low of the first bar.  The penetration of this line is a buy/sell signal.
This buy/sell condition is on until the Time Out (5 bars) is exceeded. Any inside bars will be ignored. */
@StudyHeader(
    namespace="com.motivewave",
    id="NET_LINES",
    rb="com.motivewave.platform.study.nls.strings2",
    name="NAME_NET_LINES", 
    desc="DESC_NET_LINES",
    label="LBL_NL",
    helpLink= "", 
    overlay=true )
public class NetLines extends Study 
{
  final static String SHOW_LABEL = "showLabel";
  final static String SHOW_PRICE = "showPrice";
  final static String TIME_OUT = "timeOut";
  
  enum Signals {BUY, SELL};
  enum Values { BUY_PIVOT, SELL_PIVOT, UP_TREND, DN_TREND};

  int highsOffset = 3;
  int lowsOffset = 3;

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("GENERAL"));
    
    var inputs = tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("HIGH_INPUT"), Enums.BarInput.HIGH));
    inputs.addRow(new InputDescriptor(Inputs.INPUT2, get("LOW_INPUT"), Enums.BarInput.LOW));
    inputs.addRow(new IntegerDescriptor(TIME_OUT, get("TIME_OUT"), 3, 1, 99, 1));
    inputs.addRow(new FontDescriptor(Inputs.FONT, get("FONT"), defaults.getFont()));
    inputs.addRow(new BooleanDescriptor(SHOW_LABEL, get("SHOW_LABELS"), false));
    inputs.addRow(new BooleanDescriptor(SHOW_PRICE, get("SHOW_PRICES"), false));
    
    var colors = tab.addGroup(get("COLORS"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("HIGHER_PRICES"), defaults.getGreen(), 1.0f, null, true, false, false));
    colors.addRow(new PathDescriptor(Inputs.PATH2, get("LOWER_PRICES"), defaults.getRed(), 1.0f, null, true, false, false));

    var markers=tab.addGroup(get("MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("BUY_MARKER"), Enums.MarkerType.LINE_ARROW,
        Enums.Size.VERY_LARGE, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("SELL_MARKER"), Enums.MarkerType.LINE_ARROW,
        Enums.Size.VERY_LARGE, defaults.getRed(), defaults.getLineColor(), true, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.INPUT2, TIME_OUT, Inputs.PATH, Inputs.PATH2);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));
    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));
    
    desc.declareSignal(Signals.SELL, get("SELL"));
    desc.declareSignal(Signals.BUY, get("BUY"));

    desc.setLabelSettings(Inputs.INPUT, Inputs.INPUT2, TIME_OUT);
    desc.setRangeKeys(Values.BUY_PIVOT, Values.SELL_PIVOT);
  }
  
  @Override
  public void onLoad(Defaults defaults)
  {
    int p = getSettings().getInteger(TIME_OUT);
    setMinBars(p * 10);
  }
  
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    boolean showLabel = getSettings().getBoolean(SHOW_LABEL, false);
    boolean showPrice = getSettings().getBoolean(SHOW_PRICE, false);
    
    int timeOut = getSettings().getInteger(TIME_OUT);
    var series = ctx.getDataSeries();
    if(index < timeOut + 3) return;
    
    Object hInput = getSettings().getInput(Inputs.INPUT);
    Object lInput = getSettings().getInput(Inputs.INPUT2);

    var hPath = getSettings().getPath(Inputs.PATH);
    Color hColor = hPath.getColor();
    var lPath = getSettings().getPath(Inputs.PATH2);
    Color lColor = lPath.getColor();
    
    var fi=getSettings().getFont(Inputs.FONT);
    Font f=fi.getFont();
    var dnMark=getSettings().getMarker(Inputs.DOWN_MARKER);
    var upMark=getSettings().getMarker(Inputs.UP_MARKER);
    
    double pointSize = series.getInstrument().getTickSize() * 10.0;
    int round = (int) Math.log10(1.0 / pointSize) + 1;  
 
    highsOffset = lowsOffset = 3;
    int hInd = highsIndex(index, series, hInput);
    int lInd = lowsIndex(index, series, lInput);
    double netLineHighs = series.getLow(hInd);
    double netLineLows = series.getHigh(lInd);
    int highsLineLth = timeOut + highsOffset -1;
    int lowsLineLth = timeOut + lowsOffset -1;
     if(hInd != -1){
      var sl = new Coordinate(series.getStartTime(hInd), netLineHighs);
      var el = new Coordinate(series.getStartTime(hInd+highsLineLth), netLineHighs);
      Line bp = new Line(sl, el, lPath);
      if(showPrice) bp.setText(Double.toString(Util.round(netLineHighs, round)), f);
      addFigure(bp);
      for (int i = hInd; i <= hInd + highsLineLth; i++){  //modify timeOut
        series.setDouble(i, Values.BUY_PIVOT, netLineHighs);
      }
      if(showLabel){  
        Label lbl=new Label(get("SP"), f,  X11Colors.BLACK, lColor);
        var lowC = new Coordinate(series.getStartTime(hInd), netLineHighs);
        lbl.setLocation(lowC);
        addFigure(lbl);
      }
    }
     
     if(lInd != -1){
       var sl = new Coordinate(series.getStartTime(lInd), netLineLows);
       var el = new Coordinate(series.getStartTime(lInd+lowsLineLth), netLineLows);
       Line sp = new Line(sl, el, hPath);
       if(showPrice) sp.setText(Double.toString(Util.round(netLineLows, round)), f);
       addFigure(sp);
       
      for (int i = lInd; i <= lInd + lowsLineLth; i++){
       series.setDouble(i, Values.SELL_PIVOT, netLineLows);
      }
      if(showLabel){
        Label lbl=new Label(get("BP"), f,  X11Colors.BLACK, hColor);
        var highC = new Coordinate(series.getStartTime(lInd), netLineLows);
        lbl.setLocation(highC);
        addFigure(lbl);
      }
    }
    
    double curSellPivot = series.getDouble(index, Values.SELL_PIVOT, Double.MAX_VALUE);
    double curBuyPivot = series.getDouble(index, Values.BUY_PIVOT, 0);
    double high = series.getHigh(index);
    double low = series.getLow(index);
    
    boolean sell = high > curSellPivot;
    boolean buy = low < curBuyPivot;
    series.setBoolean(Signals.BUY, buy);
    series.setBoolean(Signals.SELL, sell);
    long time = series.getStartTime(index);
    
    if (sell) {
      var c=new Coordinate(time, curSellPivot);
      String msg = get("SELL_PRICE", Util.round(curSellPivot, round));
      if (upMark.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, dnMark, msg));
      ctx.signal(index, Signals.SELL, msg, high);
    }
    if (buy) {
      var c=new Coordinate(time, curBuyPivot);
      String msg = get("BUY_PRICE", Util.round(curBuyPivot, round));
      if (dnMark.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, upMark, msg));
      ctx.signal(index, Signals.BUY, msg, low);
    }
    if (series.isBarComplete(index)) series.setComplete(index);
  }
  
  //returns index of Net Line (third lower high)
  //adjust highOffset if necessary (if inside bars)
  private int highsIndex(int index, DataSeries series, Object hInput) 
  {
    double lastPrice = series.getDouble(index, hInput, 0);
    double prevHigh = series.getHigh(index); //0.0;
    double prevLow = series.getLow(index);  //Double.MAX_VALUE;
    int i = 0;
    highsOffset = 3;
    for (i = index-1; i > index - highsOffset; i--){
      if(i < 0) return -1;
      double high = series.getHigh(i);
      double low = series.getLow(i);
      if(high < prevHigh && low > prevLow){ //inside bar
        highsOffset++;  //if inside bar skip over
        continue;
      }
      prevHigh = high;
      prevLow = low;
      
      double price = series.getDouble(i, hInput, 0);
      if(price >= lastPrice) return -1;
      lastPrice = price;
    }
    return i+1;
  }
  
  //returns index of Net Line (third higher low)
  //adjust lowsOffset if necessary (if inside bars)
  private int lowsIndex(int index, DataSeries series, Object lInput) 
  {
    double lastPrice = series.getDouble(index, lInput, 0);
    double prevHigh = series.getHigh(index); //0.0;
    double prevLow = series.getLow(index);  //Double.MAX_VALUE;
    int i = 0;
    lowsOffset = 3;
    for (i = index-1; i > index - lowsOffset; i--){
      if(i < 0) return -1;
      double high = series.getHigh(i);
      double low = series.getLow(i);
      if(high < prevHigh && low > prevLow){ //inside bar
        lowsOffset++;  //if inside bar skip over
        continue;
      }
      prevHigh = high;
      prevLow = low;
      
      double price = series.getDouble(i, lInput, 0);
      if(price <= lastPrice) return -1;
      lastPrice = price;
    }
    return i+1;
  }
}











