package com.motivewave.platform.study.volume;import java.awt.Color;import com.motivewave.platform.sdk.common.DataContext;import com.motivewave.platform.sdk.common.Defaults;import com.motivewave.platform.sdk.common.Inputs;import com.motivewave.platform.sdk.common.LineInfo;import com.motivewave.platform.sdk.common.desc.ColorDescriptor;import com.motivewave.platform.sdk.common.desc.GuideDescriptor;import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;import com.motivewave.platform.sdk.common.desc.ValueDescriptor;import com.motivewave.platform.sdk.study.Study;import com.motivewave.platform.sdk.study.StudyHeader;/** Multi Vote OBV  064 */@StudyHeader(    namespace="com.motivewave",     id="ID_MVOBV",     rb="com.motivewave.platform.study.nls.strings2",    name="NAME_MULTI_VOTE_OBV",    label="LBL_MVOBV",    desc="DESC_MVOBV",    //menu="MENU_VOLUME_BASED",    helpLink = "http://www.motivewave.com/studies/multi_vote_onbalance_volume.htm",    overlay=false,    requiresVolume=true,    studyOverlay=true)public class MultVoteObv extends Study{  enum Values { MVO }    @Override  public void initialize(Defaults defaults)  {    var sd = createSD();    var tab = sd.addTab(get("TAB_GENERAL"));     var settings = tab.addGroup(get("COLORS"));    settings.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreen()));    settings.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRed()));    settings.addRow(new IndicatorDescriptor(Inputs.IND, get("IND"), defaults.getLineColor(), null, false, true, true));    var guides = tab.addGroup(get("GUIDE"));    var mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("MIDDLE_GUIDE"), 0, -999.1, 999.1, .1, true);    mg.setDash(new float[] {3, 3});    guides.addRow(mg);    // Quick Settings (Tool Bar and Popup Editor)    sd.addQuickSettings(Inputs.UP_COLOR, Inputs.DOWN_COLOR);    var desc = createRD();    desc.setLabelSettings();    desc.exportValue(new ValueDescriptor(Values.MVO, get("LBL_MVOBV"), new String[] {}));    desc.declareBars(Values.MVO);    desc.declareIndicator(Values.MVO, Inputs.IND);    desc.setRangeKeys(Values.MVO);    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3f, 3f}));   }  @Override  public void onLoad(Defaults defaults)  {    setMinBars(20);  }  @Override    protected void calculate(int index, DataContext ctx)  {    if (index < 1) return;    var series = ctx.getDataSeries();        double close = series.getClose(index);    double prevC = series.getClose(index-1);    double low = series.getLow(index);    double prevL = series.getLow(index-1);    double high = series.getHigh(index);    double prevH = series.getHigh(index-1);    double volume = series.getVolume(index)/1000000.0; //volume in millions    int highVote = 0;    int lowVote = 0;    int closeVote = 0;    if (high > prevH) highVote = 1;    if (high < prevH) highVote = -1;    if (low > prevL) lowVote = 1;    if (low < prevL) lowVote = -1;    if (close > prevC) closeVote = 1;    if (close < prevC) closeVote = -1;           int totVote = highVote + lowVote + closeVote;    double prevMvo = series.getDouble(index, Values.MVO, volume);    double mvo = prevMvo + (volume * totVote);    series.setDouble(index, Values.MVO, mvo);        var midGuide = getSettings().getGuide(Inputs.MIDDLE_GUIDE);    double midG = midGuide.getValue();    Color upC = getSettings().getColor(Inputs.UP_COLOR);    Color dnC = getSettings().getColor(Inputs.DOWN_COLOR);    if (mvo > midG) series.setBarColor(index, Values.MVO, upC);    else series.setBarColor(index, Values.MVO, dnC);      series.setComplete(index);  }  }