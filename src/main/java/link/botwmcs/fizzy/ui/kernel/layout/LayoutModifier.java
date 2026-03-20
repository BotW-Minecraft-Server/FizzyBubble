package link.botwmcs.fizzy.ui.kernel.layout;

import link.botwmcs.fizzy.ui.kernel.state.Signal;

import java.util.Objects;

public final class LayoutModifier {
    public static final LayoutModifier DEFAULT = new LayoutModifier(
            LayoutDimension.fill(),
            LayoutDimension.fill(),
            1.0f,
            0,
            0,
            null
    );

    private final LayoutDimension width;
    private final LayoutDimension height;
    private final float grow;
    private final int minWidth;
    private final int minHeight;
    private final Signal<Boolean> visible;

    private LayoutModifier(
            LayoutDimension width,
            LayoutDimension height,
            float grow,
            int minWidth,
            int minHeight,
            Signal<Boolean> visible
    ) {
        this.width = Objects.requireNonNull(width, "width");
        this.height = Objects.requireNonNull(height, "height");
        this.grow = Math.max(0.0f, grow);
        this.minWidth = Math.max(0, minWidth);
        this.minHeight = Math.max(0, minHeight);
        this.visible = visible;
    }

    public LayoutModifier widthPx(int pixels) {
        return new LayoutModifier(LayoutDimension.pixels(pixels), height, grow, minWidth, minHeight, visible);
    }

    public LayoutModifier widthPercent(float percent) {
        return new LayoutModifier(LayoutDimension.percent(percent), height, grow, minWidth, minHeight, visible);
    }

    public LayoutModifier fillWidth() {
        return new LayoutModifier(LayoutDimension.fill(), height, grow, minWidth, minHeight, visible);
    }

    public LayoutModifier autoWidth() {
        return new LayoutModifier(LayoutDimension.auto(), height, grow, minWidth, minHeight, visible);
    }

    public LayoutModifier heightPx(int pixels) {
        return new LayoutModifier(width, LayoutDimension.pixels(pixels), grow, minWidth, minHeight, visible);
    }

    public LayoutModifier heightPercent(float percent) {
        return new LayoutModifier(width, LayoutDimension.percent(percent), grow, minWidth, minHeight, visible);
    }

    public LayoutModifier fillHeight() {
        return new LayoutModifier(width, LayoutDimension.fill(), grow, minWidth, minHeight, visible);
    }

    public LayoutModifier autoHeight() {
        return new LayoutModifier(width, LayoutDimension.auto(), grow, minWidth, minHeight, visible);
    }

    public LayoutModifier sizePx(int widthPx, int heightPx) {
        return widthPx(widthPx).heightPx(heightPx);
    }

    public LayoutModifier minSizePx(int minWidth, int minHeight) {
        return new LayoutModifier(width, height, grow, minWidth, minHeight, visible);
    }

    public LayoutModifier grow(float grow) {
        return new LayoutModifier(width, height, grow, minWidth, minHeight, visible);
    }

    public LayoutModifier visibleWhen(Signal<Boolean> visible) {
        return new LayoutModifier(width, height, grow, minWidth, minHeight, Objects.requireNonNull(visible, "visible"));
    }

    LayoutDimension width() {
        return width;
    }

    LayoutDimension height() {
        return height;
    }

    float growValue() {
        return grow;
    }

    int minWidth() {
        return minWidth;
    }

    int minHeight() {
        return minHeight;
    }

    boolean isVisible() {
        Signal<Boolean> signal = visible;
        if (signal == null) {
            return true;
        }
        Boolean value = signal.get();
        return value == null || value;
    }
}
