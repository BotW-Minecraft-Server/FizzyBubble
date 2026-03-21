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
    WORLD_8X8("world_8x8.png");

    private static final String SLIM_ICON_DIR = "textures/gui/components/icon/slim/";

    private final String fileName;
    private final ResourceLocation texture;

    FizzyIcon(String fileName) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.texture = Fizzy.resourceLocation(SLIM_ICON_DIR + fileName);
    }

    public String fileName() {
        return this.fileName;
    }

    public ResourceLocation texture() {
        return this.texture;
    }
}
