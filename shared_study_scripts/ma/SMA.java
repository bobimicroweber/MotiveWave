package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates and displays a simple moving average as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="SMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_SMA",
 label="LBL_SMA",
 desc="DESC_SMA",
 menu="MENU_MOVING_AVERAGE",
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/simple_moving_average.htm")
public class SMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.SMA;
    MA_LABEL = get("LBL_SMA");
    super.initialize(defaults);
  }
}
