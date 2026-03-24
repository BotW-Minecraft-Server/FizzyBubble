package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.emoji.EmojiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Screen.class)
public abstract class ScreenEmojiInteractionMixin {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void fizzy$handleEmojiChatClick(@Nullable Style style, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.ENABLE_FIZZY_COMPONENT.get()) {
            return;
        }
        if (style == null) {
            return;
        }
        Screen self = (Screen) (Object) this;
        if (!(self instanceof ChatScreen chatScreen)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        if (EmojiRegistry.dispatchChatInteraction(style, minecraft, chatScreen)) {
            cir.setReturnValue(true);
        }
    }
}
