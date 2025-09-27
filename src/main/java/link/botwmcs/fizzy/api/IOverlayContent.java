package link.botwmcs.fizzy.api;

import net.minecraft.client.gui.GuiGraphics;

public interface IOverlayContent {
    /** 在 Overlay 内部内容区域渲染 */
    void render(GuiGraphics g, int x, int y, int width, int height, float partialTick);

    /** 每帧更新逻辑（可选） */
    default void tick() {}
}
