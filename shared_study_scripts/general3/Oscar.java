package com.motivewave.platform.study.general3;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Oscar 187 */@StudyHeader(  namespace="com.motivewave",  id="ID_OSCAR",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_OSCAR",  name="NAME_OSCAR",  desc="DESC_OSCAR",  helpLink="http://www.motivewave.com/studies/oscar.htm",  overlay=false,  studyOverlay=true)public class Oscar extends Study{  enum Values { OSCAR }  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 8, 1, 999, 1));    var settings=tab.addGroup(get("PATH_INDICATOR"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("INDICATOR"), defaults.getLineColor(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var guides=tab.addGroup(get("GUIDES"));    var topDesc=new GuideDescriptor(Inputs.TOP_GUIDE, get("TOP_GUIDE"), 70, 0, 999.1, .1, true);    topDesc.setLineColor(defaults.getRed());    guides.addRow(topDesc);    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 50, 0, 999.1, .1, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    var bottomDesc=new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("BOTTOM_GUIDE"), 30, 0, 999.1, .1, true);    bottomDesc.setLineColor(defaults.getGreen());    guides.addRow(bottomDesc);    settings=tab.addGroup(get("SHADING"));    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 8, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.OSCAR, get("LBL_OSCAR"), new String[] {Inputs.INPUT,Inputs.PERIOD}));    desc.declarePath(Values.OSCAR, Inputs.PATH);    desc.declareIndicator(Values.OSCAR, Inputs.IND);    desc.setRangeKeys(Values.OSCAR);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int period=getSettings().getInteger(Inputs.PERIOD);    if (index < period) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);    var series=ctx.getDataSeries();    double price = series.getDouble(index, key, 0);    double lowest = series.lowest(index, period, Enums.BarInput.LOW);    double highest = series.highest(index, period, Enums.BarInput.HIGH);    double rough = ((price - lowest) / (highest - lowest)) * 100.0;    double prevOscar = series.getDouble(index-1, Values.OSCAR, 0);    double oscar = ((prevOscar / 3.0) * 2.0) + (rough / 3.0);    series.setDouble(index, Values.OSCAR, oscar);        series.setComplete(index);  }}