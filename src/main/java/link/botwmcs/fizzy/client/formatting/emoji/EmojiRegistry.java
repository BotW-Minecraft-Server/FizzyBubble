package link.botwmcs.fizzy.client.formatting.emoji;

import link.botwmcs.fizzy.client.formatting.inline.AnimatedInlineImageSource;
import link.botwmcs.fizzy.client.formatting.inline.InlineImageSource;
import link.botwmcs.fizzy.client.formatting.inline.StaticInlineImageSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Global client-side emoji registry.
 *
 * Workflow:
 * 1) register static/animated/custom emoji source to a token
 * 2) player writes :token:
 * 3) parser maps token -> inline emoji and renderer draws it
 */
public final class EmojiRegistry {
    private static final String INTERACTIVE_MARKER_PREFIX = "\u0001fizzy_emoji_click\u0001";
    private static final char MARKER_END = '\u0001';

    private static final Map<String, EmojiEntry> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> PACK_INDEX = new ConcurrentHashMap<>();
    private static final AtomicLong VERSION = new AtomicLong(1L);

    private EmojiRegistry() {
    }

    public static void register(String token, InlineImageSource source) {
        register(token, source, null);
    }

    public static void registerInteractive(String token, InlineImageSource source, EmojiClickHandler clickHandler) {
        register(token, source, clickHandler);
    }

    private static void register(String token, InlineImageSource source, EmojiClickHandler clickHandler) {
        String normalized = normalizeToken(token);
        if (normalized == null || normalized.isEmpty()) {
            throw new IllegalArgumentException("emoji token must not be blank");
        }
        if (source == null) {
            throw new IllegalArgumentException("emoji source must not be null");
        }
        REGISTRY.put(normalized, new EmojiEntry(source, clickHandler));
        VERSION.incrementAndGet();
    }

    public static void registerStatic(String token, Identifier texture, float width, float height) {
        if (texture == null) {
            throw new IllegalArgumentException("emoji texture must not be null");
        }
        register(token, new StaticInlineImageSource(texture, Math.max(1.0F, width), Math.max(1.0F, height)));
    }

    public static void registerStaticInteractive(
            String token,
            Identifier texture,
            float width,
            float height,
            EmojiClickHandler clickHandler
    ) {
        if (texture == null) {
            throw new IllegalArgumentException("emoji texture must not be null");
        }
        registerInteractive(token, new StaticInlineImageSource(texture, Math.max(1.0F, width), Math.max(1.0F, height)), clickHandler);
    }

