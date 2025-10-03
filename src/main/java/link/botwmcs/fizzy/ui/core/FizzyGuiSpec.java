package link.botwmcs.fizzy.ui.core;

public record FizzyGuiSpec(int cols, int rows, HostType hostType) {
    public int pixelWidth()  { return cols * UiUnit.SLOT_PX; }
    public int pixelHeight() { return rows * UiUnit.SLOT_PX; }
}
