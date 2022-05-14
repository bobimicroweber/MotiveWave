package com.motivewave.platform.study.chaikin;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Accumulation Distribution Line */
@StudyHeader(
    namespace="com.motivewave", 
    id="AD", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ADL",
    label="LBL_ADL",
    tabName="TAB_ADL",
    desc="DESC_ADL",
    menu="MENU_MARC_CHAIKIN",
    menu2="MENU_VOLUME",
    overlay=false,
    requiresVolume=true,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/accumulation_distribution_line.htm")
public class ADLine extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { ADL }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.PATH, Inputs.IND);
    
    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.ADL, get("TAB_ADL"), new String[] {}));
    desc.declarePath(Values.ADL, Inputs.PATH);
    desc.declareIndicator(Values.ADL, Inputs.IND);
    desc.setRangeKeys(Values.ADL);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    var series = ctx.getDataSeries();
    // Calculate the OBV
    Double prev = series.getDouble(index-1, Values.ADL);
    if (prev == null) prev = 0.0;
    double adl = prev + series.getVolume(index)*series.mfm(index);
    series.setDouble(index, Values.ADL, adl);
    series.setComplete(index);
  }  
  
}
