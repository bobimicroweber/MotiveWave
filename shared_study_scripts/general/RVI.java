/*
 * Relative Volatility Index 019
 * 04/02/2018
 * @author www.motivewave.com
 */
package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Relative Volatility Index 019 */
@StudyHeader(
  name="NAME_RELATIVE_VOLATILITY_INDEX",
  id="ID_RVI",
  desc="DESC_RVI",
  namespace="com.motivewave",
  overlay=false,
  rb="com.motivewave.platform.study.nls.strings2",
  helpLink="http://www.motivewave.com/studies/relative_volatility_index.htm",
  label="LBL_RVI",
  studyOverlay=true,
  signals=true)

public class RVI extends Study
{
  public enum Values { STDEVHIUP, STDEVHIDOWN, STDEVLOUP, STDEVLODOWN, RVI, UP, DOWN }
  public enum Signals { RVI_TOP, RVI_BOTTOM }

  final static String RVI_LINE="rviLine";
  final static String RVI_IND="rviInd";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));
    
    // Inputs Group
    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("STD_DEV_PERIOD"), 10, 1, 999, 1));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("SMOOTH_PERIOD"), 14, 1, 999, 1));

    // Lines Group
    var lines=tab.addGroup(get("PATH_INDICATOR"));
    lines.addRow(new PathDescriptor(RVI_LINE, get("LBL_RVI"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new IndicatorDescriptor(RVI_IND, get("IND"), defaults.getLineColor(), null, false, true, true));

    // Display Tab
    tab=sd.addTab(get("TAB_DISPLAY"));
    var guides=tab.addGroup(get("GUIDES"));
    var topDesc=new GuideDescriptor(Inputs.TOP_GUIDE, get("TOP_GUIDE"), 70, 0, 999.1, .1, true);
    topDesc.setLineColor(defaults.getRed());
    guides.addRow(topDesc);
    var middleGuide=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 50, 0, 999.1, .1, true);
    middleGuide.setDash(new float[] { 3, 3 });
    guides.addRow(middleGuide);
    var bottomDesc=new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("BOTTOM_GUIDE"), 30, 0, 999.1, .1, true);
    bottomDesc.setLineColor(defaults.getGreen());
    guides.addRow(bottomDesc);

    // Shaded Lines Group
    var shadedlines=tab.addGroup(get("SHADING"));
    shadedlines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.TOP_GUIDE, RVI_LINE,
        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    shadedlines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, RVI_LINE,
        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

    // Set Markers
    var markers=tab.addGroup(get("MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,
        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,
        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT, Inputs.METHOD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("STD_DEV_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("SMOOTH_PERIOD"), 14, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(RVI_LINE);

    // Set Runtime Values
    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.METHOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.RVI, get("RVI"), new String[] { Inputs.INPUT, Inputs.PERIOD, Inputs.METHOD }));
    desc.declarePath(Values.RVI, RVI_LINE);
    desc.declareIndicator(Values.RVI, RVI_IND);
    desc.setMaxBottomValue(15);
    desc.setMinTopValue(85);
    desc.setRangeKeys(Values.RVI);
    desc.declareSignal(Signals.RVI_TOP, get("RVI_TOP"));
    desc.declareSignal(Signals.RVI_BOTTOM, get("RVI_BOTTOM"));
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    var series=ctx.getDataSeries();
    // Set Inputs
    int stDevLength=getSettings().getInteger(Inputs.PERIOD);
    int averageLength=getSettings().getInteger(Inputs.PERIOD2);

    // Min. Bar Check
    if (index <= stDevLength) return;

    var averageType=getSettings().getMAMethod(Inputs.METHOD);

    // Set Variables
    double high=series.getHigh(index);
    double low=series.getLow(index);
    double prevHigh=series.getHigh(index - 1);
    double prevLow=series.getLow(index - 1);

    // Standard Deviation Calculations
    double stDevHi=series.std(index, stDevLength, Enums.BarInput.HIGH);
    double stDevLo=series.std(index, stDevLength, Enums.BarInput.LOW);
    double stDevHiUp=(high > prevHigh) ? stDevHi : 0d;
    double stDevHiDown=(high < prevHigh) ? stDevHi : 0d;
    double stDevLoUp=(low > prevLow) ? stDevLo : 0d;
    double stDevLoDown=(low < prevLow) ? stDevLo : 0d;

    // Set Enum Values
    series.setDouble(index, Values.STDEVHIUP, Math.abs(stDevHiUp));
    series.setDouble(index, Values.STDEVHIDOWN, Math.abs(stDevHiDown));
    series.setDouble(index, Values.STDEVLOUP, Math.abs(stDevLoUp));
    series.setDouble(index, Values.STDEVLODOWN, Math.abs(stDevLoDown));

    // Min. Bar Check
    if (index <= averageLength + stDevLength) return;

    // Calculate Averages
    double avgStDevHiUp=Util.toDouble(series.ma(averageType, index, averageLength, Values.STDEVHIUP));
    double avgStDevHiDown=Util.toDouble(series.ma(averageType, index, averageLength, Values.STDEVHIDOWN));
    double avgStDevLoUp=Util.toDouble(series.ma(averageType, index, averageLength, Values.STDEVLOUP));
    double avgStDevLoDown=Util.toDouble(series.ma(averageType, index, averageLength, Values.STDEVLODOWN));

    // Calculate RVI
    double rviHi=(avgStDevHiUp + avgStDevHiDown == 0d) ? 50d : 100d * avgStDevHiUp / (avgStDevHiUp + avgStDevHiDown);
    double rviLo=(avgStDevLoUp + avgStDevLoDown == 0d) ? 50d : 100d * avgStDevLoUp / (avgStDevLoUp + avgStDevLoDown);
    double rvi=(rviHi + rviLo) / 2;
    series.setDouble(index, Values.RVI, rvi);

    // Do we need to generate a signal?
    var topGuide=getSettings().getGuide(Inputs.TOP_GUIDE);
    var bottomGuide=getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    if (crossedAbove(series, index, Values.RVI, topGuide.getValue())) {
      series.setBoolean(index, Signals.RVI_TOP, true);
      var location=new Coordinate(series.getStartTime(index), rvi);
      var marker=getSettings().getMarker(Inputs.DOWN_MARKER);
      if (marker.isEnabled()) addFigure(new Marker(location, Enums.Position.TOP, marker));
      ctx.signal(index, Signals.RVI_TOP, get("SIGNAL_RVI_TOP", topGuide.getValue(), round(rvi)), round(rvi));
    }
    else if (crossedBelow(series, index, Values.RVI, bottomGuide.getValue())) {
      series.setBoolean(index, Signals.RVI_BOTTOM, true);
      var location=new Coordinate(series.getStartTime(index), rvi);
      var marker=getSettings().getMarker(Inputs.UP_MARKER);
      if (marker.isEnabled()) addFigure(new Marker(location, Enums.Position.BOTTOM, marker));
      ctx.signal(index, Signals.RVI_BOTTOM, get("SIGNAL_RVI_BOTTOM", bottomGuide.getValue(), round(rvi)), round(rvi));
    }
    series.setComplete(index);
  }
}
