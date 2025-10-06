package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import link.botwmcs.fizzy.ui.pad.SlotPadSpec;
import link.botwmcs.fizzy.ui.split.SplitPainter;
import link.botwmcs.fizzy.ui.split.SplitSpec;
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

        ElementPainter.InitContext context = new ScreenInitContext();
        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (var element : pad.elements()) {
                element.init(context, bounds.left(), bounds.top(), bounds.width(), bounds.height());
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float dt) {
//        super.render(g, mx, my, dt);
        // 初始化frame
        FramePainter frame = gui.frame();
        int widthPx = gui.widthPx();
        int heightPx = gui.heightPx();
        frame.setLayout(left, top, widthPx, heightPx, true);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        // 绘制在frame后面的背景
        BehindPainter behind = gui.behind();
        if (behind != null) {
            behind.paint(g, frame, dt);
            // I think we don't need post this event...
//            NeoForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, g));
        }

        // 绘制背景
        BgPainter bg = gui.background();
        if (bg != null) {
            bg.paint(g, frame);
        }

        // 绘制各个pad内的元素
        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (var element : pad.elements()) {
                element.render(g, bounds.left(), bounds.top(), bounds.width(), bounds.height(), dt);
            }
        }

        // 绘制frame
//        frame.setLayout(left, top, widthPx, heightPx, true);
        frame.paint(g, left, top, widthPx, heightPx, true);

        // 绘制分割线
        SplitPainter splitPainter = gui.splitPainter();
        if (slotArea != null && splitPainter != null) {
            for (SplitSpec split : gui.splits()) {
                split.paint(g, splitPainter, slotArea);
            }
        }

        // 绘制renderables
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
