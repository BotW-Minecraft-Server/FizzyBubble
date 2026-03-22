package link.botwmcs.fizzy.ui.kernel.modal;

import link.botwmcs.fizzy.client.overlay.Anchor;
import link.botwmcs.fizzy.client.util.animate.LerpedFloat;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Objects;

public final class ModalOverlay implements OverlayRenderable {
    private final ModalSpec spec;
    private final Font font = Minecraft.getInstance().font;
    private final LerpedFloat alpha = LerpedFloat.linear().startWithValue(0);

    private Anchor anchor;
    private boolean targetVisible = true;
    private boolean active = true;
    private int targetX;
    private int targetY;
    private int viewportWidth;
    private int viewportHeight;

    public ModalOverlay(ModalSpec spec) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.anchor = spec.anchor();
        this.alpha.chase(1.0, 0.22, LerpedFloat.Chaser.EXP);
    }

    public void setViewport(int screenWidth, int screenHeight) {
        this.viewportWidth = Math.max(0, screenWidth);
        this.viewportHeight = Math.max(0, screenHeight);
    }

    @Override
    public void beforeLayout(int screenWidth, int screenHeight) {
        setViewport(screenWidth, screenHeight);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void hide() {
        if (!targetVisible) {
            return;
        }
        targetVisible = false;
        alpha.chase(0.0, 0.22, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void dispose() {
        active = false;
    }

    @Override
    public int getWidthPx() {
        return Math.max(1, viewportWidth);
    }

    @Override
    public int getHeightPx() {
        return Math.max(1, viewportHeight);
    }

    @Override
    public Anchor getAnchor() {
        return anchor;
    }

    @Override
    public void assignAnchor(Anchor anchor) {
        this.anchor = anchor == null ? Anchor.TOP_LEFT : anchor;
    }

    @Override
    public void setTargetPos(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    @Override
    public void render(GuiGraphics graphics, float partialTick) {
        if (!active || viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }

        alpha.tickChaser();
        float a = Mth.clamp(alpha.getValue(), 0.0f, 1.0f);
        if (!targetVisible && a <= 0.01f) {
            alpha.startWithValue(0.0f);
            active = false;
            return;
        }

        int viewLeft = targetX;
        int viewTop = targetY;
        int viewRight = viewLeft + viewportWidth;
        int viewBottom = viewTop + viewportHeight;

        graphics.fill(viewLeft, viewTop, viewRight, viewBottom, withAlpha(0xFF000000, a * 0.55f));

        int cardWidth = Math.min(spec.widthPx(), Math.max(120, viewportWidth - 24));
        int cardHeight = Math.min(spec.heightPx(), Math.max(64, viewportHeight - 24));
        int cardLeft = viewLeft + (viewportWidth - cardWidth) / 2;
        int cardTop = viewTop + (viewportHeight - cardHeight) / 2;
        int cardRight = cardLeft + cardWidth;
        int cardBottom = cardTop + cardHeight;

        graphics.fill(cardLeft, cardTop, cardRight, cardBottom, withAlpha(0xFF1B1E27, a));
        graphics.fill(cardLeft, cardTop, cardRight, cardTop + 1, withAlpha(0xFFFFFFFF, a * 0.10f));
        graphics.fill(cardLeft, cardBottom - 1, cardRight, cardBottom, withAlpha(0xFF000000, a * 0.50f));
        graphics.fill(cardLeft, cardTop, cardLeft + 1, cardBottom, withAlpha(0xFF000000, a * 0.50f));
        graphics.fill(cardRight - 1, cardTop, cardRight, cardBottom, withAlpha(0xFF000000, a * 0.50f));

        int innerWidth = cardWidth - 16;
        String title = font.plainSubstrByWidth(spec.title().getString(), innerWidth);
        int titleColor = withAlpha(0xFFE8F6FF, a);
        if (!title.isEmpty()) {
            if ((titleColor >>> 24) != 0) {
                graphics.drawString(font, title, cardLeft + 8, cardTop + 8, titleColor, false);
            }
        }

        String message = font.plainSubstrByWidth(spec.message().getString(), innerWidth);
        int messageColor = withAlpha(0xFFC8D4E8, a);
        if (!message.isEmpty()) {
            if ((messageColor >>> 24) != 0) {
                graphics.drawString(font, message, cardLeft + 8, cardTop + 24, messageColor, false);
            }
        }
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f) & 0xFF;
        return (a << 24) | (rgb & 0x00FFFFFF);
    }
}
