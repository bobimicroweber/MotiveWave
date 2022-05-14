package com.motivewave.platform.study.custom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.draw.ColorRange;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
    namespace="com.motivewave", 
    id="SWAMI_STOCHASTICS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_SWAMI_STOCHASTICS",
    desc="DESC_SWAMI_STOCHASTICS",
    menu="MENU_CUSTOM",
    overlay=false,
    supportsBarUpdates=false,
    helpLink="http://www.motivewave.com/studies/swami_stochastics.htm")
public class SwamiStochastic extends Study
{
  enum Values { NUMERATOR, DENOMINATOR, STOCH }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var range = tab.addGroup(get("LBL_RANGE"));
    range.addRow(new IntegerDescriptor(Inputs.MIN_PERIOD, get("LBL_MIN_PERIOD"), 12, 1, 9999, 1));
    range.addRow(new IntegerDescriptor(Inputs.MAX_PERIOD, get("LBL_MAX_PERIOD"), 48, 1, 9999, 1));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.MIN_PERIOD, get("LBL_MIN_PERIOD"), 12, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.MAX_PERIOD, get("LBL_MAX_PERIOD"), 48, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    
    var desc = createRD();
    desc.setLabelSettings(Inputs.MIN_PERIOD, Inputs.MAX_PERIOD);
    desc.setBottomInsetPixels(0);
    desc.setTopInsetPixels(0);
  }

  @Override
  public void clearState()
  {
    super.clearState();
    bars.clear();
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int minPeriod = getSettings().getInteger(Inputs.MIN_PERIOD);
    int maxPeriod = getSettings().getInteger(Inputs.MAX_PERIOD);
    if (minPeriod > maxPeriod) {
      int tmp = maxPeriod;
      minPeriod = maxPeriod;
      maxPeriod = tmp;
    }
    getRuntimeDescriptor().setFixedBottomValue(minPeriod);
    getRuntimeDescriptor().setFixedTopValue(maxPeriod);
    if (index+1 < minPeriod) {
      return;
    }
    
    DataSeries series = ctx.getDataSeries();
    int count = maxPeriod - minPeriod;

    // Calculate the stochastic
    long time = series.getStartTime(index);
    ColorRange range = bars.get(time);
    if (range == null) {
      range = new ColorRange(time);
      bars.put(time, range);
      addFigure(range);
    }
    range.clearRegions();
    
    double pNum[] = (double[])series.getValue(index-1, Values.NUMERATOR);
    double pDenom[] = (double[])series.getValue(index-1,Values.DENOMINATOR);
    double pStoch[] = (double[])series.getValue(index-1, Values.STOCH);
    double num[] = new double[count];
    double denom[] = new double[count];
    double stoch[] = new double[count];
    
    for(int i = 0; i < count; i++) {
      int period = i + minPeriod;
      Double high = series.highest(index, period, Enums.BarInput.HIGH);
      Double low = series.lowest(index, period, Enums.BarInput.LOW);
      
      if (high == null || low == null) {
        break;
      }
      
      num[i] = (series.getClose(index) - low + (pNum == null ? 0 : pNum[i]))/2;
      denom[i] = (high - low + (pDenom == null ? 0 : pDenom[i]))/2;
      
      if (denom[i] != 0) {
        stoch[i] = 0.2*(num[i]/denom[i]) + 0.8*(pStoch == null ? 0 : pStoch[i]);
      }
      
      int R = 255;
      int G = 255;
      
      if (stoch[i] > 0.5) R = (int)(255*(2 - 2*stoch[i]));
      else G = (int)(255*2*stoch[i]);
      
      range.addRegion(Util.awtColor(R, G, 0), period, period+1);
    }
    
    series.setValue(index, Values.NUMERATOR, num);
    series.setValue(index, Values.DENOMINATOR, denom);
    series.setValue(index, Values.STOCH, stoch);
    series.setComplete(index, series.isBarComplete(index));
  }
  
  private Map<Long, ColorRange> bars = Collections.synchronizedMap(new HashMap());
}