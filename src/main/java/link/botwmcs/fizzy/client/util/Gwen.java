package link.botwmcs.fizzy.client.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Gwen {
    private Gwen() {
    }

    public static void withScissor(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, Runnable renderTask) {
        if (right <= left || bottom <= top) {
            return;
        }
        graphics.enableScissor(left, top, right, bottom);
        try {
            renderTask.run();
        } finally {
            graphics.disableScissor();
        }
    }
}
