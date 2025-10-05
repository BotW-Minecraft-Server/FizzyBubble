package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.SlotPadSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

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
        this.clearWidgets();
        initElements();
    }



    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        recalcCenter();
        this.clearWidgets();
        initElements();
    }

    private void recalcCenter() {
        int gw = gui.widthPx();
        int gh = gui.heightPx();
        this.left = (this.width  - gw) / 2;
        this.top  = (this.height - gh) / 2;
    }

    private void initElements() {
        FramePainter frame = gui.frame();
        int widthPx = gui.widthPx();
        int heightPx = gui.heightPx();
        frame.setLayout(left, top, widthPx, heightPx, true);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();
        if (slotArea == null) {
            return;
        }
        ElementPainter.InitContext context = new ScreenInitContext();
        for (SlotPadSpec pad : gui.pads()) {
            int padLeft = slotArea.x() + (pad.colStart() - 1) * UiUnit.SLOT_PX;
            int padTop = slotArea.y() + (pad.rowStart() - 1) * UiUnit.SLOT_PX;
            int padWidth = pad.widthSlots() * UiUnit.SLOT_PX;
            int padHeight = pad.heightSlots() * UiUnit.SLOT_PX;
            for (var element : pad.elements()) {
                element.init(context, padLeft, padTop, padWidth, padHeight);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float dt) {
//        super.render(g, mx, my, dt);
        FramePainter frame = gui.frame();
        int widthPx = gui.widthPx();
        int heightPx = gui.heightPx();
        BehindPainter behind = gui.behind();

        if (behind != null) {
            behind.paint(g, frame, dt);
            // I think we don't need post this event...
//            NeoForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, g));
        }
        frame.setLayout(left, top, widthPx, heightPx, true);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();
        BgPainter bg = gui.background();
        if (bg != null) {
            bg.paint(g, frame);
        }
        frame.paint(g, left, top, widthPx, heightPx, true);
        if (slotArea != null) {
            for (SlotPadSpec pad : gui.pads()) {
                int padLeft = slotArea.x() + (pad.colStart() - 1) * UiUnit.SLOT_PX;
                int padTop = slotArea.y() + (pad.rowStart() - 1) * UiUnit.SLOT_PX;
                int padWidth = pad.widthSlots() * UiUnit.SLOT_PX;
                int padHeight = pad.heightSlots() * UiUnit.SLOT_PX;
                for (var element : pad.elements()) {
                    element.render(g, padLeft, padTop, padWidth, padHeight, dt);
                }
            }
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(g, mx, my, dt);
        }
//         super.render(g, mx, my, dt);
    }

    private class ScreenInitContext implements ElementPainter.InitContext {
        @Override
        public <T extends AbstractWidget> T addRenderableWidget(T widget) {
            return FizzyScreenHost.this.addRenderableWidget(widget);
        }
    }

}
