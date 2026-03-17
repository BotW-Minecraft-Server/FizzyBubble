package link.botwmcs.fizzy.ui.host;

import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.menu.FizzyMenuLayout;
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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FizzyMenuScreenHost<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation GENERIC_54 = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int GENERIC_TEX_SIZE = 256;
    private static final int PLAYER_INV_TOP_TRIM = 13;
    private static final int PLAYER_INV_SECTION_V = 126 + PLAYER_INV_TOP_TRIM;
    private static final int PLAYER_INV_SECTION_W = 176;
    private static final int PLAYER_INV_SECTION_H = UiUnit.VANILLA_PLAYER_INV_HEIGHT - PLAYER_INV_TOP_TRIM;
    private static final int PLAYER_INV_FIRST_SLOT_INDEX_FROM_END = 36;
    private static final int HOTBAR_FIRST_SLOT_INDEX_FROM_END = 9;
    private final FizzyGui gui;

    public FizzyMenuScreenHost(T menu, Inventory inv, Component title, FizzyGui gui) {
        super(menu, inv, title);
        this.gui = gui;
    }

    @Override
    protected void init() {
        this.imageWidth = gui.widthPx();
        this.imageHeight = gui.heightPx();
        super.init();

        int totalH = this.imageHeight + PLAYER_INV_SECTION_H;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - totalH) / 2;
        this.clearWidgets();
        initElements();

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = playerInventorySlotLeft() - this.leftPos;
        this.inventoryLabelY = playerInventorySlotTop() - this.topPos - 10;
    }

    private void initElements() {
        FramePainter frame = gui.frame();
        boolean hasBelowBand = true;
        boolean drawBottomEdge = false;
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        MenuInitContext context = new MenuInitContext();
        for (PadSpec pad : gui.pads()) {
            for (var element : pad.elements()) {
                PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
                element.init(context, bounds.left(), bounds.top(), bounds.width(), bounds.height());
            }
        }
        if (gui.hasBelow() && gui.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null) {
                gui.below().init(context, belowArea.left(), belowArea.top(), belowArea.width(), belowArea.height());
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Disable vanilla container dim/blur background.
        // Menu background effects are controlled explicitly via BehindPainter.
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderBg(g, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Ensure blit color state stays valid even if another renderer changed it.
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        FramePainter frame = gui.frame();
        boolean hasBelowBand = true;
        boolean drawBottomEdge = false;
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        BehindPainter behind = gui.behind();
        if (behind != null) {
            behind.paint(g, frame, partialTick);
        }

        BgPainter bg = gui.background();
        if (bg != null) {
            bg.paint(g, frame);
        }

        drawPlayerInventoryBackground(g);
        // Draw frame before elements so elements stay on top.
        frame.paint(g, leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);

        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (var element : pad.elements()) {
                element.render(g, bounds.left(), bounds.top(), bounds.width(), bounds.height(), partialTick);
            }
        }

        if (gui.hasBelow() && gui.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null) {
                gui.below().render(g, belowArea.left(), belowArea.top(), belowArea.width(), belowArea.height(), partialTick);
            }
        }

        SplitPainter splitPainter = gui.splitPainter();
        if (slotArea != null && splitPainter != null) {
            for (SplitSpec split : gui.splits()) {
                split.paint(g, splitPainter, slotArea);
            }
        }

//        renderCustomMenuBackground(g, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Intentionally skip AbstractContainerScreen labels (left gray title/inventory text).
        // Frame title is rendered by FramePainter and custom foreground is rendered below.
//        renderCustomMenuForeground(g, mouseX, mouseY);
    }

    protected void renderCustomMenuBackground(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
    }

    protected void renderCustomMenuForeground(GuiGraphics g, int mouseX, int mouseY) {
    }

    protected int playerInventorySlotLeft() {
        return this.leftPos + resolvePlayerInventoryAnchors().firstInventorySlotX();
    }

    protected int playerInventorySlotTop() {
        return this.topPos + resolvePlayerInventoryAnchors().firstInventorySlotY();
    }

    protected int playerHotbarSlotTop() {
        return this.topPos + resolvePlayerInventoryAnchors().firstHotbarSlotY();
    }

    private int playerInventoryBackgroundLeft() {
        return playerInventorySlotLeft() - 8;
    }

    private int playerInventoryBackgroundTop() {
        return this.topPos + this.imageHeight;
    }

    private void drawPlayerInventoryBackground(GuiGraphics g) {
        g.blit(
                GENERIC_54,
                playerInventoryBackgroundLeft(),
                playerInventoryBackgroundTop(),
                0,
                PLAYER_INV_SECTION_V,
                PLAYER_INV_SECTION_W,
                PLAYER_INV_SECTION_H,
                GENERIC_TEX_SIZE,
                GENERIC_TEX_SIZE
        );
    }

    private PlayerInventoryAnchors resolvePlayerInventoryAnchors() {
        int fallbackInvX = FizzyMenuLayout.playerInvSlotX(0);
        int fallbackInvY = FizzyMenuLayout.playerInvSlotY(this.imageHeight, 0);
        int fallbackHotbarY = FizzyMenuLayout.hotbarSlotY(this.imageHeight);

        Slot firstInventorySlot = slotFromEnd(PLAYER_INV_FIRST_SLOT_INDEX_FROM_END);
        Slot firstHotbarSlot = slotFromEnd(HOTBAR_FIRST_SLOT_INDEX_FROM_END);
        if (firstInventorySlot == null || firstHotbarSlot == null) {
            return new PlayerInventoryAnchors(fallbackInvX, fallbackInvY, fallbackHotbarY);
        }
        return new PlayerInventoryAnchors(firstInventorySlot.x, firstInventorySlot.y, firstHotbarSlot.y);
    }

    private Slot slotFromEnd(int fromEnd) {
        int index = this.menu.slots.size() - fromEnd;
        if (index < 0 || index >= this.menu.slots.size()) {
            return null;
        }
        return this.menu.slots.get(index);
    }

    private class MenuInitContext implements ElementPainter.InitContext {
        @Override
        public <W extends AbstractWidget> W addRenderableWidget(W widget) {
            return FizzyMenuScreenHost.this.addRenderableWidget(widget);
        }
    }

    public List<ElementPainter> elementsAtSlot(int row, int col) {
        FramePainter frame = gui.frame();
        boolean hasBelowBand = true;
        boolean drawBottomEdge = false;
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();
        if (slotArea == null) {
            return Collections.emptyList();
        }

        int slotX = slotArea.x() + (col - 1) * UiUnit.SLOT_PX;
        int slotY = slotArea.y() + (row - 1) * UiUnit.SLOT_PX;
        return elementsInRect(frame, slotArea, slotX, slotY, UiUnit.SLOT_PX, UiUnit.SLOT_PX);
    }

    public List<ElementPainter> elementsAtPx(int x, int y) {
        FramePainter frame = gui.frame();
        boolean hasBelowBand = true;
        boolean drawBottomEdge = false;
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);
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

    private record PlayerInventoryAnchors(int firstInventorySlotX, int firstInventorySlotY, int firstHotbarSlotY) {
    }
}
