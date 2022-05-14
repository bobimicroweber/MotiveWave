package com.motivewave.platform.study.volume;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** See: http://www.emini-watch.com Uses a combination of bid/ask volume and range to identify volume climax, high
 * volume churn and low volume bars. Note: bid/ask volume is not available within MotiveWave so this is estimated (see
 * below) */
@StudyHeader(
  namespace="com.motivewave",
  id="BETTER_VOLUME",
  rb="com.motivewave.platform.study.nls.strings",
  name="TITLE_BETTER_VOLUME",
  tabName="BV_TAB",
  menu="MENU_VOLUME",
  desc="DESC_BETTER_VOLUME",
  overlay=false,
  requiresVolume=true,
  helpLink="http://www.motivewave.com/studies/better_volume.htm")
public class BetterVolumeIndicator extends com.motivewave.platform.sdk.study.Study
{
  enum Values
  {
    VOLUME, AVG, BARCOLOR, VAL1, VAL2, VAL3, VAL4, VAL5, VAL6, VAL7, VAL8, VAL9, VAL10, VAL11, VAL12, VAL13, VAL14, VAL15, VAL16, VAL17, VAL18, VAL19, VAL20, VAL21, VAL22, VAL23
  };

  final static String LOOKBACK="lookback", USE2BARS="use2Bars", PAINTBARS="paintBars", AVG_PERIOD="avgPeriod", VOLUME_BARS="volumeBars";
  // Colors
  final static String LOW_VOL_COLOR="lowVolColor", CLIMAX_UP_COLOR="climaxUpColor", CLIMAX_DOWN_COLOR="climaxDownColor", CHURN_COLOR="churnColor", CLIMAX_CHURN_COLOR="climaxChurnColor";
  final static String VOLUME_IND="volumeInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));

    var inputs=tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(LOOKBACK, get("LBL_LOOKBACK"), 20, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(AVG_PERIOD, get("LBL_AVG_PERIOD"), 100, 1, 9999, 1));
    inputs.addRow(new BooleanDescriptor(USE2BARS, get("LBL_USE_2_BARS"), true));
    inputs.addRow(new BooleanDescriptor(PAINTBARS, get("LBL_PAINT_BARS"), true));

    var colors=tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_AVG_LINE"), null, 1.0f, null, true, false, true));
    colors.addRow(new BarDescriptor(VOLUME_BARS, get("LBL_VOLUME_BARS"), defaults.getBlue(), true, false),
        new ColorDescriptor(LOW_VOL_COLOR, get("LBL_LOW_VOL_COLOR"), defaults.getYellow()));
    colors.addRow(new ColorDescriptor(CLIMAX_UP_COLOR, get("LBL_CLIMAX_UP_COLOR"), defaults.getRed()),
        new ColorDescriptor(CLIMAX_DOWN_COLOR, get("LBL_CLIMAX_DOWN_COLOR"), defaults.getGrey()));
    colors.addRow(new ColorDescriptor(CHURN_COLOR, get("LBL_CHURN_COLOR"), defaults.getGreen()),
        new ColorDescriptor(CLIMAX_CHURN_COLOR, get("LBL_CLIMAX_CHURN_COLOR"), defaults.getPurple()));
    colors.addRow(new IndicatorDescriptor(VOLUME_IND, get("LBL_VOLUME_IND"), null, null, false, true, true));

    sd.addQuickSettings(new SliderDescriptor(LOOKBACK, get("LBL_LOOKBACK"), 20, 1, 9999, true, () -> Enums.Icon.ARROW_LEFT.get()));
    sd.addQuickSettings(new SliderDescriptor(AVG_PERIOD, get("LBL_AVG_PERIOD"), 100, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(USE2BARS, PAINTBARS, Inputs.PATH, VOLUME_BARS, LOW_VOL_COLOR, CLIMAX_UP_COLOR, CLIMAX_DOWN_COLOR, CHURN_COLOR, CLIMAX_CHURN_COLOR);
    
    var desc=createRD();
    desc.setLabelSettings(LOOKBACK, AVG_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.AVG, get("LBL_BV_AVG"), new String[] { AVG_PERIOD }));
    desc.exportValue(new ValueDescriptor(Values.VOLUME, get("LBL_VOLUME"), new String[] {}));
    desc.declarePath(Values.AVG, Inputs.PATH);
    desc.declareBars(Values.VOLUME, VOLUME_BARS);
    desc.setFixedBottomValue(0);
    desc.setBottomInsetPixels(0);
    desc.setRangeKeys(Values.VOLUME);
    desc.declareIndicator(Values.VOLUME, VOLUME_IND);
  }

