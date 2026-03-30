package link.botwmcs.fizzy.client.formatting.inline;

import net.minecraft.resources.Identifier;

public record StaticInlineImageSource(Identifier resource, float width, float height) implements InlineImageSource {
    @Override
    public Identifier texture(long nowMillis) {
        return resource;
    }
}
