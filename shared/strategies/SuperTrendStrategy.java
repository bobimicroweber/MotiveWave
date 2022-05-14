package com.motivewave.platform.study.strategies;

import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;
import com.motivewave.platform.study.overlay.SuperTrend;

/** Moving Average Cross Strategy. This is based of the SampleMACross study and adds the ability to trade. */
@StudyHeader(
  namespace="com.motivewave", 
  id="SUPERTREND_STRATEGY", 
  name="NAME_SUPER_TREND_STRATEGY",
  desc="DESC_SUPER_TREND_STRATEGY",
  rb="com.motivewave.platform.study.nls.strings",
  menu="MENU_GENERAL",
  overlay = true,
  signals = true,
  strategy = true,
  autoEntry = true,
  manualEntry = false,
  supportsUnrealizedPL = true,
  supportsRealizedPL = true,
  supportsTotalPL = true)
public class SuperTrendStrategy extends SuperTrend
{
  @Override
  public void onActivate(OrderContext ctx)
  {
    if (getSettings().isEnterOnActivate()) {
      var series = ctx.getDataContext().getDataSeries();
      int ind = series.isLastBarComplete() ? series.size()-1 : series.size()-2;
      var tsl = series.getDouble(ind, Values.TREND);
      if (tsl == null) return;
      int tradeLots = getSettings().getTradeLots();
      float qty=tradeLots*=ctx.getInstrument().getDefaultQuantityAsFloat();

      // Create a long or short position if we are above or below the signal line
      if (tsl > 0) ctx.buy(qty);
      else ctx.sell(qty);
    }
  }

  @Override
  public void onSignal(OrderContext ctx, Object signal)
  {
    var instr = ctx.getInstrument();
    float position=ctx.getPositionAsFloat();
    float qty=(getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

    qty += Math.abs(position); // Stop and Reverse if there is an open position
    if (position <= 0 && signal == Signals.BUY) {
      ctx.buy(qty); // Open Long Position
    }
    if (position >= 0 && signal == Signals.SELL) {
      ctx.sell(qty); // Open Short Position
    }
  }
}
