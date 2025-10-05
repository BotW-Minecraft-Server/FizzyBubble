package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.PadBuilder;
import link.botwmcs.fizzy.ui.pad.SlotPadSpec;

import java.util.ArrayList;
import java.util.List;

public final class FizzyGuiBuilder {
    private int cols = 9, rows = 3;
    private HostType hostType = HostType.SCREEN;
    private FramePainter frame;
    private BgPainter bg;
    private BehindPainter behind;
    private Integer overrideW, overrideH;
    private final List<PadBuilder> pads;

    private FizzyGuiBuilder() {
        this.pads = new ArrayList<>();
    }

    public static FizzyGuiBuilder start() { return new FizzyGuiBuilder(); }

    public FizzyGuiBuilder sizeSlots(int rows) {this.cols = 9; this.rows = rows; return this; }
    public FizzyGuiBuilder sizeSlots(int cols, int rows) { this.cols = cols; this.rows = rows; return this; }
    public FizzyGuiBuilder host(HostType hostType) { this.hostType = hostType; return this; }
    public FizzyGuiBuilder frame(FramePainter painter) { this.frame = painter; return this; }
    public FizzyGuiBuilder background(BgPainter painter) { this.bg = painter; return this; }
    public FizzyGuiBuilder behind(BehindPainter behind) { this.behind = behind; return this; }
    public FizzyGuiBuilder overrideSizePx(int w, int h) { this.overrideW = w; this.overrideH = h; return this; } // 增加 overrideSizePx() 链式方法

    public PadBuilder pad(int rowStart, int colStart, int rowEnd, int colEnd) {
        if (rowStart < 1) {
            throw new IllegalArgumentException("rowStart must be >= 1");
        }
        if (colStart < 1) {
            throw new IllegalArgumentException("colStart must be >= 1");
        }
        if (rowEnd < rowStart) {
            throw new IllegalArgumentException("rowEnd must be >= rowStart");
        }
        if (colEnd < colStart) {
            throw new IllegalArgumentException("colEnd must be >= colStart");
        }

        PadBuilder builder = new PadBuilder(this, rowStart, colStart, rowEnd, colEnd);
        pads.add(builder);
        return builder;
    }

    public FizzyGui build() {
        if (frame == null) throw new IllegalStateException("FramePainter not set");
        FizzyGuiSpec spec = new FizzyGuiSpec(cols, rows, hostType);
        BgPainter effectiveBg = (bg != null) ? bg : new FizzyBg(BgType.STONE);
        BehindPainter effectiveBehind = (behind != null) ? behind : new BlurBehind();
        List<SlotPadSpec> padSpecs = new ArrayList<>(pads.size());
        for (PadBuilder pad : pads) {
            if (pad.rowEnd > rows) {
                throw new IllegalStateException("Pad rowEnd exceeds configured rows");
            }
            if (pad.colEnd > cols) {
                throw new IllegalStateException("Pad colEnd exceeds configured cols");
            }
            padSpecs.add(new SlotPadSpec(pad.rowStart, pad.colStart, pad.rowEnd, pad.colEnd, pad.elements));
        }
        return new FizzyGui(spec, frame, effectiveBg, effectiveBehind, overrideW, overrideH, padSpecs);

    }

}
