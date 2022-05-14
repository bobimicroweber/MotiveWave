package com.motivewave.platform.study.bar_pattern;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Stochastic BarFigure Pattern */
@StudyHeader(
    namespace="com.motivewave", 
    id="STO_BARS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_STO_BARS",
    desc="DESC_STO_BARS",
    menu="MENU_BAR_PATTERNS",
    overlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/stochastic_bars.htm")
public class StochasticBarPattern extends com.motivewave.platform.sdk.study.Study 
{
  final static String K_PERIOD = "kPeriod", K_FULL_PERIOD = "kFullPeriod", D_FULL_PERIOD = "dFullPeriod";
  final static String STO_TOP = "STOTop", STO_BOTTOM = "STOBottom", USE_D_PERIOD = "useDPeriod";

  enum Values { K, P_K, P_D }
  enum Signals { TOP_BAR, BOTTOM_BAR, NEUTRAL_BAR }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(K_PERIOD, get("LBL_K_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(K_FULL_PERIOD, get("LBL_K_FULL_PERIOD"), 3, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(D_FULL_PERIOD, get("LBL_D_FULL_PERIOD"), 3, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(STO_TOP, get("LBL_STO_TOP"), 80, 1, 99, 0.1));
    inputs.addRow(new DoubleDescriptor(STO_BOTTOM, get("LBL_STO_BOTTOM"), 20, 1, 99, 0.1));
    inputs.addRow(new BooleanDescriptor(USE_D_PERIOD, get("LBL_USE_D_PERIOD"), false));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new ColorDescriptor(Inputs.TOP_COLOR, get("LBL_TOP_COLOR"), defaults.getGreen(), true, true));
    colors.addRow(new ColorDescriptor(Inputs.NEUTRAL_COLOR, get("LBL_NEUTRAL_COLOR"), defaults.getBlue(), true, true));
    colors.addRow(new ColorDescriptor(Inputs.BOTTOM_COLOR, get("LBL_BOTTOM_COLOR"), defaults.getRed(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(K_PERIOD, get("LBL_K_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(K_FULL_PERIOD, get("LBL_K_FULL_PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(D_FULL_PERIOD, get("LBL_D_FULL_PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(STO_TOP, STO_BOTTOM, Inputs.TOP_COLOR, Inputs.NEUTRAL_COLOR, Inputs.BOTTOM_COLOR);

    var desc = createRD();
    desc.setLabelSettings(K_PERIOD, K_FULL_PERIOD, D_FULL_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.P_K, get("LBL_K_FULL"), new String[] {K_PERIOD, K_FULL_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.P_D, get("LBL_D_FULL"), new String[] {K_PERIOD, K_FULL_PERIOD, D_FULL_PERIOD}));
    desc.declareSignal(Signals.TOP_BAR, get("LBL_STO_TOP_BAR"));
    desc.declareSignal(Signals.BOTTOM_BAR, get("LBL_STO_BOTTOM_BAR"));
    desc.declareSignal(Signals.NEUTRAL_BAR, get("LBL_STO_NEUTRAL_BAR"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int kPeriod = getSettings().getInteger(K_PERIOD);
    int slowPeriod = getSettings().getInteger(K_FULL_PERIOD);
    int fastPeriod = getSettings().getInteger(D_FULL_PERIOD);
    if (index < kPeriod) return;

    var method = getSettings().getMAMethod(Inputs.METHOD);
    var series = ctx.getDataSeries();
    boolean complete = series.isBarComplete(index);

    // Calculate K value
    double high = series.highest(index, kPeriod, Enums.BarInput.HIGH);
    double low = series.lowest(index, kPeriod, Enums.BarInput.LOW);
    double denom = high - low;
    double K = 0;
    if (denom > 0) K = 100.0 * ( (series.getClose(index) - low) / denom );
    if (K > 100) {
      error("StochasticBarPattern::calculateBaseValues() K > 100: " + K);
      K = 100;
    }

    series.setDouble(index, Values.K, K);
    
    if (index < kPeriod + slowPeriod) return;
    
    // Calculate the Slow MA
    Double slowK = series.ma(method,  index, slowPeriod, Values.K);
    series.setDouble(index, Values.P_K, slowK);
    if (slowK == null) return;

    if (index < kPeriod + slowPeriod + fastPeriod) return;

    Double fastK = series.ma(method,  index, fastPeriod, Values.P_K);
    series.setDouble(index, Values.P_D, fastK);
    if (fastK == null) return;

    double top = getSettings().getDouble(STO_TOP);
    double bottom = getSettings().getDouble(STO_BOTTOM);
    Color topColor = getSettings().getColor(Inputs.TOP_COLOR);
    Color bottomColor = getSettings().getColor(Inputs.BOTTOM_COLOR);
    Color neutralColor = getSettings().getColor(Inputs.NEUTRAL_COLOR);
    Instrument instr = ctx.getInstrument();
    Color prev = series.getPriceBarColor(index-1);
    double val = slowK;
    if (getSettings().getBoolean(USE_D_PERIOD)) val = fastK;
    String valStr = Util.round(val, 0.01) + "";
    String lastPrice = instr.format(series.getClose(index));
    
    if (val >= top) {
      series.setPriceBarColor(index, topColor);
      if (complete && !Util.compare(prev, topColor)) {
        ctx.signal(index, Signals.TOP_BAR, get("SIGNAL_STO_TOP_BAR", valStr, lastPrice), valStr);
      }
    }
    else if (val <= bottom) {
      series.setPriceBarColor(index, bottomColor);
      if (complete && !Util.compare(prev, bottomColor)) {
        ctx.signal(index, Signals.BOTTOM_BAR, get("SIGNAL_STO_BOTTOM_BAR", valStr, lastPrice), valStr);
      }
    }
    else {
      series.setPriceBarColor(index, neutralColor);
      if (complete && !Util.compare(prev, neutralColor)) {
        ctx.signal(index, Signals.NEUTRAL_BAR, get("SIGNAL_STO_NEUTRAL_BAR", valStr, lastPrice), valStr);
      }
    }

    series.setComplete(index, complete);
  }
}
