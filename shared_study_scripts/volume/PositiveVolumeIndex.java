package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Positive Volume Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="POS_VOL", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_POS_VOL",
    tabName="TAB_POS_VOL",
    desc="DESC_POS_VOL",
    menu="MENU_VOLUME",
    overlay=false,
    requiresVolume=true)
public class PositiveVolumeIndex extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { VAL }

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
    desc.exportValue(new ValueDescriptor(Values.VAL, get("VAL_POS_VOL"), new String[] {}));
    desc.declarePath(Values.VAL, Inputs.PATH);
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.setRangeKeys(Values.VAL);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    var series = ctx.getDataSeries();
    
    Double prev = series.getDouble(index-1, Values.VAL);
    if (prev == null) prev = 1.0;
    double value = prev;
    if (series.getVolume(index) > series.getVolume(index-1)) {
      value = prev + ((series.getClose(index) - series.getClose(index-1))/series.getClose(index-1))*prev;  
    }
    series.setDouble(index, Values.VAL, value);
    series.setComplete(index);
  }  
}
