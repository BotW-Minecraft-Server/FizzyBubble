package link.botwmcs.fizzy.ui.split;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class FizzySplit implements SplitPainter {
    private final Identifier texture;
    private final SplitMetrics metrics;

    public FizzySplit() {
        this(Fizzy.resourceLocation("textures/gui/ui/panel_default.png"), FizzySplitMetrics.ofDefault());
    }

    public FizzySplit(Identifier texture, SplitMetrics metrics) {
        this.texture = texture;
        this.metrics = metrics;
    }
    @Override
    public void paint(GuiGraphicsExtractor g, int x, int y, int lengthPx, SplitType type) {
        if (lengthPx <= 0) {
            return;
        }

        int texW = metrics.texW();
        int texH = metrics.texH();
        int u = metrics.splitorStartU(type);
        int v = metrics.splitorStartV(type);
        int tileW = metrics.splitorWidth(type);
        int tileH = metrics.splitorHeight(type);

        if (type == SplitType.VERTICAL) {
            int drawn = 0;
            while (drawn < lengthPx) {
                int drawH = Math.min(tileH, lengthPx - drawn);
                g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + drawn, (float) u, (float) v, tileW, drawH, tileW, drawH, texW, texH);
                drawn += drawH;
            }
        } else {
            int drawn = 0;
            while (drawn < lengthPx) {
                int drawW = Math.min(tileW, lengthPx - drawn);
                g.blit(RenderPipelines.GUI_TEXTURED, texture, x + drawn, y, (float) u, (float) v, drawW, tileH, drawW, tileH, texW, texH);
                drawn += drawW;
            }
        }
    }

    @Override
    public SplitMetrics metrics() {
        return metrics;
    }
}
