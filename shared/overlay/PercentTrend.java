package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Percent Trend */
@StudyHeader(
    namespace="com.motivewave", 
    id="PERCENT_TREND", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_PERCENT_TREND", 
    menu="MENU_OVERLAY",
    desc="DESC_PERCENT_TREND",
    label="LBL_PERCENT_TREND",
    overlay=true)
public class PercentTrend extends Study 
{
  final static String PERCENT_K = "percentK";
  
  enum Values { TREND, TP }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_MAX_PERIOD"), 20, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(PERCENT_K, get("LBL_PERCENT_K"), 15, 0.1, 999, 0.01));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, true, false));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), X11Colors.WHITE, X11Colors.BLACK, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_MAX_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(PERCENT_K, Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, PERCENT_K);
    desc.exportValue(new ValueDescriptor(Values.TREND, get("LBL_PERCENT_TREND"), new String[] {Inputs.PERIOD, PERCENT_K}));
    desc.declarePath(Values.TREND, Inputs.PATH);
    desc.declareIndicator(Values.TREND, Inputs.IND);
    desc.setRangeKeys(Values.TREND);
  }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    int Max_per = getSettings().getInteger(Inputs.PERIOD);
    double K = getSettings().getDouble(PERCENT_K);
    var series = ctx.getDataSeries();

    // Assuming C is the closing price
    double C = series.getClose(0);
    double Trend = C;
    int Period = 0;     //Vars: Trend(C), Period(0) ; {Trend Calculation}

    //boolean Condition1 = C > Trend; // {UpTrend}
    //boolean Condition2 = C <= Trend; //{ DownTrend}
    
    double prevC = C;
    
    for(int i = 1; i < series.size(); i++) {
      prevC = C;
      C = series.getClose(i);
      if (series.isComplete(i)) {
        Period = Util.toInt(series.getDouble(i, Values.TP));
        Trend = series.getDouble(i, Values.TREND);
        continue;
      }

      // {SetUp Period When New Trend Begin}
      if ( (prevC <= Trend && C > Trend)  || (prevC >= Trend && C < Trend) ) {
        Period = 0; //>C Cross over Trend[1] or C Cross Below Trend[1] Then Period = 0;
      }
      
      if (Period < Max_per ) { //Counting UpTrends with dynamic period}
        if (C > Trend) {
          Period = Period +1;
          double high = C;
          for(int j = i-Period; j <= i; j++) {
            if ( j < 0) continue;
            double close = series.getClose(j);
            if (close > high) high = close;
          }
          Trend = high*(1 - (K/100));
        } // Counting DownTrends with dynamic period}
        if (C <= Trend) {
          Period = Period + 1;
          double low = C;
          for(int j = i-Period; j <= i; j++) {
            if ( j < 0) continue;
            double close = series.getClose(j);
            if (close < low) low = close;
          }
          Trend = low*(1 + (K/100));
        }
      } 
      else { // {Counting UpTrends with constant period}
        if (C > Trend) {
          double high = C;
          for(int j = i-Max_per; j <= i; j++) {
            if ( j < 0) continue;
            double close = series.getClose(j);
            if (close > high) high = close;
          }
          Trend = high*(1 - (K/100));
        }
        //{Counting DownTrends with constant period}
        if (C <= Trend) {
          double low = C;
          for(int j = i-Max_per; j <= i; j++) {
            if ( j < 0) continue;
            double close = series.getClose(j);
            if (close < low) low = close;
          }
          Trend = low*(1 + (K/100));
        }
      } //

      series.setDouble(i, Values.TP, (double)Period);
      series.setDouble(i, Values.TREND, Trend);
      series.setComplete(i, series.isBarComplete(i));
    } 
  }  

}
