package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;
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

        BgPainter bg = gui.background();
        if (bg != null) {
            bg.paint(g, frame);
        }
        frame.paint(g, leftPos, topPos, imageWidth, imageHeight, false);    }
}
