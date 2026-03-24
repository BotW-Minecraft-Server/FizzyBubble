package link.botwmcs.fizzy.mixin.client;

import com.mojang.blaze3d.font.GlyphInfo;
import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRegistry;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRenderer;
import link.botwmcs.fizzy.client.formatting.inline.InlineImageSource;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontSet.class)
public abstract class FontSetInlineImageGlyphInfoMixin {
    @Inject(method = "getGlyphInfo", at = @At("HEAD"), cancellable = true)
    private void fizzy$inlineImageGlyphInfo(int character, boolean filterFishyGlyphs, CallbackInfoReturnable<GlyphInfo> cir) {
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
        cir.setReturnValue(new InlineImageGlyphInfo(advance));
    }

    @Unique
    private static final class InlineImageGlyphInfo implements GlyphInfo.SpaceGlyphInfo {
        private final float advance;

        private InlineImageGlyphInfo(float advance) {
            this.advance = Math.max(0.0F, advance);
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public float getAdvance(boolean bold) {
            return this.advance;
        }

        @Override
        public float getBoldOffset() {
            return 0.0F;
        }
    }
}
