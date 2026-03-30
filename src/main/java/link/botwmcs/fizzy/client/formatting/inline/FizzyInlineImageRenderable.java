package link.botwmcs.fizzy.client.formatting.inline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;

public final class FizzyInlineImageRenderable implements TextRenderable.Styled {
    private static final Identifier FALLBACK_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

    private final InlineImageSource source;
    private final Identifier fallbackTexture;
    private final float x;
    private final float y;
    private final int color;
    private final int shadowColor;
    private final Style style;
    private final float shadowOffset;
    private final float width;
    private final float height;
    private final float yOffset;

    public FizzyInlineImageRenderable(
            InlineImageSource source,
            float x,
            float y,
            int color,
            int shadowColor,
            Style style,
            float shadowOffset,
            float width,
            float height,
            float yOffset
    ) {
        this.source = source;
        this.fallbackTexture = source.texture(System.currentTimeMillis());
        this.x = x;
        this.y = y;
        this.color = color;
        this.shadowColor = shadowColor;
        this.style = style;
        this.shadowOffset = shadowOffset;
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
    }

    @Override
    public void render(Matrix4fc pose, VertexConsumer buffer, int packedLightCoords, boolean flat) {
        float frontDepth = 0.0F;
        if (this.shadowColor != 0) {
            this.renderQuad(pose, buffer, packedLightCoords, this.shadowOffset, this.shadowOffset, 0.0F, this.shadowColor);
            if (!flat) {
                frontDepth = 0.03F;
            }
        }

        this.renderQuad(pose, buffer, packedLightCoords, 0.0F, 0.0F, frontDepth, this.color);
    }

    @Override
    public RenderType renderType(Font.DisplayMode displayMode, boolean blur) {
        Identifier texture = this.texture();
        if (texture == null) {
            return RenderTypes.text(FALLBACK_TEXTURE);
        }

        return switch (displayMode) {
            case NORMAL -> RenderTypes.text(texture);
            case SEE_THROUGH -> RenderTypes.textSeeThrough(texture);
            case POLYGON_OFFSET -> RenderTypes.textPolygonOffset(texture);
        };
    }

    @Deprecated
    @Override
    public RenderType renderType(Font.DisplayMode displayMode) {
        return this.renderType(displayMode, false);
    }

    @Override
    public GpuTextureView textureView() {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(this.texture());
        return texture != null ? texture.getTextureView() : null;
    }

    @Override
    public RenderPipeline guiPipeline() {
        return RenderPipelines.GUI_TEXT;
    }

    @Override
    public Style style() {
        return this.style;
    }

    @Override
    public float left() {
        return this.x;
    }

    @Override
    public float top() {
        return this.y + this.yOffset;
    }

    @Override
    public float right() {
        return this.left() + this.width + (this.shadowColor != 0 ? this.shadowOffset : 0.0F);
    }

    @Override
    public float bottom() {
        return this.top() + this.height + (this.shadowColor != 0 ? this.shadowOffset : 0.0F);
    }

    private Identifier texture() {
        Identifier current = this.source.texture(System.currentTimeMillis());
        return current != null ? current : this.fallbackTexture;
    }

    private void renderQuad(Matrix4fc pose, VertexConsumer buffer, int packedLightCoords, float offsetX, float offsetY, float z, int color) {
        float x0 = this.x + offsetX;
        float y0 = this.y + this.yOffset + offsetY;
        float x1 = x0 + this.width;
        float y1 = y0 + this.height;

        buffer.addVertex(pose, x0, y0, z).setColor(color).setUv(0.0F, 0.0F).setLight(packedLightCoords);
        buffer.addVertex(pose, x0, y1, z).setColor(color).setUv(0.0F, 1.0F).setLight(packedLightCoords);
        buffer.addVertex(pose, x1, y1, z).setColor(color).setUv(1.0F, 1.0F).setLight(packedLightCoords);
        buffer.addVertex(pose, x1, y0, z).setColor(color).setUv(1.0F, 0.0F).setLight(packedLightCoords);
    }
}
