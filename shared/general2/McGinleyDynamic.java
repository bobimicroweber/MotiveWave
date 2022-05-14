package com.motivewave.platform.study.general2;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
  namespace="com.motivewave",
  id="ID_MGDI",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_MGDI",
  menu="MENU_GENERAL",
  name="NAME_MCGINLEY_DYNAMIC_INDICATOR",
  desc="DESC_MGDI",
  signals=false,
  overlay=true,
  studyOverlay=true)
public class McGinleyDynamic extends Study
{
  enum Values { MGDI }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));

    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 12, 1, 999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("SMOOTHING_FACTOR"), 125, 1, 999, 1));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.EMA));

    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 12, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("SMOOTHING_FACTOR"), 125, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.METHOD, Inputs.PATH);

    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.MGDI, get("LBL_MGDI"),
        new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2 }));
    desc.declarePath(Values.MGDI, Inputs.PATH);
    desc.declareIndicator(Values.MGDI, Inputs.IND);
    desc.setRangeKeys(Values.MGDI);
  }

  @Override
  public void onLoad(Defaults defaults)
  {
    int p1=getSettings().getInteger(Inputs.PERIOD);
    setMinBars(p1 + 1);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int pd=getSettings().getInteger(Inputs.PERIOD);
    int smoothing=getSettings().getInteger(Inputs.PERIOD2);

    if (index < pd + 1) return;
    var series=ctx.getDataSeries();
    var method=getSettings().getMAMethod(Inputs.METHOD);
    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);

    double price=series.getDouble(index, key, 0.0);
    // Ref(Mov(C,12,E),-1)+ ((C-(Ref(Mov(C,12,E),-1))) / (C/(Ref(Mov(C,12,E),-1))*125));

    double mgdi=series.ma(method, index - 1, pd, key) + ((price - (series.ma(method, index - 1, pd, key)))
        / (price / (series.ma(method, index - 1, pd, key)) * smoothing));

    series.setDouble(index, Values.MGDI, mgdi);
    series.setComplete(index, series.isBarComplete(index));
  }
}
