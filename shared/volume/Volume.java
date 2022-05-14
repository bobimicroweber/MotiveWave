package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Displays volume as bars */
@StudyHeader(
    namespace="com.motivewave", 
    id="VOLUME", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_VOLUME",
    menu="MENU_VOLUME",
    desc="DESC_VOLUME",
    overlay=false,
    requiresVolume=true,
    requiresBarUpdates=true)
public class Volume extends com.motivewave.platform.sdk.study.Study 
{
  final static String VOLUME_IND = "volumeInd";
  final static String VMA_IND = "vmaInd";
  
	enum Values { VOLUME, VMA }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup(get("LBL_VOLUME"));
    var bars = new PathDescriptor(Inputs.BAR, get("LBL_VOLUME_BARS"), defaults.getBarColor(), 1.0f, null, true, false, true);
    bars.setShowAsBars(true);
    bars.setSupportsShowAsBars(true);
    bars.setSupportsDisable(false);
    bars.setColorPolicies(new Enums.ColorPolicy[] { Enums.ColorPolicy.PRICE_BAR, Enums.ColorPolicy.SOLID, Enums.ColorPolicy.HIGHER_LOWER, Enums.ColorPolicy.GRADIENT });
    bars.setColorPolicy(Enums.ColorPolicy.PRICE_BAR);
    grp.addRow(bars);
    grp.addRow(new IndicatorDescriptor(VOLUME_IND, get("LBL_INDICATOR"), null, null, false, true, true));

    grp = tab.addGroup(get("LBL_MOVING_AVERAGE"));
    grp.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    grp.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    var pdesc = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), Util.awtColor(225, 102, 0), 1.0f, null, false, false, true);
    pdesc.setShadeType(Enums.ShadeType.BELOW);
    grp.addRow(pdesc);
    grp.addRow(new IndicatorDescriptor(VMA_IND, get("LBL_INDICATOR"), null, null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.BAR, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc = createRD();
    desc.exportValue(new ValueDescriptor(Values.VOLUME, get("LBL_VOLUME"), new String[] {}));
    desc.exportValue(new ValueDescriptor(Values.VMA, get("LBL_VMA"), new String[] {Inputs.METHOD, Inputs.PERIOD}));
    desc.declarePath(Values.VOLUME, Inputs.BAR);
    desc.declarePath(Values.VMA, Inputs.PATH);
    desc.setFixedBottomValue(0);
    desc.setBottomInsetPixels(0);
    desc.setRangeKeys(Values.VOLUME, Values.VMA);
    desc.setMinTopValue(10);
    desc.declareIndicator(Values.VOLUME, VOLUME_IND);
    desc.declareIndicator(Values.VMA, VMA_IND);
    desc.setMinTick(1.0);
    desc.setTopInsetPixels(5);
    desc.setBottomInsetPixels(0);
  }

  @Override
  public void clearState()
  {
    // TODO Auto-generated method stub
    super.clearState();
    path = getSettings().getPath(Inputs.PATH);
    period = getSettings().getInteger(Inputs.PERIOD, 20);
    method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.SMA);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    float vol = series.getVolumeAsFloat(index);
    series.setFloat(index, Values.VOLUME, vol);
    if (path != null && path.isEnabled() && index >= period) {
      series.setDouble(index, Values.VMA, series.ma(method, index, period, Values.VOLUME));
    }
    series.setComplete(index);
  }
  
  private int period;
  private Enums.MAMethod method;
  private PathInfo path;
}
