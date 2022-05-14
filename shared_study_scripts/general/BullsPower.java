package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Bulls Power. Developed by Alexander Elder */
@StudyHeader(
    namespace="com.motivewave", 
    id="BULLS_POWER", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_BULLS_POWER",
    label="LBL_BULLS_POWER",
    desc="DESC_BULLS_POWER",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/bulls_power.htm")
public class BullsPower extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { BP };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.MIDPOINT));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    var line = new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false);
    line.setSupportsShowAsBars(true);
    settings.addRow(line);
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 13, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.BP, get("LBL_BULLS_POWER"), new String[] {Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD}));
    desc.declarePath(Values.BP, Inputs.PATH);
    desc.declareIndicator(Values.BP, Inputs.IND);
    desc.setRangeKeys(Values.BP);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD, 10);
    if (index < period) return;

    Object input = getSettings().getInput(Inputs.INPUT, Enums.BarInput.MIDPOINT);
    Enums.MAMethod method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.EMA);
    DataSeries series = ctx.getDataSeries();

    double high = series.getHigh(index);
    Double ma = series.ma(method, index, period, input); 
    if (ma == null) return;
    
    series.setDouble(index, Values.BP, high - ma);
    series.setComplete(index);
  }
}
