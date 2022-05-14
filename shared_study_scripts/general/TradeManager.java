package com.motivewave.platform.study.general;

import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.ExitPointDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.TifDescriptor;
import com.motivewave.platform.sdk.order_mgmt.Order;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader (
  id="TradeManager",
  namespace="com.motivewave",
  rb="com.motivewave.platform.study.nls.strings",
  name="TITLE_TRADE_MANAGER",
  desc="DESC_TRADE_MANAGER",
  menu="MENU_GENERAL",
  overlay=true,
  strategy=true,
  autoEntry=false,
  manualEntry=true,
  supportsStopPL=true,
  supportsUnrealizedPL=true,
  supportsRealizedPL=true,
  supportsTotalPL=true,
  supportsTargetPL=true,
  supportsRiskRatio=true,
  showTradeOptions=false,
  requiresBarUpdates=true)
public class TradeManager extends com.motivewave.platform.sdk.study.Study 
{
  public final static String STEP = "step";
  // Entry inputs
  public final static String TRADE_LOTS = "tradeLots", TIME_IN_FORCE = "tif";
  // Exit inputs
  public final static String FIRST_EXIT = "firstExit", SECOND_EXIT = "secondExit", THIRD_EXIT = "thirdExit";
  // Stop Loss
  public final static String BREAK_EVEN_ENABLED = "breakEvenEnabled", BREAK_EVEN = "breakEvenPips", COVER = "coverPips", MAX_LOSS = "maxLoss",
      ATR_PERIOD = "atrPeriod", ATR_MULTIPLIER = "atrMultiplier", ATR_ENABLED = "atrEnabled";

  public final static long ENTRY_DELAY = 2000;

  // Step Types
  final static String PIPS = "PIPS", TICKS = "TICKS";

  @Override
  public void destroy()
  {
    super.destroy();
    clearOrders();
  }
  
