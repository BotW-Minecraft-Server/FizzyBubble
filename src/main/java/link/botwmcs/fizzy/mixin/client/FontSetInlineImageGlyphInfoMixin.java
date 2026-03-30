package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageGlyph;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRegistry;
import link.botwmcs.fizzy.client.formatting.inline.FizzyInlineImageRenderer;
import link.botwmcs.fizzy.client.formatting.inline.InlineImageSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.font.FontSet$Source")
public abstract class FontSetInlineImageGlyphInfoMixin {
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
}
