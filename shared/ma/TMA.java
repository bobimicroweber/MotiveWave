package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates and displays the Triangular Moving Average (SMA(SMA)) as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="TMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_TMA",
 label="LBL_TMA",
 desc="DESC_TMA",
 menu="MENU_MOVING_AVERAGE",
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/triangular_moving_average.htm")
public class TMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.TMA;
    MA_LABEL = get("LBL_TMA");
    super.initialize(defaults);
  }
}