    public static void registerAnimated(String token, List<Identifier> frames, long frameDurationMs, float width, float height) {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("emoji frames must not be empty");
        }
        register(token, new AnimatedInlineImageSource(frames, frameDurationMs, Math.max(1.0F, width), Math.max(1.0F, height)));
    }

    public static void registerAnimatedInteractive(
            String token,
            List<Identifier> frames,
            long frameDurationMs,
            float width,
            float height,
            EmojiClickHandler clickHandler
    ) {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("emoji frames must not be empty");
        }
        registerInteractive(token, new AnimatedInlineImageSource(frames, frameDurationMs, Math.max(1.0F, width), Math.max(1.0F, height)), clickHandler);
    }

    public static Optional<InlineImageSource> find(String token) {
        String normalized = normalizeToken(token);
        if (normalized == null || normalized.isEmpty()) {
            return Optional.empty();
        }
        EmojiEntry entry = REGISTRY.get(normalized);
        return entry == null ? Optional.empty() : Optional.of(entry.source());
    }

    public static void unregister(String token) {
        String normalized = normalizeToken(token);
        if (normalized == null || normalized.isEmpty()) {
            return;
        }
        if (REGISTRY.remove(normalized) != null) {
            VERSION.incrementAndGet();
        }
    }

    public static void registerPack(EmojiPack pack) {
        if (pack == null) {
            return;
        }
        String packId = normalizePackId(pack.id());
        if (packId == null || packId.isEmpty()) {
            throw new IllegalArgumentException("emoji pack id must not be blank");
        }

        Set<String> tokens = new HashSet<>();
        pack.register(new EmojiPack.Registrar() {
            @Override
            public void token(String token, InlineImageSource source) {
                registerFromPack(token, source, null, tokens);
            }

            @Override
            public void tokenInteractive(String token, InlineImageSource source, EmojiClickHandler clickHandler) {
                registerFromPack(token, source, clickHandler, tokens);
            }
        });

        Set<String> previous = PACK_INDEX.put(packId, tokens);
        if (previous != null) {
            for (String oldToken : previous) {
                if (!tokens.contains(oldToken)) {
                    REGISTRY.remove(oldToken);
                }
            }
        }
        VERSION.incrementAndGet();
    }

    /**
     * Convenience overload for built-in or external modules that prefer a lambda
     * over implementing {@link EmojiPack}.
     */
    public static void registerPack(String packId, Consumer<EmojiPack.Registrar> consumer) {
        if (consumer == null) {
            return;
        }
        registerPack(new EmojiPack() {
            @Override
            public String id() {
                return packId;
            }

            @Override
            public void register(Registrar registrar) {
                consumer.accept(registrar);
            }
        });
    }

    public static void unregisterPack(String packId) {
        String normalizedPack = normalizePackId(packId);
        if (normalizedPack == null || normalizedPack.isEmpty()) {
            return;
        }
        Set<String> removed = PACK_INDEX.remove(normalizedPack);
        if (removed == null || removed.isEmpty()) {
            return;
        }
        for (String token : removed) {
            REGISTRY.remove(token);
        }
        VERSION.incrementAndGet();
    }

    public static Collection<String> tokens() {
        return Collections.unmodifiableSet(new HashSet<>(REGISTRY.keySet()));
    }

    public static void clearAll() {
        if (REGISTRY.isEmpty() && PACK_INDEX.isEmpty()) {
            return;
        }
        REGISTRY.clear();
        PACK_INDEX.clear();
        VERSION.incrementAndGet();
    }

    public static long version() {
        return VERSION.get();
    }

    public static String normalizeToken(String token) {
        if (token == null) {
            return null;
        }
        String normalized = token.trim();
        if (normalized.startsWith(":")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith(":")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.trim().toLowerCase();
    }

    private static String normalizePackId(String id) {
        return id == null ? null : id.trim().toLowerCase();
    }

    public static boolean isInteractive(String token) {
        String normalized = normalizeToken(token);
        if (normalized == null || normalized.isEmpty()) {
            return false;
        }
        EmojiEntry entry = REGISTRY.get(normalized);
        return entry != null && entry.clickHandler() != null;
    }

    public static Style markInteractiveStyle(Style style, String token) {
        String normalized = normalizeToken(token);
        if (normalized == null || normalized.isEmpty()) {
            return style;
        }
        EmojiEntry entry = REGISTRY.get(normalized);
        if (entry == null || entry.clickHandler() == null) {
            return style;
        }
        Style safeStyle = style == null ? Style.EMPTY : style;
        String marker = encodeInteractiveMarker(normalized);
        String insertion = safeStyle.getInsertion();
        if (insertion != null && insertion.contains(marker)) {
            return safeStyle;
        }
        String newInsertion = insertion == null ? marker : insertion + marker;
        return safeStyle.withInsertion(newInsertion);
    }

    public static boolean hasInteractiveMarker(Style style) {
        return extractInteractiveToken(style).isPresent();
    }

    public static Optional<String> extractInteractiveToken(Style style) {
        if (style == null) {
            return Optional.empty();
        }
        String insertion = style.getInsertion();
        if (insertion == null || insertion.isEmpty()) {
            return Optional.empty();
        }
        int markerStart = insertion.indexOf(INTERACTIVE_MARKER_PREFIX);
        if (markerStart < 0) {
            return Optional.empty();
        }
        int tokenStart = markerStart + INTERACTIVE_MARKER_PREFIX.length();
        int tokenEnd = insertion.indexOf(MARKER_END, tokenStart);
        if (tokenEnd <= tokenStart) {
            return Optional.empty();
        }
        String token = normalizeToken(insertion.substring(tokenStart, tokenEnd));
        return token == null || token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    public static Style stripInteractiveMarker(Style style) {
        if (style == null) {
            return Style.EMPTY;
        }
        String insertion = style.getInsertion();
        if (insertion == null || insertion.isEmpty()) {
            return style;
        }
        int markerStart = insertion.indexOf(INTERACTIVE_MARKER_PREFIX);
        if (markerStart < 0) {
            return style;
        }
        int tokenStart = markerStart + INTERACTIVE_MARKER_PREFIX.length();
        int tokenEnd = insertion.indexOf(MARKER_END, tokenStart);
        if (tokenEnd < 0) {
            return style;
        }
        String stripped = insertion.substring(0, markerStart) + insertion.substring(tokenEnd + 1);
        return style.withInsertion(stripped.isEmpty() ? null : stripped);
    }

    public static boolean dispatchChatInteraction(Style style, Minecraft minecraft, ChatScreen chatScreen) {
        Optional<String> tokenOpt = extractInteractiveToken(style);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        String token = tokenOpt.get();
        EmojiEntry entry = REGISTRY.get(token);
        if (entry == null || entry.clickHandler() == null) {
            return true;
        }
        Style cleanStyle = stripInteractiveMarker(style);
        entry.clickHandler().onClick(new EmojiClickContext(minecraft, chatScreen, token, cleanStyle));
        return true;
    }

    private static String encodeInteractiveMarker(String token) {
        return INTERACTIVE_MARKER_PREFIX + token + MARKER_END;
    }

    private static void registerFromPack(String token, InlineImageSource source, EmojiClickHandler clickHandler, Set<String> tokens) {
        String normalizedToken = normalizeToken(token);
        if (normalizedToken == null || normalizedToken.isEmpty()) {
            throw new IllegalArgumentException("emoji token must not be blank");
        }
        if (source == null) {
            throw new IllegalArgumentException("emoji source must not be null");
        }
        REGISTRY.put(normalizedToken, new EmojiEntry(source, clickHandler));
        tokens.add(normalizedToken);
    }

    private record EmojiEntry(InlineImageSource source, EmojiClickHandler clickHandler) {
    }
}
