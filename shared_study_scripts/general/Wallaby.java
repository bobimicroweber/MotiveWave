package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Wallaby Indicator.  Developed by Rob Booker. */
@StudyHeader(
    namespace="com.motivewave", 
    id="WALLABY", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_WALLABY",
    desc="DESC_WALLABY",
    menu="MENU_GENERAL",
    menu2="MENU_CUSTOM",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/wallaby.htm")
public class Wallaby extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { WALLABY, K, P_K, P_D };

  final static String K_PERIOD = "kPeriod";
  final static String D_PERIOD = "dPeriod";
  final static String TIMEFRAME1 = "timeframe1";
  final static String TIMEFRAME2 = "timeframe2";
  final static String TIMEFRAME3 = "timeframe3";
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(K_PERIOD, get("LBL_K_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(D_PERIOD, get("LBL_D_PERIOD"), 3, 1, 9999, 1));
    inputs.addRow(new BarSizeDescriptor(TIMEFRAME1, get("LBL_TIMEFRAME1"), BarSize.getBarSize(BarSizeType.LINEAR, 15)));
    inputs.addRow(new BarSizeDescriptor(TIMEFRAME2, get("LBL_TIMEFRAME2"), BarSize.getBarSize(BarSizeType.LINEAR, 30)));
    inputs.addRow(new BarSizeDescriptor(TIMEFRAME3, get("LBL_TIMEFRAME3"), BarSize.getBarSize(BarSizeType.LINEAR, 60)));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, true));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 80, 1, 100, 1, true));
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 20, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(K_PERIOD, get("LBL_K_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(D_PERIOD, get("LBL_D_PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(TIMEFRAME1, TIMEFRAME2, TIMEFRAME3, Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.METHOD, K_PERIOD, D_PERIOD, TIMEFRAME1, TIMEFRAME2, TIMEFRAME3);
    desc.exportValue(new ValueDescriptor(Values.WALLABY, get("LBL_WALLABY"), new String[] {Inputs.METHOD, K_PERIOD, D_PERIOD, TIMEFRAME1, TIMEFRAME2, TIMEFRAME3}));
    desc.declarePath(Values.WALLABY, Inputs.PATH);
    desc.declareIndicator(Values.WALLABY, Inputs.IND);
    desc.setRangeKeys(Values.WALLABY);
    desc.setMaxBottomValue(10);
    desc.setMinTopValue(90);
    desc.setMinTick(0.1);
  }

  /** By default, this method is called on events where the data series has been affected. */
  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series = ctx.getDataSeries();

    // Calculate the stochastic values for each of the three data series
    var series1 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME1));
    for(int i = 0; i < series1.size(); i++) {
      if (series1.isComplete(i)) {
        continue;
      }
      stochastic(i, series1);
    }
    var series2 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME2));
    for(int i = 0; i < series2.size(); i++) {
      if (series2.isComplete(i)) {
        continue;
      }
      stochastic(i, series2);
    }
    var series3 = ctx.getDataSeries(getSettings().getBarSize(TIMEFRAME3));
    for(int i = 0; i < series3.size(); i++) {
      if (series3.isComplete(i)) {
        continue;
      }
      stochastic(i, series3);
    }
    
    var bs = series.getBarSize();
    
    for(int i = 0; i < series.size(); i++) {
      // We cannot use series.isComplete() since the series may be one of the timeframes (computed above)
      // Instead check to see if the WALLABY value has already been computed and its not the latest bar
      if (series.getDouble(i, Values.WALLABY) != null && i < series.size()-1) continue;
      Double s1 = getSeriesValue(series1, series.getStartTime(i), series.getEndTime(i), bs);  
      Double s2 = getSeriesValue(series2, series.getStartTime(i), series.getEndTime(i), bs);  
      Double s3 = getSeriesValue(series3, series.getStartTime(i), series.getEndTime(i), bs);  
      
      if (s1 == null || s2 == null || s3 == null) continue;
      series.setDouble(i, Values.WALLABY, (s1 + s2 + s3)/3);
      series.setComplete(i, series.isBarComplete(i));
    }
  }
  
  protected void stochastic(int index, DataSeries series)
  {
    int kPeriod = getSettings().getInteger(K_PERIOD);
    int period = getSettings().getInteger(D_PERIOD);
    int signalPeriod = 3;
    if (index < kPeriod) return;

    series.setDouble(index, Values.K, series.stochasticK(index, kPeriod));
    
    if (index < kPeriod + period) return;
    
    // Calculate the Slow MA
    var method = getSettings().getMAMethod(Inputs.METHOD);
    Double slowK = series.ma(method,  index, period, Values.K);
    series.setDouble(index, Values.P_K, slowK);

    if (index < kPeriod + period + signalPeriod) return;

    Double fastK = series.ma(method,  index, signalPeriod, Values.P_K);
    series.setDouble(index, Values.P_D, fastK);

    series.setComplete(index);
  }  

  protected Double getSeriesValue(DataSeries series, long start, long end, BarSize bs)
  {
    int s = series.findIndex(start);
    if (s < 0) return null;
    if (series.getBarSize().getIntervalMinutes() == bs.getIntervalMinutes()) {
      // exact match, just get the value
      Double val = series.getDouble(s, Values.P_D);
      return val;
    }
    if (series.getBarSize().getIntervalMinutes() > bs.getIntervalMinutes()) {
      // Get the value of the higher level timeframe
      Double val = series.getDouble(s, Values.P_D);
      return val;
    }
    
    // Get all of the values and return the average
    double total = 0;
    double count = 0;
    for(int i = s; i < series.size(); i++) {
      if (series.getStartTime(i) >= end) break;
      Double val = series.getDouble(i, Values.P_D);
      if (val == null) continue;
      total += val;
      count++;
    }
    return total/count;
  }
}
