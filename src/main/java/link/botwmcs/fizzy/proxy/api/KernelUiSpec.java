package link.botwmcs.fizzy.proxy.api;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import link.botwmcs.fizzy.ui.split.SplitPainter;
import link.botwmcs.fizzy.ui.split.SplitSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class KernelUiSpec {
    private static final KernelUiSpec EMPTY = builder().build();

    private final @Nullable FizzyGui baseKernel;
    private final @Nullable FramePainter frame;
    private final @Nullable BgPainter background;
    private final @Nullable BehindPainter behind;
    private final @Nullable SplitPainter splitPainter;
    private final @Nullable ElementPainter below;
    private final @Nullable Integer overrideWidthPx;
    private final @Nullable Integer overrideHeightPx;
    private final List<PadSpec> pads;
    private final List<SplitSpec> splits;

    private KernelUiSpec(Builder builder) {
        this.baseKernel = builder.baseKernel;
        this.frame = builder.frame;
        this.background = builder.background;
        this.behind = builder.behind;
        this.splitPainter = builder.splitPainter;
        this.below = builder.below;
        this.overrideWidthPx = builder.overrideWidthPx;
        this.overrideHeightPx = builder.overrideHeightPx;
        this.pads = List.copyOf(builder.pads);
        this.splits = List.copyOf(builder.splits);
    }

    public static KernelUiSpec empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(KernelUiSpec source) {
        return new Builder(source);
    }

    public @Nullable FizzyGui baseKernel() {
        return baseKernel;
    }

    public @Nullable FramePainter frame() {
        return frame;
    }

    public @Nullable BgPainter background() {
        return background;
    }

    public @Nullable BehindPainter behind() {
        return behind;
    }

    public @Nullable SplitPainter splitPainter() {
        return splitPainter;
    }

    public @Nullable ElementPainter below() {
        return below;
    }

    public @Nullable Integer overrideWidthPx() {
        return overrideWidthPx;
    }

    public @Nullable Integer overrideHeightPx() {
        return overrideHeightPx;
    }

    public List<PadSpec> pads() {
        return pads;
    }

    public List<SplitSpec> splits() {
        return splits;
    }

    public boolean isEmpty() {
        return baseKernel == null
                && frame == null
                && background == null
                && behind == null
                && splitPainter == null
                && below == null
                && overrideWidthPx == null
                && overrideHeightPx == null
                && pads.isEmpty()
                && splits.isEmpty();
    }

    public static final class Builder {
        private @Nullable FizzyGui baseKernel;
        private @Nullable FramePainter frame;
        private @Nullable BgPainter background;
        private @Nullable BehindPainter behind;
        private @Nullable SplitPainter splitPainter;
        private @Nullable ElementPainter below;
        private @Nullable Integer overrideWidthPx;
        private @Nullable Integer overrideHeightPx;
        private final List<PadSpec> pads = new ArrayList<>();
        private final List<SplitSpec> splits = new ArrayList<>();

        private Builder() {
        }

        private Builder(KernelUiSpec source) {
            this.baseKernel = source.baseKernel;
            this.frame = source.frame;
            this.background = source.background;
            this.behind = source.behind;
            this.splitPainter = source.splitPainter;
            this.below = source.below;
            this.overrideWidthPx = source.overrideWidthPx;
            this.overrideHeightPx = source.overrideHeightPx;
            this.pads.addAll(source.pads);
            this.splits.addAll(source.splits);
        }

        public Builder baseKernel(@Nullable FizzyGui baseKernel) {
            this.baseKernel = baseKernel;
            return this;
        }

        public Builder frame(@Nullable FramePainter frame) {
            this.frame = frame;
            return this;
        }

        public Builder background(@Nullable BgPainter background) {
            this.background = background;
            return this;
        }

        public Builder behind(@Nullable BehindPainter behind) {
            this.behind = behind;
            return this;
        }

        public Builder splitPainter(@Nullable SplitPainter splitPainter) {
            this.splitPainter = splitPainter;
            return this;
        }

        public Builder below(@Nullable ElementPainter below) {
            this.below = below;
            return this;
        }

        public Builder overrideSizePx(@Nullable Integer widthPx, @Nullable Integer heightPx) {
            this.overrideWidthPx = widthPx;
            this.overrideHeightPx = heightPx;
            return this;
        }

        public Builder addPad(PadSpec padSpec) {
            if (padSpec != null) {
                this.pads.add(padSpec);
            }
            return this;
        }

        public Builder addPads(List<PadSpec> padSpecs) {
            if (padSpecs != null) {
                for (PadSpec padSpec : padSpecs) {
                    addPad(padSpec);
                }
            }
            return this;
        }

        public Builder addSplit(SplitSpec splitSpec) {
            if (splitSpec != null) {
                this.splits.add(splitSpec);
            }
            return this;
        }

        public Builder addSplits(List<SplitSpec> splitSpecs) {
            if (splitSpecs != null) {
                for (SplitSpec splitSpec : splitSpecs) {
                    addSplit(splitSpec);
                }
            }
            return this;
        }

        public KernelUiSpec build() {
            return new KernelUiSpec(this);
        }
    }
}

