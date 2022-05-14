package com.motivewave.platform.study.chaikin;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Chaikin Money Flow */
@StudyHeader(
    namespace="com.motivewave", 
    id="CMF", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_CMF",
    label="LBL_CMF",
    tabName="TAB_CMF",
    desc="DESC_CMF",
    menu="MENU_MARC_CHAIKIN",
    overlay=false,
    requiresVolume=true,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/chaikin_money_flow.htm")
public class ChaikinMoneyFlow extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { VAL }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false);
    path.setSupportsShowAsBars(true);
    settings.addRow(path);
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.VAL, get("TAB_CMF"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.VAL, Inputs.PATH);
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.setRangeKeys(Values.VAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period-1) return;
    var series = ctx.getDataSeries();
    
    // Get the total for the AD line for the period
    double adTotal = 0;
    double volumeTotal = 0;
    
    for(int i = index-period+1; i <= index; i++) {
      adTotal += series.getVolume(i)*series.mfm(i);
      volumeTotal += series.getVolume(i); 
    }
    
    if (volumeTotal == 0) return;
    
    series.setDouble(index, Values.VAL, adTotal/volumeTotal);
    series.setComplete(index);
  }  
}
