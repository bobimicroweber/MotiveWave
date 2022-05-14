package com.motivewave.platform.study.general;

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
import com.motivewave.platform.sdk.study.StudyHeader;

/** Bar Time Histogram
    Displays the bar time in seconds as a histogram.  This is intended to be used for non-linear type bars.
    Optionally, a moving average may be displayed with the histogram. */
@StudyHeader(
    namespace="com.motivewave", 
    id="BAR_TIME_HISTOGRAM", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_BAR_TIME_HISTOGRAM",
    label="LBL_BAR_TIME_HISTOGRAM",
    desc="DESC_BAR_TIME_HISTOGRAM",
    menu="MENU_GENERAL",
    overlay=false)
public class BarTimeHistogram extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { BAR_TIME, MA };
  final static String MA_PATH = "maPath";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_SETTINGS"));
    var histogram = new PathDescriptor(Inputs.BAR, get("LBL_BAR_COLOR"), defaults.getBarColor(), 1.0f, null, true, false, true);
    histogram.setShowAsBars(true);
    histogram.setSupportsShowAsBars(true);
    histogram.setColorPolicies(Enums.ColorPolicy.values());
    lines.addRow(histogram);
    //lines.addRow(new BarDescriptor(Inputs.BAR, get("LBL_BAR_COLOR"), defaults.getBarColor(), true, false));
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), defaults.getBarColor(), null, false, false, true));
    lines.addRow(new PathDescriptor(MA_PATH, get("LBL_MA_LINE"), defaults.getLineColor(), 1.0f, null, true, false, true));
    lines.addRow(new IndicatorDescriptor(Inputs.IND2, get("LBL_MA_IND"), defaults.getLineColor(), null, false, false, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.BAR);

    var desc = createRD();
    desc.setLabelSettings(Inputs.METHOD, Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.BAR_TIME, get("LBL_BAR_TIME"), new String[] {}));
    desc.exportValue(new ValueDescriptor(Values.MA, get("LBL_MA"), new String[] {Inputs.METHOD, Inputs.PERIOD}));
    desc.declarePath(Values.BAR_TIME, Inputs.BAR);
    desc.declarePath(Values.MA, MA_PATH);
    desc.declareIndicator(Values.BAR_TIME, Inputs.IND);
    desc.declareIndicator(Values.MA, Inputs.IND2);
    desc.setRangeKeys(Values.BAR_TIME);
    desc.setFixedBottomValue(0);
    desc.setBottomInsetPixels(0);
    desc.setMinTopValue(10);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    // Store the bar time
    var series = ctx.getDataSeries();
    // Calculate the bar time in seconds
    int sec = (int)((series.getEndTime(index) - series.getStartTime(index))/1000);
    series.setInt(index, Values.BAR_TIME, sec);
    
    int period = getSettings().getInteger(Inputs.PERIOD, 12);
    if (index > period) {
      var method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA);
      Double MA = series.ma(method, index, period, Values.BAR_TIME);
      if (MA != null) series.setInt(index, Values.MA, MA.intValue());
    }
    
    series.setComplete(index, series.isBarComplete(index));
  }
}
