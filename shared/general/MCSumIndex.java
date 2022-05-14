package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.InstrumentDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Cumulative summation of the McClelland Oscillator value.  This indicator is derived from Net Advances.
    This study requires access to Advances and Declines instruments. */
@StudyHeader(
    namespace="com.motivewave", 
    id="MC_SUM_IND", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MC_SUM_IND",
    label="NAME_MC_SUM_IND",
    desc="DESC_MC_SUM_IND",
    menu="MENU_GENERAL",
    overlay=false,
    multipleInstrument=true)
public class MCSumIndex extends com.motivewave.platform.sdk.study.Study 
{
  final static String ADV="adv", DEC="dec", RATIO_ADJ="radj";
  enum Values { NET, FAST, SLOW, SUM, MC_OSC }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new InstrumentDescriptor(ADV, get("LBL_MC_ADVANCING")));
    grp.addRow(new InstrumentDescriptor(DEC, get("LBL_MC_DECLINING")));
    grp.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    grp.addRow(new BooleanDescriptor(RATIO_ADJ, get("LBL_MC_RATIO_ADJUSTED"), true));
    grp.addRow(new IntegerDescriptor(Inputs.FAST_PERIOD, get("LBL_MC_FAST_PERIOD"), 19, 1, 999, 1));
    grp.addRow(new IntegerDescriptor(Inputs.SLOW_PERIOD, get("LBL_MC_SLOW_PERIOD"), 39, 1, 999, 1));

    grp = tab.addGroup(get("LBL_DISPLAY"));
    grp.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), null, 1f, null));
    grp.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(ADV, DEC, Inputs.INPUT, RATIO_ADJ);
    sd.addQuickSettings(new SliderDescriptor(Inputs.FAST_PERIOD, get("LBL_MC_FAST_PERIOD"), 19, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SLOW_PERIOD, get("LBL_MC_SLOW_PERIOD"), 39, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(ADV, DEC);
    desc.exportValue(new ValueDescriptor(Values.MC_OSC, get("LBL_MC_OSC"), new String[] {Inputs.FAST_PERIOD, Inputs.SLOW_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SUM, get("LBL_MC_SUM"), null));
    desc.declarePath(Values.SUM, Inputs.PATH);
    desc.declareIndicator(Values.SUM, Inputs.IND);
    desc.setRangeKeys(Values.SUM);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
    desc.setMinTick(1d);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var adv = getSettings().getInstrument(ADV);
    var dec = getSettings().getInstrument(DEC);
    var series = ctx.getDataSeries();
    if (series == null || adv == null || dec == null) return;
    
    var input = (Enums.BarInput)getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
    var a = series.getDouble(index, input, adv);
    var d = series.getDouble(index, input, dec);
    if (a == null || d == null) return;

    var net = adv.round(a - d);
    if (getSettings().getBoolean(RATIO_ADJ, true)) net = 1000*net/(a + d);
    series.setDouble(index, Values.NET, net);

    int fp = getSettings().getInteger(Inputs.FAST_PERIOD, 19);
    if (index > fp) series.setDouble(index, Values.FAST, series.ema(index, fp, Values.NET));
    int sp = getSettings().getInteger(Inputs.SLOW_PERIOD, 39);
    if (index > fp) series.setDouble(index, Values.SLOW, series.ema(index, sp, Values.NET));
    
    var f = series.getDouble(index, Values.FAST);
    var s = series.getDouble(index, Values.SLOW);
    if (f == null || s == null) return;
    var osc = f - s;
    series.setDouble(index, Values.MC_OSC, osc);
    var p = series.getDouble(index-1, Values.SUM, 0);
    series.setDouble(index, Values.SUM, p + osc);
    
    series.setComplete(index, index < series.size()-1);
  }
}
