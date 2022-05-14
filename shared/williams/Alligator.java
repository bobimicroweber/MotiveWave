package com.motivewave.platform.study.williams;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Alligator */
@StudyHeader(
    namespace="com.motivewave", 
    id="ALLIGATOR", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ALLIGATOR", 
    label="LBL_ALLIGATOR",
    desc="DESC_ALLIGATOR",
    menu="MENU_OVERLAY",
    menu2="MENU_BILL_WILLIAMS",
    overlay=true,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/alligator.htm")
public class Alligator extends Study 
{
  final static String JAW_PERIOD = "jawPeriod", JAW_SHIFT = "jawShift", JAW_LINE = "jawLine", JAW_IND = "jawInd",
      TEETH_PERIOD = "teethPeriod", TEETH_SHIFT = "teethShift", TEETH_LINE = "teethLine", TEETH_IND = "teethInd",
      LIPS_PERIOD = "lipsPeriod", LIPS_SHIFT = "lipsShift", LIPS_LINE = "lipsLine", LIPS_IND = "lipsInd";

  enum Values { JAW, TEETH, LIPS }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.MIDPOINT));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA));
    inputs.addRow(new IntegerDescriptor(JAW_PERIOD, get("LBL_JAW_PERIOD"), 13, 1, 9999, 1),
        new IntegerDescriptor(JAW_SHIFT, get("LBL_SHIFT"), 8, -999, 999, 1));
    inputs.addRow(new IntegerDescriptor(TEETH_PERIOD, get("LBL_TEETH_PERIOD"), 8, 1, 9999, 1),
        new IntegerDescriptor(TEETH_SHIFT, get("LBL_SHIFT"), 5, -999, 999, 1));
    inputs.addRow(new IntegerDescriptor(LIPS_PERIOD, get("LBL_LIPS_PERIOD"), 5, 1, 9999, 1),
        new IntegerDescriptor(LIPS_SHIFT, get("LBL_SHIFT"), 3, -999, 999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(JAW_LINE, get("LBL_JAW_LINE"), defaults.getBlue(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(TEETH_LINE, get("LBL_TEETH_LINE"), defaults.getRed(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(LIPS_LINE, get("LBL_LIPS_LINE"), defaults.getGreen(), 1.0f, null, true, true, true));
    lines.addRow(new IndicatorDescriptor(JAW_IND, get("LBL_JAW_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    lines.addRow(new IndicatorDescriptor(TEETH_IND, get("LBL_TEETH_IND"), defaults.getRed(), X11Colors.WHITE, false, false, true));
    lines.addRow(new IndicatorDescriptor(LIPS_IND, get("LBL_LIPS_IND"), defaults.getGreen(), X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(JAW_PERIOD, get("LBL_JAW_PERIOD"), 13, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(TEETH_PERIOD, get("LBL_TEETH_PERIOD"), 8, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(LIPS_PERIOD, get("LBL_LIPS_PERIOD"), 5, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(JAW_LINE, TEETH_LINE, LIPS_LINE);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.METHOD, JAW_PERIOD, TEETH_PERIOD, LIPS_PERIOD);
    
    desc.exportValue(new ValueDescriptor(Values.JAW, get("LBL_JAW"), new String[] {Inputs.INPUT, Inputs.METHOD, JAW_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.TEETH, get("LBL_TEETH"), new String[] {Inputs.INPUT, Inputs.METHOD, TEETH_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.LIPS, get("LBL_LIPS"), new String[] {Inputs.INPUT, Inputs.METHOD, LIPS_PERIOD}));
    
    desc.declarePath(Values.JAW, JAW_LINE);
    desc.declarePath(Values.TEETH, TEETH_LINE);
    desc.declarePath(Values.LIPS, LIPS_LINE);
    
    desc.declareIndicator(Values.JAW, JAW_IND);
    desc.declareIndicator(Values.TEETH, TEETH_IND);
    desc.declareIndicator(Values.LIPS, LIPS_IND);
    
    desc.setRangeKeys(Values.JAW, Values.TEETH, Values.LIPS);
  }

  @Override
  public void onBarUpdate(DataContext ctx)
  {
    if (!getSettings().isBarUpdates()) return;
    doUpdate(ctx);
  }

  @Override
  public void onBarClose(DataContext ctx)
  {
    doUpdate(ctx);
  }
  
  private void doUpdate(DataContext ctx)
  {
    Util.calcLatestMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(JAW_PERIOD), getSettings().getInteger(JAW_SHIFT), Values.JAW, false);
    Util.calcLatestMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(TEETH_PERIOD), getSettings().getInteger(TEETH_SHIFT), Values.TEETH, false);
    Util.calcLatestMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(LIPS_PERIOD), getSettings().getInteger(LIPS_SHIFT), Values.LIPS, false);
  }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    Util.calcSeriesMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(JAW_PERIOD), getSettings().getInteger(JAW_SHIFT), Values.JAW, false, getSettings().isBarUpdates());
    Util.calcSeriesMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(TEETH_PERIOD), getSettings().getInteger(TEETH_SHIFT), Values.TEETH, false, getSettings().isBarUpdates());
    Util.calcSeriesMA(ctx, getSettings().getMAMethod(Inputs.METHOD), getSettings().getInput(Inputs.INPUT), getSettings().getInteger(LIPS_PERIOD), getSettings().getInteger(LIPS_SHIFT), Values.LIPS, false, getSettings().isBarUpdates());
  }
}
