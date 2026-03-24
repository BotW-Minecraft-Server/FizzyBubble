package link.botwmcs.fizzy.client.formatting;

import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.emoji.EmojiRegistry;
import link.botwmcs.fizzy.client.formatting.placeholder.PlaceholderRegistry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class FizzyComponentService {
    private static final ThreadLocal<Boolean> FORMATTING_GUARD = ThreadLocal.withInitial(() -> false);
    private static final Map<Component, CacheEntry> CACHE = new WeakHashMap<>();

    private FizzyComponentService() {
    }

    public static FormattedText formatFormattedText(FormattedText input) {
        if (input == null || !Config.ENABLE_FIZZY_COMPONENT.get()) {
            return input;
        }
        if (FORMATTING_GUARD.get()) {
            return input;
        }
        if (input instanceof Component component) {
            return formatComponent(component);
        }
        if (!containsFormattingSyntax(input)) {
            return input;
        }

        PlaceholderRegistry.ensureDefaults();
        FORMATTING_GUARD.set(true);
        try {
            MutableComponent out = Component.empty();
            input.visit((style, text) -> {
                if (!text.isEmpty()) {
                    out.append(FizzyComponentParser.parseText(text, style));
                }
                return Optional.empty();
            }, Style.EMPTY);
            return out;
        } finally {
            FORMATTING_GUARD.set(false);
        }
    }

    public static Component formatComponent(Component original) {
        if (original == null || !Config.ENABLE_FIZZY_COMPONENT.get()) {
            return original;
        }
        if (FORMATTING_GUARD.get()) {
            return original;
        }
        if (!containsFormattingSyntax(original)) {
            return original;
        }

        PlaceholderRegistry.ensureDefaults();
        long placeholderVersion = PlaceholderRegistry.version();
        long emojiVersion = EmojiRegistry.version();
        synchronized (CACHE) {
            CacheEntry cached = CACHE.get(original);
            if (cached != null && cached.placeholderVersion == placeholderVersion && cached.emojiVersion == emojiVersion) {
                return cached.formatted;
            }
        }

        FORMATTING_GUARD.set(true);
        try {
            MutableComponent out = Component.empty();
            original.visit((style, text) -> {
                if (!text.isEmpty()) {
                    out.append(FizzyComponentParser.parseText(text, style));
                }
                return Optional.empty();
            }, Style.EMPTY);

            synchronized (CACHE) {
                CACHE.put(original, new CacheEntry(placeholderVersion, emojiVersion, out));
            }
            return out;
        } finally {
            FORMATTING_GUARD.set(false);
        }
    }

    public static FormattedCharSequence formatVisualOrder(Component original, FormattedCharSequence fallback) {
        Component formatted = formatComponent(original);
        if (formatted == original) {
            return fallback;
        }
        return Language.getInstance().getVisualOrder(formatted);
    }

    public static FormattedCharSequence formatVisualOrder(String text) {
        if (text == null || !Config.ENABLE_FIZZY_COMPONENT.get()) {
            return null;
        }
        if (FORMATTING_GUARD.get()) {
            return null;
        }
        if (!containsFormattingSyntax(text)) {
            return null;
        }

        PlaceholderRegistry.ensureDefaults();
        FORMATTING_GUARD.set(true);
        try {
            Component parsed = FizzyComponentParser.parseText(text, Style.EMPTY);
            return Language.getInstance().getVisualOrder(parsed);
        } finally {
            FORMATTING_GUARD.set(false);
        }
    }

    private static boolean containsFormattingSyntax(FormattedText text) {
        final boolean[] found = new boolean[] {false};
        text.visit((style, piece) -> {
            if (piece.indexOf('&') >= 0 || piece.indexOf(':') >= 0) {
                found[0] = true;
                return Optional.of(Boolean.TRUE);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return found[0];
    }

    private static boolean containsFormattingSyntax(String text) {
        return text.indexOf('&') >= 0 || text.indexOf(':') >= 0;
    }

    private record CacheEntry(long placeholderVersion, long emojiVersion, Component formatted) {
    }
}
