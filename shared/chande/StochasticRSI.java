package com.motivewave.platform.study.chande;

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
import com.motivewave.platform.sdk.study.StudyHeader;

/** Stochastic RSI */
@StudyHeader(
    namespace="com.motivewave", 
    id="STO_RSI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="NAME_STOCHASTIC_RSI",
    tabName="TAB_STO_RSI",
    desc="DESC_STOCH_RSI",
    menu="MENU_TUSCHARD_CHANDE",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/stochastic_rsi.htm")
public class StochasticRSI extends com.motivewave.platform.sdk.study.Study 
{
  enum Values { RSI, UP,DOWN, NUM, DEN, FASTK, SLOWK, SIG }
  enum Signals { BUY, SELL}

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("RSI_PERIOD"), 14, 1, 9999, 1), 
                  new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("FAST_K_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD3, get("SLOW_K_PERIOD"), 5, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD4, get("SIGNAL_PERIOD"), 3, 1, 9999, 1),
                  new MAMethodDescriptor(Inputs.METHOD2, get("LBL_METHOD"), Enums.MAMethod.SMA));
    
    var settings = tab.addGroup(get("LBL_PATHS"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("STOCHASTIC_K"),  defaults.getLineColor(), 1.0f, null, true, false, true));
    settings.addRow(new PathDescriptor(Inputs.PATH2, get("SIGNAL"),  defaults.getRed(), 1.0f, null, true, false, true));
    
    settings = tab.addGroup(get("LBL_INDICATORS"));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("STOCHASTIC_K"), defaults.getLineColor(), null, false, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND2, get("SIGNAL"), defaults.getRed(), null, false, true, true));
    
    tab = sd.addTab(get("TAB_DISPLAY"));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    var topDesc = new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 80, 0, 999.1, .1, true);
    topDesc.setLineColor(defaults.getRed());
    guides.addRow(topDesc); 
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 0, 999.1, .1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    var bottomDesc = new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 20, 0, 999.1, .1, true);
    bottomDesc.setLineColor(defaults.getGreen());
    guides.addRow(bottomDesc); 
 
    settings = tab.addGroup(get("LBL_SHADING"));
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH,
        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH,
        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
         Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
         Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("RSI_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("FAST_K_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD3, get("SLOW_K_PERIOD"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD4, get("SIGNAL_PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.METHOD2);
    sd.addQuickSettings(Inputs.PATH, Inputs.PATH2, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.METHOD, Inputs.PERIOD2, Inputs.PERIOD3, Inputs.PERIOD4,Inputs.METHOD2);
    desc.exportValue(new ValueDescriptor(Values.SLOWK, get("STOCHASTIC_K"), new String[] {Inputs.INPUT, Inputs.PERIOD, Inputs.METHOD, Inputs.PERIOD2, Inputs.PERIOD3}));
    desc.exportValue(new ValueDescriptor(Values.SIG, get("SIGNAL"), new String[] {Inputs.METHOD2, Inputs.PERIOD4}));
    desc.declarePath(Values.SLOWK, Inputs.PATH);
    desc.declarePath(Values.SIG, Inputs.PATH2);
    desc.declareIndicator(Values.SLOWK, Inputs.IND);
    desc.declareIndicator(Values.SIG, Inputs.IND2);

    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));
    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));
    desc.declareSignal(Signals.SELL, get("SELL"));
    desc.declareSignal(Signals.BUY, get("BUY"));    

    desc.setRangeKeys(Values.SLOWK);
  }
  @Override
  public void onLoad(Defaults defaults)
  {
    int p1 = getSettings().getInteger(Inputs.PERIOD);
    int p2 = getSettings().getInteger(Inputs.PERIOD2);
    int p3 = getSettings().getInteger(Inputs.PERIOD3);
    int p4 = getSettings().getInteger(Inputs.PERIOD4);

    setMinBars(p1 + p2 + p3 + p4 + 1);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    var series = ctx.getDataSeries();

    int rsiP = getSettings().getInteger(Inputs.PERIOD);
    int fastKP = getSettings().getInteger(Inputs.PERIOD2);
    int slowKP = getSettings().getInteger(Inputs.PERIOD3);
    int sigP = getSettings().getInteger(Inputs.PERIOD4);

    Object input = getSettings().getInput(Inputs.INPUT);
    var rsiMethod = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.SMMA);
    var sigMethod = getSettings().getMAMethod(Inputs.METHOD2, Enums.MAMethod.SMA);

    double price = series.getDouble(index, input);
    double prevPrice = series.getDouble(index-1, input);
    double diff = price - prevPrice;
    double up = 0, down = 0;
    if (diff > 0) up = diff;
    else down = diff;
    down = Math.abs(down);
    series.setDouble(index, Values.UP, up);
    series.setDouble(index, Values.DOWN, down);
    if (index < (rsiP + 1)) return;

    //RSI calculation
    Double avUp = series.ma(rsiMethod, index,  rsiP, Values.UP);
    if (avUp == null) return;
    Double avDn = series.ma(rsiMethod, index,  rsiP, Values.DOWN);
    if (avDn == null) return;
    double dSum = avUp + avDn;
    double rsi = (avUp / dSum) * 100.0;
    series.setDouble(index, Values.RSI, rsi);
    if (index < (rsiP + fastKP + 1)) return;

    // Stochastics calculation
    Double high = series.highest(index, fastKP, Values.RSI);
    Double low = series.lowest(index, fastKP, Values.RSI);
    if (high == null || low == null) return;
    
    double fastK = 0.0;
    if (high == low) fastK = 100;
    else fastK = (rsi-low) / (high-low) * 100.0;
    series.setDouble(index, Values.FASTK, fastK);
    if (index < (rsiP + fastKP + slowKP + 1)) return;
    
    Double slowk = series.ma(sigMethod, index, slowKP, Values.FASTK);
    if (slowk == null) return;

    series.setDouble(index, Values.SLOWK, slowk);
    if (index < (rsiP + fastKP + slowKP + sigP + 1)) return;

    Double sig = series.ma(sigMethod, index,  sigP, Values.SLOWK);
    if (sig == null) return;
    series.setDouble(index, Values.SIG, sig);

     // Check for signal events
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    double topG = topGuide.getValue();
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    double bottG = bottomGuide.getValue();

    boolean sell = crossedBelow(series, index, Values.SLOWK, Values.SIG) && slowk > topG;
    boolean buy = crossedAbove(series, index, Values.SLOWK, Values.SIG) && slowk < bottG;
    
    boolean wasBuy = index == series.size()-1 && series.getBoolean(index, Signals.BUY, false);
    boolean wasSell = index == series.size()-1 && series.getBoolean(index, Signals.SELL, false);
    
    series.setBoolean(index, Signals.BUY, buy);
    series.setBoolean(index, Signals.SELL, sell);
    
    if (sell) {
      var c = new Coordinate(series.getStartTime(index), slowk);
      var marker = getSettings().getMarker(Inputs.DOWN_MARKER); 
      String msg = get("SELL_PRICE_K", Util.round(price, 2), Util.round(slowk, 3));
      if (!wasSell && marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.SELL, msg, price);
    }
    if (buy) {
      var c = new Coordinate(series.getStartTime(index), slowk);
      var marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("BUY_PRICE_K", Util.round(price, 2), Util.round(slowk, 3));
      if (!wasBuy && marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.BUY, msg, price);
    }
    series.setComplete(index);
  }
}
