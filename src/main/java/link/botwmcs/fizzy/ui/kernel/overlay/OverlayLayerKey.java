package link.botwmcs.fizzy.ui.kernel.overlay;

import link.botwmcs.fizzy.client.overlay.Anchor;

public enum OverlayLayerKey {
    HUD(100, LayoutMode.FIXED_ANCHOR, Anchor.TOP_LEFT, true),
    NOTIFICATION(200, LayoutMode.PER_INSTANCE_ANCHOR, Anchor.TOP_RIGHT, false),
    MODAL(300, LayoutMode.FIXED_ANCHOR, Anchor.TOP_LEFT, true),
    DEBUG(1000, LayoutMode.PER_INSTANCE_ANCHOR, Anchor.TOP_LEFT, false);

    private final int priority;
    private final LayoutMode layoutMode;
    private final Anchor defaultAnchor;
    private final boolean forceAnchorIntoInstance;

    OverlayLayerKey(int priority, LayoutMode layoutMode, Anchor defaultAnchor, boolean forceAnchorIntoInstance) {
        this.priority = priority;
        this.layoutMode = layoutMode;
        this.defaultAnchor = defaultAnchor;
        this.forceAnchorIntoInstance = forceAnchorIntoInstance;
    }

    public int priority() {
        return priority;
    }

    public LayoutMode layoutMode() {
        return layoutMode;
    }

    public Anchor defaultAnchor() {
        return defaultAnchor;
    }

    public boolean forceAnchorIntoInstance() {
        return forceAnchorIntoInstance;
    }

    public boolean usesPerAnchorLayout() {
        return layoutMode == LayoutMode.PER_INSTANCE_ANCHOR;
    }

    public enum LayoutMode {
        FIXED_ANCHOR,
        PER_INSTANCE_ANCHOR
    }
}
