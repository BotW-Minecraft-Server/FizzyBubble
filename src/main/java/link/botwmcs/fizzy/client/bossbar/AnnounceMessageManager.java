package link.botwmcs.fizzy.client.bossbar;

import link.botwmcs.fizzy.client.overlay.OverlayManager;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayLayerKey;
import net.minecraft.network.chat.Component;

public final class AnnounceMessageManager {
    private static final AnnounceMessage INSTANCE = new AnnounceMessage();
    private static boolean registered;

    private AnnounceMessageManager() {
    }

    public static void show(Component text, int ticks) {
        ensureRegistered();
        INSTANCE.show(text, ticks);
    }

    public static void hideAll() {
        ensureRegistered();
        INSTANCE.hide();
    }

    public static void clear() {
        hideAll();
    }

    public static void ensureRegistered() {
        if (registered) {
            return;
        }
        OverlayManager.addOverlay(OverlayLayerKey.ANNOUNCE, INSTANCE);
        registered = true;
    }
}
