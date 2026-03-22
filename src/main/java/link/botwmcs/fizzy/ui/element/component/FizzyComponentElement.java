package link.botwmcs.fizzy.ui.element.component;

import link.botwmcs.fizzy.client.util.TextRenderer;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class FizzyComponentElement implements AnimatableElement {
    private static final float FCE_LAYER_Z = 80.0f;

    private final TextRenderer.Builder<?> baseBuilder;
    private final List<LineSpec> lineSpecs;
    private final boolean wrap;
    private final boolean autoEllipsis;
    private final boolean centerEllipsis;
    private final TextRenderer.Align align;
    private final float lineSpacing;

    private int cachedWidthPx = Integer.MIN_VALUE;
    private LineLayout cachedLayout;

    private FizzyComponentElement(Builder builder) {
        this.baseBuilder = builder.baseBuilder.copyForText(Component.empty());
        this.lineSpecs = List.copyOf(builder.lines);
        this.wrap = builder.wrap;
        this.autoEllipsis = builder.autoEllipsis;
        this.centerEllipsis = builder.centerEllipsis;
        this.align = builder.align;
        this.lineSpacing = builder.lineSpacing;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FizzyComponentElement singleLine(Component text) {
        return builder().addText(text).build();
    }

    public static FizzyComponentElement singleLine(String text) {
        return singleLine(Component.literal(text));
    }

    public static FizzyComponentElement multiLine(Component text) {
        return builder().addTextLines(text).wrap(true).build();
    }

    public static FizzyComponentElement multiLine(String text) {
        return multiLine(Component.literal(text));
    }

    public static FizzyComponentElement multiLine(List<Component> lines) {
        Builder builder = builder().wrap(false);
        for (Component line : lines) {
            builder.addText(line);
        }
        return builder.build();
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        Font font = mc.font;
        LineLayout layout = ensureLayout(font, widthPx);
        List<TextRenderer> lineRenderers = layout.renderers();
        if (lineRenderers.isEmpty()) {
            return;
        }

        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, FCE_LAYER_Z);
        try {
            float availableWidthPx = widthPx;
            float availableHeightPx = heightPx;
            float totalHeightPx = layout.totalHeightPx();
            float y = 0.0f;
            if (align == TextRenderer.Align.CENTER) {
                y = (availableHeightPx - totalHeightPx) * 0.5f;
            }

            List<Float> lineWidthsPx = layout.widthsPx();
            int lineCount = lineRenderers.size();
            for (int i = 0; i < lineCount; i++) {
                TextRenderer lineRenderer = lineRenderers.get(i);
                float lineWidthPx = lineWidthsPx.get(i);
                float x;
                switch (align) {
                    case CENTER -> x = (availableWidthPx - lineWidthPx) * 0.5f;
                    case RIGHT -> x = availableWidthPx - lineWidthPx;
                    default -> x = 0.0f;
                }
                int offsetXPx = Math.round(x);
                int offsetYPx = Math.round(y);
                g.pose().pushPose();
                g.pose().translate(offsetXPx, offsetYPx, 0.0f);
                lineRenderer.render(g, leftPx, topPx, widthPx, heightPx, partialTick);
                g.pose().popPose();

                float scale = Math.max(0.01f, lineRenderer.textScale());
                float lineHeightPx = font.lineHeight * scale;
                y += lineHeightPx;
                if (i < lineCount - 1) {
                    y += lineSpacing * scale;
                }
            }
        } finally {
            g.pose().popPose();
        }
    }

    @Override
    public ElementType type() {
        return ElementType.COMPONENT;
    }

    public TextRenderer.Align alignMode() {
        return this.align;
    }

    public float textScale() {
        return Math.max(0.01f, this.baseBuilder.getTextScale());
    }

    public Component plainText() {
        if (lineSpecs.isEmpty()) {
            return Component.empty();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lineSpecs.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(lineSpecs.get(i).text().getString());
        }
        return Component.literal(sb.toString());
    }

    private LineLayout ensureLayout(Font font, int widthPx) {
        if (!wrap && !autoEllipsis) {
            if (cachedLayout == null) {
                cachedLayout = buildLayout(font, Integer.MAX_VALUE);
            }
            return cachedLayout;
        }
        if (cachedLayout == null || cachedWidthPx != widthPx) {
            cachedWidthPx = widthPx;
            cachedLayout = buildLayout(font, widthPx);
        }
        return cachedLayout;
    }

    private LineLayout buildLayout(Font font, int widthPx) {
        if (lineSpecs.isEmpty()) {
            return new LineLayout(List.of(), List.of(), 0.0f, 0.0f);
        }

        List<TextRenderer> renderers = new ArrayList<>();
        List<Float> widthsPx = new ArrayList<>();
        float maxWidthPx = 0.0f;
        float totalHeightPx = 0.0f;
        boolean first = true;

        for (LineSpec spec : lineSpecs) {
            TextRenderer.Builder<?> configured = baseBuilder.copyForText(spec.text);
            if (spec.config != null) {
                spec.config.accept(configured);
            }
            float lineScale = Math.max(0.01f, configured.getTextScale());
            int wrapWidth = resolveWrapWidth(widthPx, lineScale);
            Component lineText = applyEllipsisIfNeeded(font, spec.text, widthPx, lineScale);
            List<Component> parts = wrap ? FizzyGuiUtils.splitLine(font, spec.text, wrapWidth) : List.of(lineText);
            for (Component part : parts) {
                TextRenderer renderer = configured.copyForText(part).buildRenderer();
                float scale = Math.max(0.01f, renderer.textScale());
                int lineWidth = renderer.measure(font, Integer.MAX_VALUE).maxWidth();
                float lineWidthPx = lineWidth * scale;
                float lineHeightPx = font.lineHeight * scale;

                renderers.add(renderer);
                widthsPx.add(lineWidthPx);
                maxWidthPx = Math.max(maxWidthPx, lineWidthPx);
                if (!first) {
                    totalHeightPx += lineSpacing * scale;
                }
                totalHeightPx += lineHeightPx;
                first = false;
            }
        }

        return new LineLayout(List.copyOf(renderers), List.copyOf(widthsPx), totalHeightPx, maxWidthPx);
    }

    private int resolveWrapWidth(int widthPx, float scale) {
        if (!wrap) {
            return Integer.MAX_VALUE;
        }
        return resolveWidthInFontUnits(widthPx, scale);
    }

    private int resolveWidthInFontUnits(int widthPx, float scale) {
        float safeScale = Math.max(0.01f, scale);
        int wrapWidth = (int) Math.floor(widthPx / safeScale);
        return Math.max(1, wrapWidth);
    }

    private Component applyEllipsisIfNeeded(Font font, Component text, int widthPx, float scale) {
        if (wrap || !autoEllipsis) {
            return text;
        }
        int maxWidth = resolveWidthInFontUnits(widthPx, scale);
        return switch (align) {
            case RIGHT -> FizzyGuiUtils.ellipsizeTextLeft(font, text, maxWidth);
            case CENTER -> centerEllipsis ? FizzyGuiUtils.ellipsizeText(font, text, maxWidth) : text;
            case LEFT -> FizzyGuiUtils.ellipsizeText(font, text, maxWidth);
        };
    }

    private record LineSpec(Component text, Consumer<TextRenderer.Builder<?>> config) {
    }

    private record LineLayout(List<TextRenderer> renderers, List<Float> widthsPx, float totalHeightPx, float maxWidthPx) {
    }

    public static final class Builder {
        private final TextRenderer.Builder<?> baseBuilder = TextRenderer.builder(Component.empty());
        private final List<LineSpec> lines = new ArrayList<>();
        private boolean wrap = true;
        private boolean autoEllipsis;
        private boolean centerEllipsis;
        private TextRenderer.Align align = TextRenderer.Align.CENTER;
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

        public Builder autoEllipsis(boolean autoEllipsis) {
            this.autoEllipsis = autoEllipsis;
            return this;
        }

        public Builder autoEllipsis() {
            return autoEllipsis(true);
        }

        public Builder centerEllipsis(boolean centerEllipsis) {
            this.centerEllipsis = centerEllipsis;
            return this;
        }

        public Builder centerEllipsis() {
            return centerEllipsis(true);
        }

        public Builder align(TextRenderer.Align align) {
            this.align = Objects.requireNonNull(align, "align");
            return this;
        }

        public Builder lineSpacing(float spacing) {
            this.lineSpacing = spacing;
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

        public Builder bold(boolean bold) {
            baseBuilder.bold(bold);
            return this;
        }

        public Builder bold() {
            baseBuilder.bold();
            return this;
        }

        public Builder underline(boolean underline) {
            baseBuilder.underline(underline);
            return this;
        }

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

        public Builder gradient(int... colors) {
            baseBuilder.gradient(colors);
            return this;
        }

        public Builder rainbow() {
            baseBuilder.rainbow();
            return this;
        }

        public Builder rainbow(char... codes) {
            baseBuilder.rainbow(codes);
            return this;
        }

        public Builder rainbow(float speed) {
            baseBuilder.rainbow(speed);
            return this;
        }

        public Builder rainbow(float speed, char... codes) {
            baseBuilder.rainbow(speed, codes);
            return this;
        }

        public Builder rainbowStatic(boolean staticMode) {
            baseBuilder.rainbowStatic(staticMode);
            return this;
        }

        public Builder floating() {
            baseBuilder.floating();
            return this;
        }

        public Builder floating(boolean pixelated) {
            baseBuilder.floating(pixelated);
            return this;
        }

        public Builder floating(float speed) {
            baseBuilder.floating(speed);
            return this;
        }

        public Builder floating(float speed, float amplitude) {
            baseBuilder.floating(speed, amplitude);
            return this;
        }

        public Builder floating(boolean pixelated, float speed) {
            baseBuilder.floating(pixelated, speed);
            return this;
        }

        public Builder floating(boolean pixelated, float speed, float amplitude) {
            baseBuilder.floating(pixelated, speed, amplitude);
            return this;
        }

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

        public FizzyComponentElement build() {
            return new FizzyComponentElement(this);
        }
    }
}
