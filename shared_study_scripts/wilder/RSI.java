package com.motivewave.platform.study.wilder;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Relative Strength Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="RSI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_RSI",
    label="LBL_RSI",
    desc="DESC_RSI",
    menu="MENU_WELLES_WILDER",
    menu2="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/rsi.htm")
public class RSI extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { RSI, UP, DOWN }
	enum Signals { RSI_TOP, RSI_BOTTOM }
  
	final static String RSI_LINE = "rsiLine";
  final static String RSI_IND = "rsiInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 2, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(RSI_LINE, get("LBL_RSI_LINE"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, RSI_LINE, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, RSI_LINE, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(RSI_IND, get("LBL_RSI_IND"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 70, 1, 100, 1, true));
    var gd = new GuideDescriptor(Inputs.TOP_GUIDE2, get("LBL_TOP_GUIDE2"), 60, 1, 100, 1, true);
    gd.setEnabled(false);
    guides.addRow(gd);
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    gd = new GuideDescriptor(Inputs.BOTTOM_GUIDE2, get("LBL_BOTTOM_GUIDE2"), 40, 1, 100, 1, true);
    gd.setEnabled(false);
    guides.addRow(gd);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 30, 1, 100, 1, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(RSI_LINE, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);
   
    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.RSI, get("LBL_RSI"), new String[] {Inputs.INPUT, Inputs.PERIOD}));
    desc.declarePath(Values.RSI, RSI_LINE);
    desc.declareIndicator(Values.RSI, RSI_IND);
    desc.setMaxBottomValue(15);
    desc.setMinTopValue(85);
    desc.setRangeKeys(Values.RSI);
    desc.declareSignal(Signals.RSI_TOP, get("RSI_TOP"));
    desc.declareSignal(Signals.RSI_BOTTOM, get("RSI_BOTTOM"));
    desc.setMinTick(0.1);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return; // not enough data
    var series = ctx.getDataSeries();
    Object input = getSettings().getInput(Inputs.INPUT);
    Double v1 = series.getDouble(index, input);
    Double v2 = series.getDouble(index-1, input);
    if (v1 == null || v2 == null) return;
    
    double diff =  v1 - v2;
    double up = 0, down = 0;
    if (diff > 0) up = diff;
    else down = diff;
    
    series.setDouble(index, Values.UP, up);
    series.setDouble(index, Values.DOWN, Math.abs(down));
    
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index <= period +1) return;
    
    var method = getSettings().getMAMethod(Inputs.METHOD);
    Double avgUp = series.ma(method, index,  period, Values.UP);
    Double avgDown = series.ma(method, index,  period, Values.DOWN);
    if (avgUp == null || avgDown == null || avgDown == 0) return;
    double RS = avgUp / avgDown;
    double RSI = 100.0 - ( 100.0 / (1.0 + RS));

    series.setDouble(index, Values.RSI, RSI);
    
    // Do we need to generate a signal?
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    if (crossedAbove(series, index, Values.RSI, topGuide.getValue())) {
      series.setBoolean(index, Signals.RSI_TOP, true);
      ctx.signal(index, Signals.RSI_TOP, get("SIGNAL_RSI_TOP", topGuide.getValue(), round(RSI)), round(RSI));
    }
    else if (crossedBelow(series, index, Values.RSI, bottomGuide.getValue())) {
      series.setBoolean(index, Signals.RSI_BOTTOM, true);
      ctx.signal(index, Signals.RSI_BOTTOM, get("SIGNAL_RSI_BOTTOM", bottomGuide.getValue(), round(RSI)), round(RSI));
    }
    
    series.setComplete(index, series.isBarComplete(index));
  }  
 
}
