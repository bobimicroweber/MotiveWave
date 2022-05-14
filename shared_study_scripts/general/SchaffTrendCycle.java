package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Schaff Trend Cycle  207 */
@StudyHeader(
  namespace="com.motivewave",
  id="ID_SCHAFF_TREND_CYCLE",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_STC",
  name="NAME_SCHAFF_TREND_CYCLE",
  desc="DESC_SCHAFF_TREND_CYCLE",
  helpLink= "",  //"http://www.motivewave.com/studies/schaff_trend_cycle.htm",
  signals=false,
  overlay=false,
  studyOverlay=true)
public class SchaffTrendCycle extends Study 
{
  static final String IND3 = "ind3";
  static final String FACTOR = "factor";
  enum Values { MACD, FRAC1, PF, FRAC2, STC };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));

    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("MACD_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("STC_PERIOD"), 10, 1, 9099, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("MACD_PERIOD1"), 23, 1, 9099, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD3, get("MACD_PERIOD2"), 50, 1, 9099, 1));
    inputs.addRow(new DoubleDescriptor(FACTOR, get("FACTOR"), .5, .1, 1, .1));

    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    tab=sd.addTab(get("DISPLAY"));

    var guides=tab.addGroup(get("GUIDES"));
    var topDesc=new GuideDescriptor(Inputs.TOP_GUIDE, get("TOP_GUIDE"), 75, 0, 999, .1, true);
    topDesc.setLineColor(defaults.getRed());
    guides.addRow(topDesc);
    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 50, 0, 999, .1, true);
    mg.setDash(new float[] { 3, 3 });
    guides.addRow(mg);
    var bottomDesc=new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("BOTTOM_GUIDE"), 25, 0, 999, .1, true);
    bottomDesc.setLineColor(defaults.getGreen());
    guides.addRow(bottomDesc);
   
    settings=tab.addGroup(get("SHADING"));
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("STC_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("MACD_PERIOD1"), 23, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD3, get("MACD_PERIOD2"), 50, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(FACTOR, Inputs.PATH);

    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3, FACTOR);
    desc.exportValue(new ValueDescriptor(Values.STC, get("LBL_STC"), new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3, FACTOR }));
    desc.declarePath(Values.STC, Inputs.PATH);
    desc.declareIndicator(Values.STC, Inputs.IND);
    desc.setRangeKeys(Values.STC);
  }

  @Override
  public void onLoad(Defaults defaults) {
    int p1=getSettings().getInteger(Inputs.PERIOD);
    int p2=getSettings().getInteger(Inputs.PERIOD2);
    int p3=getSettings().getInteger(Inputs.PERIOD3);
    setMinBars(Math.max(p2, p3) + p1);
  }

  @Override
  protected void calculate(int index, DataContext ctx) {
    int tcLen=getSettings().getInteger(Inputs.PERIOD);
    int macP1=getSettings().getInteger(Inputs.PERIOD2);
    int macP2=getSettings().getInteger(Inputs.PERIOD3);
    if(index < Math.max(macP1, macP2)) return;
    
    Object key = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    var macM = getSettings().getMAMethod(Inputs.METHOD);
    double factor = getSettings().getDouble(FACTOR);
    var series=ctx.getDataSeries();
    
    Double ma1 = series.ma(macM, index, macP1, key);
    Double ma2 = series.ma(macM, index, macP2, key);
    if (ma1 == null || ma2 == null) return;
    
    Double macd =  ma1 - ma2;
    series.setDouble(index, Values.MACD, macd);
    
    Double value1 = series.lowest(index, tcLen, Values.MACD);
    if(value1 == null) return;
    Double value2 = series.highest(index, tcLen, Values.MACD) - value1;
    
    Double prevFrac1 = series.getDouble(index-1, Values.FRAC1, 0.0);
    Double frac1 = (value2 > 0.0) ? ((macd - value1) / value2) * 100.0 : prevFrac1;
    series.setDouble(index, Values.FRAC1, frac1);
 
    Double prevPf = series.getDouble(index-1, Values.PF, 0.0);  //frac1);

    Double pf = prevPf + (factor * (frac1 - prevPf));
    series.setDouble(index, Values.PF, pf);
    
    Double value3 = series.lowest(index, tcLen, Values.PF);
    Double value4 = series.highest(index, tcLen, Values.PF) - value3;
    
    Double prevFrac2 = series.getDouble(index-1, Values.FRAC2, 0.0);
    Double frac2 = (value4 > 0.0) ? ((pf - value3) / value4) * 100.0 : prevFrac2;
    series.setDouble(index, Values.FRAC2, frac2);
    
    Double prevStc = series.getDouble(index-1, Values.STC, 0.0);  //frac2);

    Double stc = prevStc + (factor * (frac2 - prevStc));
    series.setDouble(index, Values.STC, stc);
 
    series.setComplete(index, series.isBarComplete(index));
  }
}