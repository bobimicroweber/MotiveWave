package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates the volume weighted moving average and displays it as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="VWMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_VWMA",
 label="LBL_VWMA",
 desc="DESC_VWMA",
 menu="MENU_MOVING_AVERAGE",
 menu2="MENU_VOLUME",
 requiresVolume=true,
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/volume_weighted_moving_average.htm")
public class VWMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.VWMA;
    MA_LABEL = get("LBL_VWMA");
    super.initialize(defaults);
  }
}
