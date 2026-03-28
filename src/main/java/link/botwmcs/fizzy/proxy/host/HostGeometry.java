package link.botwmcs.fizzy.proxy.host;

import javax.annotation.Nullable;

public record HostGeometry(
        int rootLeft,
        int rootTop,
        int rootWidth,
        int rootHeight,
        @Nullable SlotGridGeometry slotGrid
) {
}

