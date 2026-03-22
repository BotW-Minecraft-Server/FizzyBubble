package link.botwmcs.fizzy.ui.kernel.overlay;

import link.botwmcs.fizzy.client.overlay.Anchor;
import net.minecraft.client.gui.GuiGraphics;

public interface OverlayRenderable {
    default void beforeLayout(int screenWidth, int screenHeight) {
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
