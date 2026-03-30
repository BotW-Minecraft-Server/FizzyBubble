package link.botwmcs.fizzy.ui.element.background;

import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class MapBackgroundElement implements AnimatableElement {
    private static final Identifier MAP_BG = Identifier.withDefaultNamespace("textures/map/map_background.png");
    private static final int BORDER_PX = 3;

    @Override
    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }

        FizzyGuiUtils.TextureSize size = FizzyGuiUtils.textureSize(MAP_BG);
        int texW = Math.max(1, size.w());
        int texH = Math.max(1, size.h());

        FizzyGuiUtils.drawNineSlice(g, MAP_BG, leftPx, topPx, widthPx, heightPx, texW, texH, BORDER_PX);
    }

    @Override
    public ElementType type() {
        return ElementType.IMAGE;
    }
}
