package link.botwmcs.fizzy.ui.kernel.overlay;

public final class OverlayCaptureState {
    private OverlayRenderable pointerOwner;
    private int pointerButton = -1;

    public OverlayRenderable pointerOwner() {
        return pointerOwner;
    }

    public int pointerButton() {
        return pointerButton;
    }

    public boolean hasCapture() {
        return pointerOwner != null;
    }

    public void capture(OverlayRenderable owner, int button) {
        pointerOwner = owner;
        pointerButton = button;
    }

    public void release() {
        pointerOwner = null;
        pointerButton = -1;
    }

    public void releaseIfInactive() {
        if (pointerOwner != null && !pointerOwner.isActive()) {
            release();
        }
    }
}
