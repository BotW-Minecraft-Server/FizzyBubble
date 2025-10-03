package link.botwmcs.fizzy.ui.frame;

import net.minecraft.client.gui.GuiGraphics;

public interface FramePainter {
    /**
     * @param left GUI 左上角 X（像素）
     * @param top  GUI 左上角 Y（像素）
     * @param w    GUI 宽度（像素）
     * @param h    GUI 高度（像素）
     * @param drawBottomEdge 是否绘制底边（Screen 渲染 true；Menu 渲染 false）
     */
    void paint(GuiGraphics g, int left, int top, int w, int h, boolean drawBottomEdge);
    FrameMetrics metrics();
    void setLayout(int left, int top, int w, int h, boolean drawBottomEdge);
    Layout layout();

    record Layout(int left, int top, int w, int h, boolean drawBottomEdge) {}

    // 可选：便捷方法，直接算本次 slot 区域
    default SlotArea currentSlotArea() {
        var m = metrics();
        var L = layout();
        int contentH = L.h - m.slotStartTopPx() - m.bottomPadHeight()
                - (L.drawBottomEdge ? m.bottomEdgeHeight() : 0);
        if (contentH < 0) contentH = 0;
        contentH -= contentH % m.slotSizePx();
        return new SlotArea(L.left, L.top + m.slotStartTopPx(), L.w, Math.max(0, contentH));
    }
    record SlotArea(int x, int y, int w, int h) {}

}
