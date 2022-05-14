package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** ATR Channel */
@StudyHeader(
    namespace="com.motivewave", 
    id="ATR_CHANNEL", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ATR_CHANNEL", 
    menu="MENU_OVERLAY",
    desc="DESC_ATR_CHANNEL",
    label="LBL_ATR_CHANNEL",
    overlay=true,
    helpLink="http://www.motivewave.com/studies/atr_channel.htm")
public class ATRChannel extends Study 
{
  final static String MULTIPLIER = "multiplier";
  enum Values { TOP, BOTTOM }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1),
        new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new DoubleDescriptor(MULTIPLIER, get("LBL_MULTIPLIER"), 2.5, 0.1, 999, 0.1));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    var path = new PathDescriptor(Inputs.TOP_PATH, get("LBL_TOP_LINE"), defaults.getGreen(), 1.0f, null, true, true, true);
    path.setSupportsColorPolicy(false);
    colors.addRow(path);
    path = new PathDescriptor(Inputs.BOTTOM_PATH, get("LBL_BOTTOM_LINE"), defaults.getRed(), 1.0f, null, true, true, true);
    path.setSupportsColorPolicy(false);
    colors.addRow(path);
    colors.addRow(new ShadeDescriptor(Inputs.FILL, get("LBL_FILL_COLOR"), Inputs.TOP_PATH, Inputs.BOTTOM_PATH, Enums.ShadeType.BOTH, defaults.getFillColor(), false, true));
    colors.addRow(new IndicatorDescriptor(Inputs.TOP_IND, get("LBL_TOP_IND"), defaults.getGreen(), X11Colors.BLACK, false, false, true));
    colors.addRow(new IndicatorDescriptor(Inputs.BOTTOM_IND, get("LBL_BOTTOM_IND"), defaults.getRed(), X11Colors.BLACK, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, true, () -> Enums.Icon.SHIFT.get()));
    sd.addQuickSettings(MULTIPLIER, Inputs.TOP_PATH, Inputs.BOTTOM_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, MULTIPLIER);
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_ATR_TOP"), new String[] {Inputs.PERIOD, MULTIPLIER}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_ATR_BOTTOM"), new String[] {Inputs.PERIOD, MULTIPLIER}));
    desc.declarePath(Values.TOP, Inputs.TOP_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.BOTTOM_PATH);
    desc.declareIndicator(Values.TOP, Inputs.TOP_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.BOTTOM_IND);
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
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
    int period = getSettings().getInteger(Inputs.PERIOD);
    int shift = getSettings().getInteger(Inputs.SHIFT, 0);
    double mult = getSettings().getDouble(MULTIPLIER);
    var series = ctx.getDataSeries();
    int latest = series.size()-1;

    var method = getSettings().getMAMethod(Inputs.METHOD, MAMethod.SMA);
    Double atr = series.ma(method, latest,  period, Enums.BarInput.TR);
    if (atr == null) return;
    double close = series.getClose(latest);
    double top = ctx.getInstrument().round(close + (atr*mult));
    double bottom = ctx.getInstrument().round(close - (atr*mult));
    
    series.setDouble(latest+shift, Values.TOP, top);
    series.setDouble(latest+shift, Values.BOTTOM, bottom);
  }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    var method = getSettings().getMAMethod(Inputs.METHOD, MAMethod.SMA);
    int period = getSettings().getInteger(Inputs.PERIOD);
    int shift = getSettings().getInteger(Inputs.SHIFT, 0);
    double mult = getSettings().getDouble(MULTIPLIER);
    var series = ctx.getDataSeries();
    int latest = series.size()-1;
    
    // Calculate top and middle lines
    boolean updates = getSettings().isBarUpdates();
    for(int i = period; i < series.size(); i++) {
      if (series.isComplete(i+shift)) continue;
      if (!updates && !series.isBarComplete(i)) continue;
      Double atr = series.ma(method, i,  period, Enums.BarInput.TR);
      if (atr == null) continue;
      double close = series.getClose(i);
      double top = ctx.getInstrument().round(close + (atr*mult));
      double bottom = ctx.getInstrument().round(close - (atr*mult));
      series.setDouble(i+shift, Values.TOP, top);
      series.setDouble(i+shift, Values.BOTTOM, bottom);
      series.setComplete(i+shift, i >= 0 && i < latest); // latest bar is not complete
    }    
  }
}
