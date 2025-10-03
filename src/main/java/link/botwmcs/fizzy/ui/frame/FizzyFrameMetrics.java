package link.botwmcs.fizzy.ui.frame;

public record FizzyFrameMetrics(
        int texW,                // 纹理宽（例如 256）
        int texH,                // 纹理高（例如 256）
        int panelW,              // 176：默认的宽度
        int titleStartH,         // 7：标题到顶部的距离
        int slotStartTopPx,      // 28：顶部装饰到首个 slot 顶线的距离
        int slotStartLeftPx,     // 7 ：左侧装饰到首个 slot 左边界的距离
        int slotSizePx,          // 18：slot 总高（含上下 1px 黑边）
        int slotInnerStartY,     // 29：slot 内容区起始 Y
        int slotInnerHeight,     // 16：slot 内容区高度（可重复）
        int topBorderY,          // 28：slot 顶线 Y（1px）
        int bottomBorderY,       // 45：slot 底线 Y（1px）
        int bottomPadStartY,     // 46：slot 区域后的留白起始 Y
        int bottomPadHeight,     // 5 ：留白高
        int bottomEdgeStartY,    // 51：底边起始 Y
        int bottomEdgeHeight     // 4 ：底边高

) implements FrameMetrics {
    public int totalHeightForRows(int rows, boolean includeBottomEdge) {
        // = 顶部装饰 28 + rows * 18 + 留白 5 + （可选底边 4）
        return slotStartTopPx + rows * slotSizePx + bottomPadHeight + (includeBottomEdge ? bottomEdgeHeight : 0);
    }
    public int gridOriginX(int panelLeftPx) { return panelLeftPx + slotStartLeftPx; }
    public int gridOriginY(int panelTopPx)  { return panelTopPx  + slotStartTopPx; }
    public static FizzyFrameMetrics ofDefault256x256() {
        return new FizzyFrameMetrics(
                256, 256, 176, 7,
                28, 7,
                18,
                29, 16,
                28, 45,
                46, 5,
                51, 4
        );
    }

}
