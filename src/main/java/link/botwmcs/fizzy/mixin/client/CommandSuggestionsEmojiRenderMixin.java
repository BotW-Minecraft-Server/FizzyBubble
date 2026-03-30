package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.FizzyComponentService;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList")
public abstract class CommandSuggestionsEmojiRenderMixin {
    @Redirect(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            )
    )
    private void fizzy$renderFormattedEmojiSuggestion(GuiGraphicsExtractor guiGraphics, Font font, String text, int x, int y, int color) {
        if (!Config.ENABLE_FIZZY_COMPONENT.get()) {
            guiGraphics.text(font, text, x, y, color);
            return;
        }

        FormattedCharSequence formatted = FizzyComponentService.formatVisualOrder(text);
        if (formatted != null) {
            guiGraphics.text(font, formatted, x, y, color);
            return;
        }
        guiGraphics.text(font, text, x, y, color);
    }
}
