package link.botwmcs.fizzy.ui.kernel.overlay;

public final class OverlayLayoutConfig {
    private int margin = 8;
    private int verticalGap = 6;
    private int horizontalGap = 6;
    private int maxColumns = 3;

    public int margin() {
        return margin;
    }

    public int verticalGap() {
        return verticalGap;
    }

    public int horizontalGap() {
        return horizontalGap;
    }

    public int maxColumns() {
        return maxColumns;
    }

    public void set(int marginPx, int verticalGapPx, int horizontalGapPx, int maxColumns) {
        this.margin = Math.max(0, marginPx);
        this.verticalGap = Math.max(0, verticalGapPx);
        this.horizontalGap = Math.max(0, horizontalGapPx);
        this.maxColumns = Math.max(1, maxColumns);
    }
}
