package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Function;

@Mixin(SignText.class)
public abstract class SignTextDynamicRenderMessagesMixin {
    @Shadow
    public abstract Component getMessage(int index, boolean isFiltered);

    @Inject(method = "getRenderMessages", at = @At("HEAD"), cancellable = true)
    private void fizzy$dynamicRenderMessages(
            boolean renderMessagesFiltered,
            Function<Component, FormattedCharSequence> formatter,
            CallbackInfoReturnable<FormattedCharSequence[]> cir
    ) {
        if (!Config.ENABLE_FIZZY_COMPONENT.get()) {
            return;
        }

        boolean dynamic = false;
        for (int i = 0; i < 4; i++) {
            if (containsDynamicSyntax(this.getMessage(i, renderMessagesFiltered))) {
                dynamic = true;
                break;
            }
        }
        if (!dynamic) {
            return;
        }

        FormattedCharSequence[] recomputed = new FormattedCharSequence[4];
        for (int i = 0; i < 4; i++) {
            FormattedCharSequence line = formatter.apply(this.getMessage(i, renderMessagesFiltered));
            recomputed[i] = line == null ? FormattedCharSequence.EMPTY : line;
        }
        cir.setReturnValue(recomputed);
    }

    private static boolean containsDynamicSyntax(Component component) {
        if (component == null) {
            return false;
        }
        final boolean[] found = new boolean[] {false};
        component.visit((style, text) -> {
            if (containsDynamicSyntax(text)) {
                found[0] = true;
                return Optional.of(Boolean.TRUE);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return found[0];
    }

    private static boolean containsDynamicSyntax(String text) {
        if (text == null || text.length() < 2) {
            return false;
        }
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '&' && Character.toLowerCase(text.charAt(i + 1)) == 'h') {
                return true;
            }
        }
        return text.indexOf(':') >= 0;
    }
}