  /** This method is called when the latest bar in the data series has been updated. */
  @Override
  public void onBarUpdate(DataContext ctx)
  {
    // Just update the volume
    var series=ctx.getDataSeries();
    int last=series.size() - 1;
    series.setDouble(last, Values.VOLUME, (double) series.getVolumeAsFloat(last));
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series=ctx.getDataSeries();
    int last=series.size() - 1;

    // Inputs
    boolean use2Bars=getSettings().getBoolean(USE2BARS);
    boolean paintBars=getSettings().getBoolean(PAINTBARS);
    int avgPeriod=getSettings().getInteger(AVG_PERIOD);
    int lookback=getSettings().getInteger(LOOKBACK);
    Color lowVolColor=getSettings().getColor(LOW_VOL_COLOR);
    Color climaxUpColor=getSettings().getColor(CLIMAX_UP_COLOR);
    Color climaxDownColor=getSettings().getColor(CLIMAX_DOWN_COLOR);
    Color churnColor=getSettings().getColor(CHURN_COLOR);
    Color climaxChurnColor=getSettings().getColor(CLIMAX_CHURN_COLOR);
    var barInfo=getSettings().getBars(VOLUME_BARS);

    boolean C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15, C16, C17, C18, C19, C20;
    int places=6;

    for (int i=0; i < series.size(); i++) {
      if (series.isComplete(i)) continue;
      long vol=series.getVolume(i);
      if (vol == 0) {
        if (i == last) {
          // Hack: if this is a new bar, the volume will be 0. Calc the average anyway...
          series.setDouble(i, Values.AVG, series.sma(i, avgPeriod, Values.VAL3));
          if (paintBars) series.setPriceBarColor(i, barInfo.getColor());
        }
        continue;
      }

      series.setDouble(i, Values.VOLUME, (double) series.getVolumeAsFloat(i));

      double range=series.getRange(i);
      double open=series.getOpen(i);
      double close=series.getClose(i);
      double value1=0;
      double value2=0;

      // Note: upticks and downticks are not available here
      // Estimate based on formula provided in Better_Volume_Indicator.pdf
      if (close > open && range != 0) {
        value1=vol * (range / (2 * range + open - close));
      }
      else if (close < open && range != 0) {
        value1=vol * ((range + close - open) / (2 * range + close - open));
      }
      else {
        value1=0.5 * vol;
      }
      value2=vol - value1;

      value1=Util.round(value1, 1);
      value2=Util.round(value2, 1);

      double value3=Util.round(Math.abs(value1 + value2), places);
      double value4=Util.round(value1 * range, places);
      double value5=Util.round((value1 - value2) * range, places);
      double value6=Util.round(value2 * range, places);
      double value7=Util.round((value2 - value1) * range, places);
      double value8=0;
      double value9=0;
      double value10=0;
      double value11=0;
      double value12=0;

      if (range != 0) {
        value8=Util.round(value1 / range, places);
        value9=Util.round((value1 - value2) / range, places);
        value10=Util.round(value2 / range, places);
        value11=Util.round((value2 - value1) / range, places);
        value12=Util.round(value3 / range, places);
      }

      // Save these values for future use
      series.setDouble(i, Values.VAL1, value1);
      series.setDouble(i, Values.VAL2, value2);
      series.setDouble(i, Values.VAL3, value3);
      series.setDouble(i, Values.VAL4, value4);
      series.setDouble(i, Values.VAL5, value5);
      series.setDouble(i, Values.VAL6, value6);
      series.setDouble(i, Values.VAL7, value7);
      series.setDouble(i, Values.VAL8, value8);
      series.setDouble(i, Values.VAL9, value9);
      series.setDouble(i, Values.VAL10, value10);
      series.setDouble(i, Values.VAL11, value11);
      series.setDouble(i, Values.VAL12, value12);

      if (i < 1) continue;

      double highest=series.highest(i, 2, Enums.BarInput.HIGH);
      double lowest=series.lowest(i, 2, Enums.BarInput.LOW);
      double hl=highest - lowest;
      if (series.getDouble(i - 1, Values.VAL1) == null) continue;
      double pv1=series.getDouble(i - 1, Values.VAL1);
      double pv2=series.getDouble(i - 1, Values.VAL2);
      double pv3=series.getDouble(i - 1, Values.VAL3);

      double value13=Util.round(value3 + pv3, places);
      double value14=Util.round((value1 + pv1) * hl, places);
      double value15=Util.round((value1 + pv1 - value2 - pv2) * hl, places);
      double value16=Util.round((value2 + pv2) * hl, places);
      double value17=Util.round((value2 + pv2 - value1 - pv1) * hl, places);
      double value18=0;
      double value19=0;
      double value20=0;
      double value21=0;
      double value22=0;
      if (hl != 0) {
        value18=Util.round((value1 + pv1) / hl, places);
        value19=Util.round((value1 + pv1 - value2 - pv2) / hl, places);
        value20=Util.round((value2 + pv2) / hl, places);
        value21=Util.round((value2 + pv2 - value1 - pv1) / hl, places);
        value22=Util.round((value13) / hl, places);
      }

      series.setDouble(i, Values.VAL13, value13);
      series.setDouble(i, Values.VAL14, value14);
      series.setDouble(i, Values.VAL15, value15);
      series.setDouble(i, Values.VAL16, value16);
      series.setDouble(i, Values.VAL17, value17);
      series.setDouble(i, Values.VAL18, value18);
      series.setDouble(i, Values.VAL19, value19);
      series.setDouble(i, Values.VAL20, value20);
      series.setDouble(i, Values.VAL21, value21);
      series.setDouble(i, Values.VAL22, value22);

      if (i < lookback) continue;

      C1=C2=C3=C4=C5=C6=C7=C8=C9=C10=C11=C12=C13=C14=C15=C16=C17=C18=C19=C20=false;
      double pclose=series.getClose(i - 1);
      double popen=series.getOpen(i - 1);

      if (!use2Bars) {
        C1=value3 == series.lowest(i, lookback, Values.VAL3);
        C2=value4 == series.highest(i, lookback, Values.VAL4) && close > open;
        C3=value5 == series.highest(i, lookback, Values.VAL5) && close > open;
        C4=value6 == series.highest(i, lookback, Values.VAL6) && close < open;
        C5=value7 == series.highest(i, lookback, Values.VAL7) && close < open;
        C6=value8 == series.lowest(i, lookback, Values.VAL8) && close < open;
        C7=value9 == series.lowest(i, lookback, Values.VAL9) && close < open;
        C8=value10 == series.lowest(i, lookback, Values.VAL10) && close > open;
        C9=value11 == series.lowest(i, lookback, Values.VAL11) && close > open;
        C10=value12 == series.highest(i, lookback, Values.VAL12);
      }
      else {
        C11=value13 == series.lowest(i, lookback, Values.VAL13);
        C12=value14 == series.highest(i, lookback, Values.VAL14) && close > open && pclose > popen;
        C13=value15 == series.highest(i, lookback, Values.VAL15) && close > open && pclose > popen;
        C14=value16 == series.highest(i, lookback, Values.VAL16) && close < open && pclose < popen;
        C15=value17 == series.highest(i, lookback, Values.VAL17) && close < open && pclose < popen;
        C16=value18 == series.lowest(i, lookback, Values.VAL18) && close < open && pclose < popen;
        C17=value19 == series.lowest(i, lookback, Values.VAL19) && close < open && pclose < popen;
        C18=value20 == series.lowest(i, lookback, Values.VAL20) && close > open && pclose > popen;
        C19=value21 == series.lowest(i, lookback, Values.VAL21) && close > open && pclose > popen;
        C20=value22 == series.highest(i, lookback, Values.VAL22);
      }

      Color barColor=barInfo.getColor();
      if (C1 || C11) {
        barColor=lowVolColor;
      }
      if (C2 || C3 || C8 || C9 || C12 || C13 || C18 || C19) {
        barColor=climaxUpColor;
      }
      if (C4 || C5 || C6 || C7 || C14 || C15 || C16 || C17) {
        barColor=climaxDownColor;
      }
      if (C10 || C20) {
        barColor=churnColor;
      }
      if ((C10 || C20)
          && (C2 || C3 || C4 || C5 || C6 || C7 || C8 || C9 || C12 || C13 || C14 || C15 || C16 || C17 || C18 || C19)) {
        barColor=climaxChurnColor;
      }

      Double avg=series.sma(i, avgPeriod, Values.VAL3);
      if (avg != null) series.setDouble(i, Values.AVG, avg);
      series.setBarColor(i, Values.VOLUME, barColor);

      if (paintBars) series.setPriceBarColor(i, barColor);

      // Mark this as complete
      series.setComplete(i);
    }

  }
}
