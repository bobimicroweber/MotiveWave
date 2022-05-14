package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** On Balance Volume */
@StudyHeader(
    namespace="com.motivewave", 
    id="OBV", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_OBV",
    tabName="TAB_OBV",
    desc="DESC_OBV",
    menu="MENU_VOLUME",
    overlay=false,
    requiresVolume=true,
    helpLink="http://www.motivewave.com/studies/on_balance_volume.htm")
public class OnBalanceVolume extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { OBV }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var grp = tab.addGroup(get("LBL_COLORS"));
    grp.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false));
    grp.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.PATH, Inputs.IND);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.OBV, get("VAL_OBV"), new String[] {}));
    desc.declarePath(Values.OBV, Inputs.PATH);
    desc.declareIndicator(Values.OBV, Inputs.IND);
    desc.setRangeKeys(Values.OBV);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    var series = ctx.getDataSeries();
    // Determine OBVprev
    Double prev = series.getDouble(index-1, Values.OBV);
    if (prev == null) prev = 0.0;
    double value = prev;
    if (series.getClose(index) > series.getClose(index-1)) {
      value = prev + series.getVolume(index);
    }
    else if (series.getClose(index) < series.getClose(index-1)) {
      value = prev - series.getVolume(index);
    }
    
    series.setDouble(index, Values.OBV, value);
    series.setComplete(index);
  }  
}
