package link.botwmcs.fizzy;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<String> GALLERY_URL = BUILDER
            .comment("Online gallery url")
            .comment("Do not change unless you know what you are doing")
            .define("galleryUrl", "https://mirror.botwmcs.link/eazyimages");
    public static final ModConfigSpec.ConfigValue<String> TOKEN = BUILDER
            .comment("Token for online gallery")
            .define("token", "1c17b11693cb5ec63859b091c5b9c1b2");
    public static final ModConfigSpec.BooleanValue ENABLE_FIZZY_COMPONENT = BUILDER
            .comment("Enable Fizzy global text stylization pipeline for all client-side text rendering")
            .comment("Supports '&' styles and placeholder resolution (image/button API)")
            .define("enableFizzyComponent", true);
    public static final ModConfigSpec.BooleanValue ENABLE_EMOJI_SUGGESTIONS = BUILDER
            .comment("Enable chat ':' emoji suggestions using vanilla suggestion UI (client-side only)")
            .define("enableEmojiSuggestions", true);



//    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
//            .comment("Whether to log the dirt block on common setup")
//            .define("logDirtBlock", true);
//
//    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
//            .comment("A magic number")
//            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);
//
//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
//            .comment("What you want the introduction message to be for the magic number")
//            .define("magicNumberIntroduction", "The magic number is... ");
//
//    // a list of strings that are treated as resource locations for items
//    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();
}
