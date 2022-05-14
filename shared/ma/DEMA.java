package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates and displays a double exponential moving average as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="DEMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_DEMA",
 label="LBL_DEMA",
 desc="DESC_DEMA",
 menu="MENU_MOVING_AVERAGE",
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/double_exponential_moving_average.htm")
public class DEMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.DEMA;
    MA_LABEL = get("LBL_DEMA");
    super.initialize(defaults);
  }
}
