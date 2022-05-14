package com.motivewave.platform.study.bar_pattern;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Relative Strength Index BarFigure Pattern */
@StudyHeader(
    namespace="com.motivewave", 
    id="RSI_BARS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_RSI_BARS",
    desc="DESC_RSI_BARS",
    menu="MENU_WELLES_WILDER",
    menu2="MENU_BAR_PATTERNS",
    overlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/rsi_bars.htm")
public class RSIBarPattern extends com.motivewave.platform.sdk.study.Study 
{
  final static String RSI_TOP = "RSITop", RSI_BOTTOM = "RSIBottom";

  enum Values { RSI, UP, DOWN }
  enum Signals { TOP_BAR, BOTTOM_BAR, NEUTRAL_BAR }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new DoubleDescriptor(RSI_TOP, get("LBL_RSI_TOP"), 70, 1, 99, 0.1));
    inputs.addRow(new DoubleDescriptor(RSI_BOTTOM, get("LBL_RSI_BOTTOM"), 30, 1, 99, 0.1));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new ColorDescriptor(Inputs.TOP_COLOR, get("LBL_TOP_COLOR"), defaults.getGreen(), true, true));
    colors.addRow(new ColorDescriptor(Inputs.NEUTRAL_COLOR, get("LBL_NEUTRAL_COLOR"), defaults.getBlue(), true, true));
    colors.addRow(new ColorDescriptor(Inputs.BOTTOM_COLOR, get("LBL_BOTTOM_COLOR"), defaults.getRed(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(RSI_TOP, RSI_BOTTOM, Inputs.TOP_COLOR, Inputs.NEUTRAL_COLOR, Inputs.BOTTOM_COLOR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, RSI_TOP, RSI_BOTTOM);
    desc.exportValue(new ValueDescriptor(Values.RSI, get("LBL_RSI"), new String[] {Inputs.INPUT, Inputs.PERIOD}));
    desc.declareSignal(Signals.TOP_BAR, get("LBL_RSI_TOP_BAR"));
    desc.declareSignal(Signals.BOTTOM_BAR, get("LBL_RSI_BOTTOM_BAR"));
    desc.declareSignal(Signals.NEUTRAL_BAR, get("LBL_RSI_NEUTRAL_BAR"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var s = getSettings();
    int period = s.getInteger(Inputs.PERIOD);
    if (index < 1) return; // not enough data
    var series = ctx.getDataSeries();
    Object input = s.getInput(Inputs.INPUT);
    
    double diff = series.getDouble(index, input) - series.getDouble(index-1, input);
    double up = 0, down = 0;
    if (diff > 0) up = diff;
    else down = diff;
    
    series.setDouble(index, Values.UP, up);
    series.setDouble(index, Values.DOWN, Math.abs(down));
    
    if (index <= period +1) return;
    
    var method = s.getMAMethod(Inputs.METHOD);
    Double avgUp = series.ma(method, index,  period, Values.UP);
    Double avgDown = series.ma(method, index,  period, Values.DOWN);
    if (avgUp == null || avgDown == null) return;
    double RS = avgUp / avgDown;
    double RSI = 100.0 - ( 100.0 / (1.0 + RS));
    boolean complete = series.isBarComplete(index);
    var instr = ctx.getInstrument();
    
    series.setDouble(index, Values.RSI, RSI);
    
    double top = s.getDouble(RSI_TOP);
    double bottom = s.getDouble(RSI_BOTTOM);
    
    var topColor = s.getColorInfo(Inputs.TOP_COLOR);
    var bottomColor = s.getColorInfo(Inputs.BOTTOM_COLOR);
    var neutralColor = s.getColorInfo(Inputs.NEUTRAL_COLOR);
    
    // Only fire the signal once per bar...
    Color prev = series.getPriceBarColor(index-1);
    String rsiStr = Util.formatDouble(RSI, 2);
    String lastPrice = instr.format(series.getClose(index));

    if (RSI >= top) {
      series.setPriceBarColor(index, topColor.isEnabled() ? topColor.getColor() : null);
      if (complete && !Util.compare(prev, topColor.getColor())) {
        ctx.signal(index, Signals.TOP_BAR, get("SIGNAL_RSI_TOP_BAR", rsiStr, lastPrice), rsiStr);
      }
    }
    else if (RSI <= bottom) {
      series.setPriceBarColor(index, bottomColor.isEnabled() ? bottomColor.getColor() : null);
      if (complete && !Util.compare(prev, bottomColor.getColor())) {
        ctx.signal(index, Signals.BOTTOM_BAR, get("SIGNAL_RSI_BOTTOM_BAR", rsiStr, lastPrice), rsiStr);
      }
    }
    else {
      series.setPriceBarColor(index, neutralColor.isEnabled() ? neutralColor.getColor() : null);
      if (complete && !Util.compare(prev, neutralColor.getColor())) {
        ctx.signal(index, Signals.NEUTRAL_BAR, get("SIGNAL_RSI_NEUTRAL_BAR", rsiStr, lastPrice), rsiStr);
      }
    }
    
    series.setComplete(index, complete);
  }  
}