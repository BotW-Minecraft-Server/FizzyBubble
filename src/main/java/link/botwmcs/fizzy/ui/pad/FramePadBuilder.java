package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;

public final class FramePadBuilder extends BasePadBuilder<FramePadBuilder> {
    public FramePadBuilder(FizzyGuiBuilder parent) {
        super(parent);
    }

    @Override
    protected FramePadBuilder self() {
        return this;
    }

    @Override
    public PadSpec toSpec() {
        return new FramePadSpec(elements);
    }
}
