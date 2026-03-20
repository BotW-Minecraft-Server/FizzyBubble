package link.botwmcs.fizzy.ui.kernel.layout;

public record LayoutRect(int x, int y, int width, int height) {
    public static LayoutRect of(int x, int y, int width, int height) {
        return new LayoutRect(x, y, Math.max(0, width), Math.max(0, height));
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }
}
