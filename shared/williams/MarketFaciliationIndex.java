package com.motivewave.platform.study.williams;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/**
   Market Facilitation Index
 */
@StudyHeader(
    namespace="com.motivewave", 
    id="MKT_FAC_IND", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_MKT_FAC_IND",
    desc="DESC_MKT_FAC_IND",
    menu="MENU_BILL_WILLIAMS",
    overlay=false,
    helpLink="http://www.motivewave.com/studies/market_facilitation_index.htm")
public class MarketFaciliationIndex extends com.motivewave.platform.sdk.study.Study 
{
  final static String MFI_UP_VOL_UP = "mfiUpVolUpColor", MFI_DOWN_VOL_DOWN = "mfiDownVolDownColor", MFI_UP_VOL_DOWN = "mfiUpVolDownColor", 
      MFI_DOWN_VOL_UP = "mfiDownVolUpColor";
  
	enum Values { MFI };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var grp = tab.addGroup(get("LBL_COLORS"));
    grp.addRow(new ColorDescriptor(MFI_UP_VOL_UP, get("LBL_MFI_UP_VOL_UP"), defaults.getGreen()));
    grp.addRow(new ColorDescriptor(MFI_DOWN_VOL_DOWN, get("LBL_MFI_DOWN_VOL_DOWN"), defaults.getBrown()));
    grp.addRow(new ColorDescriptor(MFI_UP_VOL_DOWN, get("LBL_MFI_UP_VOL_DOWN"), defaults.getBlue()));
    grp.addRow(new ColorDescriptor(MFI_DOWN_VOL_UP, get("LBL_MFI_DOWN_VOL_UP"), defaults.getRed()));
    grp.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));

    sd.addQuickSettings(MFI_UP_VOL_UP, MFI_DOWN_VOL_DOWN, MFI_UP_VOL_DOWN, MFI_DOWN_VOL_UP);
    
    var desc = createRD();
    desc.setLabelSettings();
    desc.exportValue(new ValueDescriptor(Values.MFI, get("LBL_MFI"), new String[] {}));
    desc.declareBars(Values.MFI);
    desc.declareIndicator(Values.MFI, Inputs.IND);
    desc.setRangeKeys(Values.MFI);
    desc.setFixedBottomValue(0);
    desc.setBottomInsetPixels(0);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    double MFI = 0;
    var series = ctx.getDataSeries();
    long vol = series.getVolume(index);
    if (vol > 0) {
      MFI = (series.getHigh(index) - series.getLow(index)) * 1000.0 / vol;
    }
    series.setDouble(index, Values.MFI, MFI);

    if (index <= 1) return; // need the previous bar...

    Double pMFI = series.getDouble(index-1, Values.MFI);
    if (pMFI == null) return;
    long pVol = series.getVolume(index-1);
    
    Color c = getSettings().getColor(MFI_UP_VOL_UP);
    if (MFI < pMFI && vol < pVol) c = getSettings().getColor(MFI_DOWN_VOL_DOWN);
    else if (MFI > pMFI && vol < pVol) c = getSettings().getColor(MFI_UP_VOL_DOWN);
    else if (MFI < pMFI && vol > pVol) c = getSettings().getColor(MFI_DOWN_VOL_UP);
    series.setBarColor(index, Values.MFI, c);
    series.setComplete(index);
  }  
  
}
