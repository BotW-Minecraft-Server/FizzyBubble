package link.botwmcs.fizzy.ui.kernel.layout;

public final class LayoutDimension {
    static final LayoutDimension AUTO = new LayoutDimension(LayoutDimensionType.AUTO, 0.0f);
    static final LayoutDimension FILL = new LayoutDimension(LayoutDimensionType.FILL, 0.0f);

    private final LayoutDimensionType type;
    private final float value;

    private LayoutDimension(LayoutDimensionType type, float value) {
        this.type = type;
        this.value = value;
    }

    static LayoutDimension auto() {
        return AUTO;
    }

    static LayoutDimension fill() {
        return FILL;
    }

    static LayoutDimension pixels(int value) {
        return new LayoutDimension(LayoutDimensionType.PIXELS, Math.max(0, value));
    }

    static LayoutDimension percent(float percent) {
        return new LayoutDimension(LayoutDimensionType.PERCENT, Math.max(0.0f, percent));
    }

    LayoutDimensionType type() {
        return type;
    }

    int resolve(int parent, int autoFallback) {
        return switch (type) {
            case AUTO -> Math.max(0, Math.min(autoFallback, parent));
            case PIXELS -> Math.max(0, Math.min(Math.round(value), parent));
            case PERCENT -> Math.max(0, Math.min(Math.round(parent * value), parent));
            case FILL -> Math.max(0, parent);
        };
    }
}
