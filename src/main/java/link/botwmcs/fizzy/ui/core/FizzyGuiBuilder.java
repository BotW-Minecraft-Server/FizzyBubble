package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.*;
import link.botwmcs.fizzy.ui.split.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FizzyGuiBuilder {
    private int cols = 9, rows = 3;
    private HostType hostType = HostType.SCREEN;
    private FramePainter frame;
    private BgPainter bg;
    private BehindPainter behind;
    private Integer overrideW, overrideH;
    private final List<BasePadBuilder<?>> pads;
    private SplitPainter splitPainter;
    private final List<SplitSpec> splits;

    private FizzyGuiBuilder() {
        this.pads = new ArrayList<>();
        this.splits = new ArrayList<>();
    }

    public static FizzyGuiBuilder start() { return new FizzyGuiBuilder(); }

    public FizzyGuiBuilder sizeSlots(int rows) {this.cols = 9; this.rows = rows; return this; }
    public FizzyGuiBuilder sizeSlots(int cols, int rows) { this.cols = cols; this.rows = rows; return this; }
    public FizzyGuiBuilder host(HostType hostType) { this.hostType = hostType; return this; }
    public FizzyGuiBuilder frame(FramePainter painter) { this.frame = painter; return this; }
    public FizzyGuiBuilder background(BgPainter painter) { this.bg = painter; return this; }
    public FizzyGuiBuilder behind(BehindPainter behind) { this.behind = behind; return this; }
    public FizzyGuiBuilder overrideSizePx(int w, int h) { this.overrideW = w; this.overrideH = h; return this; } // 增加 overrideSizePx() 链式方法
    private FizzyGuiBuilder splitPainter(SplitPainter painter) { this.splitPainter = Objects.requireNonNull(splitPainter, "splitPainter"); return this; }

    public SlotPadBuilder pad(int rowStart, int colStart, int rowEnd, int colEnd) {
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

        SlotPadBuilder builder = new SlotPadBuilder(this, rowStart, colStart, rowEnd, colEnd);
        pads.add(builder);
        return builder;
    }

    public PixelPadBuilder padByPx(int leftPx, int topPx, int widthPx, int heightPx) {
        PixelPadBuilder builder = new PixelPadBuilder(this, leftPx, topPx, widthPx, heightPx);
        pads.add(builder);
        return builder;
    }

    public FramePadBuilder padByFrame() {
        FramePadBuilder builder = new FramePadBuilder(this);
        pads.add(builder);
        return builder;
    }

    public FizzyGuiBuilder split(int rowStart, int colStart, int rowEnd, int colEnd) {
        splits.add(new SlotSplitSpec(rowStart, colStart, rowEnd, colEnd));
        return this;
    }

    public FizzyGuiBuilder splitByPx(int offsetX, int offsetY, int lengthPx, SplitType type) {
        splits.add(new PixelSplitSpec(offsetX, offsetY, lengthPx, Objects.requireNonNull(type, "type")));
        return this;
    }


    public FizzyGui build() {
        if (frame == null) throw new IllegalStateException("FramePainter not set");
        // Base spec
        FizzyGuiSpec spec = new FizzyGuiSpec(cols, rows, hostType);

        // Background
        BgPainter effectiveBg = (bg != null) ? bg : new FizzyBg(BgType.STONE);
        BehindPainter effectiveBehind = (behind != null) ? behind : new BlurBehind();

        // Pads
        List<PadSpec> padSpecs = new ArrayList<>(pads.size());
        for (BasePadBuilder<?> pad : pads) {
            if (pad instanceof SlotPadBuilder slotPad) {
                if (slotPad.rowEnd > rows) {
                    throw new IllegalStateException("Pad rowEnd exceeds configured rows");
                }
                if (slotPad.colEnd > cols) {
                    throw new IllegalStateException("Pad colEnd exceeds configured cols");
                }
            }
            padSpecs.add(pad.toSpec());
        }

        // Splits
        List<SplitSpec> splitSpecs = new ArrayList<>(splits.size());
        for (SplitSpec split : splits) {
            if (split instanceof SlotSplitSpec slotSplit) {
                if (slotSplit.rowStart() < 1 || slotSplit.rowEnd() > rows) {
                    throw new IllegalStateException("Split row range exceeds configured rows");
                }
                if (slotSplit.colStart() < 1 || slotSplit.colEnd() > cols) {
                    throw new IllegalStateException("Split column range exceeds configured cols");
                }
            }
            splitSpecs.add(split);
        }
        SplitPainter effectiveSplitPainter = this.splitPainter;
        if (effectiveSplitPainter == null && !splitSpecs.isEmpty()) {
            effectiveSplitPainter = new FizzySplit();
        }

        // Final return
        return new FizzyGui(spec, frame, effectiveBg, effectiveBehind, overrideW, overrideH, padSpecs, effectiveSplitPainter, splitSpecs);
    }

}
