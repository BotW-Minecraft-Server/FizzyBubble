package link.botwmcs.fizzy.ui.frame;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface FramePainter {
    /**
     * @param left GUI 左上角 X（像素）
     * @param top  GUI 左上角 Y（像素）
     * @param w    GUI 宽度（像素）
     * @param h    GUI 高度（像素）
     * @param drawBottomEdge 是否绘制底边（Screen 渲染 true；Menu 渲染 false）
     */
    void paint(GuiGraphicsExtractor g, int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow);
    FrameMetrics metrics();
    void setLayout(int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow);
    Layout layout();

    record Layout(int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {}

    // 便捷方法，直接算本次 slot 区域
    default SlotArea currentSlotArea() {
        var m = metrics();
        var L = layout();
        if (L == null) {
            throw new IllegalStateException("Layout has not been set. Did you forget to call setLayout()?");
        }
        int contentH = L.h() - m.slotStartTopPx() - m.bottomPadHeight()
                - (L.hasBelow() ? m.buttomInvExtraHeight() : 0)
                - (L.drawBottomEdge() ? m.bottomEdgeHeight() : 0);
        if (contentH < 0) contentH = 0;
        contentH -= contentH % m.slotSizePx();
        return new SlotArea(L.left() + m.slotStartLeftPx(), L.top() + m.slotStartTopPx(), L.w(), Math.max(0, contentH));
    }

    // 便捷方法，直接算本次背景区域
    default SlotArea currentBackgroundArea() {
        var L = layout();
        if (L == null) {
            throw new IllegalStateException("Layout has not been set. Did you forget to call setLayout()?");
        }
        var slotArea = currentSlotArea();
        return new SlotArea(
                L.left(),
                slotArea.y(),
                L.w(),
                slotArea.h()
        );
    }

    record SlotArea(int x, int y, int w, int h) {}

    default BelowArea currentBelowArea() {
        var L = layout();
        if (L == null) {
            throw new IllegalStateException("Layout has not been set. Did you forget to call setLayout()?");
        }
        if (!L.hasBelow()) {
            return null;
        }
        var m = metrics();
        SlotArea slotArea = currentSlotArea();
        int top = L.top() + m.slotStartTopPx() + slotArea.h() + m.bottomPadHeight() - 1;
        return new BelowArea(L.left(), top, L.w(), m.buttomInvExtraHeight());
    }

    record BelowArea(int left, int top, int width, int height) {}

}
