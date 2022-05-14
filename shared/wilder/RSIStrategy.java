package com.motivewave.platform.study.wilder;

import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
    namespace = "com.motivewave",
    id = "RSI_STRATEGY",
    rb = "com.motivewave.platform.study.nls.strings",
    name = "TITLE_RSI_STRATEGY",
    desc = "DESC_RSI_STRATEGY",
    menu = "MENU_WELLES_WILDER",
    overlay = false,
    signals = true,
    strategy = true,
    autoEntry = true,
    manualEntry = false,
    supportsUnrealizedPL = true,
    supportsRealizedPL = true,
    supportsTotalPL = true,
    supportsPositionType = true,
    supportsUseAccountPosition=false)
public class RSIStrategy extends RSI
{
  @Override
  public void onActivate(OrderContext ctx)
  {
    if (getSettings().isEnterOnActivate()) {
      var series = ctx.getDataContext().getDataSeries();
      int ind = series.isLastBarComplete() ? series.size()-1 : series.size()-2;
      Double RSI = series.getDouble(ind, Values.RSI);
      if (RSI == null) return;
      int tradeLots = getSettings().getTradeLots();
      float qty=tradeLots*=ctx.getInstrument().getDefaultQuantityAsFloat();

      switch(getSettings().getPositionType()) {
      case LONG: // Only Long Positions are allowed.
        if (RSI < 50) ctx.buy(qty);
        break;
      case SHORT: // Only Short Positions are allowed.
        if (RSI > 50) ctx.sell(qty);
        break;
      default: // Both Long and Short Positions Allowed
        if (RSI < 50) ctx.buy(qty);
        else ctx.sell(qty);
      }
    }
  }

  @Override
  public void onBarClose(OrderContext ctx)
  {
    var series = ctx.getDataContext().getDataSeries();
    var instr = ctx.getInstrument();
    float position = ctx.getPositionAsFloat();
    float qty = (getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

    switch(getSettings().getPositionType()) {
    case LONG: // Only Long Positions are allowed.
      if (position == 0 && series.getBoolean(Signals.RSI_BOTTOM, false)) {
        ctx.buy(qty); // Open Long Position
      }
      if (position > 0 && series.getBoolean(Signals.RSI_TOP, false)) {
        ctx.sell(qty); // Close Long Position
      }
      break;
    case SHORT: // Only Short Positions are allowed.
      if (position == 0 && series.getBoolean(Signals.RSI_TOP, false)) {
        ctx.sell(qty); // Open Short Position
      }
      if (position < 0 && series.getBoolean(Signals.RSI_BOTTOM, false)) {
        ctx.buy(qty); // Close Short Position
      }
      break;
    default: // Both Long and Short Positions Allowed
      qty += Math.abs(position); // Stop and Reverse if there is an open position
      if (position <= 0 && series.getBoolean(Signals.RSI_BOTTOM, false)) {
        ctx.buy(qty); // Open Long Position
      }
      if (position >= 0 && series.getBoolean(Signals.RSI_TOP, false)) {
        ctx.sell(qty); // Open Short Position
      }
    }
  }

}
