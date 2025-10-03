package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.frame.FramePainter;

public final class FizzyGuiBuilder {
    private int cols = 9, rows = 3;
    private HostType hostType = HostType.SCREEN;
    private FramePainter frame;
    private Integer overrideW, overrideH;

    public static FizzyGuiBuilder start() { return new FizzyGuiBuilder(); }

    public FizzyGuiBuilder sizeSlots(int cols, int rows) { this.cols = cols; this.rows = rows; return this; }
    public FizzyGuiBuilder host(HostType hostType) { this.hostType = hostType; return this; }
    public FizzyGuiBuilder background(FramePainter painter) { this.frame = painter; return this; }
    public FizzyGuiBuilder overrideSizePx(int w, int h) { this.overrideW = w; this.overrideH = h; return this; } // 增加 overrideSizePx() 链式方法


    public FizzyGui build() {
        if (frame == null) throw new IllegalStateException("FramePainter not set");
        FizzyGuiSpec spec = new FizzyGuiSpec(cols, rows, hostType);
        return new FizzyGui(spec, frame, overrideW, overrideH);
    }

}
