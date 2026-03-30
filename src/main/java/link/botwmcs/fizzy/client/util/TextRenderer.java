package link.botwmcs.fizzy.client.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TextRenderer {
    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    private static final char[] DEFAULT_RAINBOW_CODES = new char[] {
            'c', '6', 'e', 'a', 'b', '9', 'd'
    };

    private static final float RAINBOW_SPEED_SCALE = 30.0f;
    private static final float RAINBOW_MAX_DELTA_SECONDS = 0.25f;
    private static final float RAINBOW_CHAR_STEP = 0.08f;
    private static final float FLOAT_SPEED_SCALE = 30.0f;
    private static final float FLOAT_MAX_DELTA_SECONDS = 0.25f;
    private static final float FLOAT_CHAR_STEP = 0.08f;
    private static final float FLOAT_DEFAULT_AMPLITUDE = 2.0f;
    private static final int CODEPOINT_CACHE_LIMIT = 256;
    private static final String[] CODEPOINT_CACHE = new String[CODEPOINT_CACHE_LIMIT];

    static {
        for (int i = 0; i < CODEPOINT_CACHE_LIMIT; i++) {
            CODEPOINT_CACHE[i] = String.valueOf((char) i);
        }
    }

    private final Component text;
    private final List<Component> lines;
    private final boolean multiline;
    private final boolean wrap;
    private final Align align;
    private final float textScale;
    private final float lineSpacing;
    private final float letterSpacing;
    private final int color;
    private final boolean shadow;
    private final boolean clipToPad;
    private final boolean rainbow;
    private final int[] rainbowColors;
    private final float rainbowSpeed;
    private final boolean rainbowStatic;
    private final boolean bold;
    private final boolean underline;
    private final boolean strikethrough;
    private final int[] gradientColors;
    private final boolean floating;
    private final float floatingSpeed;
    private final float floatingAmplitude;
    private final boolean floatingPixelated;
    private final List<StyleSpan> styleSpans;
    private final List<StyleKey> styleKeys;

    private float rainbowPhase;
    private long rainbowLastNanos = -1L;
    private float floatingPhase;
    private long floatingLastNanos = -1L;

    private TextRenderer(Builder<?> builder) {
        this.text = builder.text;
        this.lines = builder.lines != null ? List.copyOf(builder.lines) : null;
        this.multiline = builder.multiline;
        this.wrap = builder.wrap;
        this.align = builder.align;
        this.textScale = builder.textScale;
        this.lineSpacing = builder.lineSpacing;
        this.letterSpacing = builder.letterSpacing;
        this.color = builder.color;
        this.shadow = builder.shadow;
        this.clipToPad = builder.clipToPad;
        this.rainbow = builder.rainbow;
        this.rainbowColors = builder.rainbowColors;
        this.rainbowSpeed = builder.rainbowSpeed;
        this.rainbowStatic = builder.rainbowStatic;
        this.bold = builder.bold;
        this.underline = builder.underline;
        this.strikethrough = builder.strikethrough;
        this.gradientColors = builder.gradientColors;
        this.floating = builder.floating;
        this.floatingSpeed = builder.floatingSpeed;
        this.floatingAmplitude = builder.floatingAmplitude;
        this.floatingPixelated = builder.floatingPixelated;
        this.styleSpans = List.copyOf(builder.styleSpans);
        this.styleKeys = List.copyOf(builder.styleKeys);
    }

    public static Builder<?> builder(Component text) {
        return new Builder<>(Objects.requireNonNull(text, "text"));
    }

    public float textScale() {
        return textScale;
    }

    @Deprecated
    public boolean wrap() {
        return wrap;
    }

    public TextMetrics measure(Font font, int wrapWidth) {
        FormattedCharSequence line = this.text.getVisualOrderText();
        String lineText = toPlainString(line);
        if (lineText.isEmpty()) {
            return new TextMetrics(List.of(line), 0, font.lineHeight);
        }
        int codepoints = lineText.codePointCount(0, lineText.length());
        int width = Math.round(measureLineWidth(font, lineText, codepoints));
        float totalHeight = font.lineHeight;
        return new TextMetrics(List.of(line), width, totalHeight);
    }

    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        Font font = mc.font;
        float scale = Math.max(0.01f, this.textScale);
        FormattedCharSequence lineSequence = this.text.getVisualOrderText();
        String lineText = toPlainString(lineSequence);
        if (lineText.isEmpty()) {
            return;
        }

        List<StyleSpan> spans = resolveStyleSpans(lineText);
        List<StyleSegment> segments = buildStyleSegments(lineText, spans);

        int totalVisibleChars = lineText.codePointCount(0, lineText.length());

        boolean anyFloating = this.floating;
        if (!anyFloating && !spans.isEmpty()) {
            for (StyleSpan span : spans) {
                TextStyle s = span.style();
                if (s.floating() != null || s.floatingSpeed() != null || s.floatingAmplitude() != null) {
                    anyFloating = true;
                    break;
                }
            }
        }

        boolean anyRainbowAnimated = this.rainbow && this.rainbowSpeed > 0f;
        if (!spans.isEmpty()) {
            for (StyleSpan span : spans) {
                TextStyle s = span.style();
                if (!s.isRainbow()) {
                    continue;
                }
                float speed = s.rainbowSpeed() != null ? s.rainbowSpeed() : this.rainbowSpeed;
                if (speed > 0f) {
                    anyRainbowAnimated = true;
                    break;
                }
            }
        }

        float rainbowTime = this.rainbowPhase;
        if (anyRainbowAnimated) {
            float delta = advanceRainbowPhase();
            this.rainbowPhase += delta;
            rainbowTime = this.rainbowPhase;
        }
        float baseFloatPhase = 0f;
        if (anyFloating) {
            float delta = advanceFloatingPhase();
            this.floatingPhase += delta;
            baseFloatPhase = this.floatingPhase;
        }

        if (clipToPad) {
            g.enableScissor(leftPx, topPx, leftPx + widthPx, topPx + heightPx);
        }

        g.pose().pushMatrix();
        g.pose().translate(leftPx, topPx);
        g.pose().scale(scale, scale);

        int globalCharIndex = 0;
        int visibleIndex = 0;
        int segmentIndex = 0;
        StyleSegment currentSegment = segments.isEmpty() ? null : segments.get(0);
        int lineCharIndex = 0;
        int lineVisibleIndex = 0;
        float drawX = 0.0f;
        float y = 0.0f;
        while (lineCharIndex < lineText.length()) {
            int codePoint = lineText.codePointAt(lineCharIndex);
            int charLen = Character.charCount(codePoint);
            boolean lineBreak = codePoint == '\n' || codePoint == '\r';
            String ch = lineBreak ? " " : stringForCodePoint(codePoint);

            while (currentSegment != null && globalCharIndex >= currentSegment.end()) {
                segmentIndex++;
                currentSegment = segmentIndex < segments.size() ? segments.get(segmentIndex) : null;
            }
            ResolvedStyle resolved = resolveStyleFromState(
                    currentSegment,
                    globalCharIndex,
                    visibleIndex,
                    lineVisibleIndex,
                    totalVisibleChars,
                    rainbowTime
            );

            float advance = font.width(ch);
            if (!lineBreak) {
                float yOffset = 0.0f;
                if (resolved.floating()) {
                    float phase = baseFloatPhase * resolved.floatingSpeed() + lineVisibleIndex * FLOAT_CHAR_STEP;
                    yOffset = (float) Math.sin(phase * Math.PI * 2.0f) * resolved.floatingAmplitude();
                }
                if (resolved.floating() && !resolved.floatingPixelated()) {
                    drawStyledCharSmooth(g, font, ch, drawX, y + yOffset, advance, resolved, shadow);
                } else {
                    drawStyledChar(g, font, ch, drawX, y + yOffset, advance, resolved, shadow);
                }
            }

            drawX += advance + letterSpacing;
            lineCharIndex += charLen;
            globalCharIndex += charLen;
            lineVisibleIndex++;
            visibleIndex++;
        }
        g.pose().popMatrix();
        if (clipToPad) {
            g.disableScissor();
        }
    }

    private void drawStyledChar(GuiGraphicsExtractor g, Font font, String ch, float x, float y, float width,
                                ResolvedStyle style, boolean shadow) {
        drawStyledCharAt(g, font, ch, Math.round(x), Math.round(y), width, style, shadow);
    }

    private void drawStyledCharSmooth(GuiGraphicsExtractor g, Font font, String ch, float x, float y, float width,
                                      ResolvedStyle style, boolean shadow) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        drawStyledCharAt(g, font, ch, 0, 0, width, style, shadow);
        g.pose().popMatrix();
    }

    private void drawStyledCharAt(GuiGraphicsExtractor g, Font font, String ch, int x, int y, float width,
                                  ResolvedStyle style, boolean shadow) {
        int color = ensureAlpha(style.color());
        if (style.bold()) {
            g.text(font, ch, x + 1, y, color, shadow);
        }
        g.text(font, ch, x, y, color, shadow);

        if (style.underline() || style.strikethrough()) {
            int lineColor = 0xFF000000 | (color & 0xFFFFFF);
            int startX = x;
            int endX = Math.round(x + width);
            int underlineY = y + font.lineHeight - 1;
            int strikeY = y + Math.round(font.lineHeight * 0.5f);
            if (style.underline()) {
                g.fill(startX, underlineY, endX, underlineY + 1, lineColor);
            }
            if (style.strikethrough()) {
                g.fill(startX, strikeY, endX, strikeY + 1, lineColor);
            }
        }
    }

    private static int ensureAlpha(int color) {
        return (color & 0xFF000000) == 0 ? (color | 0xFF000000) : color;
    }

    private ResolvedStyle resolveStyleFromState(StyleSegment segment,
                                                int globalCharIndex,
                                                int visibleIndex,
                                                int lineVisibleIndex,
                                                int totalVisibleChars,
                                                float rainbowTime) {
        StyleState state = segment != null ? segment.state() : null;
        boolean finalBold = state != null ? state.bold() : this.bold;
        boolean finalUnderline = state != null ? state.underline() : this.underline;
        boolean finalStrike = state != null ? state.strikethrough() : this.strikethrough;
        boolean floating = state != null ? state.floating() : this.floating;
        float finalFloatingSpeed = state != null ? state.floatingSpeed() : this.floatingSpeed;
        float finalFloatingAmplitude = state != null ? state.floatingAmplitude() : this.floatingAmplitude;
        boolean pixelated = state == null || state.floatingPixelated();
        Integer directColor = state != null ? state.directColor() : null;
        int[] gradient = state != null ? state.gradientColors() : null;
        StyleSpan gradientSpan = state != null ? state.gradientSpan() : null;
        boolean useRainbow = state != null && state.useRainbow();
        Float rainbowSpeed = state != null ? state.rainbowSpeed() : null;
        Boolean rainbowStatic = state != null ? state.rainbowStatic() : null;
        int[] rainbowColors = state != null ? state.rainbowColors() : null;

        if (directColor == null && gradient == null && !useRainbow) {
            gradient = this.gradientColors;
            useRainbow = this.rainbow;
        }

        float effectiveRainbowSpeed = rainbowSpeed != null ? rainbowSpeed : this.rainbowSpeed;
        boolean effectiveRainbowStatic = rainbowStatic != null ? rainbowStatic : this.rainbowStatic;
        int[] effectiveRainbowColors = (rainbowColors != null && rainbowColors.length > 0)
                ? rainbowColors
                : this.rainbowColors;

        int finalColor;
        if (directColor != null) {
            finalColor = directColor;
        } else if (gradient != null && gradient.length > 0) {
            float t = gradientSpan != null
                    ? resolveGradientT(globalCharIndex, gradientSpan)
                    : resolveGlobalGradientT(visibleIndex, totalVisibleChars);
            finalColor = sampleGradient(gradient, t);
        } else if (useRainbow) {
            float phase = rainbowTime * Math.max(0f, effectiveRainbowSpeed);
            if (!effectiveRainbowStatic) {
                phase += lineVisibleIndex * RAINBOW_CHAR_STEP;
            }
            finalColor = sampleRainbowColor(phase, effectiveRainbowColors);
        } else {
            finalColor = this.color;
        }

        return new ResolvedStyle(finalColor, finalBold, finalUnderline, finalStrike,
                floating, finalFloatingSpeed, finalFloatingAmplitude, pixelated);
    }

    private static float resolveGradientT(int globalCharIndex, StyleSpan span) {
        if (span == null) {
            return 0f;
        }
        int length = Math.max(1, span.length());
        if (length <= 1) {
            return 0f;
        }
        int offset = globalCharIndex - span.start();
        float t = offset / (float) (length - 1);
        return clamp01(t);
    }

    private static float resolveGlobalGradientT(int visibleIndex, int totalVisibleChars) {
        if (totalVisibleChars <= 1) {
            return 0f;
        }
        float t = visibleIndex / (float) (totalVisibleChars - 1);
        return clamp01(t);
    }

    private static float clamp01(float t) {
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t;
    }

    private float measureLineWidth(Font font, String text, int codepoints) {
        if (text.isEmpty()) {
            return 0f;
        }
        float width = 0f;
        int idx = 0;
        while (idx < text.length()) {
            int codePoint = text.codePointAt(idx);
            String ch = stringForCodePoint(codePoint);
            width += font.width(ch);
            idx += Character.charCount(codePoint);
        }
        if (codepoints > 1) {
            width += letterSpacing * (codepoints - 1);
        }
        return width;
    }

    private List<StyleSpan> resolveStyleSpans(String fullText) {
        if (styleSpans.isEmpty() && styleKeys.isEmpty()) {
            return List.of();
        }
        List<StyleSpan> spans = new ArrayList<>(styleSpans);
        for (StyleKey key : styleKeys) {
            applyStyleKey(key.key(), key.style(), fullText, spans);
        }
        return spans;
    }

    private static void applyStyleKey(String key, TextStyle style, String text, List<StyleSpan> out) {
        if (key == null || key.isBlank()) {
            return;
        }
        String trimmed = key.trim();
        int len = text.length();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String body = trimmed.substring(1, trimmed.length() - 1).trim();
            if (body.contains(":")) {
                String[] parts = body.split(":", -1);
                Integer start = parseIndex(parts.length > 0 ? parts[0].trim() : "", len);
                Integer end = parseIndex(parts.length > 1 ? parts[1].trim() : "", len);
                int s = start != null ? start : 0;
                int e = end != null ? end : len;
                s = normalizeIndex(s, len);
                e = normalizeIndex(e, len);
                if (e < s) {
                    int tmp = s;
                    s = e;
                    e = tmp;
                }
                if (s < e) {
                    out.add(new StyleSpan(s, e, style));
                }
                return;
            } else {
                Integer idx = parseIndex(body, len);
                if (idx != null) {
                    int i = normalizeIndex(idx, len);
                    if (i >= 0 && i < len) {
                        out.add(new StyleSpan(i, i + 1, style));
                    }
                }
                return;
            }
        }

        int fromIndex = 0;
        while (fromIndex < len) {
            int found = text.indexOf(trimmed, fromIndex);
            if (found < 0) {
                break;
            }
            out.add(new StyleSpan(found, found + trimmed.length(), style));
            fromIndex = found + Math.max(1, trimmed.length());
        }
    }

    private static Integer parseIndex(String value, int len) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int normalizeIndex(int idx, int len) {
        if (idx < 0) {
            return Math.max(0, len + idx);
        }
        return Math.min(idx, len);
    }

    private static String toPlainString(FormattedCharSequence sequence) {
        StringBuilder sb = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    private static String stringForCodePoint(int codePoint) {
        if (codePoint >= 0 && codePoint < CODEPOINT_CACHE_LIMIT) {
            return CODEPOINT_CACHE[codePoint];
        }
        return new String(Character.toChars(codePoint));
    }

    private List<StyleSegment> buildStyleSegments(String fullText, List<StyleSpan> spans) {
        int len = fullText.length();
        if (len <= 0) {
            return List.of();
        }
        if (spans.isEmpty()) {
            return List.of(new StyleSegment(0, len, resolveStyleState(spans, 0)));
        }
        int[] boundaries = new int[spans.size() * 2 + 2];
        int count = 0;
        boundaries[count++] = 0;
        boundaries[count++] = len;
        for (StyleSpan span : spans) {
            int start = Math.max(0, Math.min(len, span.start()));
            int end = Math.max(0, Math.min(len, span.end()));
            boundaries[count++] = start;
            boundaries[count++] = end;
        }
        java.util.Arrays.sort(boundaries, 0, count);
        int uniqueCount = 0;
        for (int i = 0; i < count; i++) {
            if (i == 0 || boundaries[i] != boundaries[i - 1]) {
                boundaries[uniqueCount++] = boundaries[i];
            }
        }
        List<StyleSegment> segments = new ArrayList<>(Math.max(1, uniqueCount - 1));
        for (int i = 0; i < uniqueCount - 1; i++) {
            int start = boundaries[i];
            int end = boundaries[i + 1];
            if (start >= end) {
                continue;
            }
            segments.add(new StyleSegment(start, end, resolveStyleState(spans, start)));
        }
        return segments;
    }

    private StyleState resolveStyleState(List<StyleSpan> spans, int globalCharIndex) {
        boolean finalBold = this.bold;
        boolean finalUnderline = this.underline;
        boolean finalStrike = this.strikethrough;
        Boolean finalFloating = this.floating;
        float finalFloatingSpeed = this.floatingSpeed;
        float finalFloatingAmplitude = this.floatingAmplitude;
        Boolean finalFloatingPixelated = this.floatingPixelated;
        Integer directColor = null;
        int[] gradient = null;
        StyleSpan gradientSpan = null;
        boolean useRainbow = false;
        Float rainbowSpeed = null;
        Boolean rainbowStatic = null;
        int[] rainbowColors = null;
        for (StyleSpan span : spans) {
            if (!span.contains(globalCharIndex)) {
                continue;
            }
            TextStyle override = span.style();
            if (override.bold() != null) finalBold = override.bold();
            if (override.underline() != null) finalUnderline = override.underline();
            if (override.strikethrough() != null) finalStrike = override.strikethrough();
            if (override.floating() != null) finalFloating = override.floating();
            if (override.floatingSpeed() != null) finalFloatingSpeed = override.floatingSpeed();
            if (override.floatingAmplitude() != null) finalFloatingAmplitude = override.floatingAmplitude();
            if (override.floatingPixelated() != null) finalFloatingPixelated = override.floatingPixelated();

            if (override.color() != null) {
                directColor = override.color();
                gradient = null;
                gradientSpan = null;
                useRainbow = false;
                rainbowSpeed = null;
                rainbowStatic = null;
                rainbowColors = null;
            } else if (override.gradientColors() != null) {
                gradient = override.gradientColors();
                gradientSpan = span;
                directColor = null;
                useRainbow = false;
                rainbowSpeed = null;
                rainbowStatic = null;
                rainbowColors = null;
            } else if (override.isRainbow()) {
                useRainbow = true;
                directColor = null;
                gradient = null;
                gradientSpan = null;
                rainbowSpeed = override.rainbowSpeed();
                rainbowStatic = override.getRainbowStatic();
                rainbowColors = override.rainbowColors();
            }
        }
        boolean floating = finalFloating != null && finalFloating;
        boolean pixelated = finalFloatingPixelated == null || finalFloatingPixelated;
        return new StyleState(finalBold, finalUnderline, finalStrike,
                floating, finalFloatingSpeed, finalFloatingAmplitude, pixelated,
                directColor, gradient, gradientSpan, useRainbow, rainbowSpeed, rainbowStatic, rainbowColors);
    }

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
        return sampleRainbowColor(phase, this.rainbowColors);
    }

    private int sampleRainbowColor(float phase, int[] colors) {
        float t = phase % 1.0f;
        if (t < 0f) {
            t += 1.0f;
        }
        if (colors != null && colors.length > 0) {
            return samplePaletteWrap(colors, t);
        }
        return hsvToRgb(t, 1.0f, 1.0f);
    }

    private static int sampleGradient(int[] colors, float t) {
        int count = colors.length;
        if (count == 1) {
            return colors[0];
        }
        float scaled = t * (count - 1);
        int index = (int) Math.floor(scaled);
        if (index >= count - 1) {
            return colors[count - 1];
        }
        float frac = scaled - (float) Math.floor(scaled);
        int c1 = colors[index];
        int c2 = colors[index + 1];
        return lerpRgb(c1, c2, frac);
    }

    private static int samplePaletteWrap(int[] colors, float t) {
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

    private float advanceRainbowPhase() {
        long now = System.nanoTime();
        if (this.rainbowLastNanos < 0L) {
            this.rainbowLastNanos = now;
            return 0f;
        }
        long elapsed = now - this.rainbowLastNanos;
        this.rainbowLastNanos = now;
        if (elapsed <= 0L) {
            return 0f;
        }
        float deltaSeconds = elapsed / 1_000_000_000f;
        if (deltaSeconds > RAINBOW_MAX_DELTA_SECONDS) {
            deltaSeconds = RAINBOW_MAX_DELTA_SECONDS;
        }
        return deltaSeconds * RAINBOW_SPEED_SCALE;
    }

    private float advanceFloatingPhase() {
        long now = System.nanoTime();
        if (this.floatingLastNanos < 0L) {
            this.floatingLastNanos = now;
            return 0f;
        }
        long elapsed = now - this.floatingLastNanos;
        this.floatingLastNanos = now;
        if (elapsed <= 0L) {
            return 0f;
        }
        float deltaSeconds = elapsed / 1_000_000_000f;
        if (deltaSeconds > FLOAT_MAX_DELTA_SECONDS) {
            deltaSeconds = FLOAT_MAX_DELTA_SECONDS;
        }
        return deltaSeconds * FLOAT_SPEED_SCALE;
    }

    public record TextMetrics(List<FormattedCharSequence> lines, int maxWidth, float totalHeight) {
    }

    private record StyleSpan(int start, int end, TextStyle style) {
        boolean contains(int index) {
            return index >= start && index < end;
        }

        int length() {
            return Math.max(0, end - start);
        }
    }

    private record StyleKey(String key, TextStyle style) {
    }

    private record StyleState(boolean bold, boolean underline, boolean strikethrough,
                              boolean floating, float floatingSpeed, float floatingAmplitude, boolean floatingPixelated,
                              Integer directColor, int[] gradientColors, StyleSpan gradientSpan,
                              boolean useRainbow, Float rainbowSpeed, Boolean rainbowStatic, int[] rainbowColors) {
    }

    private record StyleSegment(int start, int end, StyleState state) {
    }

    private record ResolvedStyle(int color, boolean bold, boolean underline, boolean strikethrough,
                                 boolean floating, float floatingSpeed, float floatingAmplitude, boolean floatingPixelated) {
    }

    public static final class RainbowConfig {
        private boolean enabled = true;
        private Float speed;
        private Boolean staticMode;
        private int[] colors;

        public static RainbowConfig of() {
            return new RainbowConfig();
        }

        public RainbowConfig enable(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public boolean enabled() {
            return enabled;
        }

        public RainbowConfig speed(float speed) {
            this.speed = speed;
            return this;
        }

        public RainbowConfig height(float height) {
            return speed(height);
        }

        public Float speed() {
            return speed;
        }

        public RainbowConfig rainbowStatic(boolean staticMode) {
            this.staticMode = staticMode;
            return this;
        }

        Boolean getRainbowStatic() {
            return staticMode;
        }

        public RainbowConfig colors(char... codes) {
            if (codes == null || codes.length == 0) {
                this.colors = null;
                return this;
            }
            if (codes.length > 7) {
                throw new IllegalArgumentException("rainbow colors must be <= 7");
            }
            this.colors = colorsFromCodes(codes);
            return this;
        }

        public RainbowConfig colors(int... colors) {
            if (colors == null || colors.length == 0) {
                this.colors = null;
                return this;
            }
            if (colors.length > 7) {
                throw new IllegalArgumentException("rainbow colors must be <= 7");
            }
            this.colors = colors.clone();
            return this;
        }

        public int[] colors() {
            return colors;
        }
    }

    public static final class TextStyle {
        private final Integer color;
        private final Boolean bold;
        private final Boolean underline;
        private final Boolean strikethrough;
        private final boolean rainbow;
        private final Float rainbowSpeed;
        private final Boolean rainbowStatic;
        private final int[] rainbowColors;
        private final int[] gradientColors;
        private final Boolean floating;
        private final Float floatingSpeed;
        private final Float floatingAmplitude;
        private final Boolean floatingPixelated;

        private TextStyle(Builder builder) {
            this.color = builder.color;
            this.bold = builder.bold;
            this.underline = builder.underline;
            this.strikethrough = builder.strikethrough;
            this.rainbow = builder.rainbow;
            this.rainbowSpeed = builder.rainbowSpeed;
            this.rainbowStatic = builder.rainbowStatic;
            this.rainbowColors = builder.rainbowColors;
            this.gradientColors = builder.gradientColors;
            this.floating = builder.floating;
            this.floatingSpeed = builder.floatingSpeed;
            this.floatingAmplitude = builder.floatingAmplitude;
            this.floatingPixelated = builder.floatingPixelated;
        }

        public Integer color() {
            return color;
        }

        public Boolean bold() {
            return bold;
        }

        public Boolean underline() {
            return underline;
        }

        public Boolean strikethrough() {
            return strikethrough;
        }

        public boolean isRainbow() {
            return rainbow;
        }

        public Float rainbowSpeed() {
            return rainbowSpeed;
        }

        Boolean getRainbowStatic() {
            return rainbowStatic;
        }

        public int[] rainbowColors() {
            return rainbowColors;
        }

        public int[] gradientColors() {
            return gradientColors;
        }

        public Boolean floating() {
            return floating;
        }

        public Float floatingSpeed() {
            return floatingSpeed;
        }

        public Float floatingAmplitude() {
            return floatingAmplitude;
        }

        public Boolean floatingPixelated() {
            return floatingPixelated;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static TextStyle color(int color) {
            return builder().color(color).build();
        }

        public static TextStyle rainbow() {
            return builder().rainbow().build();
        }

        public static TextStyle rainbow(char... codes) {
            return builder().rainbow(codes).build();
        }

        public static TextStyle rainbow(float speed) {
            return builder().rainbow(speed).build();
        }

        public static TextStyle rainbow(float speed, char... codes) {
            return builder().rainbow(speed, codes).build();
        }

        public static TextStyle rainbowStatic(boolean staticMode) {
            return builder().rainbowStatic(staticMode).build();
        }

        public static TextStyle gradient(int... colors) {
            return builder().gradient(colors).build();
        }

        public static TextStyle floating(float speed) {
            return builder().floating(speed).build();
        }

        public static TextStyle floating(boolean pixelated) {
            return builder().floating(pixelated).build();
        }

        public static final class Builder {
            private Integer color;
            private Boolean bold;
            private Boolean underline;
            private Boolean strikethrough;
            private boolean rainbow;
            private Float rainbowSpeed;
            private Boolean rainbowStatic;
            private int[] rainbowColors;
            private int[] gradientColors;
            private Boolean floating;
            private Float floatingSpeed;
            private Float floatingAmplitude;
            private Boolean floatingPixelated;

            public Builder color(int color) {
                this.color = color;
                return this;
            }

            public Builder bold(boolean bold) {
                this.bold = bold;
                return this;
            }

            public Builder bold() {
                return bold(true);
            }

            public Builder underline(boolean underline) {
                this.underline = underline;
                return this;
            }

            public Builder underline() {
                return underline(true);
            }

            public Builder strikethrough(boolean strikethrough) {
                this.strikethrough = strikethrough;
                return this;
            }

            public Builder strikethrough() {
                return strikethrough(true);
            }

            public Builder rainbow() {
                this.rainbow = true;
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
                this.rainbowSpeed = speed;
                this.rainbowColors = null;
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

            public Builder rainbowStatic(boolean staticMode) {
                this.rainbow = true;
                this.rainbowStatic = staticMode;
                return this;
            }

            public Builder rainbowColors(int... colors) {
                this.rainbow = true;
                if (colors == null || colors.length == 0) {
                    this.rainbowColors = null;
                    return this;
                }
                if (colors.length > 7) {
                    throw new IllegalArgumentException("rainbow colors must be <= 7");
                }
                this.rainbowColors = colors.clone();
                return this;
            }

            public Builder gradient(int... colors) {
                if (colors == null || colors.length == 0) {
                    this.gradientColors = null;
                } else {
                    this.gradientColors = colors.clone();
                }
                return this;
            }

            public Builder floating() {
                this.floating = true;
                return this;
            }

            public Builder floating(boolean pixelated) {
                this.floating = true;
                this.floatingPixelated = pixelated;
                return this;
            }

            public Builder floating(float speed) {
                this.floating = true;
                this.floatingSpeed = speed;
                return this;
            }

            public Builder floating(float speed, float amplitude) {
                this.floating = true;
                this.floatingSpeed = speed;
                this.floatingAmplitude = amplitude;
                return this;
            }

            public Builder floating(boolean pixelated, float speed) {
                this.floating = true;
                this.floatingPixelated = pixelated;
                this.floatingSpeed = speed;
                return this;
            }

            public Builder floating(boolean pixelated, float speed, float amplitude) {
                this.floating = true;
                this.floatingPixelated = pixelated;
                this.floatingSpeed = speed;
                this.floatingAmplitude = amplitude;
                return this;
            }

            public Builder floatingPixelated(boolean pixelated) {
                this.floatingPixelated = pixelated;
                return this;
            }

            public TextStyle build() {
                return new TextStyle(this);
            }
        }
    }

    public static class Builder<B extends Builder<B>> {
        private Component text;
        private List<Component> lines;
        private boolean multiline;
        private boolean wrap = true;
        private Align align = Align.LEFT;
        private float textScale = 1.0f;
        private float lineSpacing = 0.0f;
        private float letterSpacing = 0.0f;
        private int color = 0xFFFFFF;
        private boolean shadow = false;
        private boolean clipToPad = true;
        private boolean rainbow = false;
        private int[] rainbowColors;
        private float rainbowSpeed = 0.01f;
        private boolean rainbowStatic = false;
        private boolean bold;
        private boolean underline;
        private boolean strikethrough;
        private int[] gradientColors;
        private boolean floating = false;
        private float floatingSpeed = 0.01f;
        private float floatingAmplitude = FLOAT_DEFAULT_AMPLITUDE;
        private boolean floatingPixelated = true;
        private final List<StyleSpan> styleSpans = new ArrayList<>();
        private final List<StyleKey> styleKeys = new ArrayList<>();

        protected Builder(Component text) {
            this.text = Objects.requireNonNull(text, "text");
        }

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public Builder<?> copyForText(Component text) {
            Builder<?> copy = new Builder<>(Objects.requireNonNull(text, "text"));
            copy.lines = this.lines != null ? List.copyOf(this.lines) : null;
            copy.multiline = this.multiline;
            copy.wrap = this.wrap;
            copy.align = this.align;
            copy.textScale = this.textScale;
            copy.lineSpacing = this.lineSpacing;
            copy.letterSpacing = this.letterSpacing;
            copy.color = this.color;
            copy.shadow = this.shadow;
            copy.clipToPad = this.clipToPad;
            copy.rainbow = this.rainbow;
            copy.rainbowColors = this.rainbowColors != null ? this.rainbowColors.clone() : null;
            copy.rainbowSpeed = this.rainbowSpeed;
            copy.rainbowStatic = this.rainbowStatic;
            copy.bold = this.bold;
            copy.underline = this.underline;
            copy.strikethrough = this.strikethrough;
            copy.gradientColors = this.gradientColors != null ? this.gradientColors.clone() : null;
            copy.floating = this.floating;
            copy.floatingSpeed = this.floatingSpeed;
            copy.floatingAmplitude = this.floatingAmplitude;
            copy.floatingPixelated = this.floatingPixelated;
            copy.styleSpans.addAll(this.styleSpans);
            copy.styleKeys.addAll(this.styleKeys);
            return copy;
        }

        @Deprecated
        public B text(Component text) {
            this.text = Objects.requireNonNull(text, "text");
            return self();
        }

        @Deprecated
        public B lines(List<Component> lines) {
            this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
            this.multiline = true;
            return self();
        }

        @Deprecated
        public B singleLine() {
            this.multiline = false;
            return self();
        }

        @Deprecated
        public B multiLine() {
            this.multiline = true;
            return self();
        }

        @Deprecated
        public B wrap(boolean wrap) {
            this.wrap = wrap;
            return self();
        }

        @Deprecated
        public B align(Align align) {
            this.align = Objects.requireNonNull(align, "align");
            return self();
        }

        public B textScale(float scale) {
            this.textScale = scale;
            return self();
        }

        public float getTextScale() {
            return textScale;
        }

        @Deprecated
        public B lineSpacing(float spacing) {
            this.lineSpacing = spacing;
            return self();
        }

        public B letterSpacing(float spacing) {
            this.letterSpacing = spacing;
            return self();
        }

        public B color(int color) {
            this.color = color;
            return self();
        }

        public B shadow(boolean shadow) {
            this.shadow = shadow;
            return self();
        }

        public B clipToPad(boolean clipToPad) {
            this.clipToPad = clipToPad;
            return self();
        }

        public B allowOverflow(boolean allowOverflow) {
            this.clipToPad = !allowOverflow;
            return self();
        }

        public B bold(boolean bold) {
            this.bold = bold;
            return self();
        }

        public B bold() {
            return bold(true);
        }

        public B underline(boolean underline) {
            this.underline = underline;
            return self();
        }

        public B underline() {
            return underline(true);
        }

        public B strikethrough(boolean strikethrough) {
            this.strikethrough = strikethrough;
            return self();
        }

        public B strikethrough() {
            return strikethrough(true);
        }

        public B gradient(int... colors) {
            if (colors == null || colors.length == 0) {
                this.gradientColors = null;
            } else {
                this.gradientColors = colors.clone();
            }
            return self();
        }

        public B rainbow() {
            this.rainbow = true;
            this.rainbowColors = null;
            return self();
        }

        public B rainbow(char... codes) {
            this.rainbow = true;
            if (codes == null || codes.length == 0) {
                this.rainbowColors = null;
                return self();
            }
            if (codes.length > 7) {
                throw new IllegalArgumentException("rainbow colors must be <= 7");
            }
            this.rainbowColors = colorsFromCodes(codes);
            return self();
        }

        public B rainbow(float speed) {
            this.rainbow = true;
            this.rainbowColors = null;
            this.rainbowSpeed = speed;
            return self();
        }

        public B rainbow(float speed, char... codes) {
            this.rainbow = true;
            this.rainbowSpeed = speed;
            if (codes == null || codes.length == 0) {
                this.rainbowColors = null;
                return self();
            }
            if (codes.length > 7) {
                throw new IllegalArgumentException("rainbow colors must be <= 7");
            }
            this.rainbowColors = colorsFromCodes(codes);
            return self();
        }

        public B rainbowStatic(boolean staticMode) {
            this.rainbow = true;
            this.rainbowStatic = staticMode;
            return self();
        }

        public B floating() {
            this.floating = true;
            return self();
        }

        public B floating(boolean pixelated) {
            this.floating = true;
            this.floatingPixelated = pixelated;
            return self();
        }

        public B floating(float speed) {
            this.floating = true;
            this.floatingSpeed = speed;
            return self();
        }

        public B floating(float speed, float amplitude) {
            this.floating = true;
            this.floatingSpeed = speed;
            this.floatingAmplitude = amplitude;
            return self();
        }

        public B floating(boolean pixelated, float speed) {
            this.floating = true;
            this.floatingPixelated = pixelated;
            this.floatingSpeed = speed;
            return self();
        }

        public B floating(boolean pixelated, float speed, float amplitude) {
            this.floating = true;
            this.floatingPixelated = pixelated;
            this.floatingSpeed = speed;
            this.floatingAmplitude = amplitude;
            return self();
        }

        public B floatingPixelated(boolean pixelated) {
            this.floatingPixelated = pixelated;
            return self();
        }

        public B style(String key, TextStyle style) {
            Objects.requireNonNull(style, "style");
            this.styleKeys.add(new StyleKey(key, style));
            return self();
        }

        public B styles(Map<String, TextStyle> styles) {
            if (styles == null || styles.isEmpty()) {
                return self();
            }
            Map<String, TextStyle> ordered = new LinkedHashMap<>(styles);
            for (Map.Entry<String, TextStyle> entry : ordered.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.styleKeys.add(new StyleKey(entry.getKey(), entry.getValue()));
                }
            }
            return self();
        }

        public B t2c(Map<String, Integer> colors) {
            if (colors == null || colors.isEmpty()) {
                return self();
            }
            Map<String, Integer> ordered = new LinkedHashMap<>(colors);
            for (Map.Entry<String, Integer> entry : ordered.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (key != null && value != null) {
                    style(key, TextStyle.color(value));
                }
            }
            return self();
        }

        public B t2g(Map<String, int[]> gradients) {
            if (gradients == null || gradients.isEmpty()) {
                return self();
            }
            Map<String, int[]> ordered = new LinkedHashMap<>(gradients);
            for (Map.Entry<String, int[]> entry : ordered.entrySet()) {
                String key = entry.getKey();
                int[] value = entry.getValue();
                if (key != null && value != null && value.length > 0) {
                    style(key, TextStyle.gradient(value));
                }
            }
            return self();
        }

        public B t2r(Map<String, RainbowConfig> rainbows) {
            if (rainbows == null || rainbows.isEmpty()) {
                return self();
            }
            Map<String, RainbowConfig> ordered = new LinkedHashMap<>(rainbows);
            for (Map.Entry<String, RainbowConfig> entry : ordered.entrySet()) {
                String key = entry.getKey();
                RainbowConfig value = entry.getValue();
                if (key == null || value == null || !value.enabled()) {
                    continue;
                }
                TextStyle.Builder style = TextStyle.builder().rainbow();
                if (value.speed() != null) {
                    style.rainbow(value.speed());
                }
                if (value.colors() != null && value.colors().length > 0) {
                    style.rainbowColors(value.colors());
                }
                if (value.getRainbowStatic() != null) {
                    style.rainbowStatic(value.getRainbowStatic());
                }
                style(key, style.build());
            }
            return self();
        }

        public B t2f(Map<String, Float> floats) {
            return t2f(floats, true, true);
        }

        public B t2f(Map<String, Float> floats, boolean enable, boolean pixelated) {
            if (!enable) {
                return self();
            }
            if (floats == null || floats.isEmpty()) {
                return self();
            }
            Map<String, Float> ordered = new LinkedHashMap<>(floats);
            for (Map.Entry<String, Float> entry : ordered.entrySet()) {
                String key = entry.getKey();
                Float value = entry.getValue();
                if (key != null) {
                    TextStyle.Builder style = TextStyle.builder().floating();
                    if (value != null) {
                        style.floating(value);
                    }
                    style.floating(pixelated);
                    style(key, style.build());
                }
            }
            return self();
        }

        public B t2b(Map<String, Boolean> bolds) {
            if (bolds == null || bolds.isEmpty()) {
                return self();
            }
            Map<String, Boolean> ordered = new LinkedHashMap<>(bolds);
            for (Map.Entry<String, Boolean> entry : ordered.entrySet()) {
                String key = entry.getKey();
                Boolean value = entry.getValue();
                if (key != null && value != null) {
                    style(key, TextStyle.builder().bold(value).build());
                }
            }
            return self();
        }

        public B t2u(Map<String, Boolean> underlines) {
            if (underlines == null || underlines.isEmpty()) {
                return self();
            }
            Map<String, Boolean> ordered = new LinkedHashMap<>(underlines);
            for (Map.Entry<String, Boolean> entry : ordered.entrySet()) {
                String key = entry.getKey();
                Boolean value = entry.getValue();
                if (key != null && value != null) {
                    style(key, TextStyle.builder().underline(value).build());
                }
            }
            return self();
        }

        public B t2s(Map<String, Boolean> strikes) {
            if (strikes == null || strikes.isEmpty()) {
                return self();
            }
            Map<String, Boolean> ordered = new LinkedHashMap<>(strikes);
            for (Map.Entry<String, Boolean> entry : ordered.entrySet()) {
                String key = entry.getKey();
                Boolean value = entry.getValue();
                if (key != null && value != null) {
                    style(key, TextStyle.builder().strikethrough(value).build());
                }
            }
            return self();
        }

        public B styleRange(int start, int end, TextStyle style) {
            Objects.requireNonNull(style, "style");
            this.styleSpans.add(new StyleSpan(start, end, style));
            return self();
        }

        public B styleIndex(int index, TextStyle style) {
            return styleRange(index, index + 1, style);
        }

        public TextRenderer buildRenderer() {
            return new TextRenderer(this);
        }
    }
}
