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
}
