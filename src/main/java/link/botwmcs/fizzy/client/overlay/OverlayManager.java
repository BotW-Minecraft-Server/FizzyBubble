package link.botwmcs.fizzy.client.overlay;

import link.botwmcs.fizzy.api.IOverlayContent;
import link.botwmcs.fizzy.client.overlay.content.SimpleTextPage;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayLayerKey;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayLayerStack;
import link.botwmcs.fizzy.ui.kernel.overlay.OverlayRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class OverlayManager {
    private static final OverlayLayerStack STACK = new OverlayLayerStack();

    private OverlayManager() {
    }

    public static OverlayLayerStack stack() {
        return STACK;
    }

    public static void addOverlay(OverlayLayerKey layer, OverlayRenderable renderable) {
        STACK.add(layer, renderable);
    }

    public static void removeOverlay(OverlayLayerKey layer, OverlayRenderable renderable) {
        STACK.remove(layer, renderable);
    }

    public static CreateHudOverlay create() {
        CreateHudOverlay overlay = new CreateHudOverlay(new SimpleTextPage(Component.literal("Hello Overlay Content!")));
        STACK.add(OverlayLayerKey.HUD, overlay);
        return overlay;
    }

    public static CreateHudOverlay create(IOverlayContent content) {
        CreateHudOverlay overlay = new CreateHudOverlay(content);
        STACK.add(OverlayLayerKey.HUD, overlay);
        return overlay;
    }

    public static void remove(CreateHudOverlay overlay) {
        STACK.remove(OverlayLayerKey.HUD, overlay);
    }

    public static void hideAll() {
        STACK.hideAll(OverlayLayerKey.HUD);
    }

    public static void clear() {
        STACK.clear(OverlayLayerKey.HUD);
    }

    public static void renderAll(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick, Anchor anchor) {
        STACK.renderHud(graphics, screenWidth, screenHeight, partialTick, anchor, true);
    }

    public static void renderAllPerInstance(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
        STACK.renderHudPerAnchor(graphics, screenWidth, screenHeight, partialTick);
    }

    public static void renderAllLayers(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
        STACK.renderAllLayers(graphics, screenWidth, screenHeight, partialTick);
    }

    public static void onMouseMoved(double mouseX, double mouseY) {
        STACK.mouseMoved(mouseX, mouseY);
    }

    public static boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return STACK.mouseClicked(mouseX, mouseY, button);
    }

    public static boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return STACK.mouseReleased(mouseX, mouseY, button);
    }

    public static boolean onMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return STACK.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public static boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return STACK.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public static void setLayout(int marginPx, int verticalGapPx, int horizontalGapPx, int maxColumns) {
        STACK.hudLayoutConfig().set(marginPx, verticalGapPx, horizontalGapPx, maxColumns);
    }
}
