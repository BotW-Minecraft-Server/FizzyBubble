package link.botwmcs.fizzy.client.formatting.inline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class FizzyInlineImageRenderer {
    public static final float LINE_HEIGHT = 9.0F;

    private FizzyInlineImageRenderer() {
    }

    public static float render(
            InlineImageSource source,
            Font.DisplayMode mode,
            MultiBufferSource bufferSource,
            Matrix4f pose,
            float x,
            float y,
            float rgbaScale,
            float alpha,
            int packedLight
    ) {
        if (source == null) {
            return 0.0F;
        }
        InlineRenderContext context = new InlineRenderContext(
                mode,
                bufferSource,
                pose,
                x,
                y,
                rgbaScale,
                alpha,
                packedLight,
                System.currentTimeMillis()
        );
        float advance = source.render(context);
        return advance > 0.0F ? advance : measureAdvance(source);
    }

    public static float renderTextured(InlineImageSource source, InlineRenderContext context) {
        if (source == null || context == null) {
            return 0.0F;
        }
        float drawW = measureAdvance(source);
        float drawH = measureDrawHeight(source);
        float yOffset = (LINE_HEIGHT - drawH) * 0.5F;

        ResourceLocation texture = source.texture(context.nowMillis());
        if (texture == null) {
            return drawW;
        }
        VertexConsumer buffer = context.bufferSource().getBuffer(selectRenderType(context.displayMode(), texture));

        float r = context.rgbaScale();
        float g = context.rgbaScale();
        float b = context.rgbaScale();
        float a = context.alpha();

        float x0 = context.x();
        float y0 = context.y() + yOffset;
        float x1 = context.x() + drawW;
        float y1 = y0 + drawH;

        buffer.addVertex(context.pose(), x0, y0, 0.0F).setColor(r, g, b, a).setUv(0.0F, 0.0F).setLight(context.packedLight());
        buffer.addVertex(context.pose(), x0, y1, 0.0F).setColor(r, g, b, a).setUv(0.0F, 1.0F).setLight(context.packedLight());
        buffer.addVertex(context.pose(), x1, y1, 0.0F).setColor(r, g, b, a).setUv(1.0F, 1.0F).setLight(context.packedLight());
        buffer.addVertex(context.pose(), x1, y0, 0.0F).setColor(r, g, b, a).setUv(1.0F, 0.0F).setLight(context.packedLight());

        return drawW;
    }

    public static float measureAdvance(InlineImageSource source) {
        if (source == null) {
            return 0.0F;
        }
        float sourceW = Math.max(1.0F, source.width());
        float scale = measureScale(source);
        return sourceW * scale;
    }

    public static float measureDrawHeight(InlineImageSource source) {
        if (source == null) {
            return 0.0F;
        }
        float sourceH = Math.max(1.0F, source.height());
        float scale = measureScale(source);
        return sourceH * scale;
    }

    private static float measureScale(InlineImageSource source) {
        float sourceH = Math.max(1.0F, source.height());
        return Math.min(1.0F, LINE_HEIGHT / sourceH);
    }

    private static RenderType selectRenderType(Font.DisplayMode mode, ResourceLocation texture) {
        if (mode == Font.DisplayMode.SEE_THROUGH) {
            return RenderType.textSeeThrough(texture);
        }
        if (mode == Font.DisplayMode.POLYGON_OFFSET) {
            return RenderType.textPolygonOffset(texture);
        }
        return RenderType.text(texture);
    }
}
