package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.runtime.ScreenProxyRuntime;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenRenderStageMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void fizzy$renderSourceContentPre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fizzy$renderSourceContentPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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

    @Inject(
            method = "extractRenderStateWithTooltipAndSubtitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
            )
    )
    private void fizzy$renderSourceBgPre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof AbstractContainerScreen<?>)) {
            return;
        }
        ScreenProxyRuntime.instance().onRenderStage(
                self,
                HostRenderStage.SOURCE_BG_PRE,
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Inject(
            method = "extractRenderStateWithTooltipAndSubtitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void fizzy$renderSourceBgPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof AbstractContainerScreen<?>)) {
            return;
        }
        ScreenProxyRuntime.instance().onRenderStage(
                self,
                HostRenderStage.SOURCE_BG_POST,
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }
}

