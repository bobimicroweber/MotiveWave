package com.motivewave.platform.study.volume;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Computes the volume as a percentage of the volume from the previous day at the same time.
    This study only produces values when using a linear intraday bar.  */
@StudyHeader(
    namespace="com.motivewave", 
    id="RELATIVE_VOLUME", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_RELATIVE_VOLUME",
    menu="MENU_VOLUME",
    desc="DESC_RELATIVE_VOLUME",
    overlay=false,
    requiresVolume=true,
    requiresBarUpdates=true)
public class RelativeVolume extends com.motivewave.platform.sdk.study.Study 
{
  final static String REL_VOL_IND = "relVolInd", CUM_PATH="cumPath", CUM_VOL_IND = "cumVolInd", HIGH_BAR_COLOR="highBarColor", LOW_BAR_COLOR="LOW_BAR_COLOR",
      HIGH_THRESHOLD="highThreshold", LOW_THRESHOLD="lowThreshold", GUIDE_100="guide100";
  
	enum Values { REL_VOL, CUM_VOL_PER, CUM_VOL, AVG_REL_VOL, AVG_CUM_VOL }
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    var grp = tab.addGroup(get("LBL_RELATIVE_VOLUME"));
    grp.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, 1));
    var bars = new PathDescriptor(Inputs.BAR, get("LBL_BARS"), defaults.getBarColor(), 1.0f, null, true, false, true);
    bars.setShowAsBars(true);
    bars.setSupportsShowAsBars(true);
    bars.setSupportsDisable(false);
    bars.setColorPolicies(new Enums.ColorPolicy[] { Enums.ColorPolicy.PRICE_BAR, Enums.ColorPolicy.SOLID, Enums.ColorPolicy.HIGHER_LOWER, Enums.ColorPolicy.GRADIENT });
    bars.setColorPolicy(Enums.ColorPolicy.SOLID);
    grp.addRow(bars);
    grp.addRow(new IndicatorDescriptor(REL_VOL_IND, get("LBL_INDICATOR"), null, null, false, true, true));
    grp.addRow(new ColorDescriptor(HIGH_BAR_COLOR, get("LBL_HIGH_BAR_COLOR"), Util.awtColor(192, 192, 192), true, true));
    grp.addRow(new IntegerDescriptor(HIGH_THRESHOLD, get("LBL_HIGH_THRESHOLD"), 150, 1, 99999, 1));
    grp.addRow(new ColorDescriptor(LOW_BAR_COLOR, get("LBL_LOW_BAR_COLOR"), Util.awtColor(75, 75, 75), true, true));
    grp.addRow(new IntegerDescriptor(LOW_THRESHOLD, get("LBL_LOW_THRESHOLD"), 50, 1, 99999, 1));

    grp = tab.addGroup(get("LBL_CUM_RELATIVE_VOLUME"));
    grp.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_PERIOD"), 10, 1, 9999, 1));
    grp.addRow(new PathDescriptor(CUM_PATH, get("LBL_CUM_VOL_PATH"), defaults.getBlueLine(), 1.0f, null, true, false, true));
    grp.addRow(new IndicatorDescriptor(CUM_VOL_IND, get("LBL_INDICATOR"), defaults.getBlue(), X11Colors.WHITE, false, true, true));

    sd.addDependency(new EnabledDependency(LOW_BAR_COLOR, LOW_THRESHOLD));
    sd.addDependency(new EnabledDependency(HIGH_BAR_COLOR, HIGH_THRESHOLD));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(Inputs.BAR, HIGH_BAR_COLOR, HIGH_THRESHOLD, LOW_BAR_COLOR, LOW_THRESHOLD);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("LBL_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(CUM_PATH);

    var desc = createRD();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.REL_VOL, get("LBL_REL_VOLUME"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.CUM_VOL_PER, get("LBL_CUM_REL_VOLUME"), new String[] {Inputs.PERIOD2}));
    desc.declarePath(Values.REL_VOL, Inputs.BAR);
    desc.declarePath(Values.CUM_VOL_PER, CUM_PATH);
    desc.setFixedBottomValue(0);
    desc.setBottomInsetPixels(0);
    desc.setRangeKeys(Values.REL_VOL, Values.CUM_VOL_PER);
    desc.setMinTopValue(150);
    desc.declareIndicator(Values.REL_VOL, REL_VOL_IND);
    desc.declareIndicator(Values.CUM_VOL_PER, CUM_VOL_IND);
    desc.setMinTick(0.1);
    desc.setTopInsetPixels(5);
    desc.setBottomInsetPixels(0);
    desc.addHorizontalLine(new LineInfo(100, null, 1.0f, new float[] {3,3}));
  }

  @Override
  public Long getMinStartTime(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    var bs = series.getBarSize();
    if (!bs.isFixedSize() || !bs.isIntraday()) return super.getMinStartTime(ctx);
    
    var instr = series.getInstrument();
    long sod = instr.getStartOfDay(ctx.getCurrentTime(), ctx.isRTH());
    int period = Math.max(getSettings().getInteger(Inputs.PERIOD, 10), getSettings().getInteger(Inputs.PERIOD2, 10));
    for(int i = 0; i <= period; i++) { // Get an extra day here...
      sod = Util.getStartOfPrevDay(sod, instr, ctx.isRTH());
    }
    return sod;
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    int startInd = 0;
    for(startInd = 0; startInd < series.size(); startInd++) {
      if (!series.isComplete(startInd)) break;
    }
    
    var instr = series.getInstrument();
    var cumPath = getSettings().getPath(CUM_PATH);
    // Calculate the cumulative daily volume for each bar
    if (cumPath != null && cumPath.isEnabled()) {
      int ind = startInd;
      while(ind < series.size()) {
        long sod1 = instr.getStartOfDay(series.getStartTime(ind), ctx.isRTH());
        long sod2 = instr.getStartOfDay(series.getStartTime(ind-1), ctx.isRTH());
        if (sod1 != sod2) series.setFloat(ind, Values.CUM_VOL, series.getVolumeAsFloat(ind)); // First bar of the day?
        else {
          series.setFloat(ind, Values.CUM_VOL, series.getFloat(ind-1, Values.CUM_VOL, 0f) + series.getVolumeAsFloat(ind));
        }
        ind++;
      }
    }
    
    for(int ind = startInd; ind < series.size(); ind++) {
      calculate(ind, ctx);
      series.setComplete(ind);
    }
  }
  
  @Override
  protected void calculate(int ind, DataContext ctx)
  {
    var series = ctx.getDataSeries();
    var highColor = getSettings().getColorInfo(HIGH_BAR_COLOR);
    var highThreshold = getSettings().getInteger(HIGH_THRESHOLD, 150);
    var lowColor = getSettings().getColorInfo(LOW_BAR_COLOR);
    var lowThreshold = getSettings().getInteger(LOW_THRESHOLD, 50);
    int period1 = getSettings().getInteger(Inputs.PERIOD, 10);
    int period2 = getSettings().getInteger(Inputs.PERIOD2, 10);
    var instr = series.getInstrument();
    var cumPath = getSettings().getPath(CUM_PATH);

    long start = series.getStartTime(ind);
    long sod = instr.getStartOfDay(start, ctx.isRTH());
    long timeOfDay = start - sod;
    
    float vol = series.getVolumeAsFloat(ind);
    Float avg = series.getFloat(ind, Values.AVG_REL_VOL);
    if (avg == null) {
      float totalVol = 0;
      int p = 0;
      long sod1 = sod;
      for(int j=0; j < period1; j++) {
        sod1 = Util.getStartOfPrevDay(sod1, instr, ctx.isRTH());
        int _ind = series.findIndex(sod1 + timeOfDay);
        if (_ind < 0) break;
        totalVol += series.getVolumeAsFloat(_ind);
        p++;
      }
      avg = p == 0 ? 0 : totalVol / p;
      series.setFloat(ind, Values.AVG_REL_VOL, avg);
    }
    double relVol = avg == 0f ? 0 : Util.roundDouble((vol/avg) * 100, 2);
    series.setDouble(ind, Values.REL_VOL, relVol);
    if (highColor.isEnabled() && relVol >= highThreshold) series.setBarColor(ind, Values.REL_VOL, highColor.getColor());
    else if (lowColor.isEnabled() && relVol <= lowThreshold) series.setBarColor(ind, Values.REL_VOL, lowColor.getColor());
    else series.setBarColor(ind, Values.REL_VOL, null);
    
    if (cumPath == null || !cumPath.isEnabled()) return;

    avg = series.getFloat(ind, Values.AVG_CUM_VOL);
    if (avg == null) {
      float totalVol = 0;
      int p = 0;
      long sod1 = sod;
      for(int j=0; j < period2; j++) {
        sod1 = Util.getStartOfPrevDay(sod1, instr, ctx.isRTH());
        int _ind = series.findIndex(sod1 + timeOfDay);
        if (_ind < 0) break;
        totalVol += series.getFloat(_ind, Values.CUM_VOL, 0f);
        p++;
      }
      avg = totalVol / p;
      series.setFloat(ind, Values.AVG_CUM_VOL, avg);
    }
    long prevStart = series.getStartTime(ind-1);
    float cv = prevStart < sod ? vol : series.getFloat(ind-1, Values.CUM_VOL, 0f) + vol;
    double cvp = avg == 0f ? 0 : Util.roundDouble((cv/avg) * 100, 2);
    series.setFloat(ind, Values.CUM_VOL, cv);
    series.setDouble(ind, Values.CUM_VOL_PER, cvp);
  } 

}
