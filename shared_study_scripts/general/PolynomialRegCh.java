package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.BarInput;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** PRC 203 */
@StudyHeader(
  namespace="com.motivewave",
  id="ID_POLY_REG_CH",
  rb="com.motivewave.platform.study.nls.strings2",
  label="LBL_PRC",
  name="NAME_POLYNOMIAL_REGRESSION_CHANNEL",
  desc="DESC_POLY_REG_CH",
  helpLink= "",  //"http://www.motivewave.com/studies/polynomial_regressio_channel.htm",
  signals=false,
  overlay=true,
  studyOverlay=true)
public class PolynomialRegCh extends Study
{
  static final String STD_DEV1 = "stdDev1"; 
  static final String STD_DEV2 = "stdDev2"; 
  static final String PATH5 = "path5";
  static final String PATH6 = "path6"; 
  static final String PATH7 = "path7"; 

  static final String IND3 = "ind3"; 
  static final String IND4 = "ind4"; 
  static final String IND5 = "ind5"; 

  enum Values {PRC, PRC_HIGH1, PRC_LOW1, PRC_HIGH2, PRC_LOW2 };

  @Override
  public void initialize(Defaults defaults)
  {
    var sd=createSD();
    var tab=sd.addTab(get("TAB_GENERAL"));

    var inputs=tab.addGroup(get("INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("NUMBER_OF_POINTS"), 120, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("DEGREE"), 3, 1, 4, 1));
    inputs.addRow(new DoubleDescriptor(STD_DEV1, get("STD_DEV1"), 1.62, 0, 99.01, .01));
    inputs.addRow(new DoubleDescriptor(STD_DEV2, get("STD_DEV2"), 2.00, 0, 99.01, .01));

    var settings=tab.addGroup(get("PATHS"));
    settings.addRow(new PathDescriptor(Inputs.PATH, get("PRC"), defaults.getLineColor(), 1.0f, null, true, false, true));
    settings.addRow(new PathDescriptor(Inputs.PATH2, get("HIGH_BAND1"), defaults.getRed(), 1.0f, null, true, false, true));
    settings.addRow(new PathDescriptor(Inputs.PATH3, get("LOW_BAND1"), defaults.getRed(), 1.0f, null, true, false, true));
    settings.addRow(new PathDescriptor(Inputs.PATH4, get("HIGH_BAND2"), defaults.getGreen(), 1.0f, null, true, false, true));
    settings.addRow(new PathDescriptor(PATH5, get("LOW_BAND2"), defaults.getGreen(), 1.0f, null, true, false, true));
 
    tab=sd.addTab(get("DISPLAY"));

    var settings2=tab.addGroup(get("INDICATORS"));
    settings2.addRow(new IndicatorDescriptor(Inputs.IND, get("PRC_IND"), defaults.getLineColor(), null, false, true, true));
    settings2.addRow(new IndicatorDescriptor(Inputs.IND2, get("HIGH_IND1"), defaults.getRed(), null, false, true, true));
    settings2.addRow(new IndicatorDescriptor(IND3, get("LOW_IND1"), defaults.getRed(), null, false, true, true));
    settings2.addRow(new IndicatorDescriptor(IND4, get("HIGH_IND2"), defaults.getGreen(), null, false, true, true));
    settings2.addRow(new IndicatorDescriptor(IND5, get("LOW_IND2"), defaults.getGreen(), null, false, true, true));

    var settings3=tab.addGroup(get("SHADING"));
    settings3.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("TOP_FILL"), Inputs.PATH2, Inputs.PATH4, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    settings3.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("BOTTOM_FILL"), Inputs.PATH3, PATH5, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(Inputs.INPUT);
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD, get("NUMBER_OF_POINTS"), 120, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.PERIOD2, get("DEGREE"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));
    sd.addQuickSettings(STD_DEV1, STD_DEV2, Inputs.PATH, Inputs.PATH2, Inputs.PATH3, Inputs.PATH4, PATH5);

    var desc=createRD();
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.PERIOD2, STD_DEV1, STD_DEV2);
    desc.exportValue(new ValueDescriptor(Values.PRC, get("PRC"), new String[] {Inputs.INPUT, Inputs.PERIOD, Inputs.PERIOD2 }));
 
    desc.declarePath(Values.PRC, Inputs.PATH);
    desc.declarePath(Values.PRC_HIGH1, Inputs.PATH2);
    desc.declarePath(Values.PRC_LOW1, Inputs.PATH3);
    desc.declarePath(Values.PRC_HIGH2, Inputs.PATH4);
    desc.declarePath(Values.PRC_LOW2, PATH5);
    
    desc.declareIndicator(Values.PRC, Inputs.IND);
    desc.declareIndicator(Values.PRC_HIGH1, Inputs.IND2);
    desc.declareIndicator(Values.PRC_LOW1, IND3);
    desc.declareIndicator(Values.PRC_HIGH2, IND4);
    desc.declareIndicator(Values.PRC_LOW2, IND5);

    desc.setRangeKeys(Values.PRC, Values.PRC_HIGH2, Values.PRC_LOW2, Values.PRC_HIGH1, Values.PRC_LOW1);
  }

