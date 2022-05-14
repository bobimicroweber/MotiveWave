package com.motivewave.platform.study.general2;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.DataSeries;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Hilbert Transform John F. Ehlers 172 */@StudyHeader(  namespace="com.motivewave",  id="ID_HILBERT_TRANSFORM",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_HILBT",  name="NAME_HILBERT_TRANSFORM_INDICATOR",  desc="DESC_HILBT",  menu="MENU_JOHN_EHLERS",  helpLink="http://www.motivewave.com/studies/hilbert_transform_indicator.htm",  signals=false,  overlay=false,  studyOverlay=true)public class HilbertTransform extends Study{  final static double iMult=.635;  final static double qMult=.338;  enum Values { V1, INPHASE, QUAD }  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.MIDPOINT));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 7, 7, 9999, 1));    var settings=tab.addGroup(get("PATHS"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("INPHASE"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new PathDescriptor(Inputs.PATH2, get("QUADRATURE"), defaults.getRed(), 1.0f, null, true, false, true));    settings=tab.addGroup(get("INDICATORS"));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("INPHASE"), defaults.getLineColor(), null, false, true, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND2, get("QUADRATURE"), defaults.getRed(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var guides=tab.addGroup(get("GUIDE"));    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -999.01, 999.01, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    settings=tab.addGroup(get("SHADING"));    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 7, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.PATH, Inputs.PATH2, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.INPHASE, get("INPHASE"), new String[] { Inputs.INPUT, Inputs.PERIOD }));    desc.declarePath(Values.INPHASE, Inputs.PATH);    desc.declarePath(Values.QUAD, Inputs.PATH2);    desc.declareIndicator(Values.INPHASE, Inputs.IND);    desc.declareIndicator(Values.QUAD, Inputs.IND2);    desc.setRangeKeys(Values.INPHASE, Values.QUAD);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1 + 4);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    if (index < p1) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.MIDPOINT);    DataSeries series=ctx.getDataSeries();    double price=series.getDouble(index, key, 0);    double priorP=series.getDouble(index - p1, key, 0);    // v1 = Detrend price    double v1=price - priorP;    series.setDouble(index, Values.V1, v1);    if (index < p1 + 4) return;    double v2=series.getDouble(index - 2, Values.V1, 0);    double v4=series.getDouble(index - 4, Values.V1, 0);    double inPhase3=series.getDouble(index - 3, Values.INPHASE, 0);    double quad2=series.getDouble(index - 2, Values.QUAD, 0);    // Hilbert transform complex number components, inPhase (real part), quad (imaginary part)    double inPhase=1.25 * (v4 - (iMult * v2) + (iMult * inPhase3));    double quad=v2 - (qMult * v1) + (qMult * quad2);    series.setDouble(index, Values.INPHASE, inPhase);    series.setDouble(index, Values.QUAD, quad);    series.setComplete(index);  }}