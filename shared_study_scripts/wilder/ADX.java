  package com.motivewave.platform.study.wilder;

import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Average Directional Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="ADX", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ADX",
    label="LBL_ADX",
    desc="DESC_ADX",
    menu="MENU_WELLES_WILDER",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/average_directional_index.htm")
public class ADX extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { ADX, PDI, NDI, DX, PDM, NDM, TR, HIST }
  
	final static String ADX_LINE = "adxLine", PDI_LINE = "pdiLine", NDI_LINE = "ndiLine", HISTOGRAM = "histogram", 
	    ADX_IND = "adxInd", PDI_IND = "pdiInd", NDI_IND = "ndiInd", HIST_IND = "histInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    var adx = new PathDescriptor(ADX_LINE, get("LBL_ADX_LINE"), defaults.getLineColor(), 1.0f, null, true, false, true);
    adx.setSupportsShowAsBars(true);
    lines.addRow(adx);
    var pdi = new PathDescriptor(PDI_LINE, get("LBL_PDI_LINE"), defaults.getGreen(), 1.0f, null, true, false, true);
    pdi.setSupportsShowAsBars(true);
    lines.addRow(pdi);
    var ndi = new PathDescriptor(NDI_LINE, get("LBL_NDI_LINE"), defaults.getRed(), 1.0f, null, true, false, true);
    ndi.setSupportsShowAsBars(true);
    lines.addRow(ndi);
    var histogram = new PathDescriptor(HISTOGRAM, get("LBL_HISTOGRAM"), defaults.getGreen(), 1.0f, null, false, false, true);
    histogram.setColorPolicy(Enums.ColorPolicy.POSITIVE_NEGATIVE);
    histogram.setColor2(defaults.getRed());
    histogram.setSupportsShowAsBars(true);
    histogram.setShowAsBars(true);
    histogram.setColorPolicies(Enums.ColorPolicy.values());
    lines.addRow(histogram);

    tab = sd.addTab(get("TAB_ADVANCED"));

    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(ADX_IND, get("LBL_ADX_IND"), null, null, false, true, true));
    indicators.addRow(new IndicatorDescriptor(PDI_IND, get("LBL_PDI_IND"), defaults.getGreen(), null, false, false, true));
    indicators.addRow(new IndicatorDescriptor(NDI_IND, get("LBL_NDI_IND"), defaults.getRed(), null, false, false, true));
    indicators.addRow(new IndicatorDescriptor(HIST_IND, get("LBL_HIST_IND"), null, null, false, false, true));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 40, 1, 100, 1, true));
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 20, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(ADX_LINE, PDI_LINE, NDI_LINE, HISTOGRAM);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.ADX, get("LBL_ADX"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.PDI, get("LBL_PDI"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.NDI, get("LBL_NDI"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.HIST, get("LBL_HISTOGRAM"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.ADX, ADX_LINE);
    desc.declarePath(Values.PDI, PDI_LINE);
    desc.declarePath(Values.NDI, NDI_LINE);
    desc.declarePath(Values.HIST, HISTOGRAM);
    desc.declareIndicator(Values.ADX, ADX_IND);
    desc.declareIndicator(Values.PDI, PDI_IND);
    desc.declareIndicator(Values.NDI, NDI_IND);
    desc.declareIndicator(Values.HIST, HIST_IND);
    //desc.setFixedBottomValue(0);
    desc.setRangeKeys(Values.ADX, Values.PDI, Values.NDI, Values.HIST);
  }

  @Override
  public void onLoad(Defaults defaults)
  {
    updateRangeKeys();
    // Since it uses an SMMA, its better to have more data so its not so sensitive to scrolling back....
    setMinBars(getSettings().getInteger(Inputs.PERIOD, 55)*4);
    super.onLoad(defaults);
  }

  @Override
  public void onSettingsUpdated(DataContext ctx)
  {
    updateRangeKeys();
    // Since it uses an SMMA, its better to have more data so its not so sensitive to scrolling back....
    setMinBars(getSettings().getInteger(Inputs.PERIOD, 55)*4);
    super.onSettingsUpdated(ctx);
  }
  
  private void updateRangeKeys()
  {
    var desc = getRuntimeDescriptor();
    List<Object> keys = new ArrayList<>();
    if (getSettings().getPath(ADX_LINE).isEnabled()) keys.add(Values.ADX);
    if (getSettings().getPath(PDI_LINE).isEnabled()) keys.add(Values.PDI);
    if (getSettings().getPath(NDI_LINE).isEnabled()) keys.add(Values.NDI);
    if (getSettings().getPath(HISTOGRAM).isEnabled()) keys.add(Values.HIST);
    desc.setRangeKeys(keys.toArray());
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < 1) return; // not enough data
    var series = ctx.getDataSeries();
    if (series == null) return;
    
    // Calculate the +DM, -DM and TR
    Float pDM = series.getPositiveDM(index);
    Float nDM = series.getNegativeDM(index);
    Float tr = series.getTrueRange(index);
    
    series.setFloat(index, Values.PDM, pDM);
    series.setFloat(index, Values.NDM, nDM);
    series.setFloat(index, Values.TR, tr);
    
    if (index <= period) return; // not enough data to calculate the first set of averages
    
    // Calculate the Average +DM, -DM and TR
    Double PDMa = series.smma(index, period, Values.PDM);
    Double NDMa = series.smma(index, period, Values.NDM);
    Double TRa = series.smma(index, period, Values.TR);
    
    if (PDMa == null ||NDMa == null || TRa == null) return;

    // Determine the +DI, -DI and DX
    double pDI = PDMa / TRa * 100;
    double nDI = NDMa / TRa * 100;
    double DX = Math.abs((PDMa - NDMa)) / (PDMa + NDMa) * 100;
    
    series.setDouble(index, Values.DX, DX);
    series.setDouble(index, Values.PDI, pDI);
    series.setDouble(index, Values.NDI, nDI);
    var hist = getSettings().getPath(HISTOGRAM);
    if (hist != null && hist.isEnabled()) {
      series.setDouble(index, Values.HIST, pDI - nDI);
    }

    if (index < period*2) return; // not enough data to calculate the ADX

    // Calculate the Average DX
    Double ADX = series.smma(index, period, Values.DX);
    if (ADX == null) return;

    series.setDouble(index, Values.ADX, ADX);
    series.setComplete(index);
  }
}
