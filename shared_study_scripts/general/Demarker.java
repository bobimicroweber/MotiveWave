package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** DeMarker Indicator */
@StudyHeader(
    namespace="com.motivewave", 
    id="DEMARKER", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_DEMARKER",
    desc="DESC_DEMARKER",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/demarker.htm")
public class Demarker extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { DM, DMIN, DMAX };
  enum Signals { DM_TOP, DM_BOTTOM };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_COLORS"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 70, 1, 100, 1, true));
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 30, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.DM, get("LBL_DM"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.DM, Inputs.PATH);
    desc.declareIndicator(Values.DM, Inputs.IND);
    desc.setRangeKeys(Values.DM);
    desc.setMaxBottomValue(15);
    desc.setMinTopValue(85);
    desc.setMinTick(0.1);
    desc.declareSignal(Signals.DM_TOP, get("LBL_DM_TOP"));
    desc.declareSignal(Signals.DM_BOTTOM, get("LBL_DM_BOTTOM"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 14);
    if (index < 1) return;
    var series = ctx.getDataSeries();
    
    double dmax = 0;
    double dmin = 0;
    if (series.getHigh(index) > series.getHigh(index-1)) dmax = series.getHigh(index) - series.getHigh(index-1);
    if (series.getLow(index) < series.getLow(index-1)) dmin = series.getLow(index-1) - series.getLow(index);
    
    series.setDouble(index,  Values.DMAX, dmax);
    series.setDouble(index,  Values.DMIN, dmin);
    
    if (index < period+1) return;

    Double maxMA = series.sma(index,  period, Values.DMAX);
    Double minMA= series.sma(index,  period, Values.DMIN);
    if (maxMA == null || minMA == null) return;
    Double demark = (maxMA / (maxMA + minMA))*100;
    
    series.setDouble(index, Values.DM, demark);
    series.setComplete(index);

    if (!series.isBarComplete(index)) return;

    // Do we need to generate a signal?
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    
    demark = round(demark);
    if (crossedAbove(series, index, Values.DM, topGuide.getValue())) {
      ctx.signal(index, Signals.DM_TOP, get("SIGNAL_DM_TOP", demark, topGuide.getValue()), demark);
    }
    else if (crossedBelow(series, index, Values.DM, bottomGuide.getValue())) {
      ctx.signal(index, Signals.DM_BOTTOM, get("SIGNAL_DM_BOTTOM", demark, bottomGuide.getValue()), demark);
    }
  }  
}
