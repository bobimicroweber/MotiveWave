package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.Icon;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.BarDescriptor;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Commodity Channel Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="CCI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_CCI",
    label="LBL_CCI",
    desc="DESC_CCI",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/commodity_channel_index.htm")
public class CCI extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { CCI };
  enum Signals { CCI_TOP, CCI_BOTTOM };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_COLORS"));
    lines.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, true));
    lines.addRow(new BarDescriptor(Inputs.BAR, get("LBL_HISTOGRAM"), defaults.getBarColor(), false, true));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));
    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 100, -9999, 9999, 1, true));
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), -100, -9999, 9999, 1, true));
    var centerGuide = new GuideDescriptor(Inputs.CENTER_GUIDE, get("LBL_CENTER_GUIDE"), 0, -9999, 9999, 1, true);
    centerGuide.setDash(new float[] {3f, 3f});
    guides.addRow(centerGuide);

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.BAR, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setMinTick(0.1);
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.CCI, get("LBL_CCI"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.CCI, Inputs.PATH);
    desc.declareBars(Values.CCI, Inputs.BAR);
    desc.declareIndicator(Values.CCI, Inputs.IND);
    desc.setRangeKeys(Values.CCI);
    desc.declareSignal(Signals.CCI_TOP, get("LBL_CCI_TOP"));
    desc.declareSignal(Signals.CCI_BOTTOM, get("LBL_CCI_BOTTOM"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 20);
    if (index < period) return;
    var series = ctx.getDataSeries();
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    var topShade = getSettings().getShade(Inputs.TOP_FILL);
    var bottomShade = getSettings().getShade(Inputs.BOTTOM_FILL);

    // calculate the typical price(TP) for the last period+1 bars
    double TP[] = new double[period];
    int j = 0;
    double sum = 0;
    for(int i = index-period+1; i <= index; i++) {
      TP[j] = series.getTypicalPrice(i);
      sum += TP[j];
      j++;
    }

    double SMATP = sum / period;      

    // Calculate the Mean Deviation
    sum = 0;
    for(int i = 0; i < TP.length; i++) {
      sum += Math.abs(TP[i]-SMATP);
    }
    
    double MD = sum / period;
    double CCI = (TP[period-1] - SMATP) / (0.015*MD);
    series.setDouble(index, Values.CCI, CCI);
    
    if (topShade.isEnabled() && CCI >= topGuide.getValue()) {
      series.setBarColor(index, Values.CCI, topShade.getColor());
    }
    else if (bottomShade.isEnabled() && CCI <= bottomGuide.getValue()) {
      series.setBarColor(index, Values.CCI, bottomShade.getColor());
    }
    else {
      series.setBarColor(index, Values.CCI, null);
    }
    
    if (!series.isBarComplete(index)) return;
    
    // Do we need to generate a signal?
    CCI = round(CCI);
    if (crossedAbove(series, index, Values.CCI, topGuide.getValue())) {
      ctx.signal(index, Signals.CCI_TOP, get("SIGNAL_CCI_TOP", CCI, topGuide.getValue()), CCI);
    }
    else if (crossedBelow(series, index, Values.CCI, bottomGuide.getValue())) {
      ctx.signal(index, Signals.CCI_BOTTOM, get("SIGNAL_CCI_BOTTOM", CCI, bottomGuide.getValue()), CCI);
    }

    series.setComplete(index);
  }  
}
