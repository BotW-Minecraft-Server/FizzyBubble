package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.ui.core.FizzyGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FizzyScreenHost extends Screen {
    private final FizzyGui gui;
    private int left, top;

    public FizzyScreenHost(FizzyGui gui) {
        super(Component.empty());
        this.gui = gui;
    }

    @Override
    protected void init() {
        int w = gui.widthPx(), h = gui.heightPx();
        this.left = (this.width - w) / 2;
        this.top  = (this.height - h) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float dt) {
        gui.background().paint(g, left, top, gui.widthPx(), gui.heightPx(), true);
        // super.render(g, mx, my, dt);
    }

}
