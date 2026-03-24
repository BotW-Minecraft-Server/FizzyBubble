package link.botwmcs.fizzy.client.formatting.inline;

import java.util.HashMap;
import java.util.Map;

public final class FizzyInlineImageRegistry {
    private static final int PUA_START = 0xE000;
    private static final int PUA_END = 0xF8FF;

    private static final Map<Integer, InlineImageSource> BY_CODE_POINT = new HashMap<>();
    private static final Map<String, Integer> CODE_POINT_BY_KEY = new HashMap<>();
    private static int nextCodePoint = PUA_START;

    private FizzyInlineImageRegistry() {
    }

    public static synchronized int intern(String key, InlineImageSource source) {
        if (source == null) {
            return -1;
        }
        String normalizedKey = key == null || key.isBlank() ? "inline:" + source.hashCode() : key.trim();
        Integer existing = CODE_POINT_BY_KEY.get(normalizedKey);
        if (existing != null) {
            BY_CODE_POINT.put(existing, source);
            return existing;
        }
        if (nextCodePoint > PUA_END) {
            return -1;
        }
        int codePoint = nextCodePoint++;
        CODE_POINT_BY_KEY.put(normalizedKey, codePoint);
        BY_CODE_POINT.put(codePoint, source);
        return codePoint;
    }

    public static synchronized InlineImageSource get(int codePoint) {
        return BY_CODE_POINT.get(codePoint);
    }

    public static boolean isInlineImageCodePoint(int codePoint) {
        return codePoint >= PUA_START && codePoint <= PUA_END;
    }
}
