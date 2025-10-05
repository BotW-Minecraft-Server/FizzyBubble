package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.SlotPadSpec;

import java.util.List;

public class FizzyGui {
    private final FizzyGuiSpec spec;
    private final FramePainter frame;
    private final BgPainter bg;
    private final BehindPainter behind;
    private final Integer overrideW;  // 可空
    private final Integer overrideH;  // 可空
    private final List<SlotPadSpec> pads;

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame) {
        this(spec, frame, new FizzyBg(BgType.STONE), new BlurBehind(), null, null, List.of());
    }

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame, BgPainter bg, BehindPainter behind, Integer overrideW, Integer overrideH, List<SlotPadSpec> pads) {
        this.spec = spec;
        this.frame = frame;
        this.bg = bg;
        this.behind = behind;
        this.overrideW = overrideW;
        this.overrideH = overrideH;
        this.pads = List.copyOf(pads);
    }

    public FizzyGuiSpec spec() { return spec; }
    public FramePainter frame() { return frame; }
    public BgPainter background() { return bg; }
    public BehindPainter behind() { return behind; }
    public List<SlotPadSpec> pads() { return pads; }

    public int widthPx()  { return overrideW != null ? overrideW : spec.pixelWidth(); }
    public int heightPx() { return overrideH != null ? overrideH : spec.pixelHeight(); }



}
