package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Volume Oscillator */
@StudyHeader(
    namespace="com.motivewave", 
    id="VOL_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_VOL_OSC",
    label="LBL_VOL_OSC",
    desc="DESC_VOL_OSC",
    menu="MENU_VOLUME",
    overlay=false,
    requiresVolume=true,
    helpLink="http://www.motivewave.com/studies/volume_oscillator.htm")
public class VolumeOsc extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { VAL }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 30, 1, 9999, 1));
    
    var settings = tab.addGroup(get("LBL_COLORS"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null, true, false, false));
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.PATH, 0, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.PATH, 0, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD2"), 30, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.VAL, get("VAL_VOL_OSC"), new String[] {Inputs.METHOD, Inputs.PERIOD, Inputs.PERIOD2}));
    desc.declarePath(Values.VAL, Inputs.PATH);
    desc.declareIndicator(Values.VAL, Inputs.IND);
    desc.setRangeKeys(Values.VAL);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period1 = getSettings().getInteger(Inputs.PERIOD);
    int period2 = getSettings().getInteger(Inputs.PERIOD2);
    int maxPeriod = Util.max(period1, period2);
    if (index < maxPeriod) return;
    
    var series = ctx.getDataSeries();
    var method = getSettings().getMAMethod(Inputs.METHOD);
    Double MA1 = series.ma(method, index, period1, Enums.BarInput.VOLUME);
    Double MA2 = series.ma(method, index, period2, Enums.BarInput.VOLUME);
    
    if (MA1 == null || MA2 == null) return;
    
    series.setDouble(index, Values.VAL, MA1 - MA2);
    series.setComplete(index);
  }  
}
