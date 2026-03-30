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

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenRenderStageMixin {
    @Inject(method = "extractContents", at = @At("HEAD"), require = 0)
    private void fizzy$renderSourceContentPre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        fizzy$renderStage(HostRenderStage.SOURCE_CONTENT_PRE, graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "extractContents", at = @At("TAIL"), require = 0)
    private void fizzy$renderSourceContentPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        fizzy$renderStage(HostRenderStage.SOURCE_CONTENT_POST, graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void fizzy$sourceTooltipPre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        ScreenProxyRuntime runtime = ScreenProxyRuntime.instance();

        runtime.onRenderStage(
                self,
                HostRenderStage.SOURCE_TOOLTIP_PRE,
                graphics,
                mouseX,
                mouseY,
                0.0f
        );

        if (runtime.shouldCancelSourceTooltip(self, mouseX, mouseY)) {
            runtime.onRenderStage(
                    self,
                    HostRenderStage.SOURCE_TOOLTIP_POST,
                    graphics,
                    mouseX,
                    mouseY,
                    0.0f
            );
            ci.cancel();
        }
    }

    @Inject(method = "extractTooltip", at = @At("TAIL"), require = 0)
    private void fizzy$sourceTooltipPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        fizzy$renderStage(HostRenderStage.SOURCE_TOOLTIP_POST, graphics, mouseX, mouseY, 0.0f);
    }

    private void fizzy$renderStage(HostRenderStage stage, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
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
}
