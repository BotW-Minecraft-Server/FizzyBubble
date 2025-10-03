package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.frame.FramePainter;

public class FizzyGui {
    private final FizzyGuiSpec spec;
    private final FramePainter frame;
    private final BgPainter bg;
    private final Integer overrideW;  // 可空
    private final Integer overrideH;  // 可空

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame) {
        this(spec, frame, new FizzyBg(BgType.STONE), null, null);
    }

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame, BgPainter bg, Integer overrideW, Integer overrideH) {
        this.spec = spec; this.frame = frame; this.bg = bg; this.overrideW = overrideW; this.overrideH = overrideH;
    }

    public FizzyGuiSpec spec() { return spec; }
    public FramePainter frame() { return frame; }
    public BgPainter background() { return bg; }

    public int widthPx()  { return overrideW != null ? overrideW : spec.pixelWidth(); }
    public int heightPx() { return overrideH != null ? overrideH : spec.pixelHeight(); }



}
