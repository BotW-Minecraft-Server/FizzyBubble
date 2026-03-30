package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.client.formatting.FizzyComponentService;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public abstract class FontFizzyGlobalMixin {
    @Shadow
    public abstract void drawInBatch(
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            boolean dropShadow,
            Matrix4fc matrix,
            MultiBufferSource buffer,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int packedLightCoords
    );

    @Shadow
    public abstract int width(FormattedCharSequence text);

    @ModifyVariable(
            method = "split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private FormattedText fizzy$formatSplitInput(FormattedText text) {
        return FizzyComponentService.formatFormattedText(text);
    }

    @ModifyVariable(
            method = "width(Lnet/minecraft/network/chat/FormattedText;)I",
            at = @At("HEAD"),
            argsOnly = true
    )
    private FormattedText fizzy$formatWidthInput(FormattedText text) {
        return FizzyComponentService.formatFormattedText(text);
    }

    @Inject(
            method = "width(Ljava/lang/String;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fizzy$formatStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        FormattedCharSequence formatted = FizzyComponentService.formatVisualOrder(text);
        if (formatted != null) {
            cir.setReturnValue(this.width(formatted));
        }
    }

    @Inject(
            method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fizzy$drawFormattedString(
            String text,
            float x,
            float y,
            int color,
            boolean dropShadow,
            Matrix4fc matrix,
            MultiBufferSource buffer,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int packedLightCoords,
            CallbackInfo ci
    ) {
        FormattedCharSequence formatted = FizzyComponentService.formatVisualOrder(text);
        if (formatted != null) {
            this.drawInBatch(formatted, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLightCoords);
            ci.cancel();
        }
    }
}
