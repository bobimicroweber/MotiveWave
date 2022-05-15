package study;

import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.order_mgmt.Order;
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

    private int hasActivePosition = 0;
    private Order entryOrder, takeProfitOrder, stopLossOrder;

    @Override
    public void onSignal(OrderContext ctx, Object signal) {

        if (hasActivePosition == 1) {
            info("Already has position");
            return;
        }

        if (signal == Signals.CROSS_RESISTANCE) {
            // Long
            info("New position: LONG");
            createLongPosition(ctx);
        }

        if (signal == Signals.CROSS_SUPPORT) {
            // Short
            info("New position: Short");
            createShortPosition(ctx);
        }
    }

    @Override
    public void onOrderFilled(OrderContext ctx, Order order)
    {
        var instr = ctx.getInstrument();
        float qty=(getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

        // Entry order filled
        if (order == entryOrder) {

            // Its a long position ////////////////////////////////////
            if (order.getAction() == Enums.OrderAction.BUY) {
                Object ref =  entryOrder.getReferenceID();

                var orders = new ArrayList<Order>();

               // Create Take Profit Orders
                float takeProfitPercent = 1;
                float stopLossPercent = 1;
                float entryPrice = instr.getLastPrice();

                float takeProfitPrice = (entryPrice + (entryPrice / 100) * takeProfitPercent);
                float stopLossPrice = (entryPrice - (entryPrice / 100) * stopLossPercent);

                takeProfitPrice = instr.round(takeProfitPrice);
                stopLossPrice = instr.round(stopLossPrice);

                info("Order details:" + ctx.getAvgEntryPrice());
                info("ref:" + ref);
                info("entry price:" + entryPrice);
                info("take profit price:" + takeProfitPrice);
                info("stop loss price:" + stopLossPrice);

                takeProfitOrder = ctx.createLimitOrder(ref, Enums.OrderAction.SELL, Enums.TIF.GTC, qty, takeProfitPrice);
                orders.add(takeProfitOrder);

                // Stop Loss Orders
                stopLossOrder = ctx.createStopOrder(ref, Enums.OrderAction.SELL, Enums.TIF.GTC, qty, stopLossPrice);
                orders.add(stopLossOrder);

                ctx.submitOrders(orders);
                hasActivePosition = 1;
            }

            // Its a short position ////////////////////////////////////
            if (order.getAction() == Enums.OrderAction.SELL) {
                Object ref =  entryOrder.getReferenceID();

                var orders = new ArrayList<Order>();

                // Create Take Profit Orders
                float takeProfitPercent = 1;
                float stopLossPercent = 1;
                float entryPrice = instr.getLastPrice();

                float takeProfitPrice = (entryPrice - (entryPrice / 100) * takeProfitPercent);
                float stopLossPrice = (entryPrice + (entryPrice / 100) * stopLossPercent);

                takeProfitPrice = instr.round(takeProfitPrice);
                stopLossPrice = instr.round(stopLossPrice);

                info("Order details:" + ctx.getAvgEntryPrice());
                info("ref:" + ref);
                info("entry price:" + entryPrice);
                info("take profit price:" + takeProfitPrice);
                info("stop loss price:" + stopLossPrice);

                takeProfitOrder = ctx.createLimitOrder(ref, Enums.OrderAction.BUY, Enums.TIF.GTC, qty, takeProfitPrice);
                orders.add(takeProfitOrder);

                // Stop Loss Orders
                stopLossOrder = ctx.createStopOrder(ref, Enums.OrderAction.BUY, Enums.TIF.GTC, qty, stopLossPrice);
                orders.add(stopLossOrder);

                ctx.submitOrders(orders);
                hasActivePosition = 1;
            }
        }

        // Order finished
        if (order == takeProfitOrder || order == stopLossOrder) {
            info("Position are closed!");
            ctx.cancelOrders();
            hasActivePosition = 0;
        }
    }

    private void createLongPosition(OrderContext ctx)
    {
        var instr = ctx.getInstrument();
        float qty=(getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

        entryOrder = ctx.createMarketOrder(Enums.OrderAction.BUY, qty);
        ctx.submitOrders(entryOrder);

    }

    private void createShortPosition(OrderContext ctx)
    {
        var instr = ctx.getInstrument();
        float qty=(getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

        entryOrder = ctx.createMarketOrder(Enums.OrderAction.SELL, qty);
        ctx.submitOrders(entryOrder);
    }

}
