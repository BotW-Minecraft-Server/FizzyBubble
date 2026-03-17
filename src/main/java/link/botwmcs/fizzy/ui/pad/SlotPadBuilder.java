package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;

public final class SlotPadBuilder extends BasePadBuilder<SlotPadBuilder> {
    public final int rowStart;
    public final int colStart;
    public final int rowEnd;
    public final int colEnd;
    private boolean inner;

    public SlotPadBuilder(FizzyGuiBuilder parent, int rowStart, int colStart, int rowEnd, int colEnd) {
        super(parent);
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.rowEnd = rowEnd;
        this.colEnd = colEnd;
    }

    @Override
    protected SlotPadBuilder self() {
        return this;
    }

    public SlotPadBuilder inner() {
        this.inner = true;
        return this;
    }

    @Override
    public PadSpec toSpec() {
        return new SlotPadSpec(rowStart, colStart, rowEnd, colEnd, inner, elements);
    }
}
