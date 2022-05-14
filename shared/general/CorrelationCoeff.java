package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.InstrumentDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Correlation Coefficient  202 */
@StudyHeader(
  namespace="com.motivewave",
  id="ID_CORR_COEFF",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_CC",
  name="NAME_CORRELATION_COEFF",
  desc="DESC_CORR_COEFF",
  helpLink= "",  //"http://www.motivewave.com/studies/correlation_coeff.htm",
  signals=false,
  overlay=false,
  multipleInstrument=true,
  studyOverlay=true)
public class CorrelationCoeff extends Study 
{
  enum Values { CC };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));

    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new InstrumentDescriptor(Inputs.INSTRUMENT1, get("INSTRUMENT1"), true, true));
    inputs.addRow(new InstrumentDescriptor(Inputs.INSTRUMENT2, get("INSTRUMENT2")));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 10, 1, 999, 1));

    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, false, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    tab=sd.addTab(get("DISPLAY"));

    var guides=tab.addGroup(get("GUIDES"));
    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), -1, 0, 1, .1, true);
    mg.setDash(new float[] { 3, 3 });
    guides.addRow(mg);

    settings=tab.addGroup(get("SHADING"));
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH);

    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.INSTRUMENT1, Inputs.INSTRUMENT2);
    desc.exportValue(new ValueDescriptor(Values.CC, get("LBL_CC"), new String[] { Inputs.INPUT, Inputs.PERIOD}));
    desc.declarePath(Values.CC, Inputs.PATH);
    desc.declareIndicator(Values.CC, Inputs.IND);
    desc.setRangeKeys(Values.CC);
    //desc.getDefaultPlot().setFormatMK(false);
    desc.setBottomInsetPixels(10);
    desc.setTopInsetPixels(10);
  }

  @Override
  public void onLoad(Defaults defaults) 
  {
    setMinBars(getSettings().getInteger(Inputs.PERIOD));
  }

  @Override
  protected void calculate(int index, DataContext ctx) 
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index <= period) return;
    
    var input = (Enums.BarInput)getSettings().getInput(Inputs.INPUT);
    var instr1 = getSettings().getInstrument(Inputs.INSTRUMENT1);
    if (instr1 == null) instr1 = ctx.getInstrument();  
    var instr2 = getSettings().getInstrument(Inputs.INSTRUMENT2);
    if (instr2 == null) instr2 = ctx.getInstrument();  

    var series=ctx.getDataSeries();
    float v1Sq = 0f, v2Sq = 0f, v1V2 = 0f;
    float totV1 = 0f, totV2 = 0f, totV1Sq = 0f, totV2Sq = 0f, totV1V2 = 0f;

    for (int i = index - period + 1; i <= index; i++) {
      Float v1 = series.getFloat(i, input, instr1);
      Float v2 = series.getFloat(i, input, instr2);
      if (v1 == null || v2 == null) return;
      v1Sq = v1 * v1;
      v2Sq = v2 * v2;
      v1V2 = v1 * v2;
      
      totV1 = totV1 + v1;
      totV2 = totV2 + v2;
      totV1Sq = totV1Sq + v1Sq;
      totV2Sq = totV2Sq + v2Sq;
      totV1V2 = totV1V2 + v1V2;
    }
    float avgV1 = totV1 / period;
    float avgV2 = totV2 / period;
    float avgV1Sq = totV1Sq / period;
    float avgV2Sq = totV2Sq / period;
    float avgV1V2 = totV1V2 / period;
    
    float varInst1 = avgV1Sq - (avgV1 * avgV1);
    if (varInst1 <= 0f) return;
    float varInst2 = avgV2Sq - (avgV2 * avgV2);
    if (varInst2 <= 0f) return;

    float covar = avgV1V2 - (avgV1 * avgV2);
    if (covar == 0f) return;

    Float cc = (float) (covar / Math.sqrt(varInst1 * varInst2));
    series.setFloat(index, Values.CC, cc);
    
    series.setComplete(index, series.isBarComplete(index));
  }
}