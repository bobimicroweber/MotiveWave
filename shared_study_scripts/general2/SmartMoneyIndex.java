/*
 * Link: http://en.wikipedia.org/wiki/Smart_money_index
 * The basic formula for SMI is:
  Today's SMI reading = yesterday's SMI - opening gain or loss + last hour change

For example, the SMI closed yesterday at 10000. During the first 30 minutes of today's trading,
 the DJIA has gained a total of 100 points. During the final hour, the DJIA has lost 80 points. 
 So, today's SMI is 10000 - 100 + -80 = 9820.

Interpretation[edit]
The SMI sends no clear signal whether the market is bullish or bearish. There are also no fixed
 absolute or relative readings signaling about the trend. Traders need to look at the SMI dynamics
 relative to that of the market. If, for example, SMI rises sharply when the market falls, this fact
 would mean that smart money is buying, and the market is to revert to an uptrend soon. The opposite
 situation is also true. A rapidly falling SMI during a bullish market means that smart money is
 selling and that market is to revert to a downtrend soon. The SMI is, therefore, a trend-based indicator.
 */
package com.motivewave.platform.study.general2;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
  namespace="com.motivewave",
  id="ID_SMI",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_SMI",
  menu="MENU_GENERAL",
  name="NAME_SMART_MONEY_INDEX",
  desc="DESC_SMI",
  signals=false,
  overlay=false,
  studyOverlay=true)
public class SmartMoneyIndex extends Study
{
  final static String BS_HALF_HR="bsHalfHr", BS_HR="bsHr";
  enum Values { SMI };

  boolean first=true;

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));

    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    sd.addInvisibleSetting(new BarSizeDescriptor(BS_HALF_HR, "HALF_HOURLY", BarSize.getBarSize(BarSizeType.LINEAR, 30)));
    sd.addInvisibleSetting(new BarSizeDescriptor(BS_HR, "HOURLY", BarSize.getBarSize(BarSizeType.LINEAR, 60)));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.PATH, Inputs.IND);

    var desc=createRD();
    desc.setLabelSettings(BS_HALF_HR, BS_HR);
    desc.exportValue(new ValueDescriptor(Values.SMI, get("LBL_SMI"), new String[] { BS_HALF_HR, BS_HR }));

    desc.declarePath(Values.SMI, Inputs.PATH);
    desc.declareIndicator(Values.SMI, Inputs.IND);
    desc.setRangeKeys(Values.SMI);

    setRuntimeDescriptor(desc);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    if (index < 1) return;
    var series=ctx.getDataSeries();

    var bs=series.getBarSize();
    int interval=bs.getIntervalMinutes();
    if (interval != 1440) {
      if (first) {
        // JOptionPane.showMessageDialog(null, "Set Bar Size to 1 Day");
        info("Set Bar Size to 1 Day");
        first=false;
      }
      return;
    }
    var bsHalfHr=getSettings().getBarSize(BS_HALF_HR);
    var series2=ctx.getDataSeries(bsHalfHr);
    var bsHr=getSettings().getBarSize(BS_HR);
    var series3=ctx.getDataSeries(bsHr);

    double open=series.getOpen(index);
    double close=series.getClose(index);

    long startTime=series.getStartTime(index);
    int halfHrInd=series2.findIndex(startTime);
    double halfHrOpen=series2.getClose(halfHrInd); // price after 30 min of trading

    double prevSmi=series.getDouble(index - 1, Values.SMI, open);

    long endTime=series.getStartTime(index + 1);
    int ind=series3.findIndex(endTime);
    double lastHrOpen=series3.getOpen(ind - 1);

    double smi=prevSmi - (halfHrOpen - open) + (close - lastHrOpen);
    series.setDouble(index, Values.SMI, smi);

    series.setComplete(index, series.isBarComplete(index));
  }
}