  @Override
  public void initialize(Defaults defaults)
  {
    // Study Settings
    var desc = createSD();
    desc.addTab(getEntryExitTab());
    desc.addTab(getStopLossTab());

    desc.addDependency(new EnabledDependency(BREAK_EVEN_ENABLED, BREAK_EVEN, COVER, ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
    desc.addDependency(new EnabledDependency(ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
    setSettingsDescriptor(desc);
  }
  
  protected SettingTab getEntryExitTab()
  {
    List<NVP> steps = new ArrayList();
    steps.add(new NVP(get("LBL_PIPS"), PIPS));
    steps.add(new NVP(get("LBL_TICKS"), TICKS));

    var tab = new SettingTab(get("LBL_ENTRY_EXIT"));

    var grp = tab.addGroup(get("LBL_GENERAL"));
    grp.addRow(new DiscreteDescriptor(STEP, get("LBL_STEP"), TICKS, steps));

    grp = tab.addGroup(get("LBL_ENTRY"));
    //group.addRow(new OrderTypeDescriptor(ENTRY_ORDER_TYPE, get("LBL_ORDER_TYPE"), Enums.OrderType.MARKET, new Enums.OrderType[] { Enums.OrderType.STOP, Enums.OrderType.MARKET }),
    //    new IntegerDescriptor(OFFSET_TICKS, get("LBL_OFFSET_TICKS"), 1, 0, 99, 1));
    grp.addRow(new IntegerDescriptor(TRADE_LOTS, get("LBL_TRADE_LOTS"), 3, 1, 99999, 1),
        new TifDescriptor(TIME_IN_FORCE, get("LBL_TIME_IN_FORCE"), Enums.TIF.GTC));

    grp = tab.addGroup(get("LBL_EXIT"));
    grp.addRow(new ExitPointDescriptor(FIRST_EXIT, get("LBL_FIRST_EXIT"), 10, 1, true, false));
    grp.addRow(new ExitPointDescriptor(SECOND_EXIT, get("LBL_SECOND_EXIT"), 20, 1, true, true));
    grp.addRow(new ExitPointDescriptor(THIRD_EXIT, get("LBL_THIRD_EXIT"), 30, 1, true, true));
    
    return tab;
  }

  protected SettingTab getStopLossTab()
  {
    var tab = new SettingTab(get("LBL_STOP_LOSS"));
    var grp = tab.addGroup(get("LBL_BREAK_EVEN"));
    grp.addRow(new IntegerDescriptor(MAX_LOSS, get("LBL_MAX_LOSS"), 10, 0, 999, 1));
    grp.addRow(new IntegerDescriptor(BREAK_EVEN, get("LBL_BREAK_EVEN"), 6, 0, 999, 1),
        new IntegerDescriptor(COVER, get("LBL_COVER"), 0, 0, 99, 1),
        new BooleanDescriptor(BREAK_EVEN_ENABLED, get("LBL_BREAK_EVEN_ENABLED"), true));

    grp = tab.addGroup(get("LBL_ATR_STOP"));
    grp.addRow(new BooleanDescriptor(ATR_ENABLED, get("LBL_ATR_ENABLED"), true));
    grp.addRow(new IntegerDescriptor(ATR_PERIOD, get("LBL_ATR_PERIOD"), 20, 1, 999, 1),
        new DoubleDescriptor(ATR_MULTIPLIER, get("LBL_ATR_MULTIPLIER"), 2.5, 0.1, 999, 0.1));
    return tab;
  }
  
  @Override
  public void onActivate(OrderContext ctx)
  {
    entryOrder = entryOrder1 = entryOrder2 = entryOrder3 = null;
    createdExitOrders = false;
    setEntryState(Enums.EntryState.NONE);
  }
  
  @Override
  public void onDeactivate(OrderContext ctx)
  {
    createdExitOrders = false;
    clearOrders();
  }

  @Override
  public void onReset(OrderContext ctx) 
  {
    ctx.cancelOrders();
    super.onReset(ctx);
    createdExitOrders = false;
    clearOrders();
  }
  
  @Override
  public void onBarUpdate(OrderContext ctx)
  {
    //System.out.println("onBarUpdate(): " + com.motivewave.common.Util.formatDateMMMDDHHSS(ServiceInstance.getCurrentTime()));
    if (getEntryState() != Enums.EntryState.OPEN) return;
    // Check to see if we have hit the break even state, if so adjust the trail orders.
    Instrument instr = ctx.getInstrument();
    boolean pips = Util.compare(getSettings().getString(STEP, PIPS), PIPS);
    float step = (float)(pips ? instr.getPointSize() : instr.getTickSize());

    float breakEven = getSettings().getInteger(BREAK_EVEN)*step;
    if (isLong()) {
      if (instr.round(instr.getSellPrice() - ctx.getAvgEntryPrice()) >= instr.round(breakEven)) {
        doBreakEven(ctx);
      }
    }
    else {
      if (instr.round(ctx.getAvgEntryPrice() - instr.getBuyPrice()) >= instr.round(breakEven)) {
        doBreakEven(ctx);
      }
    }
  }
  
  @Override
  public void onBarClose(OrderContext ctx)
  {
    var series = ctx.getDataContext().getDataSeries();
    switch(getEntryState()) {
    case NONE:
    case PRE_ENTRY:
      if ((isLong() && series.getClose() > series.getOpen()) || (isShort() && series.getClose() < series.getOpen())) {
        info("TradeManager::onBarClose() entering at market: " + ctx.getInstrument().getSymbol());
        onEnterNow(ctx);
      }
      break;
    case OPEN:
      moveATROrders(ctx);
      break;
    default: break;
    }
  }

  @Override
  public void onOrderFilled(OrderContext ctx, Order order)
  {
    boolean thirdEnabled = getSettings().getExitPoint(THIRD_EXIT).isEnabled();
    boolean secondEnabled = getSettings().getExitPoint(SECOND_EXIT).isEnabled();
    
    // If hedging is supported, we need to deal with three separate orders
    if (order == entryOrder ||
        (thirdEnabled && order == entryOrder3) ||
        (!thirdEnabled && secondEnabled && order == entryOrder2) ||
        (!thirdEnabled && !secondEnabled && order == entryOrder1) ) {
      info("TradeManager::onOrderFilled() Entry Filled, creating exit orders " + ctx.getInstrument().getSymbol());
      setEntryState(Enums.EntryState.OPEN);
      createExitOrders(ctx);
      clearOrder(order);
      return;
    }
    else if (order == entryOrder1 || order == entryOrder2) {
      // wait for the third order to be filled
      return;
    }

    if (Util.in(order, trailOrder, trailOrder1, trailOrder2, trailOrder3)) trailFilled(ctx, order);
    else if (order == firstOrder) firstExitFilled(ctx);
    else if (order == secondOrder) secondExitFilled(ctx);
    else if (order == thirdOrder) thirdExitFilled(ctx);

    clearOrder(order);
  }

  @Override
  public void onOrderCancelled(OrderContext ctx, Order order)
  {
    if (isCancelInProgress()) return;
    if (Util.in(order, entryOrder, entryOrder1, entryOrder2, entryOrder3)) {
      ctx.cancelOrders();
    }
    clearOrder(order);
  }

  @Override
  public void onEnterNow(OrderContext ctx)
  {
    if (getEntryState() != Enums.EntryState.NONE) return;

    setEntryState(Enums.EntryState.WAITING_ENTRY);
  
    // Special Case:  If hedging is enabled we need to create separate position orders for each exit target
    var instr = ctx.getInstrument();
    if (ctx.supportsHedging() && getSettings().getExitPoint(SECOND_EXIT).isEnabled()) {
      var orders = new ArrayList<Order>();
      var ep = getSettings().getExitPoint(FIRST_EXIT);
      entryOrder1 = createMktEntry(ctx, isLong(), ep.getLots() * instr.getDefaultQuantityAsFloat());
      orders.add(entryOrder1);
      ep = getSettings().getExitPoint(SECOND_EXIT);
      if (ep.isEnabled()) {
        entryOrder2 = createMktEntry(ctx, isLong(), ep.getLots() * instr.getDefaultQuantityAsFloat());
        orders.add(entryOrder2);
      }
      ep = getSettings().getExitPoint(THIRD_EXIT);
      if (ep.isEnabled()) {
        entryOrder3 = createMktEntry(ctx, isLong(), ep.getLots() * instr.getDefaultQuantityAsFloat());
        orders.add(entryOrder3);
      }
      ctx.submitOrders(orders);
    }
    else {
      entryOrder = createMktEntry(ctx, isLong(), getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat());
      ctx.submitOrders(entryOrder);
    }
  }

  @Override
  public void onPositionClosed(OrderContext ctx)
  {
    ctx.cancelOrders();
    clearOrders();
    setState(Enums.StrategyState.INACTIVE);
  }
  
  private Order createMktEntry(OrderContext ctx, boolean buy, float qty)
  {
    if (buy) return ctx.createMarketOrder(Enums.OrderAction.BUY, qty);
    return ctx.createMarketOrder(Enums.OrderAction.SELL, qty);
  }  

  private void clearOrders()
  {
    firstOrder = secondOrder = thirdOrder = null;
    entryOrder = entryOrder1 = entryOrder2 = entryOrder3 = null;
    clearTrailOrders();
  }
  
  private void clearTrailOrders()
  {
    trailOrder = trailOrder1 = trailOrder2 = trailOrder3 = null;
  }
  
  private void doBreakEven(OrderContext ctx)
  {
    if (getEntryState() != Enums.EntryState.OPEN) return;
    if (!getSettings().getBoolean(BREAK_EVEN_ENABLED)) return;    

    atrActive = getSettings().getBoolean(ATR_ENABLED);
    breakEvenTrail(ctx, trailOrder);
    breakEvenTrail(ctx, trailOrder1);
    breakEvenTrail(ctx, trailOrder2);
    breakEvenTrail(ctx, trailOrder3);
  }  
  
  private void breakEvenTrail(OrderContext ctx, Order trail)
  {
    if (trail == null || trail.isFilled() || !trail.exists()) return;
    var instr = ctx.getInstrument();
    boolean pips = Util.compare(getSettings().getString(STEP, PIPS), PIPS);
    float step = (float)(pips ? instr.getPointSize() : instr.getTickSize());
    
    // Double check the we are not already at break even
    float coverOffset = (float)(getSettings().getDouble(COVER) * step);
    float breakEvenPrice = ctx.getAvgEntryPrice();
    if (coverOffset > 0) {
      if (isLong()) breakEvenPrice += coverOffset;
      else breakEvenPrice -= coverOffset;
    }

    boolean buy = trail.isBuy();
    if (buy) {
      float min = instr.round(instr.getAskPrice() + 2.0f*step);
      if (breakEvenPrice < min) {
        warning("TradeManager::breakEvenTrail() too close to ask price setting 2 pts away: " + instr.getSymbol() + " breakEven: " + min);
        breakEvenPrice = min;
      }
    }
    else {
      float max = instr.round(instr.getBidPrice() - 2.0f*step);
      if (breakEvenPrice > max) {
        warning("TradeManager::breakEvenTrail() too close to bid price setting 2 pts away: " + instr.getSymbol() + " breakEven: " + max);
        breakEvenPrice = max;
      }
    }    

    if ( (isLong() && trail.getStopPrice() >= breakEvenPrice) || 
        (!isLong() && trail.getStopPrice() <= breakEvenPrice)) return;

    trail.setAdjStopPrice(breakEvenPrice);
    ctx.submitOrders(trail);
  }
  
  private void createExitOrders(OrderContext ctx)
  {
    if (createdExitOrders) {
      warning("TradeManager::createExitOrders() orders already created!");
      return;
    }
    var instr = ctx.getInstrument();
    boolean pips = Util.compare(getSettings().getString(STEP, PIPS), PIPS);
    float step = (float)(pips ? instr.getPointSize() : instr.getTickSize());
    createdExitOrders = true;
    
    // Calculate the stop price
    float offset = (float)(getSettings().getDouble(MAX_LOSS) * step);
    float basePrice = ctx.getAvgEntryPrice();
    if (isLong()) basePrice -= offset;
    else basePrice += offset;
    
    // Make sure this is at least 1 pip away from the current bid/ask
    if (isLong()) {
      float maxPrice = instr.round(instr.getSellPrice() - step);
      if (basePrice > maxPrice) basePrice = maxPrice;
    }
    else {
      float minPrice = instr.round(instr.getBuyPrice() + step);
      if (basePrice < minPrice) basePrice = minPrice;
    }
    
    var action = isLong() ? Enums.OrderAction.SELL : Enums.OrderAction.BUY;
    
    var orders = new ArrayList<Order>();
    var tif = getSettings().getTIF(TIME_IN_FORCE);
    var entry = entryOrder;
    
    // Create Target Orders
    var ep = getSettings().getExitPoint(FIRST_EXIT);
    float qty = ep.getLots() * instr.getDefaultQuantityAsFloat();
    float firstExit = 0;
    if (isLong()) firstExit = instr.round(ctx.getAvgEntryPrice() + ep.getPips() * step);
    else firstExit = instr.round(ctx.getAvgEntryPrice() - ep.getPips() * step);
    
    Object ref = entryOrder1 != null ? entryOrder1.getReferenceID() : null;
    firstOrder = ctx.createLimitOrder(instr, ref, action, tif, qty, firstExit);

    orders.add(firstOrder);
    
    ep = getSettings().getExitPoint(SECOND_EXIT);
    if (ep.isEnabled()) {
      qty = ep.getLots() * instr.getDefaultQuantityAsFloat();
      float secondExit = 0;
      if (isLong()) secondExit = instr.round(ctx.getAvgEntryPrice() + ep.getPips() * step);
      else secondExit = instr.round(ctx.getAvgEntryPrice() - ep.getPips() * step);

      ref = entryOrder2 != null ? entryOrder2.getReferenceID() : null;
      secondOrder = ctx.createLimitOrder(instr, ref, action, tif, qty, secondExit);
      orders.add(secondOrder);
    }

    ep = getSettings().getExitPoint(THIRD_EXIT);
    if (ep.isEnabled()) {
      qty = ep.getLots() * instr.getDefaultQuantityAsFloat();
      float thirdExit = 0;
      if (isLong()) thirdExit = instr.round(ctx.getAvgEntryPrice() + ep.getPips() * step);
      else thirdExit = instr.round(ctx.getAvgEntryPrice() - ep.getPips() * step);

      ref = entryOrder3 != null ? entryOrder3.getReferenceID() : null;
      thirdOrder = ctx.createLimitOrder(instr, ref, action, tif, qty, thirdExit);
      orders.add(thirdOrder);
    }

    // Create the stop order(s)
    atrActive = false;
    if (entry != null) {
      // Non-hedged, just create one order
      qty = getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat();
      trailOrder = ctx.createStopOrder(instr, action, tif, qty, basePrice);
      orders.add(trailOrder);
    }
    else {
      qty = getSettings().getExitPoint(FIRST_EXIT).getLots() * instr.getDefaultQuantityAsFloat();
      trailOrder1 = ctx.createStopOrder(instr, entryOrder1.getReferenceID(), action, tif, qty, basePrice);
      orders.add(trailOrder1);
      
      if (entryOrder2 != null) {
        qty = getSettings().getExitPoint(SECOND_EXIT).getLots() * instr.getDefaultQuantityAsFloat();
        trailOrder2 = ctx.createStopOrder(instr, entryOrder2.getReferenceID(), action, tif, qty, basePrice);
        orders.add(trailOrder2);
      }

      if (entryOrder3 != null) {
        qty = getSettings().getExitPoint(THIRD_EXIT).getLots() * instr.getDefaultQuantityAsFloat();
        trailOrder3 = ctx.createStopOrder(instr, entryOrder3.getReferenceID(), action, tif, qty, basePrice);
        orders.add(trailOrder3);
      }
    }

    // To make this faster, submit these orders together
    ctx.submitOrders(orders);
  }
  
  private void moveATROrders(OrderContext ctx)
  {
    if (!atrActive) {
      return;
    }
    if (!isValidOrder(trailOrder) && !isValidOrder(trailOrder1) && !isValidOrder(trailOrder2) && !isValidOrder(trailOrder3)) return;

    // Calculate the ATR offset
    var dc = ctx.getDataContext();
    var series = dc.getDataSeries();
    var instr = dc.getInstrument();
    int latest = series.size()-1;
    int atrPeriod = getSettings().getInteger(ATR_PERIOD);
    double mult = getSettings().getDouble(ATR_MULTIPLIER);
    Double atr = series.atr(latest-1, atrPeriod);
    if (atr == null) return;
    float offset = (float)instr.round(atr*mult);
    boolean pips = Util.compare(getSettings().getString(STEP, PIPS), PIPS);
    float step = (float)(pips ? instr.getPointSize() : instr.getTickSize());
    
    boolean buy = true;
    if (isValidOrder(trailOrder)) buy = trailOrder.isBuy();
    if (isValidOrder(trailOrder1)) buy = trailOrder1.isBuy();

    float lastPrice = series.getClose(latest-1);
    float stop = 0;

    if (buy) {
      stop = instr.round(lastPrice + offset);
      float min = instr.round(instr.getAskPrice() + 2.0f*step);
      if (stop < min) stop = min;
    }
    else {
      stop = instr.round(lastPrice - offset);
      float max = instr.round(instr.getBidPrice() - 2.0f*step);
      if (stop > max) stop = max;
    }

    double stopPrice = 0;
    if (isValidOrder(trailOrder)) stopPrice = trailOrder.getStopPrice();
    else if (isValidOrder(trailOrder1)) stopPrice = trailOrder1.getStopPrice();
    else if (isValidOrder(trailOrder2)) stopPrice = trailOrder2.getStopPrice();
    else if (isValidOrder(trailOrder3)) stopPrice = trailOrder3.getStopPrice();

    if ((buy && stop >= stopPrice) || (!buy && stop <= stopPrice)) {
      // No change in price
      return;
    }

    if (isValidOrder(trailOrder)) {
      trailOrder.setAdjStopPrice(stop);
      ctx.submitOrders(trailOrder);
    }
    else {
      var orders = new ArrayList<Order>();
      if (isValidOrder(trailOrder1)) {
        trailOrder1.setAdjStopPrice(stop);
        orders.add(trailOrder1);
      }
      if (isValidOrder(trailOrder2)) {
        trailOrder2.setAdjStopPrice(stop);
        orders.add(trailOrder2);
      }
      if (isValidOrder(trailOrder3)) {
        trailOrder3.setAdjStopPrice(stop);
        orders.add(trailOrder3);
      }
      ctx.submitOrders(orders);
    }
  }
  
  private void trailFilled(OrderContext ctx, Order order)
  {
    var cancelList = new ArrayList<Order>();
    if (isValidOrder(firstOrder) && Util.isEmpty(firstOrder.getReferenceID())) cancelList.add(firstOrder);
    if (isValidOrder(secondOrder) && Util.isEmpty(secondOrder.getReferenceID())) cancelList.add(secondOrder);
    if (isValidOrder(thirdOrder) && Util.isEmpty(thirdOrder.getReferenceID())) cancelList.add(thirdOrder);
    firstOrder = secondOrder = thirdOrder = null;
    ctx.cancelOrders(cancelList);
  }

  private void firstExitFilled(OrderContext ctx)
  {
    try {
      if (trailOrder != null) {
        float qty = firstOrder.getFilledAsFloat();
        if (qty == 0) {
          // Grab the value from the config
          warning("TradeManager::firstExitFilled() filled qty is 0!");
          qty = getSettings().getExitPoint(FIRST_EXIT).getLots() * ctx.getInstrument().getDefaultQuantityAsFloat();
        }
        if (qty >= trailOrder.getQuantityAsFloat()) {
          var cancelList = new ArrayList<Order>();
          if (Util.isEmpty(trailOrder.getReferenceID())) cancelList.add(trailOrder);
          if (secondOrder != null) cancelList.add(secondOrder);
          if (thirdOrder != null) cancelList.add(thirdOrder);
          ctx.cancelOrders(cancelList);
        }
        else {
          trailOrder.setAdjQuantity(trailOrder.getQuantityAsFloat() - qty);
          ctx.submitOrders(trailOrder);
        }
      }
      else if (trailOrder1 != null) {
        if (Util.isEmpty(trailOrder1.getReferenceID())) ctx.cancelOrders(trailOrder1);
      }
    }
    finally {
      firstOrder = null;
    }
  }

  private void secondExitFilled(OrderContext ctx)
  {
    try {
      if (trailOrder != null) {
        float qty = secondOrder.getFilledAsFloat();
        if (qty == 0) {
          // Grab the value from the config
          warning("TradeManager::secondExitFilled() filled qty is 0!");
          qty = getSettings().getExitPoint(SECOND_EXIT).getLots() * ctx.getInstrument().getDefaultQuantityAsFloat();
        }
        if (qty >= trailOrder.getQuantityAsFloat()) {
          var cancelList = new ArrayList<Order>();
          if (Util.isEmpty(trailOrder.getReferenceID())) cancelList.add(trailOrder);
          if (thirdOrder != null) cancelList.add(thirdOrder);
          ctx.cancelOrders(cancelList);
        }
        else {
          trailOrder.setAdjQuantity(trailOrder.getQuantityAsFloat() - qty);
          ctx.submitOrders(trailOrder);
        }
      }
      else if (trailOrder2 != null) {
        if (Util.isEmpty(trailOrder2.getReferenceID())) ctx.cancelOrders(trailOrder2);
      }
    }
    finally {
      secondOrder = null;
    }
  }
  
  private void thirdExitFilled(OrderContext ctx)
  {
    try {
      // Cancel the stop
      if (trailOrder != null) {
        if (Util.isEmpty(trailOrder.getReferenceID())) ctx.cancelOrders(trailOrder);
      }
      else if (trailOrder3 != null) {
        if (Util.isEmpty(trailOrder3.getReferenceID())) ctx.cancelOrders(trailOrder3);
      }
    }
    finally {
      thirdOrder = null;
    }
  }

  private void clearOrder(Order order)
  {
    if (order == entryOrder) entryOrder = null;
    if (order == entryOrder1) entryOrder1 = null;
    if (order == entryOrder2) entryOrder2 = null;
    if (order == entryOrder3) entryOrder3 = null;
    if (order == firstOrder) firstOrder = null;
    if (order == secondOrder) secondOrder = null;
    if (order == thirdOrder) thirdOrder = null;
    if (order == trailOrder) trailOrder = null;
    if (order == trailOrder1) trailOrder1 = null;
    if (order == trailOrder2) trailOrder2 = null;
    if (order == trailOrder3) trailOrder3 = null;
  }
  
  private boolean isValidOrder(Order order)
  {
    if (order == null) return false;
    if (!order.exists()) return false;
    if (order.isCancelled() || order.isFilled()) return false;
    return true;
  }
  
  private Order entryOrder, entryOrder1, entryOrder2, entryOrder3;

  private Order trailOrder, trailOrder1, trailOrder2, trailOrder3;
  private boolean atrActive = false;
  
  private Order firstOrder, secondOrder, thirdOrder;
  private boolean createdExitOrders = false;
}
