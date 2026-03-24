package link.botwmcs.fizzy.client.formatting.inline;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Frame-based animated inline source.
 */
public final class AnimatedInlineImageSource implements InlineImageSource {
    private final List<ResourceLocation> frames;
    private final long frameDurationMs;
    private final float width;
    private final float height;

    public AnimatedInlineImageSource(List<ResourceLocation> frames, long frameDurationMs, float width, float height) {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("frames must not be empty");
        }
        this.frames = List.copyOf(new ArrayList<>(frames));
        this.frameDurationMs = Math.max(1L, frameDurationMs);
        this.width = Math.max(1.0F, width);
        this.height = Math.max(1.0F, height);
    }

    @Override
    public ResourceLocation texture(long nowMillis) {
        int size = this.frames.size();
        if (size == 1) {
            return this.frames.get(0);
        }
        long tick = Math.max(0L, nowMillis) / this.frameDurationMs;
        int index = (int) (tick % size);
        return this.frames.get(index);
    }

    @Override
    public float width() {
        return this.width;
    }

    @Override
    public float height() {
        return this.height;
    }
}
