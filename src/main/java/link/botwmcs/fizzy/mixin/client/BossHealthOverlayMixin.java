package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.client.util.BossbarRenderProbe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void fizzy$beginFrame(GuiGraphics gg, CallbackInfo ci) {
        BossbarRenderProbe.beginFrame();
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V"
            ),
            index = 2 // 第二个参数是 y
    )
    private int fizzy$captureY(int y) {
        BossbarRenderProbe.onDrawBarAt(y);
        return y; // 不改动原值
    }

    // 帧结束：计算底边
    @Inject(method = "render", at = @At("TAIL"))
    private void fizzy$endFrame(GuiGraphics gg, CallbackInfo ci) {
        BossbarRenderProbe.endFrame();
    }
}