  @Override
  public void onLoad(Defaults defaults) {
    int p1=getSettings().getInteger(Inputs.PERIOD);
    int p2=getSettings().getInteger(Inputs.PERIOD2);
    setMinBars(p1*p2);
  }

  @Override
  protected void calculate(int index, DataContext ctx) 
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    int degree = getSettings().getInteger(Inputs.PERIOD2);
    if (index < period) return;
    
    var input = (BarInput) getSettings().getInput(Inputs.INPUT); // Usually based on close.
    Double std1 = getSettings().getDouble(STD_DEV1);
    Double std2 = getSettings().getDouble(STD_DEV2);
    
    var series=ctx.getDataSeries();
    
    double [][] ai = new double[10] [10];
    double[] sx = new double[10];
    double[] b = new double[10];
    double[] x = new double[10];
    Double prcHigh1 = 0.0, prcLow1 = 0.0, prcHigh2 = 0.0, prcLow2 = 0.0, prc = 0.0;
    double sum = 0.0, tt = 0.0;
    int kk = 0, nn= 0;
    
    sx[1] = period + 1;
    nn = degree + 1;
    
    for (int mi = 1; mi <= nn * 2 -2; mi++){
      sum = 0;
      for(int n = 0; n <= period; n++){
        sum += Math.pow(n, mi);
      }
      sx[mi+1] = sum; 
    }
    
    for(int mi = 1; mi <= nn; mi++){
      sum = 0.0;
      for (int n = 0; n <= period; n++){
        if (mi == 1) sum += series.getDouble(index - n, input, 0);
        else sum += series.getDouble(index - n, input, 0) * Math.pow(n, mi -1);
      }
      b[mi] = sum;
    }
    
    for (int jj = 1; jj <= nn; jj++){
      for(int ii = 1; ii <= nn; ii++){
        kk = ii + jj -1;
        ai[ii][jj] = sx[kk];
      }
    }
    
    for(kk = 1; kk <= nn -1; kk++){
      int ll = 0;
      double mm = 0.0;
      for(int ii = kk; ii <= nn; ii++){
        if(Math.abs(ai[ii][kk]) > mm){
          mm = Math.abs(ai[ii][kk]);
          ll = ii;
        }
      }
      
      if (ll == 0) return;
      if (ll != kk){
        for(int jj = 1; jj <= nn; jj++){
          tt = ai[kk][jj];
          ai[kk][jj] = ai[ll][jj];
          ai[ll][jj] = tt;
        }
        tt = b[kk];
        b[kk] = b[ll];
        b[ll] = tt;
      }
      
      double qq = 0.0;
      for(int ii = kk +1; ii <= nn; ii++){
        qq = ai[ii][kk] / ai[kk][kk];
        for (int jj = 1; jj <= nn; jj++){
          if (jj == kk) ai[ii][jj] = 0;
          else ai[ii][jj] = ai[ii][jj] - qq * ai[kk][jj];
        }
        b[ii] = b[ii] - qq * b[kk];
      }
    }
    
    x[nn] = b[nn] / ai[nn][nn];
    for (int ii = nn -1; ii >=1; ii--){
      tt = 0;
      for(int jj = 1; jj <= nn - ii; jj++){
        tt = tt + ai[ii][ii+jj] * x[ii+jj];
        x[ii] = (1 / ai[ii][ii]) * (b[ii] - tt);
      }
    }
    
    double sq = 0.0, sq2 = 0.0;
    for (int n = 0; n <= period; n++){
      sum = 0;
      for(kk = 1; kk <= degree; kk++){
        sum += x[kk+1] * Math.pow(n, kk);
      }
      prc = (x[1] + sum);
      series.setDouble(index-n, Values.PRC, prc);
      
      sq += Math.pow(series.getDouble(index - n, input, 0) - prc, 2);
      sq2 += Math.pow(series.getDouble(index - n, input, 0) - prc, 2);      
    }
    
    sq = Math.sqrt(sq / (period+1)) * std1;
    sq2 = Math.sqrt(sq2 / (period+1)) * std2;
    
    for(int n = 0; n <= period; n++){
      prcHigh1 = series.getDouble(index-n, Values.PRC, 0.0) + sq;
      prcLow1 = series.getDouble(index-n, Values.PRC, 0.0) - sq;
      prcHigh2 = series.getDouble(index-n, Values.PRC, 0.0) + sq2;
      prcLow2 = series.getDouble(index-n, Values.PRC, 0.0) - sq2;
      series.setDouble(index-n, Values.PRC_HIGH1, prcHigh1);
      series.setDouble(index-n, Values.PRC_LOW1, prcLow1);
      series.setDouble(index-n, Values.PRC_HIGH2, prcHigh2);
      series.setDouble(index-n, Values.PRC_LOW2, prcLow2);
    }
    series.setComplete(index, series.isBarComplete(index));
  }
}