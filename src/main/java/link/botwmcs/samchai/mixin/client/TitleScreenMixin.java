package link.botwmcs.samchai.mixin.client;

import link.botwmcs.samchai.client.gui.FizzyTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Shadow
    @Final
    private LogoRenderer logoRenderer;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void fizzy$swapToCustomScreen(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        // 将原版主菜单替换为你自己的屏幕
        mc.setScreen(new FizzyTitleScreen(true, logoRenderer));
        // 取消原版 TitleScreen 的 init
        ci.cancel();
    }
}
