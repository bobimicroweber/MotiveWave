package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates and displays an exponential moving average as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="EMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_EMA",
 label="LBL_EMA",
 desc="DESC_EMA",
 menu="MENU_MOVING_AVERAGE",
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/exponential_moving_average.htm")
public class EMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.EMA;
    MA_LABEL = get("LBL_EMA");
    super.initialize(defaults);
  }
}
