package link.botwmcs.fizzy.client.formatting;

import link.botwmcs.fizzy.client.formatting.emoji.EmojiRegistry;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRegistry;
import link.botwmcs.fizzy.client.formatting.placeholder.PlaceholderContext;
import link.botwmcs.fizzy.client.formatting.placeholder.PlaceholderImageToken;
import link.botwmcs.fizzy.client.formatting.placeholder.PlaceholderRegistry;
import link.botwmcs.fizzy.client.formatting.placeholder.PlaceholderTextToken;
import link.botwmcs.fizzy.client.formatting.placeholder.PlaceholderToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Optional;

public final class FizzyComponentParser {
    private static final char AMP = '&';
    private static final String LEGACY_CODES = "0123456789abcdefklmnor";
    private static final String LEGACY_CODES_WITH_H = LEGACY_CODES + "h";

    private FizzyComponentParser() {
    }

    public static Component parseText(String input, Style baseStyle) {
        return parseText(input, baseStyle, 0.0F);
    }

    public static Component parseText(String input, Style baseStyle, float rainbowTime) {
        MutableComponent out = Component.empty();
        if (input == null || input.isEmpty()) {
            return out;
        }
        PlaceholderRegistry.ensureDefaults();

        StringBuilder plain = new StringBuilder(input.length());
        Style currentStyle = baseStyle == null ? Style.EMPTY : baseStyle;
        Style resetStyle = currentStyle;
        boolean rainbowActive = false;

        for (int i = 0; i < input.length(); ) {
            char c = input.charAt(i);

            if (c == AMP && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                char code = Character.toLowerCase(next);

                if (next == '#' && hasHexColor(input, i + 2)) {
                    flushPlain(out, plain, currentStyle);
                    int rgb = parseHexColor(input, i + 2);
                    currentStyle = applyLegacyColor(currentStyle, rgb);
                    i += 8;
                    continue;
                }

                if (LEGACY_CODES_WITH_H.indexOf(code) >= 0) {
                    flushPlain(out, plain, currentStyle);
                    if (code == 'h') {
                        rainbowActive = true;
                    } else if (code == 'r') {
                        currentStyle = resetStyle;
                        rainbowActive = false;
                    } else {
                        ChatFormatting formatting = ChatFormatting.getByCode(code);
                        if (formatting != null) {
                            currentStyle = currentStyle.applyLegacyFormat(formatting);
                        }
                    }
                    i += 2;
                    continue;
                }
            }

            if (c == ':') {
                PlaceholderMatch match = matchPlaceholder(input, i);
                if (match != null) {
                    flushPlain(out, plain, currentStyle);
                    Style tokenStyle = currentStyle;
                    if (rainbowActive) {
                        tokenStyle = FizzyFormattingPalette.markRainbowStyle(currentStyle);
                    }

                    Optional<PlaceholderToken> token = Optional.empty();
                    if (match.payload().isEmpty()) {
                        token = EmojiRegistry.find(match.id())
                                .map(source -> new PlaceholderImageToken("emoji:" + match.id(), source));
                        if (token.isPresent()) {
                            tokenStyle = EmojiRegistry.markInteractiveStyle(tokenStyle, match.id());
                        }
                    }

                    if (token.isEmpty()) {
                        PlaceholderContext context = new PlaceholderContext(tokenStyle, match.id(), match.payload(), match.rawToken());
                        token = PlaceholderRegistry.find(match.id()).flatMap(resolver -> resolver.resolve(match.payload(), context));
                    }
                    if (token.isPresent()) {
                        appendPlaceholder(out, token.get(), tokenStyle, match);
                        i = match.nextIndex();
                        continue;
                    }
                    out.append(Component.literal(match.rawToken()).setStyle(tokenStyle));
                    i = match.nextIndex();
                    continue;
                }
            }

            int codePoint = input.codePointAt(i);
            int charLen = Character.charCount(codePoint);
            if (rainbowActive) {
                flushPlain(out, plain, currentStyle);
                Style rainbowStyle = FizzyFormattingPalette.markRainbowStyle(currentStyle);
                out.append(Component.literal(new String(Character.toChars(codePoint))).setStyle(rainbowStyle));
            } else {
                plain.appendCodePoint(codePoint);
            }
            i += charLen;
        }

        flushPlain(out, plain, currentStyle);
        return out;
    }

    private static void appendPlaceholder(MutableComponent out, PlaceholderToken token, Style currentStyle, PlaceholderMatch match) {
        if (token instanceof PlaceholderTextToken textToken) {
            if (textToken.component() == null) {
                return;
            }
            MutableComponent copy = textToken.component().copy();
            copy.withStyle(s -> currentStyle.applyTo(s));
            out.append(copy);
            return;
        }

        if (token instanceof PlaceholderImageToken imageToken) {
            int codePoint = FizzyInlineImageRegistry.intern(imageToken.key(), imageToken.source());
            if (codePoint > 0) {
                out.append(Component.literal(new String(Character.toChars(codePoint))).setStyle(currentStyle));
                return;
            }
        }

        out.append(Component.literal(match.rawToken()).setStyle(currentStyle));
    }

    private static void flushPlain(MutableComponent out, StringBuilder plain, Style style) {
        if (plain.isEmpty()) {
            return;
        }
        out.append(Component.literal(plain.toString()).setStyle(style));
        plain.setLength(0);
    }

    private static Style applyLegacyColor(Style style, int rgb) {
        return style.withObfuscated(false)
                .withBold(false)
                .withStrikethrough(false)
                .withUnderlined(false)
                .withItalic(false)
                .withColor(rgb);
    }

    private static boolean hasHexColor(String text, int start) {
        if (start + 6 > text.length()) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            if (!isHexDigit(text.charAt(start + i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    private static int parseHexColor(String text, int start) {
        int value = 0;
        for (int i = 0; i < 6; i++) {
            value = (value << 4) | hexToInt(text.charAt(start + i));
        }
        return value;
    }

    private static int hexToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + (c - 'a');
        }
        return 10 + (c - 'A');
    }

    private static PlaceholderMatch matchPlaceholder(String text, int start) {
        if (start < 0 || start >= text.length() || text.charAt(start) != ':') {
            return null;
        }
        int i = start + 1;
        while (i < text.length() && isValidPlaceholderIdChar(text.charAt(i))) {
            i++;
        }
        if (i == start + 1) {
            return null;
        }
        String id = text.substring(start + 1, i).trim();
        if (!isValidPlaceholderId(id)) {
            return null;
        }

        String payload = "";
        int end;

        if (i < text.length() && text.charAt(i) == '(') {
            int depth = 1;
            int j = i + 1;
            while (j < text.length() && depth > 0) {
                char c = text.charAt(j);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                }
                j++;
            }
            if (depth != 0 || j >= text.length() || text.charAt(j) != ':') {
                return null;
            }
            payload = text.substring(i + 1, j - 1).trim();
            end = j;
        } else if (i < text.length() && text.charAt(i) == ':') {
            end = i;
        } else {
            return null;
        }

        String raw = text.substring(start, end + 1);
        return new PlaceholderMatch(end + 1, id.toLowerCase(), payload, raw);
    }

    private static boolean isValidPlaceholderId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        for (int i = 0; i < id.length(); i++) {
            if (!isValidPlaceholderIdChar(id.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPlaceholderIdChar(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '_'
                || c == '-'
                || c == '.';
    }

    private record PlaceholderMatch(int nextIndex, String id, String payload, String rawToken) {
    }
}
