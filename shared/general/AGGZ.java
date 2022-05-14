package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** AGGZ  201 */
@StudyHeader(
  namespace="com.motivewave",
  id="ID_AGGZ",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_AGGZ",
  name="NAME_AGGZ",
  desc="DESC_AGGZ",
  helpLink= "",  //"http://www.motivewave.com/studies/aggz.htm",
  signals=true,
  overlay=false,
  studyOverlay=true)
public class AGGZ extends Study 
{
  enum Values  { AGGZ };
  enum Signals { SELL, BUY };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));

    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("SHORT_PERIOD"), 10, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LONG_PERIOD"), 200, 1, 9999, 1));

    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    tab=sd.addTab(get("DISPLAY"));

    var guides=tab.addGroup(get("GUIDES"));
    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), -999, 0, 999.1, .1, true);
    mg.setDash(new float[] { 3, 3 });
    guides.addRow(mg);

    settings=tab.addGroup(get("SHADING"));
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    var markers=tab.addGroup(get("MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,
        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,
        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("SHORT_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LONG_PERIOD"), 200, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.AGGZ, get("LBL_AGGZ"), new String[] { Inputs.INPUT, Inputs.METHOD,
      Inputs.PERIOD, Inputs.PERIOD2 }));
    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));
    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));
    desc.declareSignal(Signals.SELL, get("SELL"));
    desc.declareSignal(Signals.BUY, get("BUY"));

    desc.declarePath(Values.AGGZ, Inputs.PATH);
    desc.declareIndicator(Values.AGGZ, Inputs.IND);

    desc.setRangeKeys(Values.AGGZ);
  }

  @Override
  public void onLoad(Defaults defaults) 
  {
    int p1=getSettings().getInteger(Inputs.PERIOD);
    int p2=getSettings().getInteger(Inputs.PERIOD2);
    setMinBars(p1 + p2);
  }

  @Override
  protected void calculate(int index, DataContext ctx) 
  {
    int shortP=getSettings().getInteger(Inputs.PERIOD);
    int longP=getSettings().getInteger(Inputs.PERIOD2);
    if (index < longP) return;

    Object key = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    var method = getSettings().getMAMethod(Inputs.METHOD);

    var series=ctx.getDataSeries();
    
    double close = series.getClose(index);
    Double shortSma = series.ma(method, index, shortP, key);
    Double longSma = series.ma(method, index, longP, key);
    Double shortStd = series.std(index, shortP, key);
    Double longStd = series.std(index, longP, key);
    if (shortSma == null || longSma == null || shortStd == null || longStd == null) return;
    
    double shortZScore = (close - shortSma) / shortStd; 
    double longZScore = (close - longSma) / longStd; 
    double aggz = (-1 *(shortZScore + longZScore)) / 2.0;
    series.setDouble(index, Values.AGGZ, aggz);
    
 
    // Check for signal events
    var midGuide = getSettings().getGuide(Inputs.MIDDLE_GUIDE);
    double midG = midGuide.getValue();

    boolean sell = aggz < midG;
    boolean buy = aggz > midG;
    series.setBoolean(index, Signals.BUY, buy);
    series.setBoolean(index, Signals.SELL, sell);
    
    Boolean lastBuy = series.getBoolean(index-1, Signals.BUY);
    Boolean lastSell = series.getBoolean(index-1, Signals.SELL);
    if (lastBuy == null || lastSell == null) return;
    long time = series.getStartTime(index);
    
    //remove any markers on the current bar
    removePrevMarks(time);
    
    if (sell && !lastSell) {
      var c=new Coordinate(time, aggz);
      var dnMark=getSettings().getMarker(Inputs.DOWN_MARKER); 
      String msg = get("SELL_PRICE_AGGZ", Util.round(close, 3), Util.round(aggz, 3));
      if (dnMark.isEnabled()){
        Marker m = new Marker(c, Enums.Position.TOP, dnMark, msg);
        addFigure(m);
        prevSellMark = m;
      }
      ctx.signal(index, Signals.SELL, msg, close);
    }

    if (buy && !lastBuy) {
      var c=new Coordinate(time, aggz);
      var upMark=getSettings().getMarker(Inputs.UP_MARKER); // set default colour to getGreen() in initialise
      String msg = get("BUY_PRICE_AGGZ", Util.round(close, 3), Util.round(aggz, 3));
      if (upMark.isEnabled()){
        var m = new Marker(c, Enums.Position.BOTTOM, upMark, msg);
        addFigure(m);
        prevBuyMark = m;
      }
      ctx.signal(index, Signals.BUY, msg, close);
    }
    series.setComplete(index, series.isBarComplete(index));
  }
  
  private void removePrevMarks(long time)
  {
    if (prevSellMark != null && prevSellMark.getTime() == time) {
      removeFigure(prevSellMark);
      prevSellMark = null;
    }
    if (prevBuyMark != null && prevBuyMark.getTime() == time) {
      removeFigure(prevBuyMark);
      prevBuyMark = null;
    }      
  }

  private Marker prevSellMark = null, prevBuyMark = null;
}



















