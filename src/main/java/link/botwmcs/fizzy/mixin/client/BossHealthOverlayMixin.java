package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.client.util.BossbarRenderProbe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void fizzy$beginFrame(GuiGraphicsExtractor gg, CallbackInfo ci) {
        BossbarRenderProbe.beginFrame();
    }

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V"
            ),
            index = 2
    )
    private int fizzy$captureY(int y) {
        BossbarRenderProbe.onDrawBarAt(y);
        return y;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fizzy$endFrame(GuiGraphicsExtractor gg, CallbackInfo ci) {
        BossbarRenderProbe.endFrame();
    }
}
