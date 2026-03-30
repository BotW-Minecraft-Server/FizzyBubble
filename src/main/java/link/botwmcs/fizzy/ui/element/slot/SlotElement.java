package link.botwmcs.fizzy.ui.element.slot;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class SlotElement implements AnimatableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/slot.png"
    );

    private static final int TEXTURE_SIZE = 256;

    private static final int SLOT_SIZE = UiUnit.SLOT_PX; // 18px per slot (vanilla standard)

    // Coordinates inside the texture (trimmed to the core 3x3 sample area).
    private static final int TEX_ORIGIN = 0;           // Top-left pixel of the 3x3 sample grid.
    private static final int TEX_BORDER = 3;           // 3px outer frame thickness.
    private static final int TEX_INNER = 16;           // 16px light background.
    private static final int TEX_DIVIDER = 2;          // 2px between-slot divider.
    private static final int SAMPLE_SLOT_COUNT = 3;    // slot.png encodes a 3x3 reference grid.

    private static final int TEX_SLOT_U = TEX_ORIGIN + TEX_BORDER; // 3
    private static final int TEX_DIVIDER_U = TEX_SLOT_U + TEX_INNER; // 19
    private static final int TEX_BORDER_RIGHT_U = TEX_SLOT_U + SAMPLE_SLOT_COUNT * TEX_INNER + (SAMPLE_SLOT_COUNT - 1) * TEX_DIVIDER; // 55

    private static final int TEX_TOP_BORDER_V = TEX_ORIGIN;                           // 0
    private static final int TEX_INNER_V = TEX_ORIGIN + TEX_BORDER;                   // 3
    private static final int TEX_DIVIDER_V = TEX_INNER_V + TEX_INNER;                 // 19
    private static final int TEX_BOTTOM_BORDER_V = TEX_INNER_V + SAMPLE_SLOT_COUNT * TEX_INNER + (SAMPLE_SLOT_COUNT - 1) * TEX_DIVIDER;                                  // 55

    @Override
    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }

        int slotsX = widthPx / SLOT_SIZE;
        int slotsY = heightPx / SLOT_SIZE;
        if (slotsX <= 0 || slotsY <= 0) {
            return;
        }

        int drawWidth = spanForSlots(slotsX);
        int drawHeight = spanForSlots(slotsY);

        int originX = leftPx + (widthPx - drawWidth) / 2;
        int originY = topPx + (heightPx - drawHeight) / 2;

        int y = originY;
        // Top border row.
        drawHorizontalStripe(g, originX, y, slotsX, TEX_TOP_BORDER_V, TEX_BORDER, TEX_SLOT_U, TEX_DIVIDER_U);
        y += TEX_BORDER;

        for (int row = 0; row < slotsY; row++) {
            drawHorizontalStripe(g, originX, y, slotsX, TEX_INNER_V, TEX_INNER, TEX_SLOT_U, TEX_DIVIDER_U);
            y += TEX_INNER;
            if (row < slotsY - 1) {
                drawHorizontalStripe(g, originX, y, slotsX, TEX_DIVIDER_V, TEX_DIVIDER, TEX_SLOT_U, TEX_DIVIDER_U);
                y += TEX_DIVIDER;
            }
        }

        // Bottom border row.
        drawHorizontalStripe(g, originX, y, slotsX, TEX_BOTTOM_BORDER_V, TEX_BORDER, TEX_SLOT_U, TEX_DIVIDER_U);
    }

    @Override
    public ElementType type() {
        return ElementType.SLOT;
    }

    private static int spanForSlots(int slots) {
        if (slots <= 0) {
            return 0;
        }
        return TEX_BORDER * 2 + slots * TEX_INNER + (slots - 1) * TEX_DIVIDER;
    }

    private static void drawHorizontalStripe(GuiGraphicsExtractor g, int destX, int destY, int slotsX, int textureV,
                                             int height, int slotTextureU, int dividerTextureU) {
        int x = destX;

        // Left border column.
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, destY, TEX_ORIGIN, textureV, TEX_BORDER, height, TEXTURE_SIZE, TEXTURE_SIZE);
        x += TEX_BORDER;

        for (int col = 0; col < slotsX; col++) {
            // Slot inner background.
            g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, destY, slotTextureU, textureV, TEX_INNER, height, TEXTURE_SIZE, TEXTURE_SIZE);
            x += TEX_INNER;

            if (col < slotsX - 1) {
                // Divider between adjacent slots.
                g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, destY, dividerTextureU, textureV, TEX_DIVIDER, height, TEXTURE_SIZE, TEXTURE_SIZE);
                x += TEX_DIVIDER;
            }
        }

        // Right border column.
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, destY, TEX_BORDER_RIGHT_U, textureV, TEX_BORDER, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
