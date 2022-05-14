package com.motivewave.platform.study.general2;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(
  name="NAME_ACCUMULATIVE_SWING_INDEX",
  id="ID_ACC_SWING_IND",
  desc="DESC_ASI",
  namespace="com.motivewave",
  overlay=false,
  rb="com.motivewave.platform.study.nls.strings2", 
  helpLink="http://www.investopedia.com/terms/a/asi.asp", 
  label="LBL_ASI", 
  studyOverlay=true,
  menu="MENU_GENERAL",
  signals=false)
public class AccumulativeSwingIndex extends Study
{
  // We need one more name for an Indicator on the vertical axis
  final static String LIMIT_MOVE="LimitMove";

  // This enumeration defines the variables that we are going to store in the DataSeries
  enum Values { SI }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("GENERAL"));

    var inputs1=tab.addGroup(get("INPUTS"));
    inputs1.addRow(new DoubleDescriptor(LIMIT_MOVE, get("LIMIT_MOVE_FACTOR"), 1.0, 0.01, 99.999, 0.01));

    var settings=tab.addGroup(get("PATH_INDICATOR"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PATH"), defaults.getLineColor(), 1.0f, null, true, true, true));
    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    tab=sd.addTab(get("DISPLAY"));

    var guides=tab.addGroup(get("GUIDES"));
    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, 0, 999.1, .1, true);
    mg.setDash(new float[] { 3, 3 });
    guides.addRow(mg);

    settings=tab.addGroup(get("SHADING"));
    settings.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    settings.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(LIMIT_MOVE, Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

    var desc=createRD();
    desc.setLabelSettings(LIMIT_MOVE);
    desc.exportValue(new ValueDescriptor(Values.SI, get("LBL_ASI"), new String[] { LIMIT_MOVE }));
    desc.declarePath(Values.SI, Inputs.PATH);
    desc.declareIndicator(Values.SI, Inputs.IND);
    desc.setRangeKeys(Values.SI);
  }

  /** This method calculates the plots for the given index in the data series.
   * @param index - index in the data series
   * @param ctx - Data Context */
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    // Get access to the data series.
    var series=ctx.getDataSeries();
    if (index < 1) return;// not enough data

    // Set the default values
    double limitMove=getSettings().getDouble(LIMIT_MOVE, 1.0);
    double open=series.getOpen(index);
    double low=series.getLow(index);
    double high=series.getHigh(index);
    double close=series.getClose(index);
    double prevOpen=series.getOpen(index - 1);
    double prevClose=series.getClose(index - 1);
    double a=Math.abs(high - prevClose);
    double b=Math.abs(low - prevClose);
    double c=Math.abs(high - low);
    double d=Math.abs(prevClose - prevOpen);
    double e=Math.abs(low - close);
    double f=Math.abs(high - close);
    double k=Math.max(a, b);
    double r=0.0;
    double prevSi=series.getDouble(index - 1, Values.SI, 0);

    // Evaluate, calculate and assign r value.
    if (a >= b && a >= c) {
      r=a - 0.5 * e + 0.25 * d;
    }
    if (b >= a && b >= c) {
      r=b - 0.5 * f + 0.25 * d;
    }
    if (c >= a && c >= b) {
      r=c + 0.25 * d;
    }

    // Calculate Swing Index
    double si=((50 * k) / limitMove) * ((close - prevClose) + .5 * (close - open) + 0.25 * (prevClose - prevOpen)) / r;

    // Calculate ASI
    si+=prevSi;

    // Set ASI value
    series.setDouble(index, Values.SI, si);
    series.setComplete(index, index < series.size() - 1);
  }
}
