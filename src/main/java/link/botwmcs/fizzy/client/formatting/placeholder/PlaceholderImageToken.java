package link.botwmcs.fizzy.client.formatting.placeholder;

import link.botwmcs.fizzy.client.formatting.inline.InlineImageSource;

public record PlaceholderImageToken(String key, InlineImageSource source) implements PlaceholderToken {
}
