package link.botwmcs.fizzy.client.overlay;

import link.botwmcs.fizzy.ui.kernel.modal.ModalOverlay;
import link.botwmcs.fizzy.ui.kernel.modal.ModalSpec;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayLayerKey;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class ModalManager {
    static {
        setLayout(0, 0, 0, 1);
    }

    private ModalManager() {
    }

    public static ModalOverlay show(Component title, Component message) {
        return show(
                ModalSpec.builder()
                        .title(title)
                        .message(message)
                        .build()
        );
    }

    public static ModalOverlay show(ModalSpec spec) {
        clear();
        ModalOverlay overlay = new ModalOverlay(Objects.requireNonNull(spec, "spec"));
        OverlayManager.stack().add(OverlayLayerKey.MODAL, overlay);
        return overlay;
    }

    public static void hideAll() {
        OverlayManager.stack().hideAll(OverlayLayerKey.MODAL);
    }

    public static void clear() {
        OverlayManager.stack().clear(OverlayLayerKey.MODAL);
    }

    public static void renderAll(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
        OverlayManager.stack().renderLayer(graphics, screenWidth, screenHeight, partialTick, OverlayLayerKey.MODAL);
    }

    public static void setLayout(int marginPx, int verticalGapPx, int horizontalGapPx, int maxColumns) {
        OverlayManager.stack().layoutConfig(OverlayLayerKey.MODAL).set(marginPx, verticalGapPx, horizontalGapPx, maxColumns);
    }
}
