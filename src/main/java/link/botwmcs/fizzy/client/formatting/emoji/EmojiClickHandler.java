package link.botwmcs.fizzy.client.formatting.emoji;

@FunctionalInterface
public interface EmojiClickHandler {
    /**
     * Called when an interactive emoji token is clicked in chat.
     */
    void onClick(EmojiClickContext context);
}
