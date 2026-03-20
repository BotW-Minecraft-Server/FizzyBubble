package link.botwmcs.fizzy.ui.kernel.overlay;

public enum OverlayLayerKey {
    HUD(100),
    NOTIFICATION(200),
    MODAL(300),
    DEBUG(1000);

    private final int priority;

    OverlayLayerKey(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
