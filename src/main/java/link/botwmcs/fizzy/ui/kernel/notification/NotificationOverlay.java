package link.botwmcs.fizzy.ui.kernel.notification;

import link.botwmcs.fizzy.client.overlay.Anchor;
import link.botwmcs.fizzy.client.util.animate.LerpedFloat;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayRenderable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Objects;

public final class NotificationOverlay implements OverlayRenderable {
    private static final int MIN_WIDTH = 120;
    private static final int MAX_WIDTH = 260;
    private static final int HEIGHT_SINGLE_LINE = 24;
    private static final int HEIGHT_TWO_LINES = 36;

    private final NotificationSpec spec;
    private final Font font = Minecraft.getInstance().font;
    private final LerpedFloat xPos = LerpedFloat.linear().startWithValue(0);
    private final LerpedFloat yPos = LerpedFloat.linear().startWithValue(0);
    private final LerpedFloat alpha = LerpedFloat.linear().startWithValue(0);

    private final int widthPx;
    private final int heightPx;
    private final long createdAtMs;
    private final long lifetimeMs;

    private Anchor anchor;
    private int targetX;
    private int targetY;
    private boolean initialized;
    private boolean targetVisible = true;
    private boolean active = true;

    public NotificationOverlay(NotificationSpec spec) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.anchor = spec.anchor();
        this.widthPx = resolveWidth(spec);
        this.heightPx = resolveHeight(spec);
        this.createdAtMs = Util.getMillis();
        this.lifetimeMs = Math.max(1000L, spec.durationTicks() * 50L);
        this.alpha.chase(1.0, 0.24, LerpedFloat.Chaser.EXP);
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
        alpha.chase(0.0, 0.24, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void dispose() {
        active = false;
    }

    @Override
    public int getWidthPx() {
        return widthPx;
    }

    @Override
    public int getHeightPx() {
        return heightPx;
    }

    @Override
    public Anchor getAnchor() {
        return anchor;
    }

    @Override
    public void assignAnchor(Anchor anchor) {
        this.anchor = anchor == null ? Anchor.TOP_RIGHT : anchor;
    }

    @Override
    public void setTargetPos(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    @Override
    public void render(GuiGraphics graphics, float partialTick) {
        if (!active) {
            return;
        }

        long nowMs = Util.getMillis();
        if (targetVisible && nowMs - createdAtMs >= lifetimeMs) {
            hide();
        }

        if (!initialized) {
            initialized = true;
            float enterOffset = switch (anchor) {
                case TOP_LEFT, BOTTOM_LEFT -> -24.0f;
                case TOP_RIGHT, BOTTOM_RIGHT -> 24.0f;
            };
            xPos.startWithValue(targetX + enterOffset);
            yPos.startWithValue(targetY);
        }

        xPos.chase(targetX, 0.32, LerpedFloat.Chaser.EXP);
        yPos.chase(targetY, 0.32, LerpedFloat.Chaser.EXP);
        xPos.tickChaser();
        yPos.tickChaser();
        alpha.tickChaser();

        float a = Mth.clamp(alpha.getValue(), 0.0f, 1.0f);
        if (!targetVisible && a <= 0.01f) {
            alpha.startWithValue(0.0f);
            active = false;
            return;
        }

        int x = Math.round(xPos.getValue(partialTick));
        int y = Math.round(yPos.getValue(partialTick));

        NotificationLevel level = spec.level();
        graphics.fill(x, y, x + widthPx, y + heightPx, withAlpha(level.backgroundColor(), a * 0.94f));
        graphics.fill(x, y, x + 2, y + heightPx, withAlpha(level.accentColor(), a));
        graphics.fill(x, y, x + widthPx, y + 1, withAlpha(0xFFFFFFFF, a * 0.10f));
        graphics.fill(x, y + heightPx - 1, x + widthPx, y + heightPx, withAlpha(0xFF000000, a * 0.40f));

        int innerWidth = widthPx - 12;
        int titleY = y + 5;
        int messageY = y + 18;

        String titleText = font.plainSubstrByWidth(spec.title().getString(), innerWidth);
        int titleColor = withAlpha(level.titleColor(), a);
        if (!titleText.isEmpty()) {
            if ((titleColor >>> 24) != 0) {
                graphics.drawString(font, titleText, x + 8, titleY, titleColor, false);
            }
        }

        String messageText = font.plainSubstrByWidth(spec.message().getString(), innerWidth);
        int messageColor = withAlpha(0xFFE6EEF7, a);
        if (!messageText.isEmpty()) {
            if ((messageColor >>> 24) != 0) {
                graphics.drawString(font, messageText, x + 8, messageY, messageColor, false);
            }
        }

        if (targetVisible) {
            float lifeRatio = Mth.clamp((float) (lifetimeMs - (nowMs - createdAtMs)) / (float) lifetimeMs, 0.0f, 1.0f);
            int lifeWidth = Math.max(0, Math.round((widthPx - 2) * lifeRatio));
            if (lifeWidth > 0) {
                graphics.fill(x + 1, y + heightPx - 2, x + 1 + lifeWidth, y + heightPx - 1, withAlpha(level.accentColor(), a * 0.90f));
            }
        }
    }

    private int resolveWidth(NotificationSpec spec) {
        int textWidth = Math.max(font.width(spec.title()), font.width(spec.message()));
        return Mth.clamp(textWidth + 20, MIN_WIDTH, MAX_WIDTH);
    }

    private int resolveHeight(NotificationSpec spec) {
        return spec.message().getString().isBlank() ? HEIGHT_SINGLE_LINE : HEIGHT_TWO_LINES;
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f) & 0xFF;
        return (a << 24) | (rgb & 0x00FFFFFF);
    }
}
