package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Choppiness Index 170 */
@StudyHeader(
  namespace = "com.motivewave",
  id = "ID_CHOPPINESS",
  rb = "com.motivewave.platform.study.nls.strings2",
  label ="LBL_CHOP",
  name = "NAME_CHOPPINESS_INDEX",
  desc = "DESC_CHOP",
  menu = "MENU_OSCILLATORS",
  signals = false,
  overlay = false,
  studyOverlay=true)
public class ChoppinessIndex extends Study
{
  enum Values {ATR, CHOP }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
  
    var grp = tab.addGroup(get("INPUTS"));
    grp.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 14, 1, 999, 1));
   
    grp = tab.addGroup(get("PATH_INDICATOR"));
    grp.addRow(new PathDescriptor(Inputs.PATH, get("PATH"),  defaults.getLineColor(), 1.0f, null, true, false, true));
    grp.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));
    
    tab = sd.addTab(get("TAB_DISPLAY"));

    grp = tab.addGroup(get("GUIDES"));
    var topDesc = new GuideDescriptor(Inputs.TOP_GUIDE, get("TOP_GUIDE"), 61.8, 0, 999.1, .1, true);
    topDesc.setLineColor(defaults.getRed());
    grp.addRow(topDesc); 
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 50, 0, 999.1, .1, true);
    mg.setDash(new float[] {3, 3});
    grp.addRow(mg);
    var bottomDesc = new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("BOTTOM_GUIDE"), 38.2, 0, 999.1, .1, true);
    bottomDesc.setLineColor(defaults.getGreen());
    grp.addRow(bottomDesc); 
 
    grp = tab.addGroup(get("SHADING"));
    grp.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    grp.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.CHOP, get("LBL_CHOP"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.CHOP, Inputs.PATH);
    desc.setRangeKeys(Values.CHOP);
  }

  @Override
  public void onLoad(Defaults defaults)
  {
    int p1 = getSettings().getInteger(Inputs.PERIOD);
    setMinBars(p1*2);
  }
  
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period) return;

    var series = ctx.getDataSeries();
    Double atr = series.atr(index, period);
    series.setDouble(index, Values.ATR, atr);
    if (index < period*2) return;
   
    Double total = series.sum(index, period, Values.ATR);
    Double lowest = series.lowest(index, period, Enums.BarInput.LOW);
    Double highest = series.highest(index, period, Enums.BarInput.HIGH);
    if (total == null || lowest == null || highest == null) return;

    double diff = highest - lowest;
    double temp = (total/diff);
    double chop = 100 * Math.log10(temp) / Math.log10(period);
    series.setDouble(index, Values.CHOP, chop);
    
    series.setComplete(index);  
  }   
}