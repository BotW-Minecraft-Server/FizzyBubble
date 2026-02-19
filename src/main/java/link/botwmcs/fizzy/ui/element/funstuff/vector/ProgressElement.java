package link.botwmcs.fizzy.ui.element.funstuff.vector;

import com.mojang.blaze3d.systems.RenderSystem;
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
    private static final int DEFAULT_MIN_NOTCH_SEGMENT_WIDTH_PX = 4;
    private static final int DEFAULT_CAP_WIDTH_PX = 4;

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
        NotchSprite notchSprite = resolveNotchSprite(widthPx);

        RenderSystem.enableBlend();
        try {
            renderNineSlice(g, color.backgroundTexture(), leftPx, drawY, widthPx, drawHeight, capWidthPx);
            blitProgress(g, color.progressTexture(), leftPx, drawY, widthPx, drawHeight, filledWidth, capWidthPx);

            if (notchSprite != null) {
                renderNineSlice(g, notchSprite.backgroundTexture(), leftPx, drawY, widthPx, drawHeight, capWidthPx);
                blitProgress(g, notchSprite.progressTexture(), leftPx, drawY, widthPx, drawHeight, filledWidth, capWidthPx);
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

    private synchronized NotchSprite resolveNotchSprite(int widthPx) {
        if (widthPx <= UiUnit.SLOT_PX) {
            return null;
        }

        NotchSprite manual = resolveManualNotchSprite(widthPx);
        if (manual != null) {
            return manual;
        }

        return autoNotches ? resolveAutoNotchSprite(widthPx) : null;
    }

    private NotchSprite resolveManualNotchSprite(int widthPx) {
        if (manualNotches.isEmpty()) {
            return null;
        }

        LinkedHashSet<NotchSprite> unique = new LinkedHashSet<>();
        for (int notchCount : manualNotches) {
            unique.add(NotchSprite.closestTo(notchCount));
        }

        NotchSprite selected = null;
        for (NotchSprite sprite : unique) {
            if (!canUseNotch(widthPx, sprite.segmentCount())) {
                continue;
            }
            if (selected == null || sprite.segmentCount() > selected.segmentCount()) {
                selected = sprite;
            }
        }
        return selected;
    }

    private NotchSprite resolveAutoNotchSprite(int widthPx) {
        for (NotchSprite sprite : NotchSprite.AUTO_ORDER) {
            if (canUseNotch(widthPx, sprite.segmentCount())) {
                return sprite;
            }
        }
        return null;
    }

    private boolean canUseNotch(int widthPx, int segmentCount) {
        if (segmentCount <= 0) {
            return false;
        }
        if (widthPx < segmentCount) {
            return false;
        }
        float segmentWidth = widthPx / (float) segmentCount;
        return segmentWidth >= minNotchSegmentWidthPx;
    }

    private static void blitProgress(GuiGraphics g,
                                     ResourceLocation texture,
                                     int x,
                                     int y,
                                     int width,
                                     int height,
                                     int filledWidth,
                                     int capWidthPx) {
        if (filledWidth <= 0 || width <= 0 || height <= 0) {
            return;
        }

        int clampedFill = Math.min(width, filledWidth);
        if (clampedFill <= 0) {
            return;
        }

        g.enableScissor(x, y, x + clampedFill, y + height);
        try {
            renderNineSlice(g, texture, x, y, width, height, capWidthPx);
        } finally {
            g.disableScissor();
        }
    }

    private static void renderNineSlice(GuiGraphics g,
                                        ResourceLocation texture,
                                        int x,
                                        int y,
                                        int width,
                                        int height,
                                        int capWidthPx) {
        if (width <= 0 || height <= 0) {
            return;
        }

        int safeCap = Math.min(validateCapWidth(capWidthPx), VANILLA_BAR_TEXTURE_WIDTH / 2);
        int sourceCenterWidth = VANILLA_BAR_TEXTURE_WIDTH - safeCap * 2;
        if (sourceCenterWidth <= 0) {
            g.blit(texture, x, y, width, height, 0, 0, VANILLA_BAR_TEXTURE_WIDTH, VANILLA_BAR_TEXTURE_HEIGHT, VANILLA_BAR_TEXTURE_WIDTH, VANILLA_BAR_TEXTURE_HEIGHT);
            return;
        }

        int leftDestWidth;
        int rightDestWidth;
        int centerDestWidth;
        if (width <= safeCap * 2) {
            float scale = width / (safeCap * 2.0f);
            leftDestWidth = Math.max(1, Math.round(safeCap * scale));
            rightDestWidth = Math.max(1, width - leftDestWidth);
            centerDestWidth = 0;
        } else {
            leftDestWidth = safeCap;
            rightDestWidth = safeCap;
            centerDestWidth = width - leftDestWidth - rightDestWidth;
        }

        // left cap
        g.blit(
                texture,
                x,
                y,
                leftDestWidth,
                height,
                0,
                0,
                safeCap,
                VANILLA_BAR_TEXTURE_HEIGHT,
                VANILLA_BAR_TEXTURE_WIDTH,
                VANILLA_BAR_TEXTURE_HEIGHT
        );

        // center stretch
        if (centerDestWidth > 0) {
            g.blit(
                    texture,
                    x + leftDestWidth,
                    y,
                    centerDestWidth,
                    height,
                    safeCap,
                    0,
                    sourceCenterWidth,
                    VANILLA_BAR_TEXTURE_HEIGHT,
                    VANILLA_BAR_TEXTURE_WIDTH,
                    VANILLA_BAR_TEXTURE_HEIGHT
            );
        }

        // right cap
        g.blit(
                texture,
                x + width - rightDestWidth,
                y,
                rightDestWidth,
                height,
                VANILLA_BAR_TEXTURE_WIDTH - safeCap,
                0,
                safeCap,
                VANILLA_BAR_TEXTURE_HEIGHT,
                VANILLA_BAR_TEXTURE_WIDTH,
                VANILLA_BAR_TEXTURE_HEIGHT
        );
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

    private enum NotchSprite {
        NOTCH_6(6, "notched_6"),
        NOTCH_10(10, "notched_10"),
        NOTCH_12(12, "notched_12"),
        NOTCH_20(20, "notched_20");

        private static final NotchSprite[] VALUES = values();
        private static final NotchSprite[] AUTO_ORDER = {NOTCH_20, NOTCH_12, NOTCH_10, NOTCH_6};

        private final int segmentCount;
        private final ResourceLocation backgroundSprite;
        private final ResourceLocation progressSprite;

        NotchSprite(int segmentCount, String spriteName) {
            this.segmentCount = segmentCount;
            this.backgroundSprite = ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/" + spriteName + "_background.png");
            this.progressSprite = ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/" + spriteName + "_progress.png");
        }

        int segmentCount() {
            return segmentCount;
        }

        ResourceLocation backgroundTexture() {
            return backgroundSprite;
        }

        ResourceLocation progressTexture() {
            return progressSprite;
        }

        static NotchSprite closestTo(int segmentCount) {
            NotchSprite closest = VALUES[0];
            int closestDiff = Math.abs(segmentCount - closest.segmentCount);
            for (int i = 1; i < VALUES.length; i++) {
                NotchSprite candidate = VALUES[i];
                int diff = Math.abs(segmentCount - candidate.segmentCount);
                if (diff < closestDiff) {
                    closest = candidate;
                    closestDiff = diff;
                }
            }
            return closest;
        }
    }
}
