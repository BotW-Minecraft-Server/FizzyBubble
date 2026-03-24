package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.FizzyFormattingPalette;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRegistry;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRenderer;
import link.botwmcs.fizzy.client.formatting.inline.InlineImageSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputInlineImageMixin {
    @Shadow @Final private MultiBufferSource bufferSource;
    @Shadow @Final private boolean dropShadow;
    @Shadow @Final private float dimFactor;
    @Shadow @Final private float a;
    @Shadow @Final private Matrix4f pose;
    @Shadow @Final private Font.DisplayMode mode;
    @Shadow @Final private int packedLightCoords;
    @Shadow float x;
    @Shadow float y;
    @Unique private boolean fizzy$delegatingRainbow;
    @Unique private boolean fizzy$rainbowRunActive;
    @Unique private int fizzy$rainbowRunIndex;

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void fizzy$renderInlineImage(int positionInCurrentSequence, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.ENABLE_FIZZY_COMPONENT.get()) {
            return;
        }

        if (!this.fizzy$delegatingRainbow) {
            if (FizzyFormattingPalette.isRainbowMarked(style)) {
                this.fizzy$rainbowRunIndex = this.fizzy$rainbowRunActive ? this.fizzy$rainbowRunIndex + 1 : 0;
                this.fizzy$rainbowRunActive = true;

                int rgb = FizzyFormattingPalette.rainbowAnimatedColorAt(
                        FizzyFormattingPalette.currentRainbowTime(),
                        this.fizzy$rainbowRunIndex
                );
                Style dynamicStyle = FizzyFormattingPalette.stripRainbowMarker(style).withColor(rgb);

                this.fizzy$delegatingRainbow = true;
                try {
                    cir.setReturnValue(((FormattedCharSink) (Object) this).accept(positionInCurrentSequence, dynamicStyle, codePoint));
                } finally {
                    this.fizzy$delegatingRainbow = false;
                }
                return;
            }
            this.fizzy$rainbowRunActive = false;
            this.fizzy$rainbowRunIndex = 0;
        }

        InlineImageSource source = FizzyInlineImageRegistry.get(codePoint);
        if (source == null) {
            return;
        }
        float advance = FizzyInlineImageRenderer.render(
                source,
                this.mode,
                this.bufferSource,
                this.pose,
                this.x + (this.dropShadow ? 1.0F : 0.0F),
                this.y + (this.dropShadow ? 1.0F : 0.0F),
                this.dimFactor,
                this.a,
                this.packedLightCoords
        );
        this.x += advance;
        cir.setReturnValue(true);
    }
}
