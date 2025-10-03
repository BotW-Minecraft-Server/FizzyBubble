package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
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
        super.init(); // 虽然不是必需，但更稳
        recalcCenter();
//        int w = gui.widthPx(), h = gui.heightPx();
//        this.left = (this.width - w) / 2;
//        this.top  = (this.height - h) / 2;
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        recalcCenter();
    }

    private void recalcCenter() {
        int gw = gui.widthPx();
        int gh = gui.heightPx();
        this.left = (this.width  - gw) / 2;
        this.top  = (this.height - gh) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float dt) {
        FramePainter frame = gui.frame();
        int widthPx = gui.widthPx();
        int heightPx = gui.heightPx();
        frame.setLayout(left, top, widthPx, heightPx, true);
        BgPainter bg = gui.background();
        if (bg != null) {
            bg.paint(g, frame);
        }
        frame.paint(g, left, top, widthPx, heightPx, true);
//         super.render(g, mx, my, dt);
    }

}
