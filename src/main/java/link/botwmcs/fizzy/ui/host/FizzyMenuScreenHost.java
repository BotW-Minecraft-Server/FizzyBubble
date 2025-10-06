package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import link.botwmcs.fizzy.ui.pad.SlotPadSpec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FizzyMenuScreenHost<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private final FizzyGui gui;

    public FizzyMenuScreenHost(T menu, Inventory inv, Component title, FizzyGui gui) {
        super(menu, inv, title);
        this.gui = gui;
    }

    @Override
    protected void init() {
        this.imageWidth  = gui.widthPx();
        this.imageHeight = gui.heightPx(); // 下方接原版 96 高度的玩家背包
        super.init();
        // 将（GUI + 玩家背包）合并后的整体垂直居中
        int totalH = this.imageHeight + UiUnit.VANILLA_PLAYER_INV_HEIGHT;
        this.leftPos = (this.width  - this.imageWidth) / 2;
        this.topPos  = (this.height - totalH) / 2;
        this.clearWidgets();
        initElements();
    }

    private void initElements() {
        FramePainter frame = gui.frame();
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, false);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        ElementPainter.InitContext context = new MenuInitContext();
        for (PadSpec pad : gui.pads()) {
            for (var element : pad.elements()) {
                PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
                element.init(context, bounds.left(), bounds.top(), bounds.width(), bounds.height());
            }
        }
    }


    @Override
    protected void renderBg(GuiGraphics g, float v, int i, int i1) {
        FramePainter frame = gui.frame();
        BehindPainter behind = gui.behind();

        if (behind != null) {
            behind.paint(g, frame, v);
            // I think we don't need post this event...
//            NeoForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, g));
        }

        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, false);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        BgPainter bg = gui.background();
        if (bg != null) {
            bg.paint(g, frame);
        }
        frame.paint(g, leftPos, topPos, imageWidth, imageHeight, false);
        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (var element : pad.elements()) {
                element.render(g, bounds.left(), bounds.top(), bounds.width(), bounds.height(), v);
            }
        }
    }

    private class MenuInitContext implements ElementPainter.InitContext {
        @Override
        public <W extends AbstractWidget> W addRenderableWidget(W widget) {
            return FizzyMenuScreenHost.this.addRenderableWidget(widget);
        }
    }
}
