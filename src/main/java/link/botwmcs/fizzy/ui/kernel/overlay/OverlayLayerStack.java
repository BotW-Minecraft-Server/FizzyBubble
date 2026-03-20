package link.botwmcs.fizzy.ui.kernel.overlay;

import link.botwmcs.fizzy.client.overlay.Anchor;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class OverlayLayerStack {
    private final Map<OverlayLayerKey, List<OverlayRenderable>> layers = new EnumMap<>(OverlayLayerKey.class);
    private final Map<OverlayLayerKey, OverlayLayoutConfig> layoutConfigs = new EnumMap<>(OverlayLayerKey.class);
    private final OverlayFocusState focusState = new OverlayFocusState();
    private final OverlayCaptureState captureState = new OverlayCaptureState();

    public OverlayLayerStack() {
        for (OverlayLayerKey key : OverlayLayerKey.values()) {
            layers.put(key, new ArrayList<>());
            layoutConfigs.put(key, new OverlayLayoutConfig());
        }
    }

    public OverlayFocusState focusState() {
        return focusState;
    }

    public OverlayCaptureState captureState() {
        return captureState;
    }

    public OverlayLayoutConfig hudLayoutConfig() {
        return layoutConfig(OverlayLayerKey.HUD);
    }

    public OverlayLayoutConfig layoutConfig(OverlayLayerKey layer) {
        OverlayLayoutConfig config = layoutConfigs.get(layer);
        if (config == null) {
            return hudLayoutConfig();
        }
        return config;
    }

    public void add(OverlayLayerKey layer, OverlayRenderable renderable) {
        List<OverlayRenderable> list = layers.get(layer);
        if (list == null) {
            return;
        }
        if (!list.contains(renderable)) {
            list.add(renderable);
        }
    }

    public void remove(OverlayLayerKey layer, OverlayRenderable renderable) {
        List<OverlayRenderable> list = layers.get(layer);
        if (list == null) {
            return;
        }
        if (list.remove(renderable)) {
            renderable.dispose();
        }
        releaseDeadRefs();
    }

    public void hideAll(OverlayLayerKey layer) {
        List<OverlayRenderable> list = layers.get(layer);
        if (list == null) {
            return;
        }
        for (OverlayRenderable renderable : list) {
            renderable.hide();
        }
    }

    public void clear(OverlayLayerKey layer) {
        List<OverlayRenderable> list = layers.get(layer);
        if (list == null) {
            return;
        }
        for (OverlayRenderable renderable : list) {
            renderable.dispose();
        }
        list.clear();
        releaseDeadRefs();
    }

    public void clearAll() {
        for (OverlayLayerKey key : OverlayLayerKey.values()) {
            clear(key);
        }
    }

    public List<OverlayRenderable> snapshot(OverlayLayerKey layer) {
        List<OverlayRenderable> list = layers.get(layer);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return List.copyOf(list);
    }

    public void renderHud(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick, Anchor anchor, boolean forceAnchor) {
        renderLayer(graphics, screenWidth, screenHeight, partialTick, OverlayLayerKey.HUD, anchor, forceAnchor);
    }

    public void renderHudPerAnchor(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
        renderLayerPerAnchor(graphics, screenWidth, screenHeight, partialTick, OverlayLayerKey.HUD);
    }

    public void renderLayer(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            float partialTick,
            OverlayLayerKey layer,
            Anchor anchor,
            boolean forceAnchor
    ) {
        List<OverlayRenderable> ordered = activeRenderables(layer);
        if (ordered.isEmpty()) {
            releaseDeadRefs();
            return;
        }
        OverlayLayoutEngine.layout(screenWidth, screenHeight, ordered, anchor, layoutConfig(layer), forceAnchor);
        for (OverlayRenderable renderable : ordered) {
            renderable.render(graphics, partialTick);
        }
        focusState.promoteTop(ordered);
        releaseDeadRefs();
    }

    public void renderLayerPerAnchor(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            float partialTick,
            OverlayLayerKey layer
    ) {
        renderLayerAnchorGroup(graphics, screenWidth, screenHeight, partialTick, layer, Anchor.TOP_LEFT);
        renderLayerAnchorGroup(graphics, screenWidth, screenHeight, partialTick, layer, Anchor.TOP_RIGHT);
        renderLayerAnchorGroup(graphics, screenWidth, screenHeight, partialTick, layer, Anchor.BOTTOM_LEFT);
        renderLayerAnchorGroup(graphics, screenWidth, screenHeight, partialTick, layer, Anchor.BOTTOM_RIGHT);
        releaseDeadRefs();
    }

    private void renderLayerAnchorGroup(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            float partialTick,
            OverlayLayerKey layer,
            Anchor anchor
    ) {
        List<OverlayRenderable> source = activeRenderables(layer);
        if (source.isEmpty()) {
            return;
        }
        List<OverlayRenderable> group = new ArrayList<>();
        for (OverlayRenderable renderable : source) {
            if (renderable.getAnchor() == anchor) {
                group.add(renderable);
            }
        }
        if (group.isEmpty()) {
            return;
        }
        OverlayLayoutEngine.layout(screenWidth, screenHeight, group, anchor, layoutConfig(layer), false);
        for (OverlayRenderable renderable : group) {
            renderable.render(graphics, partialTick);
        }
        focusState.promoteTop(group);
    }

    private List<OverlayRenderable> activeRenderables(OverlayLayerKey layer) {
        List<OverlayRenderable> source = layers.get(layer);
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        source.removeIf(renderable -> {
            if (renderable.isActive()) {
                return false;
            }
            renderable.dispose();
            return true;
        });
        if (source.isEmpty()) {
            return List.of();
        }
        List<OverlayRenderable> active = new ArrayList<>();
        for (OverlayRenderable renderable : source) {
            if (renderable.isActive()) {
                active.add(renderable);
            }
        }
        active.sort(Comparator.comparingInt(r -> r.getAnchor().ordinal()));
        return active;
    }

    private void releaseDeadRefs() {
        focusState.clearIfInactive();
        captureState.releaseIfInactive();
    }
}
