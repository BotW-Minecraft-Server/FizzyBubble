package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.runtime.ScreenProxyRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenRenderStageMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"
            ),
            require = 0
    )
    private void fizzy$renderSourceBgPre(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        fizzy$renderStage(HostRenderStage.SOURCE_BG_PRE, graphics, mouseX, mouseY, partialTick);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    private void fizzy$renderSourceBgPost(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        fizzy$renderStage(HostRenderStage.SOURCE_BG_POST, graphics, mouseX, mouseY, partialTick);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"
            ),
            require = 0
    )
    private void fizzy$renderSourceContentPre(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        fizzy$renderStage(HostRenderStage.SOURCE_CONTENT_PRE, graphics, mouseX, mouseY, 0.0f);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    private void fizzy$renderSourceContentPost(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        fizzy$renderStage(HostRenderStage.SOURCE_CONTENT_POST, graphics, mouseX, mouseY, 0.0f);
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"
            ),
            require = 0
    )
    private void fizzy$redirectSourceTooltip(AbstractContainerScreen<?> instance, GuiGraphics graphics, int mouseX, int mouseY) {
        ScreenProxyRuntime runtime = ScreenProxyRuntime.instance();
        Screen self = (Screen) (Object) this;

        runtime.onRenderStage(
                self,
                HostRenderStage.SOURCE_TOOLTIP_PRE,
                graphics,
                mouseX,
                mouseY,
                0.0f
        );

        if (!runtime.shouldCancelSourceTooltip(self, mouseX, mouseY)) {
            fizzy$invokeRenderTooltip(graphics, mouseX, mouseY);
        }

        runtime.onRenderStage(
                self,
                HostRenderStage.SOURCE_TOOLTIP_POST,
                graphics,
                mouseX,
                mouseY,
                0.0f
        );
    }

    private void fizzy$renderStage(HostRenderStage stage, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Screen self = (Screen) (Object) this;
        ScreenProxyRuntime.instance().onRenderStage(
                self,
                stage,
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Invoker("renderTooltip")
    protected abstract void fizzy$invokeRenderTooltip(GuiGraphics graphics, int mouseX, int mouseY);
}
