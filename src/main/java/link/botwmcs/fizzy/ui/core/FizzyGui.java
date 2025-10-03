package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.bg.BackgroundPainter;

public class FizzyGui {
    private final FizzyGuiSpec spec;
    private final BackgroundPainter bg;
    private final Integer overrideW;  // 可空
    private final Integer overrideH;  // 可空

    public FizzyGui(FizzyGuiSpec spec, BackgroundPainter bg) {
        this(spec, bg, null, null);
    }

    public FizzyGui(FizzyGuiSpec spec, BackgroundPainter bg, Integer overrideW, Integer overrideH) {
        this.spec = spec; this.bg = bg; this.overrideW = overrideW; this.overrideH = overrideH;
    }

    public FizzyGuiSpec spec() { return spec; }
    public BackgroundPainter background() { return bg; }

    public int widthPx()  { return overrideW != null ? overrideW : spec.pixelWidth(); }
    public int heightPx() { return overrideH != null ? overrideH : spec.pixelHeight(); }



}
