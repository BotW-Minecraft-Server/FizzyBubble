package link.botwmcs.fizzy.client.overlay;

import link.botwmcs.fizzy.ui.kernel.notification.NotificationLevel;
import link.botwmcs.fizzy.ui.kernel.notification.NotificationOverlay;
import link.botwmcs.fizzy.ui.kernel.notification.NotificationSpec;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayLayerKey;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class NotificationManager {
    static {
        setLayout(8, 4, 4, 1);
    }

    private NotificationManager() {
    }

    public static NotificationOverlay show(Component title, Component message) {
        return show(
                NotificationSpec.builder()
                        .title(title)
                        .message(message)
                        .build()
        );
    }

    public static NotificationOverlay show(Component title, Component message, NotificationLevel level, int durationTicks) {
        return show(
                NotificationSpec.builder()
                        .title(title)
                        .message(message)
                        .level(level)
                        .durationTicks(durationTicks)
                        .build()
        );
    }

    public static NotificationOverlay show(NotificationSpec spec) {
        NotificationOverlay overlay = new NotificationOverlay(Objects.requireNonNull(spec, "spec"));
        OverlayManager.stack().add(OverlayLayerKey.NOTIFICATION, overlay);
        return overlay;
    }

    public static void hideAll() {
        OverlayManager.stack().hideAll(OverlayLayerKey.NOTIFICATION);
    }

    public static void clear() {
        OverlayManager.stack().clear(OverlayLayerKey.NOTIFICATION);
    }

    public static void renderAll(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
        OverlayManager.stack().renderLayer(graphics, screenWidth, screenHeight, partialTick, OverlayLayerKey.NOTIFICATION);
    }

    public static void setLayout(int marginPx, int verticalGapPx, int horizontalGapPx, int maxColumns) {
        OverlayManager.stack().layoutConfig(OverlayLayerKey.NOTIFICATION).set(marginPx, verticalGapPx, horizontalGapPx, maxColumns);
    }
}
