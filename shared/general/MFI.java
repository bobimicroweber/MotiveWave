package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Money Flow Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="MFI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MFI",
    tabName="TAB_MFI",
    desc="DESC_MFI",
    menu="MENU_GENERAL",
    overlay=false,
    requiresVolume=true, 
    signals=true,
    helpLink="http://www.motivewave.com/studies/money_flow_index.htm")
public class MFI extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { MFI };
  enum Signals { MFI_TOP, MFI_BOTTOM };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup(get("LBL_INPUTS"));
    grp.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1));
    
    grp = tab.addGroup(get("LBL_COLORS"));
    grp.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null));
    grp.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, Inputs.PATH, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    grp.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, Inputs.PATH, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    grp.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    tab = sd.addTab(get("TAB_ADVANCED"));

    grp = tab.addGroup(get("LBL_GUIDES"));
    grp.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 80, 0, 100, 1, true));
    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    grp.addRow(mg);
    grp.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 20, 0, 100, 1, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD);
    desc.exportValue(new ValueDescriptor(Values.MFI, get("TAB_MFI"), new String[] {Inputs.PERIOD}));
    desc.declarePath(Values.MFI, Inputs.PATH);
    desc.declareIndicator(Values.MFI, Inputs.IND);
    desc.setRangeKeys(Values.MFI);
    desc.setMaxBottomValue(10);
    desc.setMinTopValue(90);
    desc.setMinTick(0.1);
    desc.declareSignal(Signals.MFI_TOP, get("LBL_MFI_TOP"));
    desc.declareSignal(Signals.MFI_BOTTOM, get("LBL_MFI_BOTTOM"));
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index < period) return;

    var series = ctx.getDataSeries();
    double PMF = 0; // positive money flow
    double NMF = 0; // negative money flow

    double pTP = series.getTypicalPrice(index-period);
    
    for(int i = index-period+1; i <= index; i++) {
      double TP = series.getTypicalPrice(i); 
      double MF = TP * series.getVolume(i); // Money Flow

      if (TP > pTP) PMF += MF;
      else NMF += MF;
      
      pTP = TP;
    }

    double MFI = 100 * ( PMF / (PMF + NMF) );
    series.setDouble(index, Values.MFI, MFI);
    
    if (!series.isBarComplete(index)) return;

    // Do we need to generate a signal?
    var topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    
    MFI = round(MFI);
    if (crossedAbove(series, index, Values.MFI, topGuide.getValue())) {
      ctx.signal(index, Signals.MFI_TOP, get("SIGNAL_MFI_TOP", MFI, topGuide.getValue()), MFI);
    }
    else if (crossedBelow(series, index, Values.MFI, bottomGuide.getValue())) {
      ctx.signal(index, Signals.MFI_BOTTOM, get("SIGNAL_MFI_BOTTOM", MFI, bottomGuide.getValue()), MFI);
    }
    series.setComplete(index);
  }  
}
