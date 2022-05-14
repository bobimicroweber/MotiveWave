package com.motivewave.platform.study.overlay;

import java.awt.Color;
import java.awt.Font;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SliderDescriptor;
import com.motivewave.platform.sdk.draw.Label;
import com.motivewave.platform.sdk.draw.Line;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Price Labels */
@StudyHeader(
    namespace="com.motivewave", 
    id="PRICE_LABELS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_PRICE_LABELS", 
    desc="DESC_PRICE_LABELS",
    menu="MENU_OVERLAY",
    overlay=true,
    helpLink="http://www.motivewave.com/studies/price_labels.htm")
public class PriceLabels extends Study 
{
  final static String TOP_LABEL = "topLabel", TOP_BG = "topBg", BOTTOM_LABEL = "bottomLabel", BOTTOM_BG = "bottomBg";
  final static String EXT_BARS = "extBars", EXT_ENABLED = "extEnabled";
  
  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));
    
    var inputs = tab.addGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.TOP_STRENGTH, get("LBL_TOP_STRENGTH"), 10, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.BOTTOM_STRENGTH, get("LBL_BOTTOM_STRENGTH"), 10, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(EXT_BARS, get("LBL_EXT_BARS"), 20, 1, 9999, 1), 
        new BooleanDescriptor(EXT_ENABLED, get("LBL_ENABLED"), false));
    inputs.addRow(new FontDescriptor(Inputs.FONT, get("LBL_FONT"), defaults.getFont()));
    
    var colors = tab.addGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(TOP_LABEL, get("LBL_TOP_LABELS"), defaults.getLineColor(), 1.0f, null, true, false, true));
    colors.addRow(new ColorDescriptor(TOP_BG, get("LBL_TOP_BG"), defaults.getBackgroundColor()));
    colors.addRow(new PathDescriptor(BOTTOM_LABEL, get("LBL_BOTTOM_LABELS"), defaults.getLineColor(), 1.0f, null, true, false, true));
    colors.addRow(new ColorDescriptor(BOTTOM_BG, get("LBL_BOTTOM_BG"), defaults.getBackgroundColor()));
    
    sd.addDependency(new EnabledDependency(EXT_ENABLED, EXT_BARS));
    sd.addDependency(new EnabledDependency(TOP_LABEL, TOP_BG));
    sd.addDependency(new EnabledDependency(BOTTOM_LABEL, BOTTOM_BG));

    // Quick Settings (Tool Bar and Popup Editor)
    sd.addQuickSettings(new SliderDescriptor(Inputs.TOP_STRENGTH, get("LBL_TOP_STRENGTH"), 10, 1, 9999, true, () -> Enums.Icon.ARROW_UP.get()));
    sd.addQuickSettings(new SliderDescriptor(Inputs.BOTTOM_STRENGTH, get("LBL_BOTTOM_STRENGTH"), 10, 1, 9999, true, () -> Enums.Icon.ARROW_DOWN.get()));
    sd.addQuickSettings(TOP_LABEL, TOP_BG, BOTTOM_LABEL, BOTTOM_BG);

    var desc = createRD();
    desc.setLabelSettings(Inputs.TOP_STRENGTH, Inputs.BOTTOM_STRENGTH);
  }
  
  @Override
  protected void calculateValues(DataContext ctx)
  {
    var defaults = ctx.getDefaults();
    int topStrength = getSettings().getInteger(Inputs.TOP_STRENGTH);
    int bottomStrength = getSettings().getInteger(Inputs.BOTTOM_STRENGTH);
    Integer extBars = getSettings().getInteger(EXT_BARS);
    if (extBars == null) extBars = 20;
    Boolean extend = getSettings().getBoolean(EXT_ENABLED);
    if (extend == null) extend = false;
    var topLabel = getSettings().getPath(TOP_LABEL);
    var bottomLabel = getSettings().getPath(BOTTOM_LABEL);
    var fi = getSettings().getFont(Inputs.FONT);
    Font f = defaults.getFont();
    if (fi != null) f = fi.getFont();
    Color topBg = getSettings().getColor(TOP_BG);
    if (topBg == null) topBg = defaults.getBackgroundColor();
    Color bottomBg = getSettings().getColor(BOTTOM_BG);
    if (bottomBg == null) bottomBg = defaults.getBackgroundColor();
    
    var series = ctx.getDataSeries();
    var instr = ctx.getInstrument();
    clearFigures();
    
    if (topLabel.isEnabled()) {
      for(var sp : series.calcSwingPoints(true, topStrength)) {
        var lbl = new Label(instr.format(sp.getValue()), f, topLabel.getColor(), topBg);
        lbl.setLocation(sp.getTime(), sp.getValue());
        lbl.getText().setBorderStroke(topLabel.getStroke());
        lbl.setLineStroke(defaults.getSolidLine());
        addFigure(lbl);
        
        if (extend) {
          var line = new Line(sp.getTime(), sp.getValue(), series.getStartTime(sp.getIndex() + extBars), sp.getValue());
          line.setStroke(topLabel.getStroke());
          line.setColor(topLabel.getColor());
          addFigure(line);
        }
      }
    }
    
    if (bottomLabel.isEnabled()) {
      for(var sp : series.calcSwingPoints(false, bottomStrength)) {
        var lbl = new Label(instr.format(sp.getValue()), f, bottomLabel.getColor(), bottomBg);
        lbl.setLocation(sp.getTime(), sp.getValue());
        lbl.getText().setBorderStroke(bottomLabel.getStroke());
        lbl.setLineStroke(defaults.getSolidLine());
        addFigure(lbl);
        if (extend) {
          var line = new Line(sp.getTime(), sp.getValue(), series.getStartTime(sp.getIndex() + extBars), sp.getValue());
          line.setStroke(bottomLabel.getStroke());
          line.setColor(bottomLabel.getColor());
          addFigure(line);
        }
      }
    }
  }
}
