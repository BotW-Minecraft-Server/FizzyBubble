package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.client.formatting.FizzyFormattingPalette;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringSplitter.class)
public abstract class StringSplitterFizzyStyleSanitizeMixin {
    @Inject(
            method = "componentStyleAtWidth(Lnet/minecraft/network/chat/FormattedText;I)Lnet/minecraft/network/chat/Style;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void fizzy$sanitizeStyleAtWidth(FormattedText content, int maxWidth, CallbackInfoReturnable<Style> cir) {
        Style style = cir.getReturnValue();
        if (FizzyFormattingPalette.isRainbowMarked(style)) {
            cir.setReturnValue(FizzyFormattingPalette.stripRainbowMarker(style));
        }
    }

    @Inject(
            method = "componentStyleAtWidth(Lnet/minecraft/util/FormattedCharSequence;I)Lnet/minecraft/network/chat/Style;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void fizzy$sanitizeStyleAtWidth(FormattedCharSequence content, int maxWidth, CallbackInfoReturnable<Style> cir) {
        Style style = cir.getReturnValue();
        if (FizzyFormattingPalette.isRainbowMarked(style)) {
            cir.setReturnValue(FizzyFormattingPalette.stripRainbowMarker(style));
        }
    }
}
