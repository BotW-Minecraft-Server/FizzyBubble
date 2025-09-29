package link.botwmcs.fizzy.api;

import net.minecraft.client.gui.GuiGraphics;

public interface IOverlayContent {
    /** 背层（先画，常用于背景/分割线/装饰） */
    default void renderBackLayer(GuiGraphics g, int x, int y, float partialTick) {}

    /** 主层（主体内容） */
    void renderMainLayer(GuiGraphics g, int x, int y, float partialTick);

    /** 前层（最后画，常用于高亮、tooltip、闪烁指示等） */
    default void renderFrontLayer(GuiGraphics g, int x, int y, float partialTick) {}

    /** 逐帧逻辑 */
    default void tick() {}

    /** 是否“重要”（影响外观，比如切换面板贴图的高亮版） */
    default boolean isImportant() { return false; }

    /** 建议内容高度（用于未来做自适应高度；现阶段可忽略） */
    default int preferredHeightPx() { return 62; }

    /** 页面关闭/销毁钩子（释放资源等） */
    default void onClose() {}

    default void setExternalAlpha(float a) {} // 由实例在渲染前传入 ca 或 na
//    /** 在 Overlay 内部内容区域渲染 */
//    void render(GuiGraphics g, int x, int y, int width, int height, float partialTick);
//
//    /** 每帧更新逻辑（可选） */
//    default void tick() {}
}
