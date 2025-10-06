package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import link.botwmcs.fizzy.ui.split.SplitPainter;
import link.botwmcs.fizzy.ui.split.SplitSpec;

import java.util.List;

public class FizzyGui {
    private final FizzyGuiSpec spec;
    private final FramePainter frame;
    private final BgPainter bg;
    private final BehindPainter behind;
    private final Integer overrideW;  // 可空
    private final Integer overrideH;  // 可空
    private final List<PadSpec> pads;
    private final SplitPainter splitPainter;
    private final List<SplitSpec> splits;
    private ElementPainter below;

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame) {
        this(spec, frame, new FizzyBg(BgType.STONE), new BlurBehind(), null, null);
    }

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame, BgPainter bg, BehindPainter behind, Integer overrideW, Integer overrideH) {
        this(spec, frame, bg, behind, overrideW, overrideH, null, null, null, null);
    }

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame, BgPainter bg, BehindPainter behind, Integer overrideW, Integer overrideH, List<PadSpec> pads, SplitPainter splitPainter, List<SplitSpec> splits, ElementPainter below) {
        this.spec = spec;
        this.frame = frame;
        this.bg = bg;
        this.behind = behind;
        this.overrideW = overrideW;
        this.overrideH = overrideH;
        this.pads = List.copyOf(pads != null ? pads : List.of());
        this.splitPainter = splitPainter;
        this.splits = List.copyOf(splits != null ? splits : List.of());
        this.below = below;
    }

    public FizzyGuiSpec spec() { return spec; }
    public FramePainter frame() { return frame; }
    public BgPainter background() { return bg; }
    public BehindPainter behind() { return behind; }
    public List<PadSpec> pads() { return pads; }
    public SplitPainter splitPainter() { return splitPainter; }
    public List<SplitSpec> splits() { return splits; }
    public ElementPainter below() { return below; }
    public boolean hasBelow() { return below != null; }

    public int widthPx()  { return overrideW != null ? overrideW : spec.pixelWidth(); }
    public int heightPx() { return overrideH != null ? overrideH : spec.pixelHeight(); }



}
