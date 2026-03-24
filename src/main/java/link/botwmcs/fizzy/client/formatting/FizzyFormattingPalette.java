package link.botwmcs.fizzy.client.formatting;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public final class FizzyFormattingPalette {
    private static final char[] DEFAULT_RAINBOW_CODES = new char[] {'c', '6', 'e', 'a', 'b', '9', 'd'};
    private static final int[] DEFAULT_RAINBOW_COLORS = colorsFromCodes(DEFAULT_RAINBOW_CODES);
    private static final float RAINBOW_SPEED_SCALE = 30.0F;
    private static final float DEFAULT_RAINBOW_SPEED = 0.01F;
    private static final float RAINBOW_CHAR_STEP = 0.08F;
    private static final long START_NANOS = System.nanoTime();
    private static final String RAINBOW_MARKER_PREFIX = "\u0001fizzy_rainbow\u0001";

    private FizzyFormattingPalette() {
    }

    public static float currentRainbowTime() {
        long elapsed = System.nanoTime() - START_NANOS;
        if (elapsed <= 0L) {
            return 0.0F;
        }
        return (elapsed / 1_000_000_000.0F) * RAINBOW_SPEED_SCALE;
    }

    public static int rainbowAnimatedColorAt(float rainbowTime, int charIndex) {
        float phase = rainbowTime * DEFAULT_RAINBOW_SPEED;
        if (charIndex > 0) {
            phase += (float) charIndex * RAINBOW_CHAR_STEP;
        }
        return sampleRainbowColor(phase, DEFAULT_RAINBOW_COLORS);
    }

    public static Style markRainbowStyle(Style style) {
        Style safeStyle = style == null ? Style.EMPTY : style;
        String insertion = safeStyle.getInsertion();
        if (insertion != null && insertion.startsWith(RAINBOW_MARKER_PREFIX)) {
            return safeStyle;
        }
        String marked = RAINBOW_MARKER_PREFIX + (insertion == null ? "" : insertion);
        return safeStyle.withInsertion(marked);
    }

    public static boolean isRainbowMarked(Style style) {
        if (style == null) {
            return false;
        }
        String insertion = style.getInsertion();
        return insertion != null && insertion.startsWith(RAINBOW_MARKER_PREFIX);
    }

    public static Style stripRainbowMarker(Style style) {
        Style safeStyle = style == null ? Style.EMPTY : style;
        String insertion = safeStyle.getInsertion();
        if (insertion == null || !insertion.startsWith(RAINBOW_MARKER_PREFIX)) {
            return safeStyle;
        }
        String stripped = insertion.substring(RAINBOW_MARKER_PREFIX.length());
        return safeStyle.withInsertion(stripped.isEmpty() ? null : stripped);
    }

    private static int[] colorsFromCodes(char[] codes) {
        if (codes == null || codes.length == 0) {
            return new int[0];
        }
        int[] out = new int[codes.length];
        int outIdx = 0;
        for (char code : codes) {
            ChatFormatting formatting = ChatFormatting.getByCode(code);
            Integer color = formatting != null ? formatting.getColor() : null;
            if (color != null) {
                out[outIdx++] = color;
            }
        }
        if (outIdx == out.length) {
            return out;
        }
        int[] trimmed = new int[outIdx];
        System.arraycopy(out, 0, trimmed, 0, outIdx);
        return trimmed;
    }

    private static int sampleRainbowColor(float phase, int[] colors) {
        float t = phase % 1.0F;
        if (t < 0.0F) {
            t += 1.0F;
        }
        if (colors != null && colors.length > 0) {
            return samplePaletteWrap(colors, t);
        }
        return hsvToRgb(t, 1.0F, 1.0F);
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
        float hh = (h % 1.0F + 1.0F) % 1.0F;
        float c = v * s;
        float x = c * (1.0F - Math.abs((hh * 6.0F) % 2.0F - 1.0F));
        float m = v - c;
        float r;
        float g;
        float b;
        int sector = (int) (hh * 6.0F);
        switch (sector) {
            case 0 -> {
                r = c;
                g = x;
                b = 0.0F;
            }
            case 1 -> {
                r = x;
                g = c;
                b = 0.0F;
            }
            case 2 -> {
                r = 0.0F;
                g = c;
                b = x;
            }
            case 3 -> {
                r = 0.0F;
                g = x;
                b = c;
            }
            case 4 -> {
                r = x;
                g = 0.0F;
                b = c;
            }
            default -> {
                r = c;
                g = 0.0F;
                b = x;
            }
        }

        int ri = (int) ((r + m) * 255.0F + 0.5F);
        int gi = (int) ((g + m) * 255.0F + 0.5F);
        int bi = (int) ((b + m) * 255.0F + 0.5F);
        return (ri << 16) | (gi << 8) | bi;
    }
}
