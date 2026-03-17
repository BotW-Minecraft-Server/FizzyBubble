package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;

public final class AutoPadBuilder extends BasePadBuilder<AutoPadBuilder> {
    public final int rowStart;
    public final int colStart;
    public final int rowEnd;
    public final int colEnd;

    public AutoPadBuilder(FizzyGuiBuilder parent, int rowStart, int colStart, int rowEnd, int colEnd) {
        super(parent);
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.rowEnd = rowEnd;
        this.colEnd = colEnd;
    }

    @Override
    protected AutoPadBuilder self() {
        return this;
    }

    @Override
    public PadSpec toSpec(PadBuildContext context) {
        return AutoPadSpec.of(rowStart, colStart, rowEnd, colEnd, context, elements);
    }
}
