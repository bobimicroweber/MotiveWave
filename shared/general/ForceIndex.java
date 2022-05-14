package com.motivewave.platform.study.general;

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

/** Force Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="FI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_FI",
    desc="DESC_FI",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/force_index.htm")
public class ForceIndex extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { MA, FI };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 13, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    var line = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false);
    line.setSupportsShowAsBars(true);
    settings.addRow(line);
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 13, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.MA, get("LBL_FI"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.MA, Inputs.PATH);
    desc.declareIndicator(Values.MA, Inputs.IND);
    desc.setRangeKeys(Values.MA);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 13);
    if (index < 1) return;
    var series = ctx.getDataSeries();

    double FI = (series.getClose(index) - series.getClose(index-1)) * series.getVolume(index); // raw force index
    
    series.setDouble(index, Values.FI, FI);
    if (index < period+1) return;

    series.setDouble(index, Values.MA, series.ema(index, period, Values.FI));
    series.setComplete(index);
  }  
}
