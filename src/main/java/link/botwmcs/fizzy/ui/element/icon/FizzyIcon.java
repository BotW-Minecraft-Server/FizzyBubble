package link.botwmcs.fizzy.ui.element.icon;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public enum FizzyIcon {
    ARROW_DOWN("arrow_down.png"),
    ARROW_UP("arrow_up.png"),
    ARROW_UP_DOWN("arrow_up_down.png"),
    BELL("bell.png"),
    COSMETICS("cosmetics.png"),
    COSMETICS_OFF("cosmetics_off.png"),
    COSMETICS_ON("cosmetics_on.png"),
    EMOTES("emotes.png"),
    EXPAND("expand.png"),
    FRIENDS("friends.png"),
    FULLSCREEN("fullscreen.png"),
    FULLSCREEN_OFF("fullscreen_off.png"),
    FULLSCREEN_ON("fullscreen_on.png"),
    INVITE("invite.png"),
    MC_FOLDER("mc_folder.png"),
    MC_FOLDER_7X6("mc_folder_7x6.png"),
    MESSAGES("messages.png"),
    MODS("mods.png"),
    NOTIFICATIONS_OFF("notifications_off.png"),
    NOTIFICATIONS_ON("notifications_on.png"),
    PICTURES("pictures.png"),
    PICTURES_SHORT("pictures_short.png"),
    RADIO_TICK("radio_tick.png"),
    SETTINGS("settings.png"),
    SETTINGS_9X8("settings_9x8.png"),
    SETTINGS_VERTICAL("settings_vertical.png"),
    SOCIAL("social.png"),
    STAR_4X3("star_4x3.png"),
    TRASH("trash.png"),
    WORLD_8X8("world_8x8.png"),

    FIZZY_ALCHEMICAL_BOMB("textures/gui/components/icon/", "alchemical_bomb.png"),
    FIZZY_AMULET_OF_CHAOS("textures/gui/components/icon/", "amulet_of_chaos.png"),
    FIZZY_ANCHOR("textures/gui/components/icon/", "anchor.png"),
    FIZZY_ANTIGRAVITY_CAPSULE("textures/gui/components/icon/", "antigravity_capsule.png"),
    FIZZY_APPLE("textures/gui/components/icon/", "apple.png"),
    FIZZY_ART_PALETTE("textures/gui/components/icon/", "art-palette.png"),
    FIZZY_BALM_OF_THE_DEPTHS("textures/gui/components/icon/", "balm_of_the_depths.png"),
    FIZZY_BLACK_STONE("textures/gui/components/icon/", "black_stone.png"),
    FIZZY_CAMERA("textures/gui/components/icon/", "camera.png"),
    FIZZY_CAMP_FORTIFICATION_STONE("textures/gui/components/icon/", "camp_fortification_stone.png"),
    FIZZY_CASTLE_ENTRANCE("textures/gui/components/icon/", "castle-entrance.png"),
    FIZZY_CHECKMARK("textures/gui/components/icon/", "checkmark.png"),
    FIZZY_CLOCK("textures/gui/components/icon/", "clock.png"),
    FIZZY_CLOUD("textures/gui/components/icon/", "cloud.png"),
    FIZZY_CLOVER("textures/gui/components/icon/", "clover.png"),
    FIZZY_COFFEE("textures/gui/components/icon/", "coffee.png"),
    FIZZY_CROWN("textures/gui/components/icon/", "crown.png"),
    FIZZY_CRYSTAL_OF_PURITY("textures/gui/components/icon/", "crystal_of_purity.png"),
    FIZZY_DIAMOND("textures/gui/components/icon/", "diamond.png"),
    FIZZY_DOLLAR("textures/gui/components/icon/", "dollar.png"),
    FIZZY_EARTH("textures/gui/components/icon/", "earth.png"),
    FIZZY_EASTER_EGG_1("textures/gui/components/icon/", "easter_egg_1.png"),
    FIZZY_EASTER_EGG_10("textures/gui/components/icon/", "easter_egg_10.png"),
    FIZZY_EASTER_EGG_2("textures/gui/components/icon/", "easter_egg_2.png"),
    FIZZY_EASTER_EGG_3("textures/gui/components/icon/", "easter_egg_3.png"),
    FIZZY_EASTER_EGG_4("textures/gui/components/icon/", "easter_egg_4.png"),
    FIZZY_EASTER_EGG_5("textures/gui/components/icon/", "easter_egg_5.png"),
    FIZZY_EASTER_EGG_6("textures/gui/components/icon/", "easter_egg_6.png"),
    FIZZY_EASTER_EGG_7("textures/gui/components/icon/", "easter_egg_7.png"),
    FIZZY_EASTER_EGG_8("textures/gui/components/icon/", "easter_egg_8.png"),
    FIZZY_EASTER_EGG_9("textures/gui/components/icon/", "easter_egg_9.png"),
    FIZZY_ECLIPSE_POTION("textures/gui/components/icon/", "eclipse_potion.png"),
    FIZZY_ENVELOPE("textures/gui/components/icon/", "envelope.png"),
    FIZZY_FEATHER("textures/gui/components/icon/", "feather.png"),
    FIZZY_FIRE("textures/gui/components/icon/", "fire.png"),
    FIZZY_FLAME_AURA_BOTTLE("textures/gui/components/icon/", "flame_aura_bottle.png"),
    FIZZY_FLOWER("textures/gui/components/icon/", "flower.png"),
    FIZZY_GAME_CONTROLLER("textures/gui/components/icon/", "game-controller.png"),
    FIZZY_GLOW_OF_MORNING_LIGHT("textures/gui/components/icon/", "glow_of_morning_light.png"),
    FIZZY_GOLD_COIN("textures/gui/components/icon/", "gold-coin.png"),
    FIZZY_HEART("textures/gui/components/icon/", "heart.png"),
    FIZZY_HEROIC_CLICK_STONE("textures/gui/components/icon/", "heroic_click_stone.png"),
    FIZZY_HIGH_VOLTAGE("textures/gui/components/icon/", "high-voltage.png"),
    FIZZY_LIGHT_BULB("textures/gui/components/icon/", "light-bulb.png"),
    FIZZY_LLAMA_PINHATA("textures/gui/components/icon/", "llama-pinhata.png"),
    FIZZY_MAG_GLASS("textures/gui/components/icon/", "mag-glass.png"),
    FIZZY_MAGNETIC_SPHERE("textures/gui/components/icon/", "magnetic_sphere.png"),
    FIZZY_MEGAPHONE("textures/gui/components/icon/", "megaphone.png"),
    FIZZY_MIST_CAMOUFLAGE_POTION("textures/gui/components/icon/", "mist_camouflage_potion.png"),
    FIZZY_MOON("textures/gui/components/icon/", "moon.png"),
    FIZZY_MUHSROOM("textures/gui/components/icon/", "muhsroom.png"),
    FIZZY_MUSIC_NOTES("textures/gui/components/icon/", "music-notes.png"),
    FIZZY_PEACE("textures/gui/components/icon/", "peace.png"),
    FIZZY_PENCIL("textures/gui/components/icon/", "pencil.png"),
    FIZZY_PERSON("textures/gui/components/icon/", "person.png"),
    FIZZY_PHOENIX_RESURRECTION_STONE("textures/gui/components/icon/", "phoenix_resurrection_stone.png"),
    FIZZY_PICKAXE("textures/gui/components/icon/", "pickaxe.png"),
    FIZZY_PORTAL_SEAL_STONE("textures/gui/components/icon/", "portal_seal_stone.png"),
    FIZZY_POTION("textures/gui/components/icon/", "potion.png"),
    FIZZY_PRESENT_BOX("textures/gui/components/icon/", "present-box.png"),
    FIZZY_RADIOACTIVITY("textures/gui/components/icon/", "radioactivity.png"),
    FIZZY_RAINBOW("textures/gui/components/icon/", "rainbow.png"),
    FIZZY_RED_SWORD("textures/gui/components/icon/", "red-sword.png"),
    FIZZY_ROCKET("textures/gui/components/icon/", "rocket.png"),
    FIZZY_SAND_CLOCK("textures/gui/components/icon/", "sand-clock.png"),
    FIZZY_SEAL_OF_THE_CLOSED_PATH("textures/gui/components/icon/", "seal_of_the_closed_path.png"),
    FIZZY_SETTINGS("textures/gui/components/icon/", "settings.png"),
    FIZZY_SHIELD("textures/gui/components/icon/", "shield.png"),
    FIZZY_SHOPPING_CART("textures/gui/components/icon/", "shopping-cart.png"),
    FIZZY_SOUL_CATCHERS_SOLUTION("textures/gui/components/icon/", "soul_catchers_solution.png"),
    FIZZY_SPHERE_OF_DESTRUCTION("textures/gui/components/icon/", "sphere_of_destruction.png"),
    FIZZY_STACKED_BOOKS("textures/gui/components/icon/", "stacked-books.png"),
    FIZZY_STONE_OF_LIGHT("textures/gui/components/icon/", "stone_of_light.png"),
    FIZZY_STUNNING_CRYSTAL("textures/gui/components/icon/", "stunning_crystal.png"),
    FIZZY_SUMMONING_STONE("textures/gui/components/icon/", "summoning_stone.png"),
    FIZZY_SUN("textures/gui/components/icon/", "sun.png"),
    FIZZY_TIME_LOOP_SPHERE("textures/gui/components/icon/", "time_loop_sphere.png"),
    FIZZY_TRANSFORMATION_VIAL("textures/gui/components/icon/", "transformation_vial.png"),
    FIZZY_TREASURE("textures/gui/components/icon/", "treasure.png"),
    FIZZY_TREE("textures/gui/components/icon/", "tree.png"),
    FIZZY_TROPHY("textures/gui/components/icon/", "trophy.png"),
    FIZZY_UMBRELLA("textures/gui/components/icon/", "umbrella.png"),
    FIZZY_VOLCANO("textures/gui/components/icon/", "volcano.png"),
    FIZZY_WARNING("textures/gui/components/icon/", "warning.png"),
    FIZZY_WATER_DROP("textures/gui/components/icon/", "water-drop.png"),
    FIZZY_WAVE("textures/gui/components/icon/", "wave.png"),
    FIZZY_WIND_CRYSTAL("textures/gui/components/icon/", "wind_crystal.png"),
    FIZZY_X("textures/gui/components/icon/", "x.png");

    private static final String SLIM_ICON_DIR = "textures/gui/components/icon/slim/";

    private final String fileName;
    private final ResourceLocation texture;

    FizzyIcon(String fileName) {
        this(SLIM_ICON_DIR, fileName);
    }

    FizzyIcon(String directory, String fileName) {
        String resolvedDirectory = Objects.requireNonNull(directory, "directory");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.texture = Fizzy.resourceLocation(resolvedDirectory + fileName);
    }

    public String fileName() {
        return this.fileName;
    }

    public ResourceLocation texture() {
        return this.texture;
    }
}
