package com.motivewave.platform.study.overlay;

import com.motivewave.platform.sdk.study.StudyHeader;

/** Zig Zag Oscillator.  This is similar to the Zig Zag study, but plots using the price movement from the top/bottom. */
@StudyHeader(
    namespace="com.motivewave", 
    id="ZIG_ZAG_OSC", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ZIG_ZAG_OSC", 
    desc="DESC_ZIG_ZAG_OSC",
    overlay=false,
    studyOverlay=true,
    supportsBarUpdates=false)
public class ZigZagOsc extends ZigZag 
{
  @Override
  protected boolean isOverlay() { return false; }

}
