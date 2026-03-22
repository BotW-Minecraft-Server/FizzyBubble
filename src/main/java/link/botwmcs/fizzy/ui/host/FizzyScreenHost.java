package link.botwmcs.fizzy.ui.host;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FizzyScreenHost extends Screen {
    private final FizzyGui gui;
    private final List<ManagedWidget> managedWidgets = new ArrayList<>();
    private UiRuntime runtime;
    private int left;
    private int top;

    public FizzyScreenHost(FizzyGui gui) {
        super(Component.empty());
        this.gui = gui;
    }

    @Override
    protected void init() {
        super.init();
        if (runtime == null || runtime.isClosed()) {
            runtime = UiRuntime.createForCurrentThread();
        }
        recalcCenter();
        this.clearWidgets();
        this.managedWidgets.clear();
        initElements();
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        recalcCenter();
        this.clearWidgets();
        this.managedWidgets.clear();
        initElements();
    }

    private void recalcCenter() {
        int gw = gui.widthPx();
        int gh = gui.heightPx();
        this.left = (this.width - gw) / 2;
        this.top = (this.height - gh) / 2;
    }

    private void initElements() {
        FramePainter frame = gui.frame();
        int widthPx = gui.widthPx();
        int heightPx = gui.heightPx();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(left, top, widthPx, heightPx, true, hasBelow);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        ScreenInitContext context = new ScreenInitContext();
        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (ElementPainter element : pad.elements()) {
                context.runWithOwner(element, () ->
                        element.init(context, bounds.left(), bounds.top(), bounds.width(), bounds.height())
                );
            }
        }
        if (hasBelow && gui.below() != null) {
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
    public void render(GuiGraphics g, int mx, int my, float dt) {
        if (runtime != null) {
            runtime.frameTick();
        }

        FramePainter frame = gui.frame();
        int widthPx = gui.widthPx();
        int heightPx = gui.heightPx();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(left, top, widthPx, heightPx, true, hasBelow);
        FramePainter.SlotArea slotArea = frame.currentSlotArea();

        List<ElementPlacement> placements = collectElementPlacements(frame, slotArea);
        boolean suppressTooltips = shouldSuppressTooltips(placements);

        UiRenderTaskQueue queue = new UiRenderTaskQueue();
        BehindPainter behind = gui.behind();
        if (behind != null) {
            queue.add(UiRenderLayer.behind(0), () -> behind.paint(g, frame, dt));
        }

        BgPainter bg = gui.background();
        if (bg != null) {
            queue.add(UiRenderLayer.background(0), () -> bg.paint(g, frame));
        }

        queue.add(UiRenderLayer.frame(0), () -> frame.paint(g, left, top, widthPx, heightPx, true, hasBelow));

        for (ElementPlacement placement : placements) {
            queue.add(placement.element().layer(), () -> renderElement(g, placement, dt));
        }

        SplitPainter splitPainter = gui.splitPainter();
        if (slotArea != null && splitPainter != null) {
            for (SplitSpec split : gui.splits()) {
                queue.add(UiRenderLayer.split(0), () -> split.paint(g, splitPainter, slotArea));
            }
        }

        for (ManagedWidget widget : this.managedWidgets) {
            queue.add(widget.layer(), () -> renderManagedWidget(g, widget, mx, my, dt));
        }

        for (Renderable renderable : this.renderables) {
            queue.add(UiRenderLayer.widgets(Integer.MAX_VALUE), () -> renderable.render(g, mx, my, dt));
        }

        if (suppressTooltips) {
            FizzyTooltipElement.pushGlobalSuppression();
        }
        try {
            queue.renderAll();
        } finally {
            if (suppressTooltips) {
                FizzyTooltipElement.popGlobalSuppression();
            }
        }
        if (suppressTooltips) {
            clearTooltipForNextRenderPass();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (dispatchOverlayMouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dispatchOverlayMouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dispatchOverlayMouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (dispatchOverlayMouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
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

    public List<ElementPainter> elementsAtSlot(int row, int col) {
        FramePainter frame = gui.frame();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(left, top, gui.widthPx(), gui.heightPx(), true, hasBelow);
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
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(left, top, gui.widthPx(), gui.heightPx(), true, hasBelow);
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

    private List<ElementPlacement> collectElementPlacements(FramePainter frame, @Nullable FramePainter.SlotArea slotArea) {
        List<ElementPlacement> out = new ArrayList<>();
        int order = 0;

        for (PadSpec pad : gui.pads()) {
            PadSpec.PadBounds bounds = pad.resolve(frame, slotArea);
            for (ElementPainter element : pad.elements()) {
                out.add(new ElementPlacement(
                        element,
                        bounds.left(),
                        bounds.top(),
                        bounds.width(),
                        bounds.height(),
                        order++
                ));
            }
        }

        if (gui.hasBelow() && gui.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null) {
                out.add(new ElementPlacement(
                        gui.below(),
                        belowArea.left(),
                        belowArea.top(),
                        belowArea.width(),
                        belowArea.height(),
                        order
                ));
            }
        }
        return out;
    }

    private void renderElement(GuiGraphics g, ElementPlacement placement, float partialTick) {
        ElementPainter element = placement.element();
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, element.zIndex());
        try {
            element.render(
                    g,
                    placement.left(),
                    placement.top(),
                    placement.width(),
                    placement.height(),
                    partialTick
            );
        } finally {
            g.pose().popPose();
        }
    }

    private void renderManagedWidget(GuiGraphics g, ManagedWidget managedWidget, int mouseX, int mouseY, float partialTick) {
        AbstractWidget widget = managedWidget.widget();
        if (!widget.visible) {
            return;
        }
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, managedWidget.zIndex());
        try {
            widget.render(g, mouseX, mouseY, partialTick);
        } finally {
            g.pose().popPose();
        }
    }

    private UiRenderLayer resolveWidgetLayer(@Nullable ElementPainter owner, AbstractWidget widget) {
        if (FizzyTooltipElement.isTooltipWidget(widget)) {
            return UiRenderLayer.tooltip(0);
        }
        if (owner != null) {
            UiRenderLayer ownerLayer = owner.layer();
            UiRenderPhase ownerPhase = ownerLayer.phase();
            if (ownerPhase == UiRenderPhase.OVERLAY || ownerPhase == UiRenderPhase.TOOLTIP) {
                return ownerLayer;
            }
        }
        return UiRenderLayer.widgets(0);
    }

    private int resolveWidgetZIndex(@Nullable ElementPainter owner, AbstractWidget widget) {
        if (FizzyTooltipElement.isTooltipWidget(widget)) {
            return FizzyTooltipElement.defaultTooltipZIndex();
        }
        return owner != null ? owner.zIndex() : 0;
    }

    private List<AbstractWidget> overlayWidgets() {
        List<AbstractWidget> out = new ArrayList<>();
        for (ManagedWidget widget : this.managedWidgets) {
            if (widget.layer().phase() != UiRenderPhase.OVERLAY) {
                continue;
            }
            out.add(widget.widget());
        }
        return out;
    }

    private boolean dispatchOverlayMouseClicked(double mouseX, double mouseY, int button) {
        List<AbstractWidget> widgets = overlayWidgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = widgets.get(i);
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    private boolean dispatchOverlayMouseReleased(double mouseX, double mouseY, int button) {
        List<AbstractWidget> widgets = overlayWidgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = widgets.get(i);
            if (!widget.visible) {
                continue;
            }
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    private boolean dispatchOverlayMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        List<AbstractWidget> widgets = overlayWidgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = widgets.get(i);
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    private boolean dispatchOverlayMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<AbstractWidget> widgets = overlayWidgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = widgets.get(i);
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldSuppressTooltips(List<ElementPlacement> placements) {
        for (ElementPlacement placement : placements) {
            if (placement.element().suppressesTooltips()) {
                return true;
            }
        }
        return false;
    }

    private class ScreenInitContext implements ElementPainter.InitContext {
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
        public <T extends AbstractWidget> T addRenderableWidget(T widget) {
            UiRenderLayer layer = resolveWidgetLayer(this.currentOwner, widget);
            int zIndex = resolveWidgetZIndex(this.currentOwner, widget);
            managedWidgets.add(new ManagedWidget(widget, layer, zIndex));
            return FizzyScreenHost.this.addWidget(widget);
        }
    }

    private record ManagedWidget(AbstractWidget widget, UiRenderLayer layer, int zIndex) {
    }

    private record ElementPlacement(ElementPainter element, int left, int top, int width, int height, int order) {
    }
}
