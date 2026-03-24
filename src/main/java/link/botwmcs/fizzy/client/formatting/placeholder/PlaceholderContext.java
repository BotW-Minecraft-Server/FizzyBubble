package link.botwmcs.fizzy.client.formatting.placeholder;

import net.minecraft.network.chat.Style;

public record PlaceholderContext(Style baseStyle, String id, String payload, String rawToken) {
}
