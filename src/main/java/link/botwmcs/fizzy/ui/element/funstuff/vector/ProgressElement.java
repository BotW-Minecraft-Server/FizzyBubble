package link.botwmcs.fizzy.ui.element.funstuff.vector;

import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ProgressElement implements AnimatableElement {
    private static final int VANILLA_BAR_HEIGHT = 5;
    private static final int VANILLA_BAR_TEXTURE_WIDTH = 182;
    private static final int VANILLA_BAR_TEXTURE_HEIGHT = 5;
    private static final ResourceLocation NOTCHED_20_BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/notched_20_background.png");
    private static final ResourceLocation NOTCHED_20_PROGRESS_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/notched_20_progress.png");
    private static final int DEFAULT_MIN_NOTCH_SEGMENT_WIDTH_PX = 4;
    private static final int DEFAULT_CAP_WIDTH_PX = 4;
    private static final int[] AUTO_NOTCH_COUNTS = {20, 12, 10, 6};
    private static final int NOTCH_SAMPLE_U = 7;
    private static final int NOTCH_SAMPLE_W = 5;
    private static final int NOTCH_SAMPLE_CENTER_OFFSET = 2;

    private float progress;
    private Color color;
    private int barHeight;
    private boolean autoNotches;
    private int minNotchSegmentWidthPx;
    private int capWidthPx;
    private final List<Integer> manualNotches;

    private ProgressElement(Builder builder) {
        this.progress = clampProgress(builder.progress);
        this.color = builder.color;
        this.barHeight = builder.barHeight;
        this.autoNotches = builder.autoNotches;
        this.minNotchSegmentWidthPx = builder.minNotchSegmentWidthPx;
        this.capWidthPx = builder.capWidthPx;
        this.manualNotches = new ArrayList<>(builder.manualNotches);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }

        int drawHeight = Math.min(heightPx, Math.max(1, barHeight));
        int drawY = topPx + (heightPx - drawHeight) / 2;

        int filledWidth = Mth.clamp(Mth.lerpDiscrete(progress, 0, widthPx), 0, widthPx);
        int notchCount = resolveNotchCount(widthPx);

        RenderSystem.enableBlend();
        try {
            FizzyGuiUtils.drawHorizontalCapNineSlice(
                    g,
                    color.backgroundTexture(),
                    leftPx,
                    drawY,
                    widthPx,
                    drawHeight,
                    VANILLA_BAR_TEXTURE_WIDTH,
                    VANILLA_BAR_TEXTURE_HEIGHT,
                    capWidthPx
            );
            FizzyGuiUtils.drawScissoredHorizontalCapProgress(
                    g,
                    color.progressTexture(),
                    leftPx,
                    drawY,
                    widthPx,
                    drawHeight,
                    filledWidth,
                    VANILLA_BAR_TEXTURE_WIDTH,
                    VANILLA_BAR_TEXTURE_HEIGHT,
                    capWidthPx
            );

            if (notchCount > 0) {
                drawAdaptiveNotch(g, NOTCHED_20_BACKGROUND_TEXTURE, leftPx, drawY, widthPx, drawHeight, notchCount);
                drawScissoredAdaptiveNotch(g, NOTCHED_20_PROGRESS_TEXTURE, leftPx, drawY, widthPx, drawHeight, filledWidth, notchCount);
            }
        } finally {
            RenderSystem.disableBlend();
        }
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }

    public synchronized void setProgress(float progress) {
        this.progress = clampProgress(progress);
    }

    public synchronized float getProgress() {
        return progress;
    }

    public synchronized void setColor(Color color) {
        this.color = Objects.requireNonNull(color, "color");
    }

    public synchronized void setColor(String colorName) {
        this.color = Color.fromName(colorName);
    }

    public synchronized Color getColor() {
        return color;
    }

    public synchronized void setBarHeight(int barHeight) {
        if (barHeight <= 0) {
            throw new IllegalArgumentException("barHeight must be > 0");
        }
        this.barHeight = barHeight;
    }

    public synchronized int getBarHeight() {
        return barHeight;
    }

    public synchronized void setAutoNotches(boolean autoNotches) {
        this.autoNotches = autoNotches;
    }

    public synchronized boolean isAutoNotches() {
        return autoNotches;
    }

    public synchronized void setMinNotchSegmentWidthPx(int px) {
        if (px <= 0) {
            throw new IllegalArgumentException("min notch segment width must be > 0");
        }
        this.minNotchSegmentWidthPx = px;
    }

    public synchronized int getMinNotchSegmentWidthPx() {
        return minNotchSegmentWidthPx;
    }

    public synchronized void setCapWidthPx(int capWidthPx) {
        this.capWidthPx = validateCapWidth(capWidthPx);
    }

    public synchronized int getCapWidthPx() {
        return capWidthPx;
    }

    public synchronized void addNotch(int notchCount) {
        validateNotchCount(notchCount);
        this.manualNotches.add(notchCount);
    }

    public synchronized void addNotches(int... notchCounts) {
        Objects.requireNonNull(notchCounts, "notchCounts");
        for (int notchCount : notchCounts) {
            addNotch(notchCount);
        }
    }

    public synchronized boolean removeNotch(int notchCount) {
        return this.manualNotches.remove((Integer) notchCount);
    }

    public synchronized int removeNotchAt(int index) {
        return this.manualNotches.remove(index);
    }

    public synchronized int getNotch(int index) {
        return this.manualNotches.get(index);
    }

    public synchronized void setNotch(int index, int notchCount) {
        validateNotchCount(notchCount);
        this.manualNotches.set(index, notchCount);
    }

    public synchronized int notchSize() {
        return this.manualNotches.size();
    }

    public synchronized List<Integer> getNotches() {
        return List.copyOf(this.manualNotches);
    }

    public synchronized void setNotches(List<Integer> notchCounts) {
        Objects.requireNonNull(notchCounts, "notchCounts");
        this.manualNotches.clear();
        for (int notchCount : notchCounts) {
            validateNotchCount(notchCount);
            this.manualNotches.add(notchCount);
        }
    }

    public synchronized void clearNotches() {
        this.manualNotches.clear();
    }

    private synchronized int resolveNotchCount(int widthPx) {
        if (widthPx <= UiUnit.SLOT_PX) {
            return 0;
        }

        int manual = resolveManualNotchCount(widthPx);
        if (manual > 0) {
            return manual;
        }

        return autoNotches ? resolveAutoNotchCount(widthPx) : 0;
    }

    private int resolveManualNotchCount(int widthPx) {
        if (manualNotches.isEmpty()) {
            return 0;
        }

        LinkedHashSet<Integer> unique = new LinkedHashSet<>();
        for (int notchCount : manualNotches) {
            unique.add(notchCount);
        }

        int selected = 0;
        for (int notchCount : unique) {
            if (!canUseNotch(widthPx, notchCount)) {
                continue;
            }
            if (notchCount > selected) {
                selected = notchCount;
            }
        }
        return selected;
    }

    private int resolveAutoNotchCount(int widthPx) {
        for (int notchCount : AUTO_NOTCH_COUNTS) {
            if (canUseNotch(widthPx, notchCount)) {
                return notchCount;
            }
        }
        return 0;
    }

    private boolean canUseNotch(int widthPx, int notchCount) {
        if (notchCount <= 0) {
            return false;
        }
        int segments = notchCount + 1;
        if (widthPx < segments) {
            return false;
        }
        float segmentWidth = widthPx / (float) segments;
        return segmentWidth >= minNotchSegmentWidthPx;
    }

    private static void drawScissoredAdaptiveNotch(GuiGraphics g,
                                                   ResourceLocation texture,
                                                   int x,
                                                   int y,
                                                   int width,
                                                   int height,
                                                   int filledWidth,
                                                   int notchCount) {
        if (width <= 0 || height <= 0 || filledWidth <= 0) {
            return;
        }
        int clampedFill = Math.min(width, filledWidth);
        if (clampedFill <= 0) {
            return;
        }

        g.enableScissor(x, y, x + clampedFill, y + height);
        try {
            drawAdaptiveNotch(g, texture, x, y, width, height, notchCount);
        } finally {
            g.disableScissor();
        }
    }

    /**
     * Draw notch overlay without scaling.
     * Width adapts by repeating/cropping the native notched_20 texture (182x5).
     */
    private static void drawAdaptiveNotch(GuiGraphics g,
                                          ResourceLocation texture,
                                          int x,
                                          int y,
                                          int width,
                                          int height,
                                          int notchCount) {
        if (width <= 0 || height <= 0 || notchCount <= 0) {
            return;
        }

        int drawHeight = Math.min(height, VANILLA_BAR_TEXTURE_HEIGHT);
        int destY = y + (height - drawHeight) / 2;
        int srcV = (VANILLA_BAR_TEXTURE_HEIGHT - drawHeight) / 2;
        int right = x + width;

        for (int i = 1; i <= notchCount; i++) {
            int centerX = x + Math.round((width * i) / (float) (notchCount + 1));
            int drawX = centerX - NOTCH_SAMPLE_CENTER_OFFSET;
            int clippedX = Math.max(drawX, x);
            int clippedRight = Math.min(drawX + NOTCH_SAMPLE_W, right);
            int clippedW = clippedRight - clippedX;
            if (clippedW <= 0) {
                continue;
            }
            int srcU = NOTCH_SAMPLE_U + (clippedX - drawX);
            g.blit(
                    texture,
                    clippedX,
                    destY,
                    clippedW,
                    drawHeight,
                    srcU,
                    srcV,
                    clippedW,
                    drawHeight,
                    VANILLA_BAR_TEXTURE_WIDTH,
                    VANILLA_BAR_TEXTURE_HEIGHT
            );
        }
    }

    private static float clampProgress(float progress) {
        return Mth.clamp(progress, 0.0f, 1.0f);
    }

    private static void validateNotchCount(int notchCount) {
        if (notchCount <= 0) {
            throw new IllegalArgumentException("notch count must be > 0");
        }
    }

    private static int validateCapWidth(int capWidthPx) {
        if (capWidthPx <= 0) {
            throw new IllegalArgumentException("capWidthPx must be > 0");
        }
        return capWidthPx;
    }

    public enum Color {
        BLUE("blue"),
        GREEN("green"),
        PINK("pink"),
        PURPLE("purple"),
        RED("red"),
        WHITE("white"),
        YELLOW("yellow");

        private final ResourceLocation backgroundSprite;
        private final ResourceLocation progressSprite;

        Color(String name) {
            this.backgroundSprite = ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/" + name + "_background.png");
            this.progressSprite = ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/" + name + "_progress.png");
        }

        public ResourceLocation backgroundTexture() {
            return backgroundSprite;
        }

        public ResourceLocation progressTexture() {
            return progressSprite;
        }

        public static Color fromName(String colorName) {
            Objects.requireNonNull(colorName, "colorName");
            return switch (colorName.toLowerCase(Locale.ROOT)) {
                case "blue" -> BLUE;
                case "green" -> GREEN;
                case "pink" -> PINK;
                case "purple" -> PURPLE;
                case "red" -> RED;
                case "white" -> WHITE;
                case "yellow" -> YELLOW;
                default -> throw new IllegalArgumentException("Unsupported progress color: " + colorName);
            };
        }
    }

    public static final class Builder {
        private float progress = 0.0f;
        private Color color = Color.BLUE;
        private int barHeight = VANILLA_BAR_HEIGHT;
        private boolean autoNotches = true;
        private int minNotchSegmentWidthPx = DEFAULT_MIN_NOTCH_SEGMENT_WIDTH_PX;
        private int capWidthPx = DEFAULT_CAP_WIDTH_PX;
        private final List<Integer> manualNotches = new ArrayList<>();

        public Builder progress(float progress) {
            this.progress = clampProgress(progress);
            return this;
        }

        public Builder color(Color color) {
            this.color = Objects.requireNonNull(color, "color");
            return this;
        }

        public Builder color(String colorName) {
            this.color = Color.fromName(colorName);
            return this;
        }

        public Builder barHeight(int barHeight) {
            if (barHeight <= 0) {
                throw new IllegalArgumentException("barHeight must be > 0");
            }
            this.barHeight = barHeight;
            return this;
        }

        public Builder autoNotches(boolean enabled) {
            this.autoNotches = enabled;
            return this;
        }

        public Builder autoNotches() {
            return autoNotches(true);
        }

        public Builder minNotchSegmentWidthPx(int px) {
            if (px <= 0) {
                throw new IllegalArgumentException("min notch segment width must be > 0");
            }
            this.minNotchSegmentWidthPx = px;
            return this;
        }

        public Builder capWidthPx(int capWidthPx) {
            this.capWidthPx = validateCapWidth(capWidthPx);
            return this;
        }

        public Builder addNotch(int notchCount) {
            validateNotchCount(notchCount);
            this.manualNotches.add(notchCount);
            return this;
        }

        public Builder addNotches(int... notchCounts) {
            Objects.requireNonNull(notchCounts, "notchCounts");
            for (int notchCount : notchCounts) {
                addNotch(notchCount);
            }
            return this;
        }

        public Builder removeNotch(int notchCount) {
            this.manualNotches.remove((Integer) notchCount);
            return this;
        }

        public Builder removeNotchAt(int index) {
            this.manualNotches.remove(index);
            return this;
        }

        public Builder updateNotch(int index, int notchCount) {
            validateNotchCount(notchCount);
            this.manualNotches.set(index, notchCount);
            return this;
        }

        public Builder clearNotches() {
            this.manualNotches.clear();
            return this;
        }

        public List<Integer> notches() {
            return List.copyOf(this.manualNotches);
        }

        public ProgressElement build() {
            return new ProgressElement(this);
        }
    }

}
