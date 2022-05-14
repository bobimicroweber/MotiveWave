package com.motivewave.platform.study.wilder;

import java.awt.Color;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Parabolic SAR 
 * This is the previous version of this study, it can be deleted once the new changes for TSU have been confirmed. */
@StudyHeader(
  namespace="com.motivewave",
  id="PSAR",
  rb="com.motivewave.platform.study.nls.strings",
  name="TITLE_PSAR",
  label="LBL_PSAR",
  desc="DESC_PSAR",
  menu="MENU_OVERLAY",
  menu2="MENU_WELLES_WILDER",
  overlay=true,
  signals=true,
  helpLink="http://www.motivewave.com/studies/parabolic_sar.htm")
public class ParabolicSAR extends Study
{
  final static String INITIAL_AF="initialAF";
  final static String MAX_AF="maxAF";
  final static String STEP="step";

  enum Values { SAR, AF, EP, LONG }
  enum Signals { SAR_LONG, SAR_SHORT }

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));

    populateGeneralTab(tab);

    var desc=createRD();
    desc.setLabelSettings(INITIAL_AF, MAX_AF, STEP);
    desc.exportValue(new ValueDescriptor(Values.SAR, get("LBL_PSAR"), new String[] { INITIAL_AF, MAX_AF, STEP }));
    desc.declarePath(Values.SAR, Inputs.PATH);
    desc.declareIndicator(Values.SAR, Inputs.IND);
    desc.setRangeKeys(Values.SAR);
    desc.declareSignal(Signals.SAR_LONG, get("LBL_SAR_LONG"));
    desc.declareSignal(Signals.SAR_SHORT, get("LBL_SAR_SHORT"));
  }

  protected void populateGeneralTab(SettingTab tab)
  {
    var inputs=tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new DoubleDescriptor(INITIAL_AF, get("LBL_INITIAL_AF"), 0.02, 0.001, 9.999, 0.001));
    inputs.addRow(new DoubleDescriptor(MAX_AF, get("LBL_MAX_AF"), 0.2, 0.001, 9.999, 0.001));
    inputs.addRow(new DoubleDescriptor(STEP, get("LBL_STEP"), 0.02, 0.001, 9.999, 0.001));

    var lines=tab.addGroup(get("LBL_LINES"));
    Color lightBlue=Util.awtColor(95, 165, 220);
    var path=new PathDescriptor(Inputs.PATH, get("LBL_PSAR"), lightBlue, Enums.PointType.DOT, Enums.Size.SMALL, true, true, false);
    path.setSupportsMaxPoints(true);
    path.setSupportsColorPolicy(false);
    lines.addRow(path);
    lines.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), lightBlue, X11Colors.WHITE, false, false, true));
    
    // Quick Settings (Tool Bar and Popup Editor)
    var sd = getSettingsDescriptor();
    sd.addQuickSettings(INITIAL_AF, MAX_AF, STEP, Inputs.PATH);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    var series=ctx.getDataSeries();
    if (index < 1) return;

    double AFinitial=getSettings().getDouble(INITIAL_AF, 0.02);
    double AFmax=getSettings().getDouble(MAX_AF, 0.2);
    double AFstep=getSettings().getDouble(STEP, 0.02);
    var instr=ctx.getInstrument();
    boolean latest=index == series.size() - 1;
    double high = instr.round((double)series.getHigh(index));
    double low = instr.round((double)series.getLow(index));
    double prevHigh = instr.round((double)series.getHigh(index-1));
    double prevLow = instr.round((double)series.getLow(index-1));
    double prevHigh2 = instr.round((double)series.getHigh(index-2));
    double prevLow2 = instr.round((double)series.getLow(index-2));
    
    if (index == 1) {
      // first entry point, calculate the first SAR
      series.setDouble(index, Values.SAR, prevHigh);
      series.setDouble(index, Values.AF, AFinitial);
      series.setDouble(index, Values.EP, prevHigh);
      series.setBoolean(index, Values.LONG, false);
      series.setComplete(index, true);
      return;
    }

    Double pSAR=series.getDouble(index - 1, Values.SAR);
    if (pSAR == null) return;
    double pAF=series.getDouble(index - 1, Values.AF);
    double pEP=series.getDouble(index - 1, Values.EP);
    boolean isLong=series.getBoolean(index - 1, Values.LONG);
    double SAR = pSAR;
    double EP = pEP;
    double AF = pAF;

    // Check for stop and reversals
    // Have we penetrated the pSAR?
    double oldSAR=pSAR;
    
    if (isLong && low < pSAR) {
      if (EP < high) EP = high;
      series.setDouble(index, Values.SAR, EP);
      series.setDouble(index, Values.AF, AFinitial);
      series.setDouble(index, Values.EP, Math.min(low, prevLow));
      series.setBoolean(index, Values.LONG, false);
      series.setComplete(index, series.isBarComplete(index));
      series.setBoolean(index, Signals.SAR_LONG, true);
      if (!latest) {
        ctx.signal(index, Signals.SAR_LONG, get("SIGNAL_SAR_LONG", instr.format(low), instr.format(oldSAR)), low);
      }
      return;
    }
    
    if (!isLong && high > pSAR) {
      if (EP > low) EP = low;
      series.setDouble(index, Values.SAR, EP);
      series.setDouble(index, Values.AF, AFinitial);
      series.setDouble(index, Values.EP, Math.max(high, prevHigh));
      series.setBoolean(index, Values.LONG, true);
      series.setComplete(index, series.isBarComplete(index));
      series.setBoolean(index, Signals.SAR_SHORT, true);
      if (!latest) {
        ctx.signal(index, Signals.SAR_SHORT, get("SIGNAL_SAR_SHORT", instr.format(high), instr.format(oldSAR)), high);
      }
      return;
    }
    
    if (isLong) {
      SAR=pSAR + pAF * (pEP - pSAR);
      if (EP < high && AF+AFstep <= AFmax) AF+=AFstep;
      if (SAR > prevLow) SAR = prevLow;
      if (SAR > prevLow2) SAR = prevLow2;
      if (SAR > low) {
        // stop and reverse
        if (EP < high) EP = high;
        series.setDouble(index, Values.SAR, EP);
        series.setDouble(index, Values.AF, AFinitial);
        series.setDouble(index, Values.EP, Math.min(low, prevLow));
        series.setBoolean(index, Values.LONG, false);
        series.setComplete(index, series.isBarComplete(index));
        series.setBoolean(index, Signals.SAR_LONG, true);
        if (!latest) {
          ctx.signal(index, Signals.SAR_LONG, get("SIGNAL_SAR_LONG", instr.format(low), instr.format(SAR)), low);
        }
        return;
      }
      if (EP < high) EP = high;      
    }
    else {
      SAR=pSAR - pAF * (pSAR - pEP); 
      if (EP > low && AF+AFstep <= AFmax) AF+=AFstep;
      if (SAR < prevHigh) SAR = prevHigh;
      if (SAR < prevHigh2) SAR = prevHigh2;
      if (SAR < high) {
        if (EP > low) EP = low;
        series.setDouble(index, Values.SAR, EP);
        series.setDouble(index, Values.AF, AFinitial);
        series.setDouble(index, Values.EP, Math.max(high, prevHigh));
        series.setBoolean(index, Values.LONG, true);
        series.setComplete(index, series.isBarComplete(index));
        series.setBoolean(index, Signals.SAR_SHORT, true);
        if (!latest) {
          ctx.signal(index, Signals.SAR_SHORT, get("SIGNAL_SAR_SHORT", instr.format(high), instr.format(oldSAR)), high);
        }
        return;
      }
      if (EP > low) EP = low;
    }
    
    series.setDouble(index, Values.SAR, SAR);
    series.setDouble(index, Values.AF, AF);
    series.setDouble(index, Values.EP, EP);
    series.setBoolean(index, Values.LONG, isLong);
    series.setComplete(index);    
  }
}
