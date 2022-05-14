package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates and displays a triple exponential moving average as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="TEMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_TEMA",
 label="LBL_TEMA",
 desc="DESC_TEMA",
 menu="MENU_MOVING_AVERAGE",
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/triple_exponential_moving_average.htm")
public class TEMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.TEMA;
    MA_LABEL = get("LBL_TEMA");
    super.initialize(defaults);
  }
}
