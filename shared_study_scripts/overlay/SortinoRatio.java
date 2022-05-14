package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;
import com.motivewave.platform.study.general.Utility;

/** Sortino Ratio 148 */
@StudyHeader(
  namespace = "com.motivewave",
  id = "ID_SORTINO",
  rb = "com.motivewave.platform.study.nls.strings2",
  label ="LBL_SORTINO",
  name = "NAME_SORTINO_RATIO",
  desc = "DESC_SORTINO",
  menu = "MENU_PERFORMANCE",
  signals = false,
  overlay = false,
  studyOverlay=true)
public class SortinoRatio extends Study
{
  final static String SAFE="Safe";

  enum Values { RET, SORTINO }
   
  @Override
  public void initialize(Defaults defaults)
  {
     var sd = createSD();
     var tab = sd.addTab(get("TAB_GENERAL"));
   
     var inputs = tab.addGroup(get("INPUTS"));
     inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
     inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("PERIOD"), 30, 1, 9999, 1));
     inputs.addRow(new DoubleDescriptor(SAFE, get("SAFE_RETURN_%"), 2, 0, 99.01, .01));
    
     var settings = tab.addGroup(get("PATH_INDICATOR"));
     settings.addRow(new PathDescriptor(Inputs.PATH, get("LBL_SORTINO"),  defaults.getLineColor(), 1.0f, null, true, false, true));
     settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));

     tab = sd.addTab(get("TAB_DISPLAY"));

     var guides = tab.addGroup(get("GUIDE"));
     var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"),  0, -9.01, 9.01, .01, true);
     mg.setDash(new float[] {3, 3});
     guides.addRow(mg);

     var shade = tab.addGroup(get("SHADING"));
     shade.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
         Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
     shade.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.MIDDLE_GUIDE, Inputs.PATH,
         Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));

     // Quick Settings (Tool Bar and Popup Editor)
     sd.addQuickSettings(Inputs.INPUT);
     sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("PERIOD"), 30, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
     sd.addQuickSettings(SAFE, Inputs.PATH, Inputs.TOP_FILL, Inputs.BOTTOM_FILL);

     var desc = createRD();
     desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, SAFE);
     desc.exportValue(new ValueDescriptor(Values.SORTINO, get("LBL_SORTINO"), new String[] {Inputs.INPUT, Inputs.PERIOD, SAFE}));
     desc.declarePath(Values.SORTINO, Inputs.PATH);
     desc.declareIndicator(Values.SORTINO, Inputs.IND);
     desc.setRangeKeys(Values.SORTINO);
   }

   @Override
   public void onLoad(Defaults defaults)
   {
     int p1 = getSettings().getInteger(Inputs.PERIOD);
     setMinBars(p1*2);
   }
   
   @Override
   protected void calculate(int index, DataContext ctx)
   {
     int p1 = getSettings().getInteger(Inputs.PERIOD);
     if (index < p1) return;
     
     Object key = getSettings().getInput(Inputs.INPUT, Enums.BarInput.CLOSE);
     double safe = getSettings().getDouble(SAFE);
     safe = safe / 100.0;
     
     var series = ctx.getDataSeries();
     double sortino = 0;
     int barMin = 0;
     var bar = series.getBarSize();
     if (bar.getType() == Enums.BarSizeType.LINEAR) barMin = bar.getIntervalMinutes();
     else return;
     double minPerYr = 60*24*30*12;
     double barsPerYr = minPerYr/barMin;
     double adjSafe = Math.pow((1 + (safe)), p1/barsPerYr) - 1; //safe return per period compounded

     double price = series.getDouble(index, key, 0);
     double priorP = series.getDouble(index-p1 , key, 0);

     double ret = (((price/priorP)-1)) - adjSafe; //safe return subtracted here to reflect Sharpe 1994 revision
     series.setDouble(index, Values.RET, ret);
     if (index < p1*2) return;
     
     double av = series.sma(index, p1, Values.RET);
     double stdMinus = Utility.sdDev(series, index, p1, Values.RET)[2];
     if (stdMinus != 0 )sortino = av / stdMinus;
     series.setDouble(index, Values.SORTINO, sortino);
     series.setComplete(index);
   }
 }