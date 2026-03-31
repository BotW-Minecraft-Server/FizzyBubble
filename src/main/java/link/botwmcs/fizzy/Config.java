package link.botwmcs.fizzy;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLE_FIZZY_COMPONENT = BUILDER
            .comment("Enable Fizzy global text stylization pipeline for all client-side text rendering")
            .comment("Supports '&' styles and placeholder resolution (image/button API)")
            .define("enableFizzyComponent", true);
    public static final ModConfigSpec.BooleanValue ENABLE_EMOJI_SUGGESTIONS = BUILDER
            .comment("Enable chat ':' emoji suggestions using vanilla suggestion UI (client-side only)")
            .define("enableEmojiSuggestions", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
