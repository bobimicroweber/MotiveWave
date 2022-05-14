package com.motivewave.platform.study.wilder;

import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Parabolic SAR Strategy. This is based of the Parabolic SAR study and adds the ability to trade. */
@StudyHeader(
  namespace = "com.motivewave",
  id = "PSAR_STRATEGY",
  rb = "com.motivewave.platform.study.nls.strings",
  name = "TITLE_PSAR_STRATEGY",
  desc = "DESC_PSAR_STRATEGY",
  menu = "MENU_WELLES_WILDER",
  helpLink="http://www.motivewave.com/strategies/psar_strategy.htm",
  overlay = true,
  signals = true,
  strategy = true,
  autoEntry = true,
  manualEntry = false,
  supportsStopPL = true,
  supportsUnrealizedPL = true,
  supportsRealizedPL = true,
  supportsTotalPL = true,
  requiresBarUpdates = true,
  supportsUseAccountPosition=false)
public class PSARStrategy extends ParabolicSAR
{
  // Key for a flag that indicates that a trade occurred on a given price bar
  final static String TRADE_OCCURRED = "TRADE_OCCURRED";

  @Override
  public void onActivate(OrderContext ctx)
  {
    if (!getSettings().isEnterOnActivate()) return;

    var series = ctx.getDataContext().getDataSeries();
    Boolean isLong = series.getBoolean(series.size() - 2, Values.LONG);
    if (isLong == null) return;
    int tradeLots = getSettings().getTradeLots();
    float qty=tradeLots*=ctx.getInstrument().getDefaultQuantityAsFloat();
    if (isLong) ctx.buy(qty);
    else ctx.sell(qty);
  }

  @Override
  public void onBarUpdate(OrderContext ctx)
  {
    var series = ctx.getDataContext().getDataSeries();
    var instr = ctx.getInstrument();

    // Only do one trade per bar. Use the latest PSAR for the stop value (since the position has reversed)
    if (series.getBoolean(TRADE_OCCURRED, false)) {
      setStopPrice((float)instr.round(series.getDouble(Values.SAR)));
      return;
    }

    int ind = series.size()-2;
    
    // Base this on the previous bar, since the latest bar is not complete
    Double PSAR = series.getDouble(ind, Values.SAR);
    Boolean isLong = series.getBoolean(ind, Values.LONG);
    
    // These values shouldn't be null, but check just in case...
    if (PSAR == null || isLong == null) return;

    double psar = instr.round(PSAR); // round this to a real value...
    setStopPrice((float)psar);

    float position = ctx.getPositionAsFloat();
    int tradeLots = getSettings().getTradeLots();
    float qty = tradeLots * instr.getDefaultQuantityAsFloat() + Math.abs(position);
    
    if (series.getBoolean(Signals.SAR_SHORT, false)) {
      series.setBoolean(TRADE_OCCURRED, true);
      ctx.buy(qty);
    }
    else if (series.getBoolean(Signals.SAR_LONG, false)) {
      series.setBoolean(TRADE_OCCURRED, true);
      ctx.sell(qty);
    }
  }
}
