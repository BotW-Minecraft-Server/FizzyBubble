package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.client.util.HostRenderSupport;
import link.botwmcs.fizzy.client.util.HostRenderSupport.ElementPlacement;
import link.botwmcs.fizzy.client.util.HostRenderSupport.ManagedWidget;
import link.botwmcs.fizzy.menu.FizzyMenuLayout;
import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.component.FizzyTooltipElement;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderLayer;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderPhase;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderTaskQueue;
import link.botwmcs.fizzy.ui.kernel.runtime.UiRuntime;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import link.botwmcs.fizzy.ui.split.SplitPainter;
import link.botwmcs.fizzy.ui.split.SplitSpec;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FizzyMenuScreenHost<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final Identifier GENERIC_54 = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int GENERIC_TEX_SIZE = 256;
    private static final int PLAYER_INV_TOP_TRIM = 13;
    private static final int PLAYER_INV_SECTION_V = 126 + PLAYER_INV_TOP_TRIM;
    private static final int PLAYER_INV_SECTION_W = 176;
    private static final int PLAYER_INV_SECTION_H = UiUnit.VANILLA_PLAYER_INV_HEIGHT - PLAYER_INV_TOP_TRIM;
    private static final int PLAYER_INV_FIRST_SLOT_INDEX_FROM_END = 36;
    private static final int HOTBAR_FIRST_SLOT_INDEX_FROM_END = 9;

    private final FizzyGui gui;
    private final List<ManagedWidget> managedWidgets = new ArrayList<>();
    private UiRuntime runtime;
    private int nextManagedWidgetSerial;

    public FizzyMenuScreenHost(T menu, Inventory inv, Component title, FizzyGui gui) {
        super(menu, inv, title, gui.widthPx(), gui.heightPx());
        this.gui = gui;
    }

    @Override
    protected void init() {
        super.init();
        if (runtime == null || runtime.isClosed()) {
            runtime = UiRuntime.createForCurrentThread();
        }

        int totalH = this.imageHeight + PLAYER_INV_SECTION_H;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - totalH) / 2;
        this.clearWidgets();
        this.managedWidgets.clear();
        this.nextManagedWidgetSerial = 0;
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
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (ElementPainter element : pad.elements()) {
                context.runWithOwner(element, () ->
                        element.init(context, bounds.left(), bounds.top(), bounds.width(), bounds.height())
                );
            }
        }
        if (gui.hasBelow() && gui.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null) {
                ElementPainter below = gui.below();
                context.runWithOwner(below, () ->
                        below.init(context, belowArea.left(), belowArea.top(), belowArea.width(), belowArea.height())
                );
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        if (runtime != null) {
            runtime.frameTick();
        }

        FramePainter frame = gui.frame();
        boolean hasBelowBand = true;
        boolean drawBottomEdge = false;
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        List<ElementPlacement> placements = HostRenderSupport.collectElementPlacements(gui, frame, slotArea);
        boolean suppressTooltips = HostRenderSupport.shouldSuppressTooltips(placements);
        UiRenderTaskQueue queue = new UiRenderTaskQueue();

        BehindPainter behind = gui.behind();
        if (behind != null) {
            queue.add(UiRenderLayer.behind(0), () -> behind.paint(g, frame, partialTick));
        }

        BgPainter bg = gui.background();
        if (bg != null) {
            queue.add(UiRenderLayer.background(0), () -> bg.paint(g, frame));
        }

        queue.add(UiRenderLayer.background(100), () -> drawPlayerInventoryBackground(g));
        queue.add(UiRenderLayer.frame(0), () -> frame.paint(g, leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand));

        for (ElementPlacement placement : placements) {
            queue.add(placement.element().layer(), () -> HostRenderSupport.renderElement(g, placement, partialTick));
        }

        SplitPainter splitPainter = gui.splitPainter();
        if (slotArea != null && splitPainter != null) {
            for (SplitSpec split : gui.splits()) {
                queue.add(UiRenderLayer.split(0), () -> split.paint(g, splitPainter, slotArea));
            }
        }

        for (ManagedWidget widget : this.managedWidgets) {
            queue.add(widget.layer(), () -> HostRenderSupport.renderManagedWidget(g, widget, mouseX, mouseY, partialTick));
        }

        if (suppressTooltips) {
            FizzyTooltipElement.pushGlobalSuppression();
        }
        try {
            queue.renderMatching(phase -> phase.ordinal() <= UiRenderPhase.FRAME.ordinal());
            renderCustomMenuBackground(g, partialTick, mouseX, mouseY);
            queue.renderMatching(phase ->
                    phase.ordinal() > UiRenderPhase.FRAME.ordinal()
                            && phase.ordinal() <= UiRenderPhase.WIDGET.ordinal()
            );
            super.extractContents(g, mouseX, mouseY, partialTick);
            queue.renderMatching(phase -> phase == UiRenderPhase.TOOLTIP || phase == UiRenderPhase.OVERLAY);
            super.extractCarriedItem(g, mouseX, mouseY);
            super.extractSnapbackItem(g);
            if (!suppressTooltips) {
                super.extractTooltip(g, mouseX, mouseY);
            }
        } finally {
            if (suppressTooltips) {
                FizzyTooltipElement.popGlobalSuppression();
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int x, int y) {
        renderCustomMenuForeground(g, x, y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (HostRenderSupport.dispatchOverlayMouseClicked(this.managedWidgets, event, doubleClick)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (HostRenderSupport.dispatchOverlayMouseReleased(this.managedWidgets, event)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (HostRenderSupport.dispatchOverlayMouseDragged(this.managedWidgets, event, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (HostRenderSupport.dispatchOverlayMouseScrolled(this.managedWidgets, mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void removed() {
        if (runtime != null) {
            runtime.close();
            runtime = null;
        }
        super.removed();
    }

    protected void renderCustomMenuBackground(GuiGraphicsExtractor g, float partialTick, int mouseX, int mouseY) {
    }

    protected void renderCustomMenuForeground(GuiGraphicsExtractor g, int mouseX, int mouseY) {
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

    private void drawPlayerInventoryBackground(GuiGraphicsExtractor g) {
        g.blit(
                RenderPipelines.GUI_TEXTURED,
                GENERIC_54,
                playerInventoryBackgroundLeft(),
                playerInventoryBackgroundTop(),
                0.0f,
                (float) PLAYER_INV_SECTION_V,
                PLAYER_INV_SECTION_W,
                PLAYER_INV_SECTION_H,
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

    public List<ElementPainter> elementsAtSlot(int row, int col) {
        FramePainter frame = gui.frame();
        boolean hasBelowBand = true;
        boolean drawBottomEdge = false;
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);
        return HostRenderSupport.elementsAtSlot(gui, frame, row, col);
    }

    public List<ElementPainter> elementsAtPx(int x, int y) {
        FramePainter frame = gui.frame();
        boolean hasBelowBand = true;
        boolean drawBottomEdge = false;
        frame.setLayout(leftPos, topPos, imageWidth, imageHeight, drawBottomEdge, hasBelowBand);
        return HostRenderSupport.elementsAtPixel(gui, frame, x, y);
    }

    private class MenuInitContext implements ElementPainter.InitContext {
        private @Nullable ElementPainter currentOwner;

        private void runWithOwner(ElementPainter owner, Runnable action) {
            ElementPainter previous = this.currentOwner;
            this.currentOwner = owner;
            try {
                action.run();
            } finally {
                this.currentOwner = previous;
            }
        }

        @Override
        public <W extends AbstractWidget> W addRenderableWidget(W widget) {
            UiRenderLayer layer = HostRenderSupport.resolveWidgetLayer(this.currentOwner, widget);
            int zIndex = HostRenderSupport.resolveWidgetZIndex(this.currentOwner, widget);
            managedWidgets.add(new ManagedWidget(widget, layer, zIndex, nextManagedWidgetSerial++));
            return FizzyMenuScreenHost.this.addWidget(widget);
        }
    }

    private record PlayerInventoryAnchors(int firstInventorySlotX, int firstInventorySlotY, int firstHotbarSlotY) {
    }
}
