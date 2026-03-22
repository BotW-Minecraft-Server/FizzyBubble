package link.botwmcs.fizzy.ui.kernel.overlay;

import link.botwmcs.fizzy.client.overlay.Anchor;
import net.minecraft.client.gui.GuiGraphics;

public interface OverlayRenderable {
    default void beforeLayout(int screenWidth, int screenHeight) {
    }

    default boolean hitTest(double mouseX, double mouseY) {
        return false;
    }

    default boolean blocksInputBelow(double mouseX, double mouseY) {
        return false;
    }

    default boolean wantsPointerCapture(int button) {
        return false;
    }

    default void mouseMoved(double mouseX, double mouseY) {
    }

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    boolean isActive();

    void hide();

    void dispose();

    int getWidthPx();

    int getHeightPx();

    Anchor getAnchor();

    void assignAnchor(Anchor anchor);

    void setTargetPos(int x, int y);

    void render(GuiGraphics graphics, float partialTick);
}
