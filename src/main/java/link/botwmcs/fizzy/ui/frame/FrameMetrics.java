package link.botwmcs.fizzy.ui.frame;

public interface FrameMetrics {
    int texW();
    int texH();
    int panelW();
    int titleStartH();
    int slotStartTopPx();
    int slotStartLeftPx();
    int slotSizePx();
    int slotInnerStartY();
    int slotInnerHeight();
    int topBorderY();
    int bottomBorderY();
    int bottomPadStartY();
    int bottomPadHeight();
    int bottomEdgeStartY();
    int bottomEdgeHeight();

    // 可选：一些通用辅助
    default int totalHeightForRows(int rows, boolean includeBottomEdge) {
        return slotStartTopPx() + rows * slotSizePx() + bottomPadHeight()
                + (includeBottomEdge ? bottomEdgeHeight() : 0);
    }
    default int totalWidthForCols(int cols) {
        return cols * slotSizePx();
    }
    default int gridOriginX(int panelLeftPx) { return panelLeftPx + slotStartLeftPx(); }
    default int gridOriginY(int panelTopPx)  { return panelTopPx  + slotStartTopPx(); }
}
