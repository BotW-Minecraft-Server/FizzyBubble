package link.botwmcs.fizzy.client.formatting.emoji;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Builds Brigadier suggestions for chat-side :emoji: token completion.
 */
public final class EmojiChatSuggestionService {
    private static final Object CACHE_LOCK = new Object();
    private static volatile long cachedVersion = Long.MIN_VALUE;
    private static volatile List<String> cachedSortedTokens = List.of();

    private EmojiChatSuggestionService() {
    }

    public static @Nullable Match findMatch(String input, int cursor) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        int clampedCursor = Math.max(0, Math.min(cursor, input.length()));
        if (clampedCursor <= 0) {
            return null;
        }

        int probe = clampedCursor - 1;
        while (probe >= 0 && isValidPlaceholderIdChar(input.charAt(probe))) {
            probe--;
        }
        if (probe < 0 || input.charAt(probe) != ':') {
            return null;
        }

        // Avoid reopening suggestions at cursor right after a completed :token: closing colon.
        if (clampedCursor == probe + 1 && probe > 0 && isValidPlaceholderIdChar(input.charAt(probe - 1))) {
            return null;
        }

        String query = input.substring(probe + 1, clampedCursor).toLowerCase(Locale.ROOT);
        return new Match(probe, query);
    }

    public static Suggestions buildSuggestions(String input, int cursor, Match match) {
        String safeInput = input == null ? "" : input;
        int clampedCursor = Math.max(0, Math.min(cursor, safeInput.length()));
        String prefix = safeInput.substring(0, clampedCursor);
        SuggestionsBuilder builder = new SuggestionsBuilder(prefix, Math.max(0, Math.min(match.replaceStart(), prefix.length())));

        List<String> tokens = sortedTokensSnapshot();
        if (tokens.isEmpty()) {
            return builder.build();
        }

        String query = match.query();
        for (String token : tokens) {
            if (!query.isEmpty() && !token.startsWith(query)) {
                continue;
            }
            builder.suggest(":" + token + ":");
        }
        return builder.build();
    }

    private static List<String> sortedTokensSnapshot() {
        long version = EmojiRegistry.version();
        if (cachedVersion == version) {
            return cachedSortedTokens;
        }

        synchronized (CACHE_LOCK) {
            if (cachedVersion == version) {
                return cachedSortedTokens;
            }

            List<String> sorted = EmojiRegistry.tokens().stream()
                    .flatMap(token -> {
                        String normalized = EmojiRegistry.normalizeToken(token);
                        return normalized == null || normalized.isEmpty() ? Stream.empty() : Stream.of(normalized);
                    })
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .toList();
            cachedSortedTokens = sorted;
            cachedVersion = version;
            return sorted;
        }
    }

    private static boolean isValidPlaceholderIdChar(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '_'
                || c == '-'
                || c == '.';
    }

    public record Match(int replaceStart, String query) {
    }
}
