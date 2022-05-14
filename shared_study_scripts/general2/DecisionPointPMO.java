package com.motivewave.platform.study.general2;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
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

@StudyHeader(
  namespace="com.motivewave",
  id="ID_DP_PMO",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_PMO",
  menu = "MENU_GENERAL",
  name="NAME_DECISION_POINT_MOMENTUM_OSC",
  desc="DESC_PMO",
  signals=true,
  overlay=false,
  studyOverlay=true)
public class DecisionPointPMO extends Study
{
  enum Values { TEMP1, TEMP2, PMO, SIG }
  enum Signals { CROSS_ABOVE, CROSS_BELOW }
  
  @Override
  public void initialize(Defaults defaults) 
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));
    
    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("FAST_PERIOD"), 20, 1, 999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("SLOW_PERIOD"), 35, 1, 999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD3, get("SIGNAL_PERIOD"), 10, 1, 999, 1));
    
    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PMO_PATH"), defaults.getLineColor(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("PMO_IND"), defaults.getLineColor(), null, false, true, true));
    settings.addRow(new PathDescriptor(Inputs.PATH2, get("SIGNAL_PATH"), defaults.getRed(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND2, get("SIGNAL_IND"), defaults.getRed(), null, false, true, true));

    tab=sd.addTab(get("DISPLAY"));
    var guides=tab.addGroup(get("GUIDES"));
    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, 0, 999.1, .1, true);
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
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("FAST_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("SLOW_PERIOD"), 35, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD3, get("SIGNAL_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.PATH2, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3);
    desc.exportValue(new ValueDescriptor(Values.PMO, get("LBL_PMO"), new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2 }));
    desc.exportValue(new ValueDescriptor(Values.SIG, get("SIG"), new String[] { Inputs.METHOD, Inputs.PERIOD3, }));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_BELOW, Enums.ValueType.BOOLEAN, get("CROSS_BELOW"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_ABOVE, Enums.ValueType.BOOLEAN, get("CROSS_ABOVE"), null));
    desc.declareSignal(Signals.CROSS_BELOW, get("CROSS_BELOW"));
    desc.declareSignal(Signals.CROSS_ABOVE, get("CROSS_ABOVE"));
    desc.declarePath(Values.PMO, Inputs.PATH);
    desc.declarePath(Values.SIG, Inputs.PATH2);
    desc.declareIndicator(Values.PMO, Inputs.IND);
    desc.declareIndicator(Values.SIG, Inputs.IND2);
    desc.setRangeKeys(Values.PMO, Values.SIG);
  }
  @Override
  public void onLoad(Defaults defaults) {
    int p1=getSettings().getInteger(Inputs.PERIOD);
    int p2=getSettings().getInteger(Inputs.PERIOD2);
    int p3=getSettings().getInteger(Inputs.PERIOD2);
    
    setMinBars(p1+p2+p3);
  }
  
  @Override
  protected void calculate(int index, DataContext ctx) {
    int shortP=getSettings().getInteger(Inputs.PERIOD);
    int longP=getSettings().getInteger(Inputs.PERIOD2);
    int sigP=getSettings().getInteger(Inputs.PERIOD3);
   
    DataSeries series = ctx.getDataSeries();
    
    Enums.MAMethod method = getSettings().getMAMethod(Inputs.METHOD);
    Object key = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    
      
   // Double ma1 = series.ma(method, index, p2, key);
   // if(ma1 == null) return;
    double price = series.getDouble(index, key, 0.0);
    double prevPrice = series.getDouble(index-1, key, 0.0);
    //(10 * 35-period Custom-EMA of ( ( (Today's Price/Yesterday's Price) * 100) - 100) )
    
    double temp1 = ((price/prevPrice) * 100.0) - 100.0;
    series.setDouble(index, Values.TEMP1, temp1);
    
    Double maLong = series.ma(method, index, longP, Values.TEMP1);
    if(maLong == null) return;
       
    double temp2 = 10 * maLong;
    series.setDouble(index, Values.TEMP2, temp2);
    
    Double pmo = series.ma(method, index, shortP, Values.TEMP2);
    if(pmo == null) return;
    series.setDouble(index, Values.PMO, pmo);
    
    Double sig = series.ma(method, index, sigP, Values.PMO);
    series.setDouble(index, Values.SIG, sig);
    
    //sell = crossBelow
    boolean crossBelow=crossedBelow(series, index, Values.PMO, Values.SIG);
    boolean crossAbove=crossedAbove(series, index, Values.PMO, Values.SIG);

    series.setBoolean(index, Signals.CROSS_ABOVE, crossAbove);
    series.setBoolean(index, Signals.CROSS_BELOW, crossBelow);
    //info("CrossAbove" + crossAbove);
    if (crossBelow) {
      Coordinate c=new Coordinate(series.getStartTime(index), pmo);
      MarkerInfo marker=getSettings().getMarker(Inputs.DOWN_MARKER); // set default colour to getRed() in initialize above
      String msg = get("CROSS_BELOW_PRICE_PMO", Util.round(price, 4), Util.round(pmo, 4));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, price);
    }
    if (crossAbove) {
      Coordinate c=new Coordinate(series.getStartTime(index), pmo);
      MarkerInfo marker=getSettings().getMarker(Inputs.UP_MARKER); // set default colour to getGreen() in initialize above
      String msg = get("CROSS_ABOVE_PRICE_PMO", Util.round(price, 4), Util.round(pmo, 4));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, price);
    }
    
    series.setComplete(index, series.isBarComplete(index));
  }
}