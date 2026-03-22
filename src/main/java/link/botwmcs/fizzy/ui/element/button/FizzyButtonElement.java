package link.botwmcs.fizzy.ui.element.button;

import link.botwmcs.fizzy.client.elements.FizzyButton;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.client.util.FizzyTooltipWidgetUtil;
import link.botwmcs.fizzy.client.util.TextRenderer;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import link.botwmcs.fizzy.ui.element.component.FizzyTooltipElement;
import link.botwmcs.fizzy.ui.element.icon.FizzyIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class FizzyButtonElement implements ElementPainter {
    private static final int DEFAULT_ICON_SIZE_PX = 12;
    private static final int DEFAULT_CONTENT_GAP_PX = 4;
    private static final int DEFAULT_CONTENT_PADDING_LEFT_PX = 6;
    private static final int DEFAULT_CONTENT_PADDING_RIGHT_PX = 6;

    private final FizzyButton.OnPress onPress;

    private @Nullable Tooltip tooltip;
    private @Nullable FizzyTooltipElement customTooltipElement;
    private @Nullable SoundEvent pressSound;
    private Component narrationMessage;

    private @Nullable Component textComponent;
    private @Nullable FizzyComponentElement customTextElement;
    private Consumer<FizzyComponentElement.Builder> textCustomizer;

    private @Nullable ResourceLocation iconTexture;
    private boolean iconStretchToFit;
    private boolean iconAllowUpscale;

    private int iconSizePx;
    private int contentGapPx;
    private int contentPaddingLeftPx;
    private int contentPaddingRightPx;
    private ContentLayout contentLayout;
    private IconVerticalAlign iconVerticalAlign;

    private @Nullable FizzyComponentElement generatedTextElement;
    private boolean generatedTextDirty = true;
    private int generatedTextWidthPx = Integer.MIN_VALUE;
    private String generatedSourceText = "";
    private String generatedDisplayText = "";

    private @Nullable FizzyButton button;
    private @Nullable ContentOverlayWidget contentOverlay;
    private @Nullable InitContext initContext;

    private FizzyButtonElement(Builder builder) {
        this.onPress = builder.onPress;
        this.tooltip = builder.tooltip;
        this.customTooltipElement = builder.customTooltipElement;
        this.pressSound = builder.pressSound;
        this.narrationMessage = builder.narrationMessage;
        this.textComponent = builder.textComponent;
        this.customTextElement = builder.customTextElement;
        this.textCustomizer = builder.textCustomizer;
        this.iconTexture = builder.iconTexture;
        this.iconStretchToFit = builder.iconStretchToFit;
        this.iconAllowUpscale = builder.iconAllowUpscale;
        this.iconSizePx = builder.iconSizePx;
        this.contentGapPx = builder.contentGapPx;
        this.contentPaddingLeftPx = builder.contentPaddingLeftPx;
        this.contentPaddingRightPx = builder.contentPaddingRightPx;
        this.contentLayout = builder.contentLayout;
        this.iconVerticalAlign = builder.iconVerticalAlign;
    }

    public static Builder builder(FizzyButton.OnPress onPress) {
        return new Builder(onPress);
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.button = null;
        this.contentOverlay = null;
        this.initContext = context;

        FizzyButton.Builder builder = FizzyButton.builder(Component.empty(), this.onPress);
        builder.createNarration(defaultMessage -> this.narrationMessage.copy());
        builder.bounds(leftPx, topPx, widthPx, heightPx);
        if (this.tooltip != null) {
            builder.tooltip(this.tooltip);
        }

        FizzyButton built = builder.build();
        this.button = built;
        context.addRenderableWidget(built);

        ContentOverlayWidget overlay = new ContentOverlayWidget(leftPx, topPx, widthPx, heightPx);
        this.contentOverlay = overlay;
        context.addRenderableWidget(overlay);
        initCustomTooltipIfNeeded(leftPx, topPx, widthPx, heightPx);

        syncButtonState();
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        FizzyGuiUtils.syncWidgetBounds(this.button, leftPx, topPx, widthPx, heightPx);
        FizzyGuiUtils.syncWidgetBounds(this.contentOverlay, leftPx, topPx, widthPx, heightPx);
        if (this.customTooltipElement != null) {
            this.customTooltipElement.render(g, leftPx, topPx, widthPx, heightPx, partialTick);
        }
    }

    @Override
    public ElementType type() {
        return ElementType.BUTTON;
    }

    @Override
    public List<AbstractWidget> widgets() {
        if (this.button == null || this.contentOverlay == null) {
            return List.of();
        }
        return List.of(this.button, this.contentOverlay);
    }

    @Nullable
    public FizzyButton button() {
        return this.button;
    }

    public FizzyButtonElement setText(Component text) {
        Component safe = Objects.requireNonNull(text, "text");
        this.textComponent = safe;
        this.customTextElement = null;
        this.narrationMessage = safe;
        this.generatedTextDirty = true;
        this.generatedTextWidthPx = Integer.MIN_VALUE;
        return this;
    }

    public FizzyButtonElement setText(FizzyComponentElement textElement) {
        this.customTextElement = Objects.requireNonNull(textElement, "textElement");
        this.generatedTextElement = null;
        this.generatedTextDirty = false;
        this.generatedTextWidthPx = Integer.MIN_VALUE;
        return this;
    }

    public FizzyButtonElement setTextConfig(Consumer<FizzyComponentElement.Builder> customizer) {
        Objects.requireNonNull(customizer, "customizer");
        this.textCustomizer = this.textCustomizer.andThen(customizer);
        this.generatedTextDirty = true;
        this.generatedTextWidthPx = Integer.MIN_VALUE;
        return this;
    }

    public FizzyButtonElement setIcon(@Nullable ResourceLocation texture) {
        this.iconTexture = texture;
        return this;
    }

    public FizzyButtonElement setIcon(@Nullable FizzyIcon icon) {
        this.iconTexture = icon == null ? null : icon.texture();
        return this;
    }

    public FizzyButtonElement clearIcon() {
        return setIcon((ResourceLocation) null);
    }

    public FizzyButtonElement setLayout(ContentLayout layout) {
        this.contentLayout = Objects.requireNonNull(layout, "layout");
        return this;
    }

    public FizzyButtonElement setIconAlign(IconVerticalAlign align) {
        this.iconVerticalAlign = Objects.requireNonNull(align, "align");
        return this;
    }

    public FizzyButtonElement setIconFit(boolean stretchToFit, boolean allowUpscale) {
        this.iconStretchToFit = stretchToFit;
        this.iconAllowUpscale = allowUpscale;
        return this;
    }

    public FizzyButtonElement setIconSizePx(int iconSizePx) {
        if (iconSizePx <= 0) {
            throw new IllegalArgumentException("iconSizePx must be > 0");
        }
        this.iconSizePx = iconSizePx;
        return this;
    }

    public FizzyButtonElement setContentGapPx(int contentGapPx) {
        if (contentGapPx < 0) {
            throw new IllegalArgumentException("contentGapPx must be >= 0");
        }
        this.contentGapPx = contentGapPx;
        return this;
    }

    public FizzyButtonElement setContentPaddingPx(int leftPx, int rightPx) {
        if (leftPx < 0 || rightPx < 0) {
            throw new IllegalArgumentException("content padding must be >= 0");
        }
        this.contentPaddingLeftPx = leftPx;
        this.contentPaddingRightPx = rightPx;
        return this;
    }

    public FizzyButtonElement setTooltip(@Nullable Tooltip tooltip) {
        hideCustomTooltip(this.customTooltipElement);
        this.customTooltipElement = null;
        this.tooltip = tooltip;
        if (this.button != null) {
            this.button.setTooltip(tooltip);
        }
        return this;
    }

    public FizzyButtonElement setTooltip(Component component) {
        return setTooltip(Tooltip.create(Objects.requireNonNull(component, "component")));
    }

    public FizzyButtonElement setTooltip(FizzyTooltipElement tooltipElement) {
        FizzyTooltipElement safe = Objects.requireNonNull(tooltipElement, "tooltipElement");
        if (this.customTooltipElement != safe) {
            hideCustomTooltip(this.customTooltipElement);
        }
        this.customTooltipElement = safe;
        this.tooltip = null;
        if (this.button != null) {
            this.button.setTooltip(null);
            initCustomTooltipIfNeeded(this.button.getX(), this.button.getY(), this.button.getWidth(), this.button.getHeight());
        }
        return this;
    }

    public FizzyButtonElement setPressSound(@Nullable SoundEvent sound) {
        this.pressSound = sound;
        if (this.button != null) {
            this.button.setPressSound(sound);
        }
        return this;
    }

    public FizzyButtonElement setNarration(Component narrationMessage) {
        this.narrationMessage = Objects.requireNonNull(narrationMessage, "narrationMessage");
        return this;
    }

    private void syncButtonState() {
        if (this.button == null) {
            return;
        }
        this.button.setMessage(Component.empty());
        this.button.setTooltip(this.tooltip);
        this.button.setPressSound(this.pressSound);
        if (this.customTooltipElement != null) {
            this.button.setTooltip(null);
        }
    }

    private void initCustomTooltipIfNeeded(int leftPx, int topPx, int widthPx, int heightPx) {
        if (this.customTooltipElement == null || this.initContext == null) {
            return;
        }
        hideCustomTooltip(this.customTooltipElement);
        this.customTooltipElement.init(this.initContext, leftPx, topPx, widthPx, heightPx);
        showCustomTooltip(this.customTooltipElement);
    }

    private static void hideCustomTooltip(@Nullable FizzyTooltipElement tooltipElement) {
        FizzyTooltipWidgetUtil.hide(tooltipElement);
    }

    private static void showCustomTooltip(@Nullable FizzyTooltipElement tooltipElement) {
        FizzyTooltipWidgetUtil.show(tooltipElement);
    }

    private void renderCompositeContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (this.button == null) {
            return;
        }

        int left = this.button.getX();
        int top = this.button.getY();
        int width = this.button.getWidth();
        int height = this.button.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        int contentLeft = left + Math.max(0, this.contentPaddingLeftPx);
        int contentRight = left + width - Math.max(0, this.contentPaddingRightPx);
        if (contentRight <= contentLeft) {
            return;
        }
        int contentWidth = contentRight - contentLeft;

        boolean hasIcon = this.iconTexture != null;
        boolean hasText = this.customTextElement != null || this.textComponent != null;
        if (!hasIcon && !hasText) {
            return;
        }

        int iconBoxWidth = hasIcon ? Math.min(Math.max(1, this.iconSizePx), contentWidth) : 0;
        int gap = hasIcon && hasText ? Math.max(0, this.contentGapPx) : 0;
        if (iconBoxWidth + gap > contentWidth) {
            iconBoxWidth = Math.min(iconBoxWidth, contentWidth);
            gap = Math.max(0, contentWidth - iconBoxWidth);
        }
        int textWidth = Math.max(0, contentWidth - iconBoxWidth - gap);

        int textLeft;
        int iconLeft;
        if (this.contentLayout == ContentLayout.ICON_LEFT_TEXT_RIGHT) {
            iconLeft = contentLeft;
            textLeft = hasIcon ? contentLeft + iconBoxWidth + gap : contentLeft;
        } else {
            textLeft = contentLeft;
            iconLeft = contentRight - iconBoxWidth;
        }

        boolean visualPressed = this.button.isFocused() || this.button.isMouseOver(mouseX, mouseY);
        int yOffset = visualPressed ? 1 : 0;
        int textBaseTop = top + yOffset;
        int textBaseHeight = Math.max(0, height);

        if (hasText && textWidth > 0) {
            FizzyComponentElement textElement = resolveTextElement(textWidth);
            DrawArea textArea = resolveTextArea(textElement, textBaseTop, textBaseHeight);
            textElement.render(g, textLeft, textArea.top(), textWidth, textArea.height(), partialTick);
        }

        if (hasIcon && iconBoxWidth > 0) {
            int availableHeight = Math.max(0, height);
            int iconHeight = Math.min(Math.max(1, this.iconSizePx), availableHeight);
            int iconTop = switch (this.iconVerticalAlign) {
                case TOP -> top + yOffset;
                case BOTTOM -> top + yOffset + availableHeight - iconHeight;
                case CENTER -> top + yOffset + (availableHeight - iconHeight) / 2;
            };
            float alpha = this.button.active ? 1.0f : 0.5f;
            FizzyGuiUtils.drawTextureFit(
                    g,
                    this.iconTexture,
                    iconLeft,
                    iconTop,
                    iconBoxWidth,
                    iconHeight,
                    this.iconStretchToFit,
                    this.iconAllowUpscale,
                    alpha
            );
        }
    }

    private DrawArea resolveTextArea(FizzyComponentElement textElement, int top, int height) {
        int safeHeight = Math.max(0, height);
        if (textElement.alignMode() == TextRenderer.Align.CENTER) {
            return new DrawArea(top, safeHeight);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || safeHeight <= 0) {
            return new DrawArea(top, safeHeight);
        }
        float lineHeightPx = mc.font.lineHeight * Math.max(0.01f, textElement.textScale());
        int drawHeight = Math.max(1, Math.min(Math.round(lineHeightPx), safeHeight));
        int offset = Math.round((safeHeight - lineHeightPx) * 0.5f);
        int maxOffset = Math.max(0, safeHeight - drawHeight);
        offset = Math.max(0, Math.min(offset, maxOffset));
        return new DrawArea(top + offset, drawHeight);
    }

    private FizzyComponentElement resolveTextElement(int textWidthPx) {
        if (this.customTextElement != null) {
            return this.customTextElement;
        }
        Component sourceText = this.textComponent != null ? this.textComponent : Component.empty();
        Component displayText = FizzyGuiUtils.ellipsizeText(sourceText, textWidthPx);
        String sourceRaw = sourceText.getString();
        String displayRaw = displayText.getString();
        if (this.generatedTextElement == null
                || this.generatedTextDirty
                || this.generatedTextWidthPx != textWidthPx
                || !this.generatedSourceText.equals(sourceRaw)
                || !this.generatedDisplayText.equals(displayRaw)) {
            FizzyComponentElement.Builder builder = FizzyComponentElement.builder()
                    .addText(displayText)
                    .wrap(false)
                    .align(TextRenderer.Align.LEFT)
                    .shadow(true);
            this.textCustomizer.accept(builder);
            this.generatedTextElement = builder.build();
            this.generatedTextDirty = false;
            this.generatedTextWidthPx = textWidthPx;
            this.generatedSourceText = sourceRaw;
            this.generatedDisplayText = displayRaw;
        }
        return this.generatedTextElement;
    }

    private record DrawArea(int top, int height) {
    }

    private final class ContentOverlayWidget extends AbstractWidget {
        private ContentOverlayWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            this.active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            renderCompositeContent(g, mouseX, mouseY, partialTick);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return false;
        }
    }

    public enum ContentLayout {
        TEXT_LEFT_ICON_RIGHT,
        ICON_LEFT_TEXT_RIGHT
    }

    public enum IconVerticalAlign {
        TOP,
        CENTER,
        BOTTOM
    }

    public static final class Builder {
        private final FizzyButton.OnPress onPress;
        private @Nullable Tooltip tooltip;
        private @Nullable FizzyTooltipElement customTooltipElement;
        private @Nullable SoundEvent pressSound;
        private Component narrationMessage = Component.empty();
        private @Nullable Component textComponent = Component.empty();
        private @Nullable FizzyComponentElement customTextElement;
        private Consumer<FizzyComponentElement.Builder> textCustomizer = builder -> {};
        private @Nullable ResourceLocation iconTexture;
        private boolean iconStretchToFit;
        private boolean iconAllowUpscale;
        private int iconSizePx = DEFAULT_ICON_SIZE_PX;
        private int contentGapPx = DEFAULT_CONTENT_GAP_PX;
        private int contentPaddingLeftPx = DEFAULT_CONTENT_PADDING_LEFT_PX;
        private int contentPaddingRightPx = DEFAULT_CONTENT_PADDING_RIGHT_PX;
        private ContentLayout contentLayout = ContentLayout.TEXT_LEFT_ICON_RIGHT;
        private IconVerticalAlign iconVerticalAlign = IconVerticalAlign.CENTER;

        private Builder(FizzyButton.OnPress onPress) {
            this.onPress = Objects.requireNonNull(onPress, "onPress");
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            this.customTooltipElement = null;
            return this;
        }

        public Builder tooltip(Component component) {
            return this.tooltip(Tooltip.create(Objects.requireNonNull(component, "component")));
        }

        public Builder tooltip(FizzyTooltipElement tooltipElement) {
            this.customTooltipElement = Objects.requireNonNull(tooltipElement, "tooltipElement");
            this.tooltip = null;
            return this;
        }

        public Builder pressSound(@Nullable SoundEvent sound) {
            this.pressSound = sound;
            return this;
        }

        public Builder narration(Component narrationMessage) {
            this.narrationMessage = Objects.requireNonNull(narrationMessage, "narrationMessage");
            return this;
        }

        public Builder text(Component text) {
            Component safe = Objects.requireNonNull(text, "text");
            this.textComponent = safe;
            this.customTextElement = null;
            this.narrationMessage = safe;
            return this;
        }

        public Builder text(FizzyComponentElement textElement) {
            this.customTextElement = Objects.requireNonNull(textElement, "textElement");
            return this;
        }

        public Builder textConfig(Consumer<FizzyComponentElement.Builder> customizer) {
            Objects.requireNonNull(customizer, "customizer");
            this.textCustomizer = this.textCustomizer.andThen(customizer);
            return this;
        }

        public Builder icon(ResourceLocation texture) {
            this.iconTexture = Objects.requireNonNull(texture, "texture");
            return this;
        }

        public Builder icon(FizzyIcon icon) {
            this.iconTexture = Objects.requireNonNull(icon, "icon").texture();
            return this;
        }

        public Builder icon(ResourceLocation texture, boolean stretchToFit, boolean allowUpscale) {
            this.iconTexture = Objects.requireNonNull(texture, "texture");
            this.iconStretchToFit = stretchToFit;
            this.iconAllowUpscale = allowUpscale;
            return this;
        }

        public Builder icon(FizzyIcon icon, boolean stretchToFit, boolean allowUpscale) {
            this.iconTexture = Objects.requireNonNull(icon, "icon").texture();
            this.iconStretchToFit = stretchToFit;
            this.iconAllowUpscale = allowUpscale;
            return this;
        }

        public Builder iconSizePx(int iconSizePx) {
            if (iconSizePx <= 0) {
                throw new IllegalArgumentException("iconSizePx must be > 0");
            }
            this.iconSizePx = iconSizePx;
            return this;
        }

        public Builder contentGapPx(int contentGapPx) {
            if (contentGapPx < 0) {
                throw new IllegalArgumentException("contentGapPx must be >= 0");
            }
            this.contentGapPx = contentGapPx;
            return this;
        }

        public Builder contentPaddingPx(int leftPx, int rightPx) {
            if (leftPx < 0 || rightPx < 0) {
                throw new IllegalArgumentException("content padding must be >= 0");
            }
            this.contentPaddingLeftPx = leftPx;
            this.contentPaddingRightPx = rightPx;
            return this;
        }

        public Builder layout(ContentLayout layout) {
            this.contentLayout = Objects.requireNonNull(layout, "layout");
            return this;
        }

        public Builder iconAlign(IconVerticalAlign align) {
            this.iconVerticalAlign = Objects.requireNonNull(align, "align");
            return this;
        }

        public FizzyButtonElement build() {
            return new FizzyButtonElement(this);
        }
    }
}
