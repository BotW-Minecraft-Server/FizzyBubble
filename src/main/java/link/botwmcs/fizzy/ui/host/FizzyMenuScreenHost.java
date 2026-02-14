package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import link.botwmcs.fizzy.ui.split.SplitPainter;
import link.botwmcs.fizzy.ui.split.SplitSpec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, false, hasBelow);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        MenuInitContext context = new MenuInitContext();
        for (PadSpec pad : gui.pads()) {
            for (var element : pad.elements()) {
                PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
                element.init(context, bounds.left(), bounds.top(), bounds.width(), bounds.height());
            }
        }
        if (hasBelow && gui.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null) {
                gui.below().init(context, belowArea.left(), belowArea.top(), belowArea.width(), belowArea.height());
            }
        }
    }


    @Override
    protected void renderBg(GuiGraphics g, float v, int i, int i1) {
        FramePainter frame = gui.frame();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, false, hasBelow);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        BehindPainter behind = gui.behind();
        if (behind != null) {
            behind.paint(g, frame, v);
            // I think we don't need post this event...
//            NeoForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, g));
        }

        BgPainter bg = gui.background();
        if (bg != null) {
            bg.paint(g, frame);
        }

        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (var element : pad.elements()) {
                element.render(g, bounds.left(), bounds.top(), bounds.width(), bounds.height(), v);
            }
        }

        if (hasBelow && gui.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null) {
                gui.below().render(g, belowArea.left(), belowArea.top(), belowArea.width(), belowArea.height(), v);
            }
        }

        frame.paint(g, leftPos, topPos, imageWidth, imageHeight, false, hasBelow);

        SplitPainter splitPainter = gui.splitPainter();
        if (slotArea != null && splitPainter != null) {
            for (SplitSpec split : gui.splits()) {
                split.paint(g, splitPainter, slotArea);
            }
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(g, i, i1, v);
        }

    }

    private class MenuInitContext implements ElementPainter.InitContext {
        @Override
        public <W extends AbstractWidget> W addRenderableWidget(W widget) {
            return FizzyMenuScreenHost.this.addRenderableWidget(widget);
        }
    }

    /** 通过 slot 坐标(1-based)获取该区域内的元素列表 */
    public List<ElementPainter> elementsAtSlot(int row, int col) {
        FramePainter frame = gui.frame();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, false, hasBelow);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();
        if (slotArea == null) {
            return Collections.emptyList();
        }

        int slotX = slotArea.x() + (col - 1) * UiUnit.SLOT_PX;
        int slotY = slotArea.y() + (row - 1) * UiUnit.SLOT_PX;
        return elementsInRect(frame, slotArea, slotX, slotY, UiUnit.SLOT_PX, UiUnit.SLOT_PX);
    }

    /** 通过屏幕像素坐标获取该点覆盖的元素列表 */
    public List<ElementPainter> elementsAtPx(int x, int y) {
        FramePainter frame = gui.frame();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, false, hasBelow);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();
        return elementsInRect(frame, slotArea, x, y, 1, 1);
    }

    private List<ElementPainter> elementsInRect(FramePainter frame, FramePainter.SlotArea slotArea,
                                                int x, int y, int w, int h) {
        List<ElementPainter> out = new ArrayList<>();
        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            if (intersects(bounds.left(), bounds.top(), bounds.width(), bounds.height(), x, y, w, h)) {
                out.addAll(pad.elements());
            }
        }
        if (gui.hasBelow() && gui.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null && intersects(belowArea.left(), belowArea.top(), belowArea.width(), belowArea.height(), x, y, w, h)) {
                out.add(gui.below());
            }
        }
        return out;
    }

    private static boolean intersects(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        int ar = ax + aw;
        int ab = ay + ah;
        int br = bx + bw;
        int bb = by + bh;
        return ar > bx && br > ax && ab > by && bb > ay;
    }
}
