package link.botwmcs.fizzy.client.formatting.emoji.builtin;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.formatting.emoji.EmojiPack;
import link.botwmcs.fizzy.client.formatting.emoji.EmojiRegistry;
import link.botwmcs.fizzy.client.formatting.inline.StaticInlineImageSource;
import link.botwmcs.fizzy.ui.element.icon.FizzyIcon;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Built-in pack that exposes Fizzy icon set as :fizzy_xxx: emoji tokens.
 */
public final class IconEmojiPack implements EmojiPack {
    public static final String PACK_ID = "builtin_fizzy_icons";
    private static final String FIZZY_PREFIX = "FIZZY_";
    private static final String ROOT_PREFIX = "ROOT_";
    private static final String TOKEN_PREFIX = "icon_";
    private static final float ICON_SIZE = 16.0F;

    private static final IconEmojiPack INSTANCE = new IconEmojiPack();

    private IconEmojiPack() {
    }

    public static void registerBuiltin() {
        EmojiRegistry.registerPack(INSTANCE);
    }

    @Override
    public String id() {
        return PACK_ID;
    }

    @Override
    public void register(Registrar registrar) {
        int registered = 0;
        for (FizzyIcon icon : FizzyIcon.values()) {
            String token = toToken(icon);
            if (token == null) {
                continue;
            }
            registrar.token(token, new StaticInlineImageSource(icon.texture(), ICON_SIZE, ICON_SIZE));
            registered++;
        }
        if (registered == 0) {
            Fizzy.LOGGER.warn("No Fizzy icon emoji tokens were registered. Expected {}-prefixed icons in FizzyIcon.", FIZZY_PREFIX);
        } else if (registered != 88) {
            Fizzy.LOGGER.warn("Fizzy icon emoji token count changed: expected 88, actual {}", registered);
        }
    }

    private static @Nullable String toToken(FizzyIcon icon) {
        String name = icon.name();
        String suffix;
        if (name.startsWith(FIZZY_PREFIX)) {
            suffix = name.substring(FIZZY_PREFIX.length());
        } else if (name.startsWith(ROOT_PREFIX)) {
            // Backward compatibility for older enum naming.
            suffix = name.substring(ROOT_PREFIX.length());
        } else {
            return null;
        }
        if (suffix.isEmpty()) {
            return null;
        }
        return TOKEN_PREFIX + suffix.toLowerCase(Locale.ROOT);
    }
}
