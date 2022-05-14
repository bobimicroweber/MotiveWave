package com.motivewave.platform.study.general2;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
  namespace="com.motivewave",
  id="ID_VLT_RATIO",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_VR",
  menu="MENU_GENERAL",
  name="NAME_VOLATILITY_RATIO",
  desc="DESC_VR",
  signals=false,
  overlay=false,
  studyOverlay=true)
public class VolatilityRatio extends Study
{
  enum Values { TR, VR }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));

    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 14, 1, 999, 1));

    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc=createRD();
    desc.setLabelSettings(Inputs.METHOD, Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.VR, get("LBL_VR"), new String[] { Inputs.METHOD, Inputs.PERIOD }));
    desc.declarePath(Values.VR, Inputs.PATH);
    desc.declareIndicator(Values.VR, Inputs.IND);
    desc.setRangeKeys(Values.VR);
  }

  @Override
  public void onLoad(Defaults defaults)
  {
    int p1=getSettings().getInteger(Inputs.PERIOD);
    setMinBars(p1 * 2);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    int prd=getSettings().getInteger(Inputs.PERIOD);
    var method=getSettings().getMAMethod(Inputs.METHOD);
    var series=ctx.getDataSeries();

    double high=series.getHigh(index);
    double low=series.getLow(index);
    double prevClose=series.getClose(index - 1);

    double tr=Math.max((Math.max(high - low, high - prevClose)), prevClose - low);
    series.setDouble(index, Values.TR, tr);

    Double ma=series.ma(method, index, prd, Values.TR);
    if (ma == null) return;

    double vr=tr / ma;
    series.setDouble(index, Values.VR, vr);
    series.setComplete(index, series.isBarComplete(index));
  }
}
