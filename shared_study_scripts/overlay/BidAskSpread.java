package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Bid Ask Spread */
@StudyHeader(
    namespace="com.motivewave", 
    id="BID_ASK", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_BID_ASK", 
    desc="DESC_BID_ASK",
    label="LBL_BID_ASK",
    menu="MENU_OVERLAY",
    helpLink="http://www.motivewave.com/studies/bid_ask_spread.htm",
    overlay=true,
    requiresBidAskHistory=true)
public class BidAskSpread extends Study 
{
  final static String ASK_LINE = "askLine", BID_LINE = "bidLine";
  
  enum Values { BID, ASK }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), new Enums.BarInput[] { Enums.BarInput.OPEN, Enums.BarInput.HIGH, Enums.BarInput.LOW, Enums.BarInput.CLOSE}, Enums.BarInput.CLOSE));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(BID_LINE, get("LBL_BID_LINE"), defaults.getBlueLine(), 1.0f, null, true, true, true));
    colors.addRow(new PathDescriptor(ASK_LINE, get("LBL_ASK_LINE"), defaults.getRedLine(), 1.0f, null, true, true, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, BID_LINE, ASK_LINE);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT);
    desc.exportValue(new ValueDescriptor(Values.BID, get("LBL_BID"), new String[] {Inputs.INPUT}));
    desc.exportValue(new ValueDescriptor(Values.ASK, get("LBL_ASK"), new String[] {Inputs.INPUT}));
    desc.declarePath(Values.BID, BID_LINE);
    desc.declarePath(Values.ASK, ASK_LINE);
    desc.setRangeKeys(Values.BID, Values.ASK);
  }
  
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    var input = (Enums.BarInput)getSettings().getInput(Inputs.INPUT);
    var series = ctx.getDataSeries();
    if (input == null) input = Enums.BarInput.CLOSE;
   
    switch(input) {
    case HIGH:
      series.setDouble(index, Values.ASK, (double)series.getAskHigh(index));
      series.setDouble(index, Values.BID, (double)series.getBidHigh(index));
      break;
    case LOW:
      series.setDouble(index, Values.ASK, (double)series.getAskLow(index));
      series.setDouble(index, Values.BID, (double)series.getBidLow(index));
      break;
    case OPEN:
      series.setDouble(index, Values.ASK, (double)series.getAskOpen(index));
      series.setDouble(index, Values.BID, (double)series.getBidOpen(index));
      break;
    default:
      series.setDouble(index, Values.ASK, (double)series.getAskClose(index));
      series.setDouble(index, Values.BID, (double)series.getBidClose(index));
      break;
    }
    
    series.setComplete(index);
  }
}
