package study;

import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
        namespace = "com.cloudvisionltd",
        id = "BIG_VOLUME_STRATEGY",
        name = "Big Volume Strategy",
        desc = "Catch big volume",
        menu = "A4Crypto.com",
        overlay = true,
        signals = true,
        strategy = true,
        autoEntry = true,
        manualEntry = false,
        supportsUnrealizedPL = true,
        supportsRealizedPL = true,
        supportsTotalPL = true)

public class BigVolumeStrategy extends Study {
    @Override
    public void onActivate(OrderContext ctx) {
        if (getSettings().isEnterOnActivate()) {

            DataSeries series = ctx.getDataContext().getDataSeries();




        }
    }

    @Override
    public void onSignal(OrderContext ctx, Object signal) {


    }
}
