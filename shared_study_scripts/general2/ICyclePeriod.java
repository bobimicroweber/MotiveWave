package com.motivewave.platform.study.general2;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.DataSeries;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Instantaneous Cycle Period Measurement John F. Ehlers 174 */@StudyHeader(  namespace="com.motivewave",  id="ID_ICYCLE_PERIOD",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_ICPM",  name="NAME_INSTANTANEOUS_CYCLE_PERIOD_MEASURMENT",  desc="DESC_ICPM",  menu="MENU_JOHN_EHLERS",  helpLink="http://www.motivewave.com/studies/instantaneous_cycle_period_measurment.htm",  signals=false,  overlay=false,  studyOverlay=true)public class ICyclePeriod extends Study{  final static double iMult=.635;  final static double qMult=.338;  enum Values { V1, INPHASE, QUAD, PHASE, DELTAPHASE, INSTPERIOD, PERIOD }  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.MIDPOINT));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("HILBERT_TRANSFORM_PERIOD"), 7, 7, 9999, 1));    var settings=tab.addGroup(get("PATH_INDICATOR"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("CYCLE_PERIOD"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("INDICATOR"), defaults.getLineColor(), null, false, true, true));    var guides=tab.addGroup(get("GUIDE"));    GuideDescriptor mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, 0, 999.01, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    settings=tab.addGroup(get("SHADING"));    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.INPUT);    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("HILBERT_TRANSFORM_PERIOD"), 7, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);    var desc=createRD();    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.PERIOD, get("HILBERT_TRANSFORM_PERIOD"), new String[] { Inputs.INPUT, Inputs.PERIOD }));    desc.declarePath(Values.PERIOD, Inputs.PATH);    desc.declareIndicator(Values.PERIOD, Inputs.IND);    desc.setRangeKeys(Values.PERIOD);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1 + 56);  }  @Override  protected void calculate(int index, DataContext ctx)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    if (index < p1) return;    Object key=getSettings().getInput(Inputs.INPUT, Enums.BarInput.MIDPOINT);    DataSeries series=ctx.getDataSeries();    double phase=0;    double price=series.getDouble(index, key, 0);    double priorP=series.getDouble(index - p1, key, 0);    // detrend price    double v1=price - priorP;    series.setDouble(index, Values.V1, v1);    if (index < p1 + 4) return;    // Calculate Hilbert Transform values inPhase(real part), quadrature(imaginary part)    double vMinus2=series.getDouble(index - 2, Values.V1, 0);    double vMinus4=series.getDouble(index - 4, Values.V1, 0);    double inPhase3=series.getDouble(index - 3, Values.INPHASE, 0);    double quad2=series.getDouble(index - 2, Values.QUAD, 0);    double inPhase=1.25 * (vMinus4 - (iMult * vMinus2) + (iMult * inPhase3));    double quad=vMinus2 - (qMult * v1) + (qMult * quad2);    series.setDouble(index, Values.INPHASE, inPhase);    series.setDouble(index, Values.QUAD, quad);    if (index < p1 + 5) return;    double prevInPhase=series.getDouble(index - 1, Values.INPHASE, 0);    double prevQuad=series.getDouble(index - 1, Values.QUAD, 0);    if (Math.abs(inPhase + prevInPhase) > 0) {      phase=Math.atan(Math.abs((quad + prevQuad) / (inPhase + prevInPhase)));    }    if (inPhase < 0 && quad > 0) phase=180 - phase;    if (inPhase < 0 && quad < 0) phase=180 + phase;    if (inPhase > 0 && quad < 0) phase=360 - phase;    series.setDouble(index, Values.PHASE, phase);    if (index < p1 + 6) return;    double prevPhase=series.getDouble(index - 1, Values.PHASE, 0);    double deltaPhase=prevPhase - phase;    if (prevPhase < 90 && phase > 270) deltaPhase=360 + prevPhase - phase;    if (deltaPhase < 1) deltaPhase=1;    if (deltaPhase > 60) deltaPhase=60;    series.setDouble(index, Values.DELTAPHASE, deltaPhase);    if (index < p1 + 55) return;    int instPeriod=0;    double v4=0, priorDP=0;    for (int count=0; count <= 50; count++) {      priorDP=series.getDouble(index - count, Values.DELTAPHASE, 0);      v4=v4 + priorDP;      if (v4 > 360 && instPeriod == 0) instPeriod=count;    }    series.setInt(index, Values.INSTPERIOD, instPeriod);    if (index < p1 + 56) return;    if (instPeriod == 0) {      instPeriod=series.getInt(index - 1, Values.INSTPERIOD);    }    double prevPeriod=series.getDouble(index - 1, Values.PERIOD, 0);    double period=(.25 * instPeriod) + (.75 * prevPeriod);    series.setDouble(index, Values.PERIOD, period);    series.setComplete(index);  }}