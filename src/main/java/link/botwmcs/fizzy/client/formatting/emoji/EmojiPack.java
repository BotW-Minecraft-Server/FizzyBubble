package link.botwmcs.fizzy.client.formatting.emoji;

import link.botwmcs.fizzy.client.formatting.inline.InlineImageSource;

/**
 * Registers a named emoji collection.
 */
public interface EmojiPack {
    String id();

    void register(Registrar registrar);

    interface Registrar {
        void token(String token, InlineImageSource source);

        void tokenInteractive(String token, InlineImageSource source, EmojiClickHandler clickHandler);
    }
}
