package study;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Support/Resistances */
@StudyHeader(
    namespace="com.cloudvisionltd",
    id="SUPPORT_RESISTANCE", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_SUPPORT_RESISTANCE", 
    desc="DESC_SUPPORT_RESISTANCE",
    menu = "A4Crypto Indicators",
    overlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/support_resistance.htm")
public class SupportResistance extends Study 
{
  enum Values { RESISTANCE, SUPPORT }
  enum Signals { CROSS_RESISTANCE, CROSS_SUPPORT }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var defaultLblPeriod = 30;

    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), defaultLblPeriod, 1, 9999, 1));

    var markers = tab.addGroup(get("LBL_MARKERS"));
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    var lines = tab.addGroup(get("LBL_LINES"));
    var top = new PathDescriptor(Inputs.TOP_PATH, get("LBL_RESISTANCE_LINE"), defaults.getRed(), Enums.PointType.DOT, Enums.Size.VERY_SMALL, true, false, true);
    top.setSupportsBoth(true);
    lines.addRow(top);
    var bottom = new PathDescriptor(Inputs.BOTTOM_PATH, get("LBL_SUPPORT_LINE"), defaults.getBlue(), Enums.PointType.DOT, Enums.Size.VERY_SMALL, true, false, true);
    bottom.setSupportsBoth(true);
    lines.addRow(bottom);
    lines.addRow(new IndicatorDescriptor(Inputs.TOP_IND, get("LBL_RESISTANCE_IND"), defaults.getRed(), X11Colors.WHITE, false, false, true));
    lines.addRow(new IndicatorDescriptor(Inputs.BOTTOM_IND, get("LBL_SUPPORT_IND"), defaults.getBlue(), X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), defaultLblPeriod, 1, 9999, true));
    sd.addQuickSettings(Inputs.TOP_PATH, Inputs.BOTTOM_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD);
    
    desc.exportValue(new ValueDescriptor(Values.RESISTANCE, get("LBL_RESISTANCE"), new String[] {Inputs.INPUT, Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SUPPORT, get("LBL_SUPPORT"), new String[] {Inputs.INPUT, Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_RESISTANCE, Enums.ValueType.BOOLEAN, get("LBL_CROSS_RESISTANCE"), null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_SUPPORT, Enums.ValueType.BOOLEAN, get("LBL_CROSS_SUPPORT"), null));
    
    desc.declarePath(Values.RESISTANCE, Inputs.TOP_PATH);
    desc.declarePath(Values.SUPPORT, Inputs.BOTTOM_PATH);
    
    desc.declareIndicator(Values.RESISTANCE, Inputs.TOP_IND);
    desc.declareIndicator(Values.SUPPORT, Inputs.BOTTOM_IND);
    
    desc.setRangeKeys(Values.RESISTANCE, Values.SUPPORT);
    // Signals
    desc.declareSignal(Signals.CROSS_RESISTANCE, get("LBL_CROSS_RESISTANCE"));
    desc.declareSignal(Signals.CROSS_SUPPORT, get("LBL_CROSS_SUPPORT"));
  }
  
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    if (index <= period) return;
    var series = ctx.getDataSeries();
    Object input = getSettings().getInput(Inputs.INPUT);

    Double prev = series.getDouble(index-1, input);
    Double current = series.getDouble(index, input);
    if (prev == null || current == null) return;
    double sma = series.sma(index-1, period, input);
    boolean crossAbove = (prev < sma && current >= sma);
    boolean crossBelow = (prev > sma && current <= sma);

    Double res = series.getDouble(index-1, Values.RESISTANCE); 
    Double supp = series.getDouble(index-1, Values.SUPPORT); 
    
    if (crossBelow) {
      // Calculate new resistance point
      res = series.highest(index,  period, Enums.BarInput.HIGH);
    }
    
    if (crossAbove) {
      // Calculate new support point
      supp = series.lowest(index,  period, Enums.BarInput.LOW);
    }
    
    if (supp != null && prev >= supp && current < supp) {
      var c = new Coordinate(series.getStartTime(index), supp);
      // March 6, 2019 Check to see if this has been triggered already, if so don't check again
      if (!series.getBoolean(index, Signals.CROSS_SUPPORT, false)) {
        series.setBoolean(index, Signals.CROSS_SUPPORT, true);
        var marker = getSettings().getMarker(Inputs.DOWN_MARKER);
        String msg = get("SIGNAL_CROSS_SUPPORT", format(current), format(supp));
        if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
        ctx.signal(index, Signals.CROSS_SUPPORT, msg, current);
      }
    }

    if (res != null && prev <= res && current > res) {
      var c = new Coordinate(series.getStartTime(index), res);
      if (!series.getBoolean(index, Signals.CROSS_RESISTANCE, false)) {
        series.setBoolean(index, Signals.CROSS_RESISTANCE, true);
        var marker = getSettings().getMarker(Inputs.UP_MARKER);
        String msg = get("SIGNAL_CROSS_RESISTANCE", format(current), format(res));
        if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
        ctx.signal(index, Signals.CROSS_RESISTANCE, msg, current);
      }
    }
    
    boolean complete = series.isBarComplete(index);
    if (res == null) {
      res = series.highest(index,  period, Enums.BarInput.HIGH);
      complete = false;
    }
    if (supp == null) {
      supp = series.lowest(index,  period, Enums.BarInput.LOW);
      complete = false;
    }

    series.setDouble(index, Values.SUPPORT, supp);
    series.setDouble(index, Values.RESISTANCE, res);
    series.setComplete(index, complete);
  }
}