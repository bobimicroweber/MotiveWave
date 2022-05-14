package com.motivewave.platform.study.instrument;

import com.motivewave.platform.databean.MWInstrument;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.PriceData;
import com.motivewave.platform.sdk.common.TickOperation;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InstrumentDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PriceBarDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Instrument Study */
@StudyHeader(
    namespace="com.motivewave", 
    id="INSTRUMENT_GRAPH", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_INSTRUMENT_GRAPH",
    desc="DESC_INSTRUMENT_GRAPH",
    menu="MENU_INSTRUMENT",
    overlay=false,
    multipleInstrument=true,
    supportsBarUpdates=false)
public class InstrumentGraph extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { PRICE_BAR, OPEN, HIGH, LOW, CLOSE, VOLUME, OPEN_INTEREST }
	final static String BOTTOM = "bottom", TOP="top", RANGE_ENABLED="rangeEnabled";
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InstrumentDescriptor(Inputs.INSTRUMENT, get("LBL_INSTRUMENT")));
    // Note: this should probably be a double, but the SDK only accepts integer values for the fixed top/bottom values
    inputs.addRow(new IntegerDescriptor(BOTTOM, get("LBL_BOTTOM"), 0, -9999999, 9999999,1),
        new IntegerDescriptor(TOP, get("LBL_TOP"), 100, -9999999, 9999999, 1),
        new BooleanDescriptor(RANGE_ENABLED, get("LBL_ENABLED"),  false, false));

    var display = tab.addGroup(get("LBL_DISPLAY"));
    display.addRow(new PriceBarDescriptor(Inputs.PRICE_BAR, get("LBL_PRICE_BAR"), null, Enums.BarInput.CLOSE, true, false));
    display.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    
    sd.addDependency(new EnabledDependency(RANGE_ENABLED, TOP, BOTTOM));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INSTRUMENT, Inputs.PRICE_BAR, Inputs.IND);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.OPEN, get("LBL_OPEN"), new String[] {Inputs.INSTRUMENT}));
    desc.exportValue(new ValueDescriptor(Values.HIGH, get("LBL_HIGH"), new String[] {Inputs.INSTRUMENT}));
    desc.exportValue(new ValueDescriptor(Values.LOW, get("LBL_LOW"), new String[] {Inputs.INSTRUMENT}));
    desc.exportValue(new ValueDescriptor(Values.CLOSE, get("LBL_CLOSE"), new String[] {Inputs.INSTRUMENT}));
    desc.exportValue(new ValueDescriptor(Values.VOLUME, get("LBL_VOLUME"), new String[] {Inputs.INSTRUMENT}));
    desc.exportValue(new ValueDescriptor(Values.OPEN_INTEREST, get("LBL_OPEN_INTEREST"), new String[] {Inputs.INSTRUMENT}));
    desc.declarePriceBar(Values.PRICE_BAR, Inputs.PRICE_BAR);
    desc.declareIndicator(Values.CLOSE, Inputs.IND);
    desc.setRangeKeys(Values.HIGH, Values.LOW);
    desc.getDefaultPlot().setFormatMK(false);
    desc.setLabelSettings(Inputs.INSTRUMENT);
  }

  @Override
  public void destroy()
  {
    super.destroy();
    if (instrument != null) instrument.removeListener(listener);
  }

  @Override
  public void onLoad(Defaults defaults)
  {
    updateDesc(getDataContext());
  }
  
  @Override
  public void onSettingsUpdated(DataContext ctx)
  {
    updateDesc(ctx);
    super.onSettingsUpdated(ctx);
  }
  
  private void updateDesc(DataContext ctx)
  {
    var s = getSettings();
    var instr = s.getInstrument(Inputs.INSTRUMENT);
    // This can be null if the instrument is the same as the graph
    if (instr == null) instr = ctx.getInstrument();
    if (instrument != null && instrument != instr) instrument.removeListener(listener);
    instrument = instr;
    if (instr == null) return;
    var desc = getRuntimeDescriptor();
    desc.setMinTick(instr.getTickSize());
    if (s.getBoolean(RANGE_ENABLED, false)) {
      desc.setFixedBottomValue(s.getInteger(BOTTOM, 0)); 
      desc.setFixedTopValue(s.getInteger(TOP, 100)); 
      desc.setRangeKeys();
    }
    else {
      desc.setRangeKeys(Values.HIGH, Values.LOW);
      desc.setFixedBottomValue(null); 
      desc.setFixedTopValue(null); 
    }
    instr.addListener(listener);
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    var instr = getSettings().getInstrument(Inputs.INSTRUMENT);
    if (!series.hasData(instr)) return; // data not available yet...
    dataCtx = ctx;
    for(int i = 0; i < series.size(); i++) {
      if (series.isComplete(i)) continue;
      float close = series.getClose(i, instr);
      float high = series.getHigh(i, instr);
      float low = series.getLow(i, instr);
      if (close == 0f && high == 0f && low == 0f) {
        continue; // There may not be a matching bar at this index
      }

      series.setValue(i, Values.PRICE_BAR, new PriceData(series.getOpen(i, instr), series.getHigh(i, instr), series.getLow(i, instr), series.getClose(i, instr)));
      // Add the values for export
      series.setFloat(i, Values.OPEN, series.getFloat(i, Enums.BarInput.OPEN, instr));
      series.setFloat(i, Values.HIGH, high);
      series.setFloat(i, Values.LOW, low);
      series.setFloat(i, Values.CLOSE, close);
      series.setDouble(i, Values.VOLUME, series.getDouble(i, Enums.BarInput.VOLUME, instr));
      series.setDouble(i, Values.OPEN_INTEREST, series.getDouble(i, Enums.BarInput.OPEN_INTEREST, instr));
      series.setComplete(i);
    }
  }

  @Override
  public String getLabel()
  {
    var instr = getSettings().getInstrument(Inputs.INSTRUMENT);
    if (instr == null) return super.getLabel();
    var info = getSettings().getPriceBar(Inputs.PRICE_BAR);
    String symbol = instr.getSymbol();
    if (MWInstrument.hasMultiple(symbol)) symbol += ":" + instr.getExchangeSymbol();
    if (info == null || info.getType() == null) return symbol;
    if (info.getType().requiresInput()) return symbol + "(" + info.getBarInput() + ")";
    return symbol;
  }
  
  private DataContext dataCtx;
  private Instrument instrument;
  private TickOperation listener = tick -> {
    if (getSettings() == null) return;
    if (dataCtx == null) return;
    var series = dataCtx.getDataSeries();
    if (series == null) return;
    int ind = series.size()-1;
    var bar = (PriceData)series.getValue(ind, Values.PRICE_BAR);
    if (bar == null) {
      // Maybe this instrument has delayed data, look back a few bars...
      for(int i=ind; i > series.size()-60 && i >= 0; i--) {
        bar = (PriceData)series.getValue(i, Values.PRICE_BAR);
        if (bar != null) {
          ind = i;
          break;
        }
      }
      if (bar == null) {
        //System.err.println("unable to find latest price bar!");
        return;
      }
    }
    float high = bar.getHigh(), low = bar.getLow();
    float close = tick.getPrice();
    boolean rangeUpdate = false;
    if (close > high) { high = close; rangeUpdate = true; }
    if (close < low) { low = close; rangeUpdate = true; }
    
    series.setValue(ind, Values.PRICE_BAR, new PriceData(bar.getOpen(), high, low, close));
    
    // Add the values for export
    series.setFloat(ind, Values.OPEN, bar.getOpen());
    series.setFloat(ind, Values.HIGH, high);
    series.setFloat(ind, Values.LOW, low);
    series.setFloat(ind, Values.CLOSE, close);
    series.setDouble(ind, Values.VOLUME, series.getDouble(ind, Enums.BarInput.VOLUME, instrument));
    series.setDouble(ind, Values.OPEN_INTEREST, series.getDouble(ind, Enums.BarInput.OPEN_INTEREST, instrument));
    
    if (rangeUpdate) notifyRangeUpdated();
    notifyRedraw();
  };
}
