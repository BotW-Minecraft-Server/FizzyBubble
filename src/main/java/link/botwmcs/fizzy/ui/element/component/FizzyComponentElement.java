package link.botwmcs.fizzy.ui.element.component;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FizzyComponentElement implements ElementPainter {
    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    private final Component text;
    private final List<Component> lines;
    private final boolean multiline;
    private final boolean wrap;
    private final Align align;
    private final float textScale;
    private final float lineSpacing;
    private final int color;
    private final boolean shadow;
    private final boolean clipToPad;
    private final boolean rainbow;
    private final int[] rainbowColors;
    private final float rainbowSpeed;
    private final boolean rainbowStatic;

    private float rainbowPhase;

    private FizzyComponentElement(Builder builder) {
        this.text = builder.text;
        this.lines = builder.lines != null ? List.copyOf(builder.lines) : null;
        this.multiline = builder.multiline;
        this.wrap = builder.wrap;
        this.align = builder.align;
        this.textScale = builder.textScale;
        this.lineSpacing = builder.lineSpacing;
        this.color = builder.color;
        this.shadow = builder.shadow;
        this.clipToPad = builder.clipToPad;
        this.rainbow = builder.rainbow;
        this.rainbowColors = builder.rainbowColors;
        this.rainbowSpeed = builder.rainbowSpeed;
        this.rainbowStatic = builder.rainbowStatic;
    }

    public static Builder builder(Component text) {
        return new Builder(text);
    }

    public static FizzyComponentElement singleLine(Component text) {
        return builder(text).singleLine().build();
    }

    public static FizzyComponentElement singleLine(String text) {
        return singleLine(Component.literal(text));
    }

    public static FizzyComponentElement multiLine(Component text) {
        return builder(text).multiLine().wrap(true).build();
    }

    public static FizzyComponentElement multiLine(String text) {
        return multiLine(Component.literal(text));
    }

    public static FizzyComponentElement multiLine(List<Component> lines) {
        return builder(Component.empty()).lines(lines).multiLine().wrap(false).build();
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
        float scale = Math.max(0.01f, this.textScale);
        float availableWidth = widthPx / scale;
        float availableHeight = heightPx / scale;
        int wrapWidth = (int) Math.floor(availableWidth);

        List<FormattedCharSequence> renderedLines = resolveLines(font, wrapWidth);
        if (renderedLines.isEmpty()) {
            return;
        }

        if (clipToPad) {
            g.enableScissor(leftPx, topPx, leftPx + widthPx, topPx + heightPx);
        }

        g.pose().pushPose();
        g.pose().translate(leftPx, topPx, 0);
        g.pose().scale(scale, scale, 1.0f);

        float step = font.lineHeight + this.lineSpacing;
        float y = 0.0f;
        if (align == Align.CENTER) {
            int lineCount = renderedLines.size();
            float totalHeight = lineCount * font.lineHeight + Math.max(0, lineCount - 1) * this.lineSpacing;
            y = (availableHeight - totalHeight) * 0.5f;
        }
        int[] colors = resolveRainbowColors();
        int colorCount = colors.length;
        float time = 0f;
        if (this.rainbow && colorCount > 0) {
            float delta = Math.max(0f, partialTick);
            this.rainbowPhase += delta * Math.max(0f, this.rainbowSpeed);
            time = this.rainbowPhase;
        }
        for (FormattedCharSequence line : renderedLines) {
            int lineWidth = font.width(line);
            float x;
            switch (align) {
                case CENTER -> x = (availableWidth - lineWidth) * 0.5f;
                case RIGHT -> x = availableWidth - lineWidth;
                default -> x = 0.0f;
            }
            if (this.rainbow && colorCount > 0) {
                float basePhase = time;
                if (this.rainbowStatic) {
                    int color = sampleRainbowColor(basePhase);
                    g.drawString(font, line, Math.round(x), Math.round(y), color, this.shadow);
                } else {
                    String text = toPlainString(line);
                    float drawX = x;
                    int charIndex = 0;
                    for (int i = 0; i < text.length(); ) {
                        int codePoint = text.codePointAt(i);
                        String ch = new String(Character.toChars(codePoint));
                        float phase = basePhase + charIndex * 0.08f;
                        int color = sampleRainbowColor(phase);
                        g.drawString(font, ch, Math.round(drawX), Math.round(y), color, this.shadow);
                        drawX += font.width(ch);
                        i += Character.charCount(codePoint);
                        charIndex++;
                    }
                }
            } else {
                g.drawString(font, line, Math.round(x), Math.round(y), this.color, this.shadow);
            }
            y += step;
        }

        g.pose().popPose();
        if (clipToPad) {
            g.disableScissor();
        }
    }

    @Override
    public ElementType type() {
        return ElementType.COMPONENT;
    }

    private List<FormattedCharSequence> resolveLines(Font font, int wrapWidth) {
        if (!multiline) {
            return List.of(this.text.getVisualOrderText());
        }
        if (lines != null && !lines.isEmpty()) {
            if (!wrap) {
                List<FormattedCharSequence> out = new ArrayList<>(lines.size());
                for (Component line : lines) {
                    out.add(line.getVisualOrderText());
                }
                return out;
            }
            if (wrapWidth <= 0) {
                return List.of();
            }
            List<FormattedCharSequence> out = new ArrayList<>();
            for (Component line : lines) {
                out.addAll(font.split(line, wrapWidth));
            }
            return out;
        }
        if (wrap) {
            if (wrapWidth <= 0) {
                return List.of();
            }
            return font.split(this.text, wrapWidth);
        }
        return font.split(this.text, Integer.MAX_VALUE);
    }

    private static String toPlainString(FormattedCharSequence sequence) {
        StringBuilder sb = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    private static final char[] DEFAULT_RAINBOW_CODES = new char[] {
            'c', '6', 'e', 'a', 'b', '9', 'd'
    };

    private int[] resolveRainbowColors() {
        if (!this.rainbow) {
            return new int[0];
        }
        if (this.rainbowColors != null && this.rainbowColors.length > 0) {
            return this.rainbowColors;
        }
        return colorsFromCodes(DEFAULT_RAINBOW_CODES);
    }

    private int sampleRainbowColor(float phase) {
        float t = phase % 1.0f;
        if (t < 0f) {
            t += 1.0f;
        }
        if (this.rainbowColors != null && this.rainbowColors.length > 0) {
            return samplePalette(this.rainbowColors, t);
        }
        return hsvToRgb(t, 1.0f, 1.0f);
    }

    private static int samplePalette(int[] colors, float t) {
        int count = colors.length;
        if (count == 1) {
            return colors[0];
        }
        float scaled = t * count;
        int index = (int) Math.floor(scaled) % count;
        float frac = scaled - (float) Math.floor(scaled);
        int c1 = colors[index];
        int c2 = colors[(index + 1) % count];
        return lerpRgb(c1, c2, frac);
    }

    private static int lerpRgb(int c1, int c2, float t) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * t + 0.5f);
        int g = (int) (g1 + (g2 - g1) * t + 0.5f);
        int b = (int) (b1 + (b2 - b1) * t + 0.5f);
        return (r << 16) | (g << 8) | b;
    }

    private static int hsvToRgb(float h, float s, float v) {
        float hh = (h % 1.0f + 1.0f) % 1.0f;
        float c = v * s;
        float x = c * (1 - Math.abs((hh * 6f) % 2 - 1));
        float m = v - c;
        float r, g, b;
        int sector = (int) (hh * 6f);
        switch (sector) {
            case 0 -> { r = c; g = x; b = 0f; }
            case 1 -> { r = x; g = c; b = 0f; }
            case 2 -> { r = 0f; g = c; b = x; }
            case 3 -> { r = 0f; g = x; b = c; }
            case 4 -> { r = x; g = 0f; b = c; }
            default -> { r = c; g = 0f; b = x; }
        }
        int ri = (int) ((r + m) * 255f + 0.5f);
        int gi = (int) ((g + m) * 255f + 0.5f);
        int bi = (int) ((b + m) * 255f + 0.5f);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static int[] colorsFromCodes(char[] codes) {
        if (codes == null || codes.length == 0) {
            return new int[0];
        }
        int count = Math.min(codes.length, 7);
        int[] out = new int[count];
        int outIdx = 0;
        for (int i = 0; i < codes.length && outIdx < count; i++) {
            ChatFormatting formatting = ChatFormatting.getByCode(codes[i]);
            Integer color = formatting != null ? formatting.getColor() : null;
            if (color != null) {
                out[outIdx++] = color;
            }
        }
        if (outIdx == count) {
            return out;
        }
        int[] trimmed = new int[outIdx];
        System.arraycopy(out, 0, trimmed, 0, outIdx);
        return trimmed;
    }

    public static final class Builder {
        private Component text;
        private List<Component> lines;
        private boolean multiline;
        private boolean wrap = true;
        private Align align = Align.LEFT;
        private float textScale = 1.0f;
        private float lineSpacing = 0.0f;
        private int color = 0xFFFFFF;
        private boolean shadow = false;
        private boolean clipToPad = true;
        private boolean rainbow = false;
        private int[] rainbowColors;
        private float rainbowSpeed = 0.01f;
        private boolean rainbowStatic = false;

        private Builder(Component text) {
            this.text = Objects.requireNonNull(text, "text");
        }

        public Builder text(Component text) {
            this.text = Objects.requireNonNull(text, "text");
            return this;
        }

        public Builder lines(List<Component> lines) {
            this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
            this.multiline = true;
            return this;
        }

        public Builder singleLine() {
            this.multiline = false;
            return this;
        }

        public Builder multiLine() {
            this.multiline = true;
            return this;
        }

        public Builder wrap(boolean wrap) {
            this.wrap = wrap;
            return this;
        }

        public Builder align(Align align) {
            this.align = Objects.requireNonNull(align, "align");
            return this;
        }

        public Builder textScale(float scale) {
            this.textScale = scale;
            return this;
        }

        public Builder lineSpacing(float spacing) {
            this.lineSpacing = spacing;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder shadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        public Builder clipToPad(boolean clipToPad) {
            this.clipToPad = clipToPad;
            return this;
        }

        public Builder allowOverflow(boolean allowOverflow) {
            this.clipToPad = !allowOverflow;
            return this;
        }

        public Builder rainbow() {
            this.rainbow = true;
            this.rainbowColors = null;
            return this;
        }

        public Builder rainbow(char... codes) {
            this.rainbow = true;
            if (codes == null || codes.length == 0) {
                this.rainbowColors = null;
                return this;
            }
            if (codes.length > 7) {
                throw new IllegalArgumentException("rainbow colors must be <= 7");
            }
            this.rainbowColors = colorsFromCodes(codes);
            return this;
        }

        public Builder rainbow(float speed) {
            this.rainbow = true;
            this.rainbowColors = null;
            this.rainbowSpeed = speed;
            return this;
        }

        public Builder rainbow(float speed, char... codes) {
            this.rainbow = true;
            this.rainbowSpeed = speed;
            if (codes == null || codes.length == 0) {
                this.rainbowColors = null;
                return this;
            }
            if (codes.length > 7) {
                throw new IllegalArgumentException("rainbow colors must be <= 7");
            }
            this.rainbowColors = colorsFromCodes(codes);
            return this;
        }

        public Builder rainbowStatic() {
            return rainbowStatic(true);
        }

        public Builder rainbowStatic(boolean staticMode) {
            this.rainbow = true;
            this.rainbowStatic = staticMode;
            return this;
        }

        public FizzyComponentElement build() {
            return new FizzyComponentElement(this);
        }
    }
}
