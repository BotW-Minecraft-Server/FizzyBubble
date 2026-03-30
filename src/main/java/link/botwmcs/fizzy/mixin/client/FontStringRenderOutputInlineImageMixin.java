package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.FizzyFormattingPalette;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public abstract class FontStringRenderOutputInlineImageMixin {
    @Unique private boolean fizzy$delegatingRainbow;
    @Unique private boolean fizzy$rainbowRunActive;
    @Unique private int fizzy$rainbowRunIndex;

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"), cancellable = true)
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
    }
}
