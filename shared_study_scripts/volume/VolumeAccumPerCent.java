package com.motivewave.platform.study.volume;import java.awt.Color;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.desc.ColorDescriptor;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Volume Accumulator % 065 */@StudyHeader(    namespace = "com.motivewave",    id = "ID_VAPC",    rb = "com.motivewave.platform.study.nls.strings2",    name = "NAME_VOLUME_ACCUM_PERCENT",    label="LBL_VAPC",    desc = "DESC_VAPC",    //menu = "MENU_VOLUME_BASED",    helpLink = "http://www.motivewave.com/studies/volume_accum_percent.htm",    requiresVolume = true,    signals = false,    overlay = false,    studyOverlay = true)public class VolumeAccumPerCent extends Study{  enum Values { TVA, VAPC }    @Override  public void initialize(Defaults defaults)   {    var sd = createSD();    var tab = sd.addTab(get("TAB_GENERAL"));        var inputs = tab.addGroup(get("INPUTS"));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 21, 1, 999, 1));    var settings = tab.addGroup(get("COLORS"));    settings.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));    settings.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));        var guides = tab.addGroup(get("GUIDE"));    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -9999.1, 9999.1, .1, true);    mg.setDash(new float[] {3, 3});    guides.addRow(mg);    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 21, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.UP_COLOR, Inputs.DOWN_COLOR);    var desc = createRD();    desc.setLabelSettings(Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.VAPC, get("LBL_VAPC"), new String[] { Inputs.PERIOD }));    desc.declareBars(Values.VAPC);    desc.declareIndicator(Values.VAPC, Inputs.IND);    desc.setRangeKeys(Values.VAPC);  }  @Override  public void onLoad(Defaults defaults)  {    int p1 = getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1);  }  @Override  protected void calculate(int index, DataContext ctx)   {    int period = getSettings().getInteger(Inputs.PERIOD);    var series = ctx.getDataSeries();    double high = series.getHigh(index);    double low = series.getLow(index);    double close = series.getClose(index);    double volume =  series.getVolume(index);     double xT = 0.0;    double vapc = 0.0;        if (high - low != 0.0) xT = ((close - low) - (high - close)) / (high - low);    double tVa = volume * xT;    series.setDouble(index, Values.TVA, tVa);    if (index < period) return;         double tVol = series.sum(index, period, Enums.BarInput.VOLUME);    double tVaSum = series.sum(index, period, Values.TVA);    if (tVol != 0.0) vapc = (tVaSum / tVol) * 100;    series.setDouble(index, Values.VAPC, vapc);    var midGuide = getSettings().getGuide(Inputs.MIDDLE_GUIDE);    double midG = midGuide.getValue();    Color upC = getSettings().getColor(Inputs.UP_COLOR);    Color dnC = getSettings().getColor(Inputs.DOWN_COLOR);    if (vapc > midG) series.setBarColor(index, Values.VAPC, upC);    else series.setBarColor(index, Values.VAPC, dnC);    series.setComplete(index);  }}