package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.client.formatting.FizzyComponentService;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MutableComponent.class)
public abstract class MutableComponentFizzyGlobalMixin {
    @Inject(method = "getVisualOrderText", at = @At("RETURN"), cancellable = true)
    private void fizzy$globalVisualOrder(CallbackInfoReturnable<FormattedCharSequence> cir) {
        Component self = (Component) (Object) this;
        cir.setReturnValue(FizzyComponentService.formatVisualOrder(self, cir.getReturnValue()));
    }
}
