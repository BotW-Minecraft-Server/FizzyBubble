package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.runtime.ScreenProxyRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenRenderStageMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void fizzy$renderSourceContentPre(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof AbstractContainerScreen<?>) {
            return;
        }
        ScreenProxyRuntime.instance().onRenderStage(
                self,
                HostRenderStage.SOURCE_CONTENT_PRE,
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void fizzy$renderSourceContentPost(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof AbstractContainerScreen<?>) {
            return;
        }
        ScreenProxyRuntime.instance().onRenderStage(
                self,
                HostRenderStage.SOURCE_CONTENT_POST,
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }
}

