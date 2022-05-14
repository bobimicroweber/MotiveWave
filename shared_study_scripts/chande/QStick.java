package com.motivewave.platform.study.chande;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** QStick (Tuschar Chande) */
@StudyHeader(
    namespace="com.motivewave", 
    id="QSTICK", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_QSTICK",
    desc="DESC_QSTICK",
    menu="MENU_TUSCHARD_CHANDE",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/qstick.htm")
public class QStick extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { MA, DIFF }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    var path = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false);
    path.setSupportsShowAsBars(true);
    settings.addRow(path);
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.MA, get("TITLE_QSTICK"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.MA, Inputs.PATH);
    desc.declareIndicator(Values.MA, Inputs.IND);
    desc.setRangeKeys(Values.MA);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 0) return;
    var series = ctx.getDataSeries();
    double diff = series.getClose(index) - series.getOpen(index);
    series.setDouble(index, Values.DIFF, diff);
    
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period) return;
    
    Double ma = series.ma(getSettings().getMAMethod(Inputs.METHOD), index, period, Values.DIFF);
    if (ma == null) return;
    series.setDouble(index, Values.MA, ma);
    series.setComplete(index);
  }  
}
