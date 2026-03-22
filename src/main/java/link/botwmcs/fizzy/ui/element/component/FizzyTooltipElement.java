package link.botwmcs.fizzy.ui.element.component;

import link.botwmcs.fizzy.client.util.TextRenderer;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class FizzyTooltipElement implements ElementPainter {
    private static final int TOOLTIP_Z = 400;
    private static int globalSuppressionDepth = 0;
    private static final TooltipColors DEFAULT_COLORS = new TooltipColors(
            -267386864,
            -267386864,
            1347420415,
            1344798847
    ); // FROM VANILLA
    private static final ClientTooltipPositioner DEFAULT_POSITIONER = (screenW, screenH, mouseX, mouseY, tooltipW, tooltipH) -> {
        int x = mouseX + 12;
        int y = mouseY - 12;

        if (x + tooltipW > screenW) {
            x = mouseX - 12 - tooltipW;
        }
        if (x < 4) {
            x = 4;
        }

        if (y + tooltipH + 6 > screenH) {
            y = screenH - tooltipH - 6;
        }
        if (y < 4) {
            y = 4;
        }

        return new Vector2i(x, y);
    };

    private final TextRenderer.Builder<?> baseBuilder;
    private final List<LineSpec> lineSpecs;
    private final boolean wrap;
    private final TextRenderer.Align align;
    private final float lineSpacing;
    private final TooltipColors colors;
    private final ClientTooltipPositioner positioner;
    private final int maxWidthPx;

    private int cachedWrapWidthPx = Integer.MIN_VALUE;
    private LineLayout cachedLayout;

    private TooltipWidget widget;

    private FizzyTooltipElement(Builder builder) {
        this.baseBuilder = builder.baseBuilder.copyForText(Component.empty());
        this.lineSpecs = List.copyOf(builder.lines);
        this.wrap = builder.wrap;
        this.align = builder.align;
        this.lineSpacing = builder.lineSpacing;
        this.colors = builder.colors != null ? builder.colors : DEFAULT_COLORS;
        this.positioner = builder.positioner != null ? builder.positioner : DEFAULT_POSITIONER;
        this.maxWidthPx = Math.max(0, builder.maxWidthPx);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void pushGlobalSuppression() {
        globalSuppressionDepth++;
    }

    public static void popGlobalSuppression() {
        if (globalSuppressionDepth > 0) {
            globalSuppressionDepth--;
        }
    }

    public static boolean isGloballySuppressed() {
        return globalSuppressionDepth > 0;
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.widget = new TooltipWidget(leftPx, topPx, widthPx, heightPx);
        this.widget.active = false;
        this.widget.visible = true;
        context.addRenderableWidget(this.widget);
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        FizzyGuiUtils.syncWidgetBounds(this.widget, leftPx, topPx, widthPx, heightPx);
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }

    @Override
    public List<AbstractWidget> widgets() {
        return this.widget == null ? List.of() : List.of(this.widget);
    }

    private final class TooltipWidget extends AbstractWidget {
        private TooltipWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            if (isGloballySuppressed() || !this.visible || !isPointInside(mouseX, mouseY)) {
                return;
            }
            renderTooltip(g, mouseX, mouseY, partialTick);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
        }

        @Override
        public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
        }

        private boolean isPointInside(double mouseX, double mouseY) {
            int w = this.getWidth();
            int h = this.getHeight();
            if (w <= 0 || h <= 0) {
                return false;
            }
            return mouseX >= this.getX()
                    && mouseX < this.getX() + w
                    && mouseY >= this.getY()
                    && mouseY < this.getY() + h;
        }
    }

    private void renderTooltip(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options.hideGui) {
            return;
        }

        Font font = mc.font;
        LineLayout layout = ensureLayout(font);
        List<TextRenderer> lineRenderers = layout.renderers();
        if (lineRenderers.isEmpty()) {
            return;
        }

        int textWidth = Math.max(1, (int) Math.ceil(layout.maxWidthPx()));
        int textHeight = Math.max(1, (int) Math.ceil(layout.totalHeightPx()));

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        Vector2ic pos = positioner.positionTooltip(screenW, screenH, mouseX, mouseY, textWidth, textHeight);
        int x = pos.x();
        int y = pos.y();

        TooltipRenderUtil.renderTooltipBackground(
                g,
                x, y,
                textWidth, textHeight,
                TOOLTIP_Z,
                colors.bgColor1(),
                colors.bgColor2(),
                colors.edgeColor1(),
                colors.edgeColor2()
        );
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, TOOLTIP_Z);

        float availableWidthPx = textWidth;
        float availableHeightPx = textHeight;
        float totalHeightPx = layout.totalHeightPx();
        float yOffset = 0.0f;
        if (align == TextRenderer.Align.CENTER) {
            yOffset = (availableHeightPx - totalHeightPx) * 0.5f;
        }

        List<Float> lineWidthsPx = layout.widthsPx();
        int lineCount = lineRenderers.size();
        for (int i = 0; i < lineCount; i++) {
            TextRenderer lineRenderer = lineRenderers.get(i);
            float lineWidthPx = lineWidthsPx.get(i);
            float xOffset;
            switch (align) {
                case CENTER -> xOffset = (availableWidthPx - lineWidthPx) * 0.5f;
                case RIGHT -> xOffset = availableWidthPx - lineWidthPx;
                default -> xOffset = 0.0f;
            }
            int offsetXPx = Math.round(xOffset);
            int offsetYPx = Math.round(yOffset);
            g.pose().pushPose();
            g.pose().translate(offsetXPx, offsetYPx, 0.0f);
            lineRenderer.render(g, x, y, textWidth, textHeight, partialTick);
            g.pose().popPose();

            float scale = Math.max(0.01f, lineRenderer.textScale());
            float lineHeightPx = font.lineHeight * scale;
            yOffset += lineHeightPx;
            if (i < lineCount - 1) {
                yOffset += lineSpacing * scale;
            }
        }

        g.pose().popPose();
    }

    private LineLayout ensureLayout(Font font) {
        int wrapWidthPx = resolveWrapWidthPx();
        if (cachedLayout == null || cachedWrapWidthPx != wrapWidthPx) {
            cachedWrapWidthPx = wrapWidthPx;
            cachedLayout = buildLayout(font, wrapWidthPx);
        }
        return cachedLayout;
    }

    private int resolveWrapWidthPx() {
        if (!wrap) {
            return Integer.MAX_VALUE;
        }
        if (maxWidthPx <= 0) {
            return Integer.MAX_VALUE;
        }
        return maxWidthPx;
    }

    private LineLayout buildLayout(Font font, int wrapWidthPx) {
        if (lineSpecs.isEmpty()) {
            return new LineLayout(List.of(), List.of(), 0.0f, 0.0f);
        }

        List<TextRenderer> renderers = new ArrayList<>();
        List<Float> widthsPx = new ArrayList<>();
        float maxWidth = 0.0f;
        float totalHeightPx = 0.0f;
        boolean first = true;
        boolean doWrap = wrap && wrapWidthPx != Integer.MAX_VALUE;

        for (LineSpec spec : lineSpecs) {
            TextRenderer.Builder<?> configured = baseBuilder.copyForText(spec.text);
            if (spec.config != null) {
                spec.config.accept(configured);
            }
            float lineScale = Math.max(0.01f, configured.getTextScale());
            int wrapWidth = doWrap ? Math.max(1, (int) Math.floor(wrapWidthPx / lineScale)) : Integer.MAX_VALUE;
            List<Component> parts = doWrap ? FizzyGuiUtils.splitLine(font, spec.text, wrapWidth) : List.of(spec.text);
            for (Component part : parts) {
                TextRenderer renderer = configured.copyForText(part).buildRenderer();
                float scale = Math.max(0.01f, renderer.textScale());
                int lineWidth = renderer.measure(font, Integer.MAX_VALUE).maxWidth();
                float lineWidthPx = lineWidth * scale;
                float lineHeightPx = font.lineHeight * scale;

                renderers.add(renderer);
                widthsPx.add(lineWidthPx);
                maxWidth = Math.max(maxWidth, lineWidthPx);
                if (!first) {
                    totalHeightPx += lineSpacing * scale;
                }
                totalHeightPx += lineHeightPx;
                first = false;
            }
        }

        return new LineLayout(List.copyOf(renderers), List.copyOf(widthsPx), totalHeightPx, maxWidth);
    }

    private record LineSpec(Component text, Consumer<TextRenderer.Builder<?>> config) {
    }

    private record LineLayout(List<TextRenderer> renderers, List<Float> widthsPx, float totalHeightPx, float maxWidthPx) {
    }

    private record TooltipColors(int bgColor1, int bgColor2, int edgeColor1, int edgeColor2) {
    }

    public static final class Builder {
        private final TextRenderer.Builder<?> baseBuilder = TextRenderer.builder(Component.empty());
        private final List<LineSpec> lines = new ArrayList<>();
        private TooltipColors colors;
        private ClientTooltipPositioner positioner;
        private int maxWidthPx;
        private boolean wrap = true;
        private TextRenderer.Align align = TextRenderer.Align.LEFT;
        private float lineSpacing = 0.0f;

        public Builder addText(Component text) {
            return addText(text, null);
        }

        public Builder addText(String text) {
            return addText(Component.literal(text));
        }

        public Builder addText(Component text, Consumer<TextRenderer.Builder<?>> config) {
            Objects.requireNonNull(text, "text");
            lines.add(new LineSpec(text, config));
            return this;
        }

        public Builder addTextLines(Component text) {
            Objects.requireNonNull(text, "text");
            String raw = text.getString();
            if (raw.isEmpty()) {
                return addText(text);
            }
            String[] parts = raw.split("\\n", -1);
            for (String part : parts) {
                addText(Component.literal(part));
            }
            return this;
        }

        public Builder wrap(boolean wrap) {
            this.wrap = wrap;
            return this;
        }

        public Builder align(TextRenderer.Align align) {
            this.align = Objects.requireNonNull(align, "align");
            return this;
        }

        public Builder lineSpacing(float spacing) {
            this.lineSpacing = spacing;
            return this;
        }

        public Builder maxWidthPx(int maxWidthPx) {
            this.maxWidthPx = maxWidthPx;
            return this;
        }

        public Builder tooltipColors(int bgColor1, int bgColor2, int edgeColor1, int edgeColor2) {
            this.colors = new TooltipColors(bgColor1, bgColor2, edgeColor1, edgeColor2);
            return this;
        }

        public Builder positioner(ClientTooltipPositioner positioner) {
            this.positioner = Objects.requireNonNull(positioner, "positioner");
            return this;
        }

        public Builder textScale(float scale) {
            baseBuilder.textScale(scale);
            return this;
        }

        public Builder letterSpacing(float spacing) {
            baseBuilder.letterSpacing(spacing);
            return this;
        }

        public Builder color(int color) {
            baseBuilder.color(color);
            return this;
        }

        public Builder shadow(boolean shadow) {
            baseBuilder.shadow(shadow);
            return this;
        }

        public Builder clipToPad(boolean clipToPad) {
            baseBuilder.clipToPad(clipToPad);
            return this;
        }

        public Builder allowOverflow(boolean allowOverflow) {
            baseBuilder.allowOverflow(allowOverflow);
            return this;
        }

        @Deprecated
        public Builder bold(boolean bold) {
            baseBuilder.bold(bold);
            return this;
        }

        @Deprecated
        public Builder bold() {
            baseBuilder.bold();
            return this;
        }

        @Deprecated
        public Builder underline(boolean underline) {
            baseBuilder.underline(underline);
            return this;
        }

        @Deprecated
        public Builder underline() {
            baseBuilder.underline();
            return this;
        }

        public Builder strikethrough(boolean strikethrough) {
            baseBuilder.strikethrough(strikethrough);
            return this;
        }

        public Builder strikethrough() {
            baseBuilder.strikethrough();
            return this;
        }

        @Deprecated
        public Builder gradient(int... colors) {
            baseBuilder.gradient(colors);
            return this;
        }

        @Deprecated
        public Builder rainbow() {
            baseBuilder.rainbow();
            return this;
        }

        @Deprecated
        public Builder rainbow(char... codes) {
            baseBuilder.rainbow(codes);
            return this;
        }

        @Deprecated
        public Builder rainbow(float speed) {
            baseBuilder.rainbow(speed);
            return this;
        }

        @Deprecated
        public Builder rainbow(float speed, char... codes) {
            baseBuilder.rainbow(speed, codes);
            return this;
        }

        @Deprecated
        public Builder rainbowStatic(boolean staticMode) {
            baseBuilder.rainbowStatic(staticMode);
            return this;
        }

        @Deprecated
        public Builder floating() {
            baseBuilder.floating();
            return this;
        }

        @Deprecated
        public Builder floating(boolean pixelated) {
            baseBuilder.floating(pixelated);
            return this;
        }

        @Deprecated
        public Builder floating(float speed) {
            baseBuilder.floating(speed);
            return this;
        }

        @Deprecated
        public Builder floating(float speed, float amplitude) {
            baseBuilder.floating(speed, amplitude);
            return this;
        }

        @Deprecated
        public Builder floating(boolean pixelated, float speed) {
            baseBuilder.floating(pixelated, speed);
            return this;
        }

        @Deprecated
        public Builder floating(boolean pixelated, float speed, float amplitude) {
            baseBuilder.floating(pixelated, speed, amplitude);
            return this;
        }

        @Deprecated
        public Builder floatingPixelated(boolean pixelated) {
            baseBuilder.floatingPixelated(pixelated);
            return this;
        }

        public Builder style(String key, TextRenderer.TextStyle style) {
            baseBuilder.style(key, style);
            return this;
        }

        public Builder styles(Map<String, TextRenderer.TextStyle> styles) {
            baseBuilder.styles(styles);
            return this;
        }

        public Builder t2c(Map<String, Integer> colors) {
            baseBuilder.t2c(colors);
            return this;
        }

        public Builder t2g(Map<String, int[]> gradients) {
            baseBuilder.t2g(gradients);
            return this;
        }

        public Builder t2r(Map<String, TextRenderer.RainbowConfig> rainbows) {
            baseBuilder.t2r(rainbows);
            return this;
        }

        public Builder t2f(Map<String, Float> floats) {
            baseBuilder.t2f(floats);
            return this;
        }

        public Builder t2f(Map<String, Float> floats, boolean enable, boolean pixelated) {
            baseBuilder.t2f(floats, enable, pixelated);
            return this;
        }

        public Builder t2b(Map<String, Boolean> bolds) {
            baseBuilder.t2b(bolds);
            return this;
        }

        public Builder t2u(Map<String, Boolean> underlines) {
            baseBuilder.t2u(underlines);
            return this;
        }

        public Builder t2s(Map<String, Boolean> strikes) {
            baseBuilder.t2s(strikes);
            return this;
        }

        public Builder styleRange(int start, int end, TextRenderer.TextStyle style) {
            baseBuilder.styleRange(start, end, style);
            return this;
        }

        public Builder styleIndex(int index, TextRenderer.TextStyle style) {
            baseBuilder.styleIndex(index, style);
            return this;
        }

        public FizzyTooltipElement build() {
            return new FizzyTooltipElement(this);
        }
    }
}
