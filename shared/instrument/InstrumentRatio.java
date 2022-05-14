package com.motivewave.platform.study.instrument;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.PriceData;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InstrumentDescriptor;
import com.motivewave.platform.sdk.common.desc.PriceBarDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Instrument Ratio */
@StudyHeader(
    namespace="com.motivewave", 
    id="INSTRUMENT_RATIO", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_INSTRUMENT_RATIO",
    desc="DESC_INSTRUMENT_RATIO",
    menu="MENU_INSTRUMENT",
    overlay=false,
    multipleInstrument=true,
    requiresBarUpdates=true)
public class InstrumentRatio extends com.motivewave.platform.sdk.study.Study 
{
  final static String MULTIPLIER1 = "multiplier1", MULTIPLIER2 = "multiplier2";

  enum Values { PRICE_BAR, OPEN, HIGH, LOW, CLOSE }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InstrumentDescriptor(Inputs.INSTRUMENT1, get("LBL_INSTRUMENT1"), true, true));
    inputs.addRow(new DoubleDescriptor(MULTIPLIER1, get("LBL_MULTIPLIER"), 1.0, 0.01, 10000, 0.01));
    inputs.addRow(new InstrumentDescriptor(Inputs.INSTRUMENT2, get("LBL_INSTRUMENT2")));
    inputs.addRow(new DoubleDescriptor(MULTIPLIER2, get("LBL_MULTIPLIER"), 1.0, 0.01, 10000, 0.01));

    var display = tab.addGroup(get("LBL_DISPLAY"));
    display.addRow(new PriceBarDescriptor(Inputs.PRICE_BAR, get("LBL_PRICE_BAR"), null, Enums.BarInput.CLOSE, true, false));
    display.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INSTRUMENT1, MULTIPLIER1, Inputs.INSTRUMENT2, MULTIPLIER2, Inputs.PRICE_BAR);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.CLOSE, get("LBL_RATIO"), new String[] {Inputs.INSTRUMENT1, Inputs.INSTRUMENT2}));
    desc.setLabelSettings(Inputs.INSTRUMENT1, Inputs.INSTRUMENT2);
    desc.declarePriceBar(Values.PRICE_BAR, Inputs.PRICE_BAR);
    desc.declareIndicator(Values.CLOSE, Inputs.IND);
    desc.setRangeKeys(Values.HIGH, Values.LOW);
    desc.getDefaultPlot().setFormatMK(false);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var instr1 = getSettings().getInstrument(Inputs.INSTRUMENT1);
    var instr2 = getSettings().getInstrument(Inputs.INSTRUMENT2);
    float mult1 = (float)getSettings().getDouble(MULTIPLIER1, 1.0);
    float mult2 = (float)getSettings().getDouble(MULTIPLIER2, 1.0);
    var series = ctx.getDataSeries();
    if (series == null) return;

    float open1 = series.getOpen(index, instr1);
    if (open1 == 0f) return;
    float open2 = series.getOpen(index, instr2);
    if (open2 == 0f) return;

    float high1 = series.getHigh(index, instr1);
    if (high1 == 0f) return;
    float high2 = series.getHigh(index, instr2);
    if (high2 == 0f) return;

    float low1 = series.getLow(index, instr1);
    if (low1 == 0f) return;
    float low2 = series.getLow(index, instr2);
    if (low2 == 0f) return;

    float close1 = series.getClose(index, instr1);
    if (close1 == 0f) return;
    float close2 = series.getClose(index, instr2);
    if (close2 == 0f) return;

    float open = (open1*mult1) / (open2*mult2);
    float high = (high1*mult1) / (high2*mult2);
    float low = (low1*mult1) / (low2*mult2);
    float close = (close1*mult1) / (close2*mult2);
    
    float max = Util.maxFloat(open, high, low, close);
    float min = Util.minFloat(open, high, low, close);
    
    series.setFloat(index, Values.OPEN, open);
    series.setFloat(index, Values.CLOSE, close);
    series.setFloat(index, Values.HIGH, max);
    series.setFloat(index, Values.LOW, min);
    series.setValue(index, Values.PRICE_BAR, new PriceData(open, max, min, close));
  }
  
  @Override
  public void onBarUpdate(DataContext ctx)
  {
    // This can be a bit quirky when dealing with delayed data
    // To work around, we need to find the last index where there is a valid bar
    var instr = getSettings().getInstrument(Inputs.INSTRUMENT1);
    var series = ctx.getDataSeries();
    int index1 = series.size();
    float close = 0f;
    do {
      index1--;
      close = series.getClose(index1, instr);
    } 
    while (index1 > 0 && close == 0f);

    instr = getSettings().getInstrument(Inputs.INSTRUMENT2);
    int index2 = series.size();
    close = 0f;
    do {
      index2--;
      close = series.getClose(index2, instr);
    } 
    while (index1 > 0 && close == 0f);

    if (index1 <= 0 || index2 <= 0) return;
    calculate(Util.minInt(index1, index2), ctx);
  }
}
