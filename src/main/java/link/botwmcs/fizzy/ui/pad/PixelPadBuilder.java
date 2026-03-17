package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;

public final class PixelPadBuilder extends BasePadBuilder<PixelPadBuilder> {
    private final int left;
    private final int top;
    private final int width;
    private final int height;

    public PixelPadBuilder(FizzyGuiBuilder parent, int left, int top, int width, int height) {
        super(parent);
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    @Override
    protected PixelPadBuilder self() {
        return this;
    }

    @Override
    public PadSpec toSpec(PadBuildContext context) {
        return new PixelPadSpec(left, top, width, height, elements);
    }
}
