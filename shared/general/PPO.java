package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Percentage Price Oscillator (PPO) */
@StudyHeader(
    namespace="com.motivewave", 
    id="PPO", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_PPO",
    label="LBL_PPO",
    desc="DESC_PPO",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/percentage_price_oscillator.htm")
public class PPO extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { VAL };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 30, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    var ppo = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false);
    ppo.setSupportsShowAsBars(true);
    settings.addRow(ppo);
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 30, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.VAL, get("LBL_PPO"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2}));
    desc.declarePath(Values.VAL, Inputs.PATH);
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.setRangeKeys(Values.VAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 10);
    int period2 = getSettings().getInteger(Inputs.PERIOD2, 30);
    if (index < Util.maxInt(period, period2)) return;
    var series = ctx.getDataSeries();
    var method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.SMA);
    Object input = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    
    Double MA1 = series.ma(method, index, period, input);
    Double MA2 = series.ma(method, index, period2, input);
    if (MA1 == null || MA2 == null) return;

    double diff = 100.0 * (MA1-MA2)/MA2;
    series.setDouble(index,  Values.VAL, diff);
    series.setComplete(index);
  }  
}
