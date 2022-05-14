package com.motivewave.platform.study.wilder;

import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Volatility Index Strategy. This is based of the Volatility Index study and adds the ability to trade. */
@StudyHeader(
  namespace="com.motivewave",
  id="VX_STRATEGY",
  rb="com.motivewave.platform.study.nls.strings",
  name="TITLE_VX_STRATEGY",
  desc="DESC_VX_STRATEGY",
  menu="MENU_WELLES_WILDER",
  helpLink="http://www.motivewave.com/strategies/volatility_index_strategy.htm",
  overlay=true,
  signals=true,
  strategy=true,
  autoEntry=true,
  manualEntry=false,
  supportsStopPL=true,
  supportsUnrealizedPL=true,
  supportsRealizedPL=true,
  supportsTotalPL=true,
  supportsUseAccountPosition=false)
public class VXStrategy extends VolatilityIndex
{
  // Key for a flag that indicates that a trade occurred on a given price bar
  final static String TRADE_OCCURRED="TRADE_OCCURRED";

  @Override
  public void onActivate(OrderContext ctx)
  {
    super.onActivate(ctx);
    if (getSettings().isEnterOnActivate()) {
      var series=ctx.getDataContext().getDataSeries();
      Boolean isLong=series.getBoolean(series.size() - 2, Values.LONG);
      if (isLong == null) return;
      int tradeLots=getSettings().getTradeLots();
      float qty=tradeLots*=ctx.getInstrument().getDefaultQuantityAsFloat();
      if (isLong) ctx.buy(qty);
      else ctx.sell(qty);
    }
  }

  @Override
  public void onDeactivate(OrderContext ctx)
  {
    if (getSettings().isCloseOnDeactivate()) ctx.closeAtMarket();
    super.onDeactivate(ctx);
  }

  @Override
  public void onBarClose(OrderContext ctx)
  {
    // The Volatility Index only works on closing prices
    // so we need to work on completed bars only
    var series=ctx.getDataContext().getDataSeries();
    var instr=ctx.getInstrument();

    // Only do one trade per bar. Use the latest PSAR for the stop value in test cases (since the position has reversed)
    if (series.getBoolean(TRADE_OCCURRED, false)) {
      Double SAR=series.getDouble(Values.SAR);
      if (SAR != null) setStopPrice((float) instr.round(SAR));
      return;
    }

    Double SAR=series.getDouble(series.size() - 2, Values.SAR);
    Boolean isLong=series.getBoolean(series.size() - 2, Values.LONG);
    // These values shouldn't be null, but check just in case...
    if (SAR == null || isLong == null) return;

    float sar=(float) instr.round(SAR); // round this to a real value...
    setStopPrice(sar);

    float position=ctx.getPositionAsFloat();
    int tradeLots=getSettings().getTradeLots();
    float qty=tradeLots*=instr.getDefaultQuantityAsFloat() + Math.abs(position);
    float close=series.getClose();

    if (isLong && close <= sar) {
      ctx.sell(qty);
      series.setBoolean(series.size() - 1, TRADE_OCCURRED, true);
    }
    else if (!isLong && close >= sar) {
      ctx.buy(qty);
      series.setBoolean(series.size() - 1, TRADE_OCCURRED, true);
    }
  }
}
