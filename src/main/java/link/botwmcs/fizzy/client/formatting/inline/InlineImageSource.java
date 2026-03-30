package link.botwmcs.fizzy.client.formatting.inline;

import net.minecraft.resources.Identifier;

/**
 * Provides the image frame used by a placeholder-backed inline glyph.
 * Implementations may return different textures over time for animation and
 * may override {@link #render(InlineRenderContext)} for custom native rendering.
 */
public interface InlineImageSource {
    /**
     * @param nowMillis current client time in milliseconds
     * @return texture to render for this frame
     */
    Identifier texture(long nowMillis);

    /**
     * Logical source width in pixels.
     */
    float width();

    /**
     * Logical source height in pixels.
     */
    float height();

    /**
     * Render this inline source using the current text rendering pipeline.
     *
     * The default implementation draws a textured quad via {@link #texture(long)}.
     * Implementations can override this to plug custom rendering behavior.
     *
     * @return horizontal advance in pixels
     */
    default float render(InlineRenderContext context) {
        return FizzyInlineImageRenderer.renderTextured(this, context);
    }
}
