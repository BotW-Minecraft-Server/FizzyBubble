package link.botwmcs.fizzy.ui.host;

import link.botwmcs.fizzy.client.util.HostRenderSupport;
import link.botwmcs.fizzy.client.util.HostRenderSupport.ElementPlacement;
import link.botwmcs.fizzy.client.util.HostRenderSupport.ManagedWidget;
import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.component.FizzyTooltipElement;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderLayer;
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
import java.util.List;

public class FizzyScreenHost extends Screen {
    private final FizzyGui gui;
    private final List<ManagedWidget> managedWidgets = new ArrayList<>();
    private UiRuntime runtime;
    private int left;
    private int top;
    private int nextManagedWidgetSerial;

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
        this.nextManagedWidgetSerial = 0;
        initElements();
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        recalcCenter();
        this.clearWidgets();
        this.managedWidgets.clear();
        this.nextManagedWidgetSerial = 0;
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

        List<ElementPlacement> placements = HostRenderSupport.collectElementPlacements(gui, frame, slotArea);
        boolean suppressTooltips = HostRenderSupport.shouldSuppressTooltips(placements);

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
            queue.add(placement.element().layer(), () -> HostRenderSupport.renderElement(g, placement, dt));
        }

        SplitPainter splitPainter = gui.splitPainter();
        if (slotArea != null && splitPainter != null) {
            for (SplitSpec split : gui.splits()) {
                queue.add(UiRenderLayer.split(0), () -> split.paint(g, splitPainter, slotArea));
            }
        }

        for (ManagedWidget widget : this.managedWidgets) {
            queue.add(widget.layer(), () -> HostRenderSupport.renderManagedWidget(g, widget, mx, my, dt));
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
        if (HostRenderSupport.dispatchOverlayMouseClicked(this.managedWidgets, mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (HostRenderSupport.dispatchOverlayMouseReleased(this.managedWidgets, mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (HostRenderSupport.dispatchOverlayMouseDragged(this.managedWidgets, mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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

    public List<ElementPainter> elementsAtSlot(int row, int col) {
        FramePainter frame = gui.frame();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(left, top, gui.widthPx(), gui.heightPx(), true, hasBelow);
        return HostRenderSupport.elementsAtSlot(gui, frame, row, col);
    }

    public List<ElementPainter> elementsAtPx(int x, int y) {
        FramePainter frame = gui.frame();
        boolean hasBelow = gui.hasBelow();
        frame.setLayout(left, top, gui.widthPx(), gui.heightPx(), true, hasBelow);
        return HostRenderSupport.elementsAtPixel(gui, frame, x, y);
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
            UiRenderLayer layer = HostRenderSupport.resolveWidgetLayer(this.currentOwner, widget);
            int zIndex = HostRenderSupport.resolveWidgetZIndex(this.currentOwner, widget);
            managedWidgets.add(new ManagedWidget(widget, layer, zIndex, nextManagedWidgetSerial++));
            return FizzyScreenHost.this.addWidget(widget);
        }
    }
}
