package link.botwmcs.fizzy.mixin.client;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import link.botwmcs.fizzy.Config;
import link.botwmcs.fizzy.client.formatting.emoji.EmojiChatSuggestionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsEmojiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Screen screen;

    @Shadow
    @Final
    private EditBox input;

    @Shadow
    @Nullable
    private ParseResults<SharedSuggestionProvider> currentParse;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    @Final
    private List<FormattedCharSequence> commandUsage;

    @Shadow
    private boolean allowSuggestions;

    @Shadow
    boolean keepSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void fizzy$injectEmojiSuggestions(CallbackInfo ci) {
        if (!Config.ENABLE_FIZZY_COMPONENT.get() || !Config.ENABLE_EMOJI_SUGGESTIONS.get()) {
            return;
        }
        if (!(this.screen instanceof ChatScreen)) {
            return;
        }

        String value = this.input.getValue();
        if (value == null || value.startsWith("/")) {
            return;
        }

        int cursor = this.input.getCursorPosition();
        EmojiChatSuggestionService.Match match = EmojiChatSuggestionService.findMatch(value, cursor);
        if (match == null) {
            return;
        }

        this.currentParse = null;
        if (!this.keepSuggestions) {
            this.input.setSuggestion(null);
            this.suggestions = null;
        }
        this.commandUsage.clear();

        Suggestions emojiSuggestions = EmojiChatSuggestionService.buildSuggestions(value, cursor, match);
        this.pendingSuggestions = CompletableFuture.completedFuture(emojiSuggestions);
        this.suggestions = null;

        if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get()) {
            this.showSuggestions(false);
        }
        ci.cancel();
    }
}
