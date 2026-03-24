package link.botwmcs.fizzy.client.formatting.emoji;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Style;

public record EmojiClickContext(
        Minecraft minecraft,
        ChatScreen chatScreen,
        String token,
        Style style
) {
}
