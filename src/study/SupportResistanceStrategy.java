package study;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
        namespace = "com.cloudvisionltd",
        id = "TITLE_SUPPORT_RESISTANCE_STRATEGY",
        name = "Support & Resistance Strategy",
        desc = "Strategy with support & resistance",
        menu = "A4Crypto Indicators",
        overlay = true,
        signals = true,
        strategy = true,
        autoEntry = true,
        manualEntry = false,
        supportsUnrealizedPL = true,
        supportsRealizedPL = true,
        supportsTotalPL = true)

public class SupportResistanceStrategy extends SupportResistance {

  @Override
  public void onActivate(OrderContext ctx)
  {
    if (getSettings().isEnterOnActivate()) {
      DataSeries series=ctx.getDataContext().getDataSeries();
      int ind=series.isLastBarComplete() ? series.size() - 1 : series.size() - 2;
      Boolean buy=series.getBoolean(ind, Signals.CROSS_RESISTANCE);//buy
      Boolean sell=series.getBoolean(ind, Signals.CROSS_SUPPORT);//sell
      if (buy == null || sell == null) return;

      int tradeLots=getSettings().getTradeLots();
      float qty=tradeLots*=ctx.getInstrument().getDefaultQuantityAsFloat();

      switch (getSettings().getPositionType()) {
        case LONG: // Only Long Positions are allowed.
          if (buy) ctx.buy(qty);
          break;
        case SHORT: // Only Short Positions are allowed.
          if (sell) ctx.sell(qty);
          break;
        default: // Both Long and Short Positions Allowed
          if (buy) ctx.buy(qty);
          else ctx.sell(qty);
      }
    }
  }

  @Override
  public void onSignal(OrderContext ctx, Object signal)
  {
    Instrument instr=ctx.getInstrument();
    float position=ctx.getPositionAsFloat();
    float qty=(getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

    switch (getSettings().getPositionType()) {
      case LONG: // Only Long Positions are allowed.
        if (position == 0 && signal == Signals.CROSS_RESISTANCE) {
          ctx.buy(qty); // Open Long Position
        }
        if (position > 0 && signal == Signals.CROSS_SUPPORT) {
          ctx.sell(qty); // Close Long Position
        }
        break;
      case SHORT: // Only Short Positions are allowed.
        if (position == 0 && signal == Signals.CROSS_SUPPORT) {
          ctx.sell(qty); // Open Short Position
        }
        if (position < 0 && signal == Signals.CROSS_RESISTANCE) {
          ctx.buy(qty); // Close Short Position
        }
        break;
      default: // Both Long and Short Positions Allowed
        qty+=Math.abs(position); // Stop and Reverse if there is an open position
        if (position <= 0 && signal == Signals.CROSS_RESISTANCE) {
          ctx.buy(qty); // Open Long Position
        }
        if (position >= 0 && signal == Signals.CROSS_SUPPORT) {
          ctx.sell(qty); // Open Short Position
        }
    }
  }
}
