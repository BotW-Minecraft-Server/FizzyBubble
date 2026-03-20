package link.botwmcs.fizzy.ui.kernel.overlay;

import java.util.List;

public final class OverlayFocusState {
    private OverlayRenderable focused;

    public OverlayRenderable focused() {
        return focused;
    }

    public void focus(OverlayRenderable renderable) {
        this.focused = renderable;
    }

    public void clearIfInactive() {
        OverlayRenderable current = focused;
        if (current != null && !current.isActive()) {
            focused = null;
        }
    }

    public void promoteTop(List<? extends OverlayRenderable> ordered) {
        clearIfInactive();
        if (focused != null) {
            return;
        }
        if (ordered.isEmpty()) {
            return;
        }
        focused = ordered.get(ordered.size() - 1);
    }
}
