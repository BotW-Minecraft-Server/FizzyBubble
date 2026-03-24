package link.botwmcs.fizzy.client.formatting.inline;

import net.minecraft.resources.ResourceLocation;

public record StaticInlineImageSource(ResourceLocation resource, float width, float height) implements InlineImageSource {
    @Override
    public ResourceLocation texture(long nowMillis) {
        return resource;
    }
}
