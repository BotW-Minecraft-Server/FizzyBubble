package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.client.formatting.FizzyComponentService;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ComponentRenderUtils.class)
public abstract class ComponentRenderUtilsFizzyGlobalMixin {
    @ModifyVariable(method = "wrapComponents", at = @At("HEAD"), argsOnly = true)
    private static FormattedText fizzy$formatWrapInput(FormattedText component) {
        return FizzyComponentService.formatFormattedText(component);
    }
}
