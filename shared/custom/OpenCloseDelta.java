package com.motivewave.platform.study.custom;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Open Close Delta.  Displays the difference between the open and close values of a bar. */
@StudyHeader(
    namespace="com.motivewave", 
    id="OCD", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_OPEN_CLOSE_DELTA",
    desc="DESC_OPEN_CLOSE_DELTA",
    tabName="LBL_OPEN_CLOSE_DELTA",
    menu="MENU_CUSTOM",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/open_close_delta.htm")
public class OpenCloseDelta extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { DELTA }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));
    colors.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));
    colors.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.UP_COLOR, Inputs.DOWN_COLOR);
    
    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.DELTA, get("LBL_OPEN_CLOSE_DELTA"), new String[] {}));
    desc.declareBars(Values.DELTA);
    desc.declareIndicator(Values.DELTA, Inputs.IND);
    desc.setRangeKeys(Values.DELTA);
    desc.setFixedBottomValue(0);
    desc.setBottomInsetPixels(0);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    Double open = series.getDouble(index, Enums.BarInput.OPEN);
    Double close = series.getDouble(index, Enums.BarInput.CLOSE);
    if (open == null || close == null) return;
    
    double delta = Math.abs(close - open);
    series.setDouble(index, Values.DELTA, delta);
    if (close > open) series.setBarColor(index, Values.DELTA, getSettings().getColor(Inputs.UP_COLOR));
    else series.setBarColor(index, Values.DELTA, getSettings().getColor(Inputs.DOWN_COLOR));
    
    series.setComplete(index);
  }
}
