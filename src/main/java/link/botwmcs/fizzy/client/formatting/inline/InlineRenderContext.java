package link.botwmcs.fizzy.client.formatting.inline;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * Native text rendering context passed to inline emoji/image renderers.
 * This allows custom sources to render through Minecraft's own text pipeline.
 */
public record InlineRenderContext(
        Font.DisplayMode displayMode,
        MultiBufferSource bufferSource,
        Matrix4f pose,
        float x,
        float y,
        float rgbaScale,
        float alpha,
        int packedLight,
        long nowMillis
) {
}
