package link.botwmcs.fizzy.ui.element.background;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;

public final class FizzyBackgroundElement implements AnimatableElement {
    private final BgType type;

    private static final Identifier TEX_STONE = Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background1.png");
    private static final Identifier TEX_BARRIER = Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background2.png");
    private static final Identifier TEX_BARRIER_BLUE = Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background3.png");
    private static final Identifier TEX_PURE_GRAY = Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background4.png");
    private static final Identifier TEX_BOTW = Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background5.png");

    private static final Map<BgType, Identifier> TEXTURES = new EnumMap<>(BgType.class);
    static {
        TEXTURES.put(BgType.STONE, TEX_STONE);
        TEXTURES.put(BgType.BARRIER, TEX_BARRIER);
        TEXTURES.put(BgType.BARRIER_BLUE, TEX_BARRIER_BLUE);
        TEXTURES.put(BgType.PURE_GRAY, TEX_PURE_GRAY);
        TEXTURES.put(BgType.BOTW, TEX_BOTW);
    }

    public FizzyBackgroundElement(BgType type) {
        this.type = type;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }
        Identifier tex = TEXTURES.get(type);
        if (tex == null) {
            return;
        }

        tile(g, tex, leftPx, topPx, widthPx, heightPx);
    }

    @Override
    public ElementType type() {
        return ElementType.IMAGE;
    }

    private static void tile(GuiGraphicsExtractor g, Identifier tex, int x, int y, int w, int h) {
        FizzyGuiUtils.TextureSize size = FizzyGuiUtils.textureSize(tex);
        int yy = y, remH = h;
        int tileW = Math.max(1, size.w());
        int tileH = Math.max(1, size.h());
        while (remH > 0) {
            int dh = Math.min(tileH, remH);
            int xx = x, remW = w;
            while (remW > 0) {
                int dw = Math.min(tileW, remW);
                g.blit(tex, xx, yy, 0, 0, dw, dh, tileW, tileH);
                xx += dw;
                remW -= dw;
            }
            yy += dh;
            remH -= dh;
        }
    }
}
