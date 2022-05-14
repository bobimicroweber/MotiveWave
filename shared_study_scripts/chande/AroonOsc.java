package com.motivewave.platform.study.chande;

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

/** Aroon Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="AROON_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_AROON_OSC",
    label="LBL_AROON_OSC",
    desc="DESC_AROON_OSC",
    menu="MENU_TUSCHARD_CHANDE",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/aroon_oscillator.htm")
public class AroonOsc extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { VAL }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 25, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    var line = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null);
    line.setSupportsShowAsBars(true);
    lines.addRow(line);
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));
    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 50, -100, 100, 1, true));
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), -50, -100, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 25, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.VAL, get("LBL_AROON_OSC"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.VAL, Inputs.PATH);
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.setRangeKeys(Values.VAL);
    desc.setMaxBottomValue(-90);
    desc.setMinTopValue(90);
    desc.setMinTick(0.1);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period-1) return;
    var series = ctx.getDataSeries();

    int high_ind = 0;
    int low_ind = 0;
    double high = Double.NEGATIVE_INFINITY;
    double low = Double.POSITIVE_INFINITY;

    // Find the periods for the highest high and lowest low
    // Note: There is room for optimization here.  We could cache the previous
    // value and find the high index from there.  Need to be careful, if the high_ind or low_ind 
    // is greater than the period, then need to discard.
    int n = period;
    for(int i = index-period+1; i <= index; i++) {
      double bhigh = series.getHigh(i), blow = series.getLow(i);
      if (bhigh >= high) {
        high = bhigh; high_ind = n;
      }
      if (blow <= low) {
        low = blow; low_ind = n;
      }
      n--;
    }

    high_ind--; low_ind--;
    double up = (((double)period - (double)high_ind)/period) * 100.0;
    double down = (((double)period - (double)low_ind)/period) * 100.0;
    series.setDouble(index, Values.VAL, up-down);
    series.setComplete(index);
  }  
}
