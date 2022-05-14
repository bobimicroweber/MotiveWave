package com.motivewave.platform.study.general;import com.motivewave.platform.sdk.common.Coordinate;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.Util;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.draw.Marker;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Vortex indicator 002 */@StudyHeader(  namespace="com.motivewave",  id="ID_VORTEX",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_VORTEX",  name="NAME_VORTEX_INDICATOR",  desc="DESC_VORTEX",  menu="MENU_OSCILLATORS",  helpLink="http://www.motivewave.com/studies/vortex_indicator.htm",  signals=true,  overlay=false,  studyOverlay=true)public class VortexIndicator extends Study{  enum Values { TR, VMPLUS, VMMINUS, VIPLUS, VIMINUS };  protected enum Signals { BUY, SELL };  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUT"));    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 21, 1, 999, 1));    var settings=tab.addGroup(get("PATHS"));    settings.addRow(new PathDescriptor(Inputs.PATH, get("PLUS"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new PathDescriptor(Inputs.PATH2, get("MINUS"), defaults.getRed(), 1.0f, null, true, false, true));    settings=tab.addGroup(get("INDICATORS"));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("PLUS"), defaults.getLineColor(), null, false, true, true));    settings.addRow(new IndicatorDescriptor(Inputs.IND2, get("MINUS"), defaults.getRed(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var guides=tab.addGroup(get("GUIDE"));    var mg=new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 1, 0, 9.01, .01, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    var markers=tab.addGroup(get("MARKERS"));    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("UP_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("DOWN_MARKER"), Enums.MarkerType.TRIANGLE,        Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));    var shade=tab.addGroup(get("SHADING"));    shade.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    shade.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,        Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 21, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Inputs.PATH, Inputs.PATH2, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);    var desc=createRD();    desc.setLabelSettings(Inputs.PERIOD);    desc.exportValue(new ValueDescriptor(Values.VIPLUS, get("PLUS"), new String[] { Inputs.PERIOD }));    desc.exportValue(new ValueDescriptor(Values.VIMINUS, get("MINUS"), new String[] { Inputs.PERIOD }));    desc.exportValue(new ValueDescriptor(Signals.SELL, Enums.ValueType.BOOLEAN, get("SELL"), null));    desc.exportValue(new ValueDescriptor(Signals.BUY, Enums.ValueType.BOOLEAN, get("BUY"), null));    desc.declareSignal(Signals.SELL, get("SELL"));    desc.declareSignal(Signals.BUY, get("BUY"));    desc.declarePath(Values.VIPLUS, Inputs.PATH);    desc.declarePath(Values.VIMINUS, Inputs.PATH2);    desc.declareIndicator(Values.VIPLUS, Inputs.IND);    desc.declareIndicator(Values.VIMINUS, Inputs.IND2);    desc.setRangeKeys(Values.VIPLUS, Values.VIMINUS);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Inputs.PERIOD);    setMinBars(p1 + 1);  }  @Override  protected void calculate(int index, DataContext ctx)  {    // need at least one record    if (index < 1) return;    int period=getSettings().getInteger(Inputs.PERIOD);    var series=ctx.getDataSeries();    double high=series.getHigh(index);    double low=series.getLow(index);    double tr=series.getTrueRange(index);    series.setDouble(index, Values.TR, tr);    // Calculate the current upward and downward movement.    double prevLow=series.getLow(index - 1);    double prevHigh=series.getHigh(index - 1);    double vmPlus=Math.abs(high - prevLow);    double vmMinus=Math.abs(low - prevHigh);    series.setDouble(index, Values.VMPLUS, vmPlus);    series.setDouble(index, Values.VMMINUS, vmMinus);    if (index < period + 1) return;    // Calculate the vortex indicator using the period entered by the user usually 21.    double sumTr=series.sum(index, period, Values.TR);    double sumVplus=series.sum(index, period, Values.VMPLUS);    double sumVminus=series.sum(index, period, Values.VMMINUS);    double viPlus=sumVplus / sumTr;    double viMinus=sumVminus / sumTr;    series.setDouble(index, Values.VIPLUS, viPlus);    series.setDouble(index, Values.VIMINUS, viMinus);    // Check for signal events    boolean buy=crossedAbove(series, index, Values.VIPLUS, Values.VIMINUS);    boolean sell=crossedBelow(series, index, Values.VIPLUS, Values.VIMINUS);    series.setBoolean(index, Signals.SELL, sell);    series.setBoolean(index, Signals.BUY, buy);    if (sell) {      var c=new Coordinate(series.getStartTime(index), viPlus);      var marker=getSettings().getMarker(Inputs.DOWN_MARKER);      String msg = get("SELL_VORTEX_HIGH_VMPLUS", Util.round(high, 2), Util.round(viPlus, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));      ctx.signal(index, Signals.SELL, msg, high);    }    if (buy) {      var c=new Coordinate(series.getStartTime(index), viPlus);      var marker=getSettings().getMarker(Inputs.UP_MARKER);      String msg = get("BUY_VORTEX_LOW_VMPLUS", Util.round(low, 2), Util.round(viPlus, 3));      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));      ctx.signal(index, Signals.BUY, msg, low);    }    series.setComplete(index);  }}