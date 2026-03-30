package link.botwmcs.fizzy.mixin.client;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRegistry;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRenderer;
import link.botwmcs.fizzy.client.formatting.inline.InlineImageSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.font.FontSet$Source")
public abstract class FontSetInlineImageGlyphInfoMixin {
    private static final Identifier FALLBACK_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

    @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
    private void fizzy$inlineImageGlyph(int character, CallbackInfoReturnable<BakedGlyph> cir) {
        if (!Config.ENABLE_FIZZY_COMPONENT.get()) {
            return;
        }
        if (!FizzyInlineImageRegistry.isInlineImageCodePoint(character)) {
            return;
        }
        InlineImageSource source = FizzyInlineImageRegistry.get(character);
        if (source == null) {
            return;
        }

        float advance = FizzyInlineImageRenderer.measureAdvance(source);
        cir.setReturnValue(new FizzyInlineImageGlyph(source, Math.max(0.0F, advance)));
    }

    private static final class FizzyInlineImageGlyph implements BakedGlyph {
        private final InlineImageSource source;
        private final GlyphInfo info;
        private final float width;
        private final float height;
        private final float yOffset;

        private FizzyInlineImageGlyph(InlineImageSource source, float advance) {
            this.source = source;
            this.info = GlyphInfo.simple(advance);
            this.width = advance;
            this.height = FizzyInlineImageRenderer.measureDrawHeight(source);
            this.yOffset = (FizzyInlineImageRenderer.LINE_HEIGHT - this.height) * 0.5F;
        }

        @Override
        public GlyphInfo info() {
            return this.info;
        }

        @Override
        public TextRenderable.Styled createGlyph(
                float x,
                float y,
                int color,
                int shadowColor,
                Style style,
                float boldOffset,
                float shadowOffset
        ) {
            return new FizzyInlineImageRenderable(
                    this.source,
                    x,
                    y,
                    ARGB.color(ARGB.alpha(color), 0xFFFFFF),
                    ARGB.color(ARGB.alpha(shadowColor), 0xFFFFFF),
                    style,
                    shadowOffset,
                    this.width,
                    this.height,
                    this.yOffset
            );
        }
    }

    private static final class FizzyInlineImageRenderable implements TextRenderable.Styled {
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

        private FizzyInlineImageRenderable(
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
            return null;
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
}
