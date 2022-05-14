package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates and displays a smoothed moving average as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="SMMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_SMMA",
 label="LBL_SMMA",
 desc="DESC_SMMA",
 menu="MENU_MOVING_AVERAGE",
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/smoothed_moving_average.htm")
public class SMMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.SMMA;
    MA_LABEL = get("LBL_SMMA");
    super.initialize(defaults);
  }
}
