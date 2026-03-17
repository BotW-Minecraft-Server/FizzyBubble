package link.botwmcs.fizzy.ui.frame;

public record MotiveFrameMetrics(
        int texW,
        int texH,
        int panelW,
        int titleStartH,
        int slotStartTopPx,
        int slotStartLeftPx,
        int slotSizePx,
        int slotInnerStartY,
        int slotInnerHeight,
        int topBorderY,
        int bottomBorderY,
        int bottomPadStartY,
        int bottomPadHeight,
        int bottomEdgeStartY,
        int bottomEdgeHeight,
        int buttomInvExtraStartY,
        int buttomInvExtraHeight
) implements FrameMetrics {
    public static MotiveFrameMetrics ofDefault256x256() {
        return new MotiveFrameMetrics(
                256, 256,
                176, 7,
                16, 8,
                18,
                3, 250,
                0, 253,
                0, 8,
                0, 0,
                0, 0
        );
    }
}
