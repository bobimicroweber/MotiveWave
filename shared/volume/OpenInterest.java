package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Displays open interest as bars */
@StudyHeader(
    namespace="com.motivewave", 
    id="OPEN_INTEREST", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_OPEN_INTEREST",
    menu="MENU_VOLUME",
    desc="DESC_OPEN_INTEREST",
    overlay=false, 
    requiresVolume=true)
public class OpenInterest extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { OPEN_INTEREST, OMA }
  
	final static String OPEN_INTEREST_IND = "openInterestInd";
  final static String OMA_IND = "omaInd";
	
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var group = tab.addGroup(get("LBL_OPEN_INTEREST"));
    var bars = new PathDescriptor(Inputs.BAR, get("LBL_OPEN_INTEREST_BARS"), defaults.getBarColor(), 1.0f, null, true, false, true);
    bars.setShowAsBars(true);
    bars.setSupportsShowAsBars(true);
    bars.setSupportsDisable(false);
    bars.setColorPolicies(new Enums.ColorPolicy[] { Enums.ColorPolicy.PRICE_BAR, Enums.ColorPolicy.SOLID, Enums.ColorPolicy.HIGHER_LOWER, Enums.ColorPolicy.GRADIENT });
    bars.setColorPolicy(Enums.ColorPolicy.PRICE_BAR);
    group.addRow(bars);
    group.addRow(new IndicatorDescriptor(OPEN_INTEREST_IND, get("LBL_INDICATOR"), null, null, false, true, true));

    group = tab.addGroup(get("LBL_MOVING_AVERAGE"));
    group.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    group.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), Util.awtColor(225, 102, 0), 1.0f, null, true, false, true);
    path.setShadeType(Enums.ShadeType.BELOW);
    group.addRow(path);
    group.addRow(new IndicatorDescriptor(OMA_IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.BAR, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.OPEN_INTEREST, get("LBL_OPEN_INTEREST"), new String[] {}));
    desc.exportValue(new ValueDescriptor(Values.OMA, get("LBL_OMA"), new String[] {Inputs.METHOD, Inputs.PERIOD}));
    desc.declarePath(Values.OPEN_INTEREST, Inputs.BAR);
    desc.declarePath(Values.OMA, Inputs.PATH);
    desc.setFixedBottomValue(0);
    desc.setBottomInsetPixels(0);
    desc.setRangeKeys(Values.OPEN_INTEREST);
    desc.setMinTopValue(10);
    desc.declareIndicator(Values.OPEN_INTEREST, OPEN_INTEREST_IND);
    desc.declareIndicator(Values.OMA, OMA_IND);
    desc.setMinTick(1.0);
    desc.setTopInsetPixels(5);
    desc.setBottomInsetPixels(0);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    double vol = series.getOpenInterest(index);
    vol = Math.floor(vol); // there could be some precision issues, so make sure it is a whole number
    series.setDouble(index, Values.OPEN_INTEREST, vol);

    int period = getSettings().getInteger(Inputs.PERIOD, 20);
    if (index < period) return;
    var method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.SMA);
    
    series.setDouble(index, Values.OMA, series.ma(method, index, period, Values.OPEN_INTEREST));
    series.setComplete(index);
  }
}