package study;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
        namespace = "com.cloudvisionltd",
        id = "TITLE_SUPPORT_RESISTANCE_STRATEGY",
        name = "Support & Resistance Strategy",
        desc = "Catch BIG Volume",
        menu = "A4Crypto Indicators",
        overlay = true,
        signals = true,
        strategy = true,
        autoEntry = true,
        manualEntry = false,
        supportsUnrealizedPL = true,
        supportsRealizedPL = true,
        supportsTotalPL = true)

public class SupportResistanceStrategy extends SupportResistance {

  @Override
  public void onActivate(OrderContext ctx) {
    if (getSettings().isEnterOnActivate()) {


    }
  }

  @Override
  public void onSignal(OrderContext ctx, Object signal) {


  }
}
