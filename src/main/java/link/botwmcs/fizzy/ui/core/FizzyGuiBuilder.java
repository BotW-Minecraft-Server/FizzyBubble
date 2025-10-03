package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.frame.FramePainter;

public final class FizzyGuiBuilder {
    private int cols = 9, rows = 3;
    private HostType hostType = HostType.SCREEN;
    private FramePainter frame;
    private BgPainter bg;
    private Integer overrideW, overrideH;

    public static FizzyGuiBuilder start() { return new FizzyGuiBuilder(); }

    public FizzyGuiBuilder sizeSlots(int rows) {this.cols = 9; this.rows = rows; return this; }
    public FizzyGuiBuilder sizeSlots(int cols, int rows) { this.cols = cols; this.rows = rows; return this; }
    public FizzyGuiBuilder host(HostType hostType) { this.hostType = hostType; return this; }
    public FizzyGuiBuilder frame(FramePainter painter) { this.frame = painter; return this; }
    public FizzyGuiBuilder background(BgPainter painter) { this.bg = painter; return this; }
    public FizzyGuiBuilder overrideSizePx(int w, int h) { this.overrideW = w; this.overrideH = h; return this; } // 增加 overrideSizePx() 链式方法


    public FizzyGui build() {
        if (frame == null) throw new IllegalStateException("FramePainter not set");
        FizzyGuiSpec spec = new FizzyGuiSpec(cols, rows, hostType);
        BgPainter effectiveBg = (bg != null) ? bg : new FizzyBg(BgType.STONE);
        return new FizzyGui(spec, frame, effectiveBg, overrideW, overrideH);

    }

}
