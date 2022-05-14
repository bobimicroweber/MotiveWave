package com.motivewave.platform.study.wilder;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Average True Range */
@StudyHeader(
    namespace="com.motivewave", 
    id="ATR", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ATR",
    tabName="TAB_ATR",
    desc="DESC_ATR",
    menu="MENU_WELLES_WILDER",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/average_true_range.htm")
public class ATR extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { ATR }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.IND);

    var desc = createRD();
    desc.setLabelPrefix(get("TAB_ATR"));
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.ATR, get("TAB_ATR"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.ATR, Inputs.PATH);
    desc.declareIndicator(Values.ATR, Inputs.IND);
    desc.setRangeKeys(Values.ATR);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period) return;
    series.setDouble(index, Values.ATR, series.atr(index, period));
    series.setComplete(index);
  }  
  
}
