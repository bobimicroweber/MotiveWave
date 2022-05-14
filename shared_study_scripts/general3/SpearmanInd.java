package com.motivewave.platform.study.general3;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Enums;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.InputDescriptor;import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;import com.motivewave.platform.sdk.common.desc.PathDescriptor;import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;import com.motivewave.platform.sdk.common.desc.SliderDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Spearman Indicator 195 */@StudyHeader(  namespace="com.motivewave",  id="ID_SPEARMAN",  rb="com.motivewave.platform.study.nls.strings2",  label="LBL_SPRMAN",  name="NAME_SPEARMAN_INDICATOR",  desc="DESC_SPRMAN",  helpLink="http://www.motivewave.com/studies/spearman_indicator.htm",  signals=true,  overlay=false,  studyOverlay=true)public class SpearmanInd extends Study {  enum Values { SC, SIG }  @Override  public void initialize(Defaults defaults)  {    var sd=createSD();    var tab=sd.addTab(get("TAB_GENERAL"));    var inputs=tab.addGroup(get("INPUTS"));    inputs.addRow(new InputDescriptor(Str.INPUT1, get("INPUT"), Enums.BarInput.CLOSE));    inputs.addRow(new MAMethodDescriptor(Str.METHOD1, get("METHOD"), Enums.MAMethod.SMA));    inputs.addRow(new IntegerDescriptor(Str.PERIOD1, get("SPEARMAN_PERIOD"), 10, 5, 200, 1));    inputs.addRow(new IntegerDescriptor(Str.PERIOD2, get("SIGNAL_PERIOD"), 3, 1, 200, 1));    var settings=tab.addGroup(get("PATHS"));    settings.addRow(new PathDescriptor(Str.PATH1, get("SPEARMAN"), defaults.getLineColor(), 1.0f, null, true, false, true));    settings.addRow(new PathDescriptor(Str.PATH2, get("SIGNAL"), defaults.getRed(), 1.0f, null, true, false, true));    settings=tab.addGroup(get("INDICATORS"));    settings.addRow(new IndicatorDescriptor(Str.IND1, get("SPEARMAN"), defaults.getLineColor(), null, false, true, true));    settings.addRow(new IndicatorDescriptor(Str.IND2, get("SIGNAL"), defaults.getRed(), null, false, true, true));    tab=sd.addTab(get("TAB_DISPLAY"));    var guides=tab.addGroup(get("GUIDES"));    var topDesc=new GuideDescriptor(Str.TOP_GUIDE1, get("TOP_GUIDE"), 80, 0, 999, 1, true);    topDesc.setLineColor(defaults.getRed());    guides.addRow(topDesc);    var mg=new GuideDescriptor(Str.MID_GUIDE1, get("MIDDLE_GUIDE"), 0, -999, 999, 1, true);    mg.setDash(new float[] { 3, 3 });    guides.addRow(mg);    var bottomDesc=new GuideDescriptor(Str.BOTT_GUIDE1, get("BOTTOM_GUIDE"), -80, -999, 0, 1, true);    bottomDesc.setLineColor(defaults.getGreen());    guides.addRow(bottomDesc);    settings=tab.addGroup(get("SHADING"));    settings.addRow(new ShadeDescriptor(Str.TOP_FILL1, get("TOP_FILL"), Str.MID_GUIDE1, Str.PATH1,      Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));    settings.addRow(new ShadeDescriptor(Str.BOTT_FILL1, get("BOTTOM_FILL"), Str.MID_GUIDE1, Str.PATH1,      Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Str.INPUT1, Str.METHOD1);    sd.addQuickSettings(new SliderDescriptor(Str.PERIOD1, get("SPEARMAN_PERIOD"), 10, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(new SliderDescriptor(Str.PERIOD2, get("SIGNAL_PERIOD"), 3, 1, 9999, true, () -> Enums.Icon.SINE_WAVE.get()));    sd.addQuickSettings(Str.PATH1, Str.PATH2, Str.TOP_FILL1, Str.BOTT_FILL1);    var desc=createRD();    desc.setLabelSettings(Str.INPUT1, Str.METHOD1, Str.PERIOD1, Str.PERIOD2);    desc.exportValue(new ValueDescriptor(Values.SC, get("SPEARMAN"), new String[] { Str.INPUT1, Str.METHOD1, Str.PERIOD1 }));    desc.exportValue(new ValueDescriptor(Values.SIG, get("SIGNAL"), new String[] { Str.METHOD1, Str.PERIOD2, }));    desc.declarePath(Values.SC, Str.PATH1);    desc.declarePath(Values.SIG, Str.PATH2);    desc.declareIndicator(Values.SC, Str.IND1);    desc.declareIndicator(Values.SIG, Str.IND2);    desc.setRangeKeys(Values.SC, Values.SIG);  }  @Override  public void onLoad(Defaults defaults)  {    int p1=getSettings().getInteger(Str.PERIOD1);    int p2=getSettings().getInteger(Str.PERIOD2);    setMinBars(Math.max(p1, p2));  }  @Override  protected void calculateValues( DataContext ctx)  {    int n=getSettings().getInteger(Str.PERIOD1);    int sigPeriod=getSettings().getInteger(Str.PERIOD2);    Object key=getSettings().getInput(Str.INPUT1, Enums.BarInput.CLOSE);    var method=getSettings().getMAMethod(Str.METHOD1);    var series=ctx.getDataSeries();      int size = series.size();    int r1[] = new int[n+1];    int r22[] = new int[n+1];    double r11[] = new double[n+1];    double r21[] = new double[n+1];    double temp = 0;    double coefcorr = 0, sc = 0;    int changed = 0, found = 0;    double absum = 0, ab = 0, ab2 = 0 ;      for (int k = n; k < size; k++ ){      for (int i = n; i >= 1; i--){        r1[i] = i;        r22[i] = i;        r11[i] = series.getDouble((k - n + i), key, 0);         r21[i] = series.getDouble((k - n + i), key, 0);       }      //sort r21 descending      changed = 1;      while ( changed > 0) {        changed = 0;        for (int i = 1; i <= (n-1); i++){          if (r21[i+1] < r21[i]) {            temp = r21[i];            r21[i] = r21[i + 1];            r21[i+1] = temp;            changed = 1;          }        } //for      }  //while      ////      for (int i = 1; i <= n; i++){        found = 0;        while (found < 1) {          for (int j = 1; j <= n; j++){            if (r21[j] == r11[i]) {  //?? i              r22[i] = j;              found = 1;            }          }  //for        }  //while      }  //for     /////////      absum = 0;      for (int i = 1; i <= n; i++){        ab = r1[i] - r22[i];        ab2 = ab * ab;        absum = absum + ab2;      }  //for      coefcorr = 1 - ((6 * absum) / (n * ((n * n) - 1)));      sc = 100.0 * coefcorr;      series.setDouble(k, Values.SC, sc);      Double sig = series.ma(method, k, sigPeriod, Values.SC);      series.setDouble(k, Values.SIG, sig);      series.setComplete(k);    }  }}