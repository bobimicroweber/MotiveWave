package com.motivewave.platform.study.general;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.motivewave.platform.sdk.common.Bar;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.BarInput;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Seasonality. Computes the average gain using start of year or start of month. */
@StudyHeader(
    namespace="com.motivewave", 
    id="SEASONALITY", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_SEASONALITY",
    desc="DESC_SEASONALITY",
    menu="MENU_GENERAL",
    overlay=false)
public class Seasonality extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { PER, PPER }
	
	final static String YEARS="years", CALC_METHOD="calcMethod";
  final static String START_OF_YEAR="SOY", START_OF_MONTH="SOM", PROJECT="project";

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    List<NVP> methods = new ArrayList();
    methods.add(new NVP(get("LBL_START_OF_YEAR"), START_OF_YEAR));
    methods.add(new NVP(get("LBL_START_OF_MONTH"), START_OF_MONTH));

    var grp = tab.addGroup("", false);
    grp.addRow(new IntegerDescriptor(YEARS, get("LBL_YEARS"), 10, 1, 999, 1));
    grp.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), 
        new BarInput[] { BarInput.OPEN, BarInput.CLOSE, BarInput.MIDPOINT, BarInput.HIGH, BarInput.LOW, BarInput.TP }, BarInput.CLOSE));
    grp.addRow(new DiscreteDescriptor(CALC_METHOD, get("LBL_CALC_METHOD"), START_OF_YEAR, methods));
    grp.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), defaults.getLineColor(), 1.0f, null));
    grp.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    grp.addRow();
    grp.addRow(new PathDescriptor(Inputs.PATH2, get("LBL_PROJECTED_LINE"), defaults.getLineColor(), 1.0f, new float[] {3,3}), 
        new BooleanDescriptor(PROJECT, get("LBL_ENABLED"), true, false));

    sd.addDependency(new EnabledDependency(PROJECT, Inputs.PATH2));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(YEARS, Inputs.INPUT, CALC_METHOD, Inputs.PATH, Inputs.PATH2, PROJECT);

    var desc = createRD();
    desc.setLabelSettings(YEARS, CALC_METHOD);
    desc.exportValue(new ValueDescriptor(Values.PER, get("LBL_PER_GAIN"), new String[] {YEARS}));
    desc.exportValue(new ValueDescriptor(Values.PPER, get("LBL_PER_GAIN"), new String[] {YEARS}));
    desc.declarePath(Values.PER, Inputs.PATH);
    desc.declarePath(Values.PPER, Inputs.PATH2);
    desc.declareIndicator(Values.PER, Inputs.IND);
    desc.setRangeKeys(Values.PER, Values.PPER);
    desc.setMinTick(0.01);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    super.calculateValues(ctx);
    // This study only applies to daily bars
    var series = ctx.getDataSeries();
    var bs = series.getBarSize();
    if (!bs.isLinear() || bs.getIntervalMinutes() != 1440) return;
    
    int years = getSettings().getInteger(YEARS, 10);
    var input = (BarInput)getSettings().getInput(Inputs.INPUT, BarInput.CLOSE);
    boolean soy = Util.compare(getSettings().getString(CALC_METHOD, START_OF_YEAR), START_OF_YEAR);
    var cal = Calendar.getInstance();
    cal.setTimeInMillis(series.getStartTime(0) - years*Util.MILLIS_IN_YEAR);
    int year = cal.get(Calendar.YEAR);
    cal.clear();
    cal.set(Calendar.YEAR, year);
    long start = cal.getTimeInMillis();
    
    var instr = series.getInstrument();
    List<Bar> bars = null;
    try {
      bars = instr.getBars(start, ctx.getCurrentTime(), bs, ctx.isRTH());
    }
    catch(Exception exc) {
      exc.printStackTrace();
    }
    if (bars == null || bars.isEmpty()) return;

    cal.setTimeInMillis(bars.get(0).getStartTime()+12*Util.MILLIS_IN_HOUR);
    int startYear = cal.get(Calendar.YEAR);

    // Organize the bars
    Map<Long, Bar> dataByTime = new HashMap<>(); // Indexed by start of day (in millis)
    Map<Integer, List<Bar>> dataByYear = new HashMap<>(); // Segmented by Year
    Map<String, List<Bar>> dataByMonth = new HashMap<>(); // Segmented by Month
    for(var bar : bars) {
      cal.setTimeInMillis(bar.getStartTime()+12*Util.MILLIS_IN_HOUR);
      int y = cal.get(Calendar.YEAR);
      int m = cal.get(Calendar.MONTH);
      dataByTime.put(bar.getStartTime(), bar);
      List<Bar> yearBars = dataByYear.get(y);
      if (yearBars == null) { yearBars = new ArrayList<>(); dataByYear.put(y, yearBars); }
      yearBars.add(bar);
      List<Bar> monthBars = dataByMonth.get(y+":"+m);
      if (monthBars == null) { monthBars = new ArrayList<>(); dataByMonth.put(y+":"+m, monthBars); }
      monthBars.add(bar);
    }

    for(int i = 0; i < series.size(); i++) {
      if (series.isComplete(i)) continue;
      
      cal.setTimeInMillis(series.getStartTime(i)+12*Util.MILLIS_IN_HOUR);
      int y = cal.get(Calendar.YEAR);
      int m = cal.get(Calendar.MONTH);
      double total = 0;
      int c = 0;
      
      if (soy) {
        List<Bar> yearBars = dataByYear.get(y);
        Bar b = dataByTime.get(series.getStartTime(i));
        if (yearBars == null || b == null) continue; // this should not happen
        int day = yearBars.indexOf(b);
        if (day < 0) continue; // this should not happen
        
        for(int yy = y; yy >= startYear; yy--) {
          yearBars = dataByYear.get(yy);
          if (yearBars == null) continue;
          float startValue = getValue(yearBars.get(0), input);
          b = day < yearBars.size() ? yearBars.get(day) : yearBars.get(yearBars.size()-1);
          total += (getValue(b, input) - startValue)/startValue;
          c++;
        }
      }
      else {
        List<Bar> monthBars = dataByMonth.get(y+":"+m);
        Bar b = dataByTime.get(series.getStartTime(i));
        if (monthBars == null || b == null) continue; // this should not happen
        int day = monthBars.indexOf(b);
        if (day < 0) continue; // this should not happen
        for(int yy = y; yy >= startYear; yy--) {
          monthBars = dataByMonth.get(yy+":"+m);
          if (monthBars == null) continue;
          float startValue = getValue(monthBars.get(0), input);
          b = day < monthBars.size() ? monthBars.get(day) : monthBars.get(monthBars.size()-1);
          total += (getValue(b, input) - startValue)/startValue;
          c++;
        }
      }
      if (c == 0) continue;
      series.setDouble(i, Values.PER, 100*(total/c));
      series.setComplete(i, series.isBarComplete(i));
    }
    
    if (!getSettings().getBoolean(PROJECT, true)) return;
    
    // Project out another year using past values
    for(int i = series.size(); i < series.size()+400; i++) {
      cal.setTimeInMillis(series.getStartTime(i)+12*Util.MILLIS_IN_HOUR);
      int y = cal.get(Calendar.YEAR)-1; // use previous year value
      int m = cal.get(Calendar.MONTH);
      double total = 0;
      int c = 0;
      
      if (soy) {
        List<Bar> yearBars = dataByYear.get(y);
        if (yearBars == null) continue;
        int day = Util.findNearest(yearBars, series.getStartTime(i)-Util.MILLIS_IN_YEAR);
        if (day < 0) continue; // this should not happen
        for(int yy = y; yy >= startYear; yy--) {
          yearBars = dataByYear.get(yy);
          if (yearBars == null) continue;
          float startValue = getValue(yearBars.get(0), input);
          Bar b = day < yearBars.size() ? yearBars.get(day) : yearBars.get(yearBars.size()-1);
          total += (getValue(b, input) - startValue)/startValue;
          c++;
        }
      }
      else {
        List<Bar> monthBars = dataByMonth.get(y+":"+m);
        if (monthBars == null) continue;
        int day = Util.findNearest(monthBars, series.getStartTime(i)-Util.MILLIS_IN_YEAR);
        if (day < 0) continue; // this should not happen
        for(int yy = y; yy >= startYear; yy--) {
          monthBars = dataByMonth.get(yy+":"+m);
          if (monthBars == null) continue;
          float startValue = getValue(monthBars.get(0), input);
          Bar b = day < monthBars.size() ? monthBars.get(day) : monthBars.get(monthBars.size()-1);
          total += (getValue(b, input) - startValue)/startValue;
          c++;
        }
      }
      if (c == 0) continue;
      series.setDouble(i, Values.PPER, 100*(total/c));
      series.setComplete(i, false);
    }
  }
  
  private float getValue(Bar bar, BarInput input)
  {
    switch(input) {
    case OPEN: return bar.getOpen();
    case CLOSE: return bar.getClose();
    case HIGH: return bar.getHigh();
    case LOW: return bar.getLow();
    case MIDPOINT: return (bar.getHigh() + bar.getLow())/2;
    case TP: return (bar.getLow() + bar.getHigh() + bar.getClose())/3;
    }
    return 0;
  }
}
