package com.motivewave.platform.study.instrument;

import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.PriceData;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InstrumentDescriptor;
import com.motivewave.platform.sdk.common.desc.PriceBarDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Instrument Spread */
@StudyHeader(
    namespace="com.motivewave", 
    id="SPREAD", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_SPREAD",
    desc="DESC_SPREAD",
    menu="MENU_INSTRUMENT",
    overlay=false,
    multipleInstrument=true,
    requiresBarUpdates=true)
public class Spread extends com.motivewave.platform.sdk.study.Study 
{
	final static String MULTIPLIER1 = "multiplier1", MULTIPLIER2 = "multiplier2";
  final static String OPERATION = "operation", INVERT = "invert";
  final static String SUBTRACT = "SUBTRACT", DIVIDE = "DIVIDE", ADD = "ADD", MULTIPLY = "MULTIPLY";

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
    List<NVP> types = new ArrayList();
    types.add(new NVP(get("LBL_SUBTRACT"), SUBTRACT));
    types.add(new NVP(get("LBL_DIVIDE"), DIVIDE));
    types.add(new NVP(get("LBL_ADD"), ADD));
    types.add(new NVP(get("LBL_MULTIPLY"), MULTIPLY));
    inputs.addRow(new DiscreteDescriptor(OPERATION, get("LBL_OPERATION"), SUBTRACT, types));
    inputs.addRow(new BooleanDescriptor(INVERT, get("LBL_INVERT"), false));
    
    var display = tab.addGroup(get("LBL_DISPLAY"));
    display.addRow(new PriceBarDescriptor(Inputs.PRICE_BAR, get("LBL_PRICE_BAR"), null, Enums.BarInput.CLOSE, true, false));
    display.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INSTRUMENT1, MULTIPLIER1, Inputs.INSTRUMENT2, MULTIPLIER2, Inputs.PRICE_BAR, OPERATION, INVERT);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.CLOSE, get("LBL_SPREAD"), new String[] {Inputs.INSTRUMENT1, Inputs.INSTRUMENT2}));
    desc.setLabelSettings(Inputs.INSTRUMENT1, Inputs.INSTRUMENT2, OPERATION);
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
    String operation = getSettings().getString(OPERATION, SUBTRACT);
    boolean invert = getSettings().getBoolean(INVERT, false);
    if (mult1 == 0f) mult1 = 1f;
    if (mult2 == 0f) mult2 = 1f;
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

    float open=0, high=0, low=0, close=0;
    
    if (Util.compare(operation, SUBTRACT)) {
      open = open1*mult1 - open2*mult2;
      high = high1*mult1 - high2*mult2;
      low = low1*mult1 - low2*mult2;
      close = close1*mult1 - close2*mult2;
    }
    else if (Util.compare(operation, DIVIDE)) {
      open = (open1*mult1) / (open2*mult2);
      high = (high1*mult1) / (high2*mult2);
      low = (low1*mult1) / (low2*mult2);
      close = (close1*mult1) / (close2*mult2);
    }
    if (Util.compare(operation, ADD)) {
      open = open1*mult1 + open2*mult2;
      high = high1*mult1 + high2*mult2;
      low = low1*mult1 + low2*mult2;
      close = close1*mult1 + close2*mult2;
    }
    if (Util.compare(operation, MULTIPLY)) {
      open = open1*mult1 * open2*mult2;
      high = high1*mult1 * high2*mult2;
      low = low1*mult1 * low2*mult2;
      close = close1*mult1 * close2*mult2;
    }
    
    if (invert) {
      open = 1f/open;
      close = 1f/close;
      high = 1f/high;
      low = 1f/low;
    }
    
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
