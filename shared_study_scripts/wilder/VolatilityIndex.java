package com.motivewave.platform.study.wilder;

import java.awt.Color;

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
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Volatility Index (Welles Wilder) */
@StudyHeader(
    namespace="com.motivewave", 
    id="VOLATILITY_INDEX", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_VOLATILITY_INDEX", 
    desc="DESC_VOLATILITY_INDEX",
    menu="MENU_OVERLAY",
    menu2="MENU_WELLES_WILDER",
    overlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/volatility_index.htm")
public class VolatilityIndex extends Study 
{
  final static String CONSTANT = "constant";
  
  enum Values { SAR, SIC, ATR, LONG }
  enum Signals { SAR_LONG, SAR_SHORT }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    populateGeneralTab(tab);
    
    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, CONSTANT);
    desc.exportValue(new ValueDescriptor(Values.SAR, get("VAL_VIX"), new String[] {Inputs.PERIOD, CONSTANT}));
    desc.declarePath(Values.SAR, Inputs.PATH);
    desc.declareIndicator(Values.SAR, Inputs.IND);
    desc.setRangeKeys(Values.SAR);
    desc.declareSignal(Signals.SAR_LONG, get("LBL_SAR_LONG"));
    desc.declareSignal(Signals.SAR_SHORT, get("LBL_SAR_SHORT"));
  }
  
  protected void populateGeneralTab(SettingTab tab)
  {
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 7, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(CONSTANT, get("LBL_CONSTANT"), 3.0, 0.001, 99.999, 0.001));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    Color lightBlue = Util.awtColor(95, 165, 220);
    var path = new PathDescriptor(Inputs.PATH, get("LBL_SAR"), lightBlue, Enums.PointType.DOT, Enums.Size.SMALL, true, true, false);
    path.setSupportsMaxPoints(true);
    path.setSupportsColorPolicy(false);
    lines.addRow(path);
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), lightBlue, X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    var sd = getSettingsDescriptor();
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 7, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(CONSTANT, Inputs.PATH);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 7);
    if (index < period) return;
    var series = ctx.getDataSeries();
    // The VX is only relevant for completed bars
    if (!series.isBarComplete(index)) return;
    
    Double SAR = series.getDouble(index-1, Values.SAR); // Stop And Reverse
    Double ATR = series.getDouble(index-1, Values.ATR); // Average True Range
    double C = getSettings().getDouble(CONSTANT, 3.0);
    Double SIC = series.getDouble(index-1, Values.SIC); // Significant Close
    Boolean isLong = series.getBoolean(index-1, Values.LONG);
    var instr = ctx.getInstrument();
    boolean latest = index == series.size()-1;
    
    if (SAR == null) {
      // first entry point, calculate the first SAR    
      isLong = series.getClose(index) > series.getClose(index-1);
      ATR = series.atr(index, period);
      SIC = (double)series.getClose(index-1);
      if (isLong) SAR = (double)instr.round(SIC-ATR*C);
      else SAR = (double)instr.round(SIC+ATR*C);
    }
    else {
      ATR = ((period-1)*ATR + series.getTrueRange(index))/period;
    }

    double ARC = ATR*C;
    double close = series.getClose(index);
    if (isLong) {
      if (close <= SAR) {
        // Stop and Reverse
        isLong = false;
        series.setBoolean(index, Signals.SAR_LONG, true);
        if (!latest) {
          ctx.signal(index, Signals.SAR_LONG, get("SIGNAL_SAR_LONG", instr.format(close), instr.format(SAR)), close);
        }
        SIC = close;
        SAR = instr.round(SIC + ARC);
      }
      else {
        SIC = Util.max(SIC, close);
        SAR = instr.round(SIC - ARC);
      }
    }
    else {
      if (close >= SAR) {
        // Stop and Reverse
        isLong = true;
        series.setBoolean(index, Signals.SAR_SHORT, true);
        if (!latest) {
          ctx.signal(index, Signals.SAR_SHORT, get("SIGNAL_SAR_SHORT", instr.format(close), instr.format(SAR)), close);
        }
        SIC = close;
        SAR = instr.round(SIC - ARC);
      }
      else {
        SIC = Util.min(SIC, close);
        SAR = instr.round(SIC + ARC);
      }
    }
    
    series.setDouble(index, Values.SAR, SAR);
    series.setDouble(index, Values.SIC, SIC);
    series.setDouble(index, Values.ATR, ATR);
    series.setBoolean(index,  Values.LONG, isLong);
    series.setComplete(index);
  }
}
