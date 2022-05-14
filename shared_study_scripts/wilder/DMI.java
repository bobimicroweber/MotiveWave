  package com.motivewave.platform.study.wilder;

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

/** Directional Movement Indicator */
@StudyHeader(
    namespace="com.motivewave", 
    id="DMI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_DMI",
    label="LBL_DMI",
    desc="DESC_DMI",
    menu="MENU_WELLES_WILDER",
    overlay=false,
    studyOverlay=true,
    helpLink="http://www.motivewave.com/studies/directional_movement_indicator.htm")
public class DMI extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { PDI, NDI, DX, PDM, NDM, TR };
  
  final static String PDI_LINE = "pdiLine";
  final static String NDI_LINE = "ndiLine";
  
  final static String PDI_IND = "pdiInd";
  final static String NDI_IND = "ndiInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    
    var lines = tab.addGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(PDI_LINE, get("LBL_PDI_LINE"), defaults.getGreen(), 1.0f, null, true, false, true));
    lines.addRow(new PathDescriptor(NDI_LINE, get("LBL_NDI_LINE"), defaults.getRed(), 1.0f, null, true, false, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    var indicators = tab.addGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(PDI_IND, get("LBL_PDI_IND"), defaults.getGreen(), null, false, true, true));
    indicators.addRow(new IndicatorDescriptor(NDI_IND, get("LBL_NDI_IND"), defaults.getRed(), null, false, true, true));

    var guides = tab.addGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 40, 1, 100, 1, true));
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 20, 1, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(PDI_LINE, NDI_LINE);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.PDI, get("LBL_PDI"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.NDI, get("LBL_NDI"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.PDI, PDI_LINE);
    desc.declarePath(Values.NDI, NDI_LINE);
    desc.declareIndicator(Values.PDI, PDI_IND);
    desc.declareIndicator(Values.NDI, NDI_IND);
    desc.setFixedBottomValue(0);
    desc.setRangeKeys(Values.PDI, Values.NDI);
    
    setRuntimeDescriptor(desc);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < 1) return; // not enough data
    var series = ctx.getDataSeries();
    
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

    // Determine the +DI, -DI and DX
    double pDI = PDMa / TRa * 100;
    double nDI = NDMa / TRa * 100;
    double DX = Math.abs((PDMa - NDMa)) / (PDMa + NDMa) * 100;
    
    series.setDouble(index, Values.DX, DX);
    series.setDouble(index, Values.PDI, pDI);
    series.setDouble(index, Values.NDI, nDI);
    series.setComplete(index);
  }
}
