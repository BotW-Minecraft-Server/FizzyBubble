package link.botwmcs.fizzy.proxy.runtime;

import link.botwmcs.fizzy.client.util.HostRenderSupport;
import link.botwmcs.fizzy.client.util.HostRenderSupport.ElementPlacement;
import link.botwmcs.fizzy.client.util.HostRenderSupport.ManagedWidget;
import link.botwmcs.fizzy.proxy.api.DefaultPhaseBridgePolicy;
import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.api.HostStageCapabilities;
import link.botwmcs.fizzy.proxy.api.InputDispatchPolicy;
import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import link.botwmcs.fizzy.proxy.api.KernelUiSpec;
import link.botwmcs.fizzy.proxy.api.PhaseBridgePolicy;
import link.botwmcs.fizzy.proxy.api.TooltipPolicy;
import link.botwmcs.fizzy.proxy.host.HostAdapter;
import link.botwmcs.fizzy.proxy.host.HostGeometry;
import link.botwmcs.fizzy.proxy.host.SlotGridGeometry;
import link.botwmcs.fizzy.ui.background.BgPainter;
import link.botwmcs.fizzy.ui.behind.BehindPainter;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FrameMetrics;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderLayer;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderPhase;
import link.botwmcs.fizzy.ui.kernel.runtime.UiRuntime;
import link.botwmcs.fizzy.ui.pad.PadResolutionSupport;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import link.botwmcs.fizzy.ui.split.SplitPainter;
import link.botwmcs.fizzy.ui.split.SplitSpec;
import link.botwmcs.fizzy.ui.core.UiUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ScreenProxySession implements AutoCloseable {
    private static final Comparator<StagedTask> TASK_ORDER = Comparator
            .comparing(StagedTask::layer)
            .thenComparingInt(StagedTask::zIndex)
            .thenComparingInt(StagedTask::serial);
    private static final Comparator<ManagedWidget> WIDGET_RENDER_ORDER = Comparator
            .comparing(ManagedWidget::layer)
            .thenComparingInt(ManagedWidget::zIndex)
            .thenComparingInt(ManagedWidget::serial);

    private final Screen sourceScreen;
    private final HostAdapter hostAdapter;
    private final KernelAttachSpec spec;
    private final HostStageCapabilities stageCapabilities;
    private final List<ManagedWidget> managedWidgets = new ArrayList<>();
    private final SessionInitContext initContext = new SessionInitContext();
    private UiRuntime runtime;
    private RenderState lastRenderState;
    private int nextManagedWidgetSerial;
    private final List<HostRenderStage> renderedStages = new ArrayList<>();
    private boolean initialized;
    private boolean closed;

    public ScreenProxySession(Screen sourceScreen, HostAdapter hostAdapter, KernelAttachSpec spec) {
        this.sourceScreen = Objects.requireNonNull(sourceScreen, "sourceScreen");
        this.hostAdapter = Objects.requireNonNull(hostAdapter, "hostAdapter");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.stageCapabilities = hostAdapter.stageCapabilities(sourceScreen);
    }

    public Screen sourceScreen() {
        return sourceScreen;
    }

    public HostAdapter hostAdapter() {
        return hostAdapter;
    }

    public KernelAttachSpec spec() {
        return spec;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isClosed() {
        return closed;
    }

    public List<HostRenderStage> renderedStages() {
        return List.copyOf(renderedStages);
    }

    public void init() {
        if (closed) {
            return;
        }
        if (runtime == null || runtime.isClosed()) {
            runtime = UiRuntime.createForCurrentThread();
        }
        this.managedWidgets.clear();
        this.nextManagedWidgetSerial = 0;
        this.lastRenderState = buildRenderState();
        initElements(this.lastRenderState);
        initialized = true;
        renderedStages.clear();
    }

    public void resize() {
        if (closed || !initialized) {
            return;
        }
        this.managedWidgets.clear();
        this.nextManagedWidgetSerial = 0;
        this.lastRenderState = buildRenderState();
        initElements(this.lastRenderState);
        renderedStages.clear();
    }

    public void renderStage(HostRenderStage stage, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (closed || !initialized) {
            return;
        }
        if (!stageCapabilities.supports(stage)) {
            return;
        }
        if (runtime != null) {
            runtime.frameTick();
        }

        RenderState state = buildRenderState();
        this.lastRenderState = state;
        renderStageTasks(stage, state, graphics, mouseX, mouseY, partialTick);
        renderedStages.add(stage);
    }

    public boolean mouseClicked(double x, double y, int button) {
        if (closed || !initialized) {
            return false;
        }
        InputDispatchPolicy policy = effectiveInputPolicy();
        if (policy.overlayFirst()) {
            if (dispatchMouseClicked(filterWidgetsByPhase(true), x, y, button)) {
                return true;
            }
            if (dispatchMouseClicked(filterWidgetsByPhase(false), x, y, button)) {
                return true;
            }
        } else if (dispatchMouseClicked(sortedWidgetsTopDown(), x, y, button)) {
            return true;
        }
        return policy.blockSourceWhenHitBlockingElement() && blocksInputAt(x, y);
    }

    public boolean mouseReleased(double x, double y, int button) {
        if (closed || !initialized) {
            return false;
        }
        InputDispatchPolicy policy = effectiveInputPolicy();
        if (policy.overlayFirst()) {
            if (dispatchMouseReleased(filterWidgetsByPhase(true), x, y, button)) {
                return true;
            }
            if (dispatchMouseReleased(filterWidgetsByPhase(false), x, y, button)) {
                return true;
            }
        } else if (dispatchMouseReleased(sortedWidgetsTopDown(), x, y, button)) {
            return true;
        }
        return policy.blockSourceWhenHitBlockingElement() && blocksInputAt(x, y);
    }

    public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        if (closed || !initialized) {
            return false;
        }
        InputDispatchPolicy policy = effectiveInputPolicy();
        if (policy.overlayFirst()) {
            if (dispatchMouseDragged(filterWidgetsByPhase(true), x, y, button, dragX, dragY)) {
                return true;
            }
            if (dispatchMouseDragged(filterWidgetsByPhase(false), x, y, button, dragX, dragY)) {
                return true;
            }
        } else if (dispatchMouseDragged(sortedWidgetsTopDown(), x, y, button, dragX, dragY)) {
            return true;
        }
        return policy.blockSourceWhenHitBlockingElement() && blocksInputAt(x, y);
    }

    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (closed || !initialized) {
            return false;
        }
        InputDispatchPolicy policy = effectiveInputPolicy();
        if (policy.overlayFirst()) {
            if (dispatchMouseScrolled(filterWidgetsByPhase(true), x, y, scrollX, scrollY)) {
                return true;
            }
            if (dispatchMouseScrolled(filterWidgetsByPhase(false), x, y, scrollX, scrollY)) {
                return true;
            }
        } else if (dispatchMouseScrolled(sortedWidgetsTopDown(), x, y, scrollX, scrollY)) {
            return true;
        }
        return policy.blockSourceWhenHitBlockingElement() && blocksInputAt(x, y);
    }

    public boolean shouldCancelSourceTooltip(double mouseX, double mouseY) {
        if (closed || !initialized) {
            return false;
        }
        TooltipPolicy policy = effectiveTooltipPolicy();
        if (policy == TooltipPolicy.SOURCE_ONLY) {
            return false;
        }
        if (policy == TooltipPolicy.FIZZY_ONLY) {
            return true;
        }
        if (policy == TooltipPolicy.AUTO_SUPPRESS_SOURCE_WHEN_BLOCKING) {
            return suppressesSourceTooltipAt(mouseX, mouseY);
        }
        return false;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        if (runtime != null) {
            runtime.close();
            runtime = null;
        }
        closed = true;
        initialized = false;
        managedWidgets.clear();
        lastRenderState = null;
        renderedStages.clear();
    }

    private void initElements(RenderState state) {
        for (ElementPlacement placement : state.placements()) {
            ElementPainter element = placement.element();
            initContext.runWithOwner(element, () ->
                    element.init(
                            initContext,
                            placement.left(),
                            placement.top(),
                            placement.width(),
                            placement.height()
                    )
            );
        }
    }

    private RenderState buildRenderState() {
        HostGeometry geometry = hostAdapter.resolveGeometry(sourceScreen);
        KernelState kernel = resolveKernelState(geometry);

        boolean drawBottomEdge = false;
        boolean hasBelow = kernel.below() != null;
        int layoutLeft = kernel.layoutLeft();
        int layoutTop = kernel.layoutTop();
        int layoutWidth = kernel.layoutWidth();
        int layoutHeight = kernel.layoutHeight();

        kernel.frame().setLayout(layoutLeft, layoutTop, layoutWidth, layoutHeight, drawBottomEdge, hasBelow);

        FramePainter.SlotArea slotArea = resolveSlotArea(kernel.frame(), geometry);
        List<ElementPlacement> placements = collectPlacements(kernel, kernel.frame(), slotArea);
        return new RenderState(
                geometry,
                kernel,
                drawBottomEdge,
                hasBelow,
                slotArea,
                placements
        );
    }

    private KernelState resolveKernelState(HostGeometry geometry) {
        KernelUiSpec ui = spec.uiSpec();
        var base = ui.baseKernel();

        FramePainter frame = firstNonNull(
                ui.frame(),
                base != null ? base.frame() : null,
                new ProxyFramePainter(Math.max(1, geometry.rootWidth()))
        );
        BgPainter background = firstNonNull(
                ui.background(),
                base != null ? base.background() : null,
                null
        );
        BehindPainter behind = firstNonNull(
                ui.behind(),
                base != null ? base.behind() : null,
                null
        );
        SplitPainter splitPainter = firstNonNull(
                ui.splitPainter(),
                base != null ? base.splitPainter() : null,
                null
        );
        ElementPainter below = firstNonNull(
                ui.below(),
                base != null ? base.below() : null,
                null
        );

        List<PadSpec> pads = new ArrayList<>();
        if (base != null) {
            pads.addAll(base.pads());
        }
        pads.addAll(ui.pads());

        List<SplitSpec> splits = new ArrayList<>();
        if (base != null) {
            splits.addAll(base.splits());
        }
        splits.addAll(ui.splits());

        int layoutWidth = firstPositive(
                ui.overrideWidthPx(),
                base != null ? base.widthPx() : null,
                frame.metrics().panelW(),
                geometry.rootWidth()
        );
        int layoutHeight = firstPositive(
                ui.overrideHeightPx(),
                base != null ? base.heightPx() : null,
                defaultHeight(frame, geometry, below != null),
                geometry.rootHeight()
        );

        int layoutLeft = geometry.rootLeft();
        int layoutTop = geometry.rootTop();
        SlotGridGeometry slotGrid = geometry.slotGrid();
        if (slotGrid != null) {
            layoutLeft = slotGrid.x() - frame.metrics().slotStartLeftPx();
            layoutTop = slotGrid.y() - frame.metrics().slotStartTopPx();
        }

        return new KernelState(
                frame,
                background,
                behind,
                splitPainter,
                below,
                List.copyOf(pads),
                List.copyOf(splits),
                layoutLeft,
                layoutTop,
                Math.max(1, layoutWidth),
                Math.max(1, layoutHeight)
        );
    }

    private static int defaultHeight(FramePainter frame, HostGeometry geometry, boolean hasBelow) {
        SlotGridGeometry slotGrid = geometry.slotGrid();
        if (slotGrid != null) {
            return frame.metrics().totalHeightForRows(Math.max(1, slotGrid.rows()), false, hasBelow);
        }
        return Math.max(1, geometry.rootHeight());
    }

    private static FramePainter.SlotArea resolveSlotArea(FramePainter frame, HostGeometry geometry) {
        SlotGridGeometry slotGrid = geometry.slotGrid();
        if (slotGrid != null) {
            int size = Math.max(1, slotGrid.slotSizePx());
            return new FramePainter.SlotArea(
                    slotGrid.x(),
                    slotGrid.y(),
                    Math.max(0, slotGrid.cols()) * size,
                    Math.max(0, slotGrid.rows()) * size
            );
        }
        return frame.currentSlotArea();
    }

    private static List<ElementPlacement> collectPlacements(
            KernelState kernel,
            FramePainter frame,
            FramePainter.SlotArea slotArea
    ) {
        List<ElementPlacement> out = new ArrayList<>();
        int order = 0;
        for (PadSpec pad : kernel.pads()) {
            PadSpec.PadBounds bounds = PadResolutionSupport.resolvePadBounds(pad, frame, slotArea);
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

        if (kernel.below() != null) {
            FramePainter.BelowArea belowArea = frame.currentBelowArea();
            if (belowArea != null) {
                out.add(new ElementPlacement(
                        kernel.below(),
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

    private void renderStageTasks(
            HostRenderStage stage,
            RenderState state,
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        List<StagedTask> tasks = new ArrayList<>();
        int serial = 0;
        KernelState kernel = state.kernel();
        FramePainter frame = kernel.frame();

        if (kernel.behind() != null) {
            tasks.add(new StagedTask(
                    UiRenderLayer.behind(0),
                    0,
                    serial++,
                    () -> kernel.behind().paint(graphics, frame, partialTick)
            ));
        }
        if (kernel.background() != null) {
            tasks.add(new StagedTask(
                    UiRenderLayer.background(0),
                    0,
                    serial++,
                    () -> kernel.background().paint(graphics, frame)
            ));
        }

        tasks.add(new StagedTask(
                UiRenderLayer.frame(0),
                0,
                serial++,
                () -> frame.paint(
                        graphics,
                        frame.layout().left(),
                        frame.layout().top(),
                        frame.layout().w(),
                        frame.layout().h(),
                        state.drawBottomEdge(),
                        state.hasBelow()
                )
        ));

        for (ElementPlacement placement : state.placements()) {
            tasks.add(new StagedTask(
                    placement.element().layer(),
                    placement.element().zIndex(),
                    serial++,
                    () -> HostRenderSupport.renderElement(graphics, placement, partialTick)
            ));
        }

        if (state.slotArea() != null && kernel.splitPainter() != null) {
            for (SplitSpec split : kernel.splits()) {
                tasks.add(new StagedTask(
                        UiRenderLayer.split(0),
                        0,
                        serial++,
                        () -> split.paint(graphics, kernel.splitPainter(), state.slotArea())
                ));
            }
        }

        for (ManagedWidget widget : managedWidgetsSortedForRender()) {
            tasks.add(new StagedTask(
                    widget.layer(),
                    widget.zIndex(),
                    serial++,
                    () -> HostRenderSupport.renderManagedWidget(graphics, widget, mouseX, mouseY, partialTick)
            ));
        }

        tasks.sort(TASK_ORDER);
        for (StagedTask task : tasks) {
            UiRenderPhase phase = task.layer().phase();
            if (phase == UiRenderPhase.TOOLTIP && effectiveTooltipPolicy() == TooltipPolicy.SOURCE_ONLY) {
                continue;
            }
            HostRenderStage mappedStage = mapStage(phase);
            if (mappedStage != stage) {
                continue;
            }
            task.action().run();
        }
    }

    private HostRenderStage mapStage(UiRenderPhase phase) {
        PhaseBridgePolicy bridgePolicy = spec.phasePolicy();
        if (bridgePolicy == null) {
            bridgePolicy = DefaultPhaseBridgePolicy.INSTANCE;
        }
        return bridgePolicy.map(phase, stageCapabilities);
    }

    private TooltipPolicy effectiveTooltipPolicy() {
        TooltipPolicy tooltipPolicy = spec.tooltipPolicy();
        return tooltipPolicy != null ? tooltipPolicy : TooltipPolicy.AUTO_SUPPRESS_SOURCE_WHEN_BLOCKING;
    }

    private InputDispatchPolicy effectiveInputPolicy() {
        InputDispatchPolicy inputPolicy = spec.inputPolicy();
        return inputPolicy != null ? inputPolicy : InputDispatchPolicy.defaults();
    }

    private List<ManagedWidget> managedWidgetsSortedForRender() {
        if (managedWidgets.isEmpty()) {
            return List.of();
        }
        List<ManagedWidget> out = new ArrayList<>(managedWidgets);
        out.sort(WIDGET_RENDER_ORDER);
        return out;
    }

    private List<ManagedWidget> sortedWidgetsTopDown() {
        if (managedWidgets.isEmpty()) {
            return List.of();
        }
        List<ManagedWidget> out = new ArrayList<>(managedWidgets);
        out.sort(WIDGET_RENDER_ORDER);
        List<ManagedWidget> reversed = new ArrayList<>(out.size());
        for (int i = out.size() - 1; i >= 0; i--) {
            reversed.add(out.get(i));
        }
        return reversed;
    }

    private List<ManagedWidget> filterWidgetsByPhase(boolean overlayLike) {
        List<ManagedWidget> all = sortedWidgetsTopDown();
        if (all.isEmpty()) {
            return List.of();
        }
        List<ManagedWidget> out = new ArrayList<>();
        for (ManagedWidget widget : all) {
            UiRenderPhase phase = widget.layer().phase();
            boolean isOverlayLike = phase == UiRenderPhase.OVERLAY || phase == UiRenderPhase.TOOLTIP;
            if (isOverlayLike == overlayLike) {
                out.add(widget);
            }
        }
        return out;
    }

    private static boolean dispatchMouseClicked(List<ManagedWidget> widgets, double x, double y, int button) {
        for (ManagedWidget managed : widgets) {
            AbstractWidget widget = managed.widget();
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseClicked(x, y, button)) {
                return true;
            }
        }
        return false;
    }

    private static boolean dispatchMouseReleased(List<ManagedWidget> widgets, double x, double y, int button) {
        for (ManagedWidget managed : widgets) {
            AbstractWidget widget = managed.widget();
            if (!widget.visible) {
                continue;
            }
            if (widget.mouseReleased(x, y, button)) {
                return true;
            }
        }
        return false;
    }

    private static boolean dispatchMouseDragged(List<ManagedWidget> widgets, double x, double y, int button, double dragX, double dragY) {
        for (ManagedWidget managed : widgets) {
            AbstractWidget widget = managed.widget();
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseDragged(x, y, button, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    private static boolean dispatchMouseScrolled(List<ManagedWidget> widgets, double x, double y, double scrollX, double scrollY) {
        for (ManagedWidget managed : widgets) {
            AbstractWidget widget = managed.widget();
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseScrolled(x, y, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
    }

    private boolean blocksInputAt(double mouseX, double mouseY) {
        RenderState state = lastRenderState != null ? lastRenderState : buildRenderState();

        for (ManagedWidget managedWidget : sortedWidgetsTopDown()) {
            UiRenderPhase phase = managedWidget.layer().phase();
            if (phase != UiRenderPhase.OVERLAY && phase != UiRenderPhase.TOOLTIP) {
                continue;
            }
            AbstractWidget widget = managedWidget.widget();
            if (!widget.visible) {
                continue;
            }
            if (widget.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }

        for (ElementPlacement placement : state.placements()) {
            UiRenderPhase phase = placement.element().layer().phase();
            if (phase != UiRenderPhase.OVERLAY && phase != UiRenderPhase.TOOLTIP) {
                continue;
            }
            if (contains(placement, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private boolean suppressesSourceTooltipAt(double mouseX, double mouseY) {
        RenderState state = lastRenderState != null ? lastRenderState : buildRenderState();
        for (ElementPlacement placement : state.placements()) {
            if (!placement.element().suppressesTooltips()) {
                continue;
            }
            if (contains(placement, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(ElementPlacement placement, double mouseX, double mouseY) {
        int left = placement.left();
        int top = placement.top();
        int right = left + Math.max(0, placement.width());
        int bottom = top + Math.max(0, placement.height());
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private static int firstPositive(Integer... values) {
        if (values == null) {
            return 1;
        }
        for (Integer value : values) {
            if (value != null && value > 0) {
                return value;
            }
        }
        return 1;
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private final class SessionInitContext implements ElementPainter.InitContext {
        private ElementPainter currentOwner;

        void runWithOwner(ElementPainter owner, Runnable action) {
            ElementPainter previous = currentOwner;
            currentOwner = owner;
            try {
                action.run();
            } finally {
                currentOwner = previous;
            }
        }

        @Override
        public <T extends AbstractWidget> T addRenderableWidget(T widget) {
            UiRenderLayer layer = HostRenderSupport.resolveWidgetLayer(currentOwner, widget);
            int zIndex = HostRenderSupport.resolveWidgetZIndex(currentOwner, widget);
            managedWidgets.add(new ManagedWidget(widget, layer, zIndex, nextManagedWidgetSerial++));
            return widget;
        }
    }

    private record KernelState(
            FramePainter frame,
            BgPainter background,
            BehindPainter behind,
            SplitPainter splitPainter,
            ElementPainter below,
            List<PadSpec> pads,
            List<SplitSpec> splits,
            int layoutLeft,
            int layoutTop,
            int layoutWidth,
            int layoutHeight
    ) {
    }

    private record RenderState(
            HostGeometry geometry,
            KernelState kernel,
            boolean drawBottomEdge,
            boolean hasBelow,
            FramePainter.SlotArea slotArea,
            List<ElementPlacement> placements
    ) {
    }

    private record StagedTask(UiRenderLayer layer, int zIndex, int serial, Runnable action) {
    }

    private static final class ProxyFramePainter implements FramePainter {
        private final FrameMetrics metrics;
        private Layout layout;

        private ProxyFramePainter(int panelWidth) {
            this.metrics = new ProxyFrameMetrics(Math.max(1, panelWidth));
        }

        @Override
        public void paint(GuiGraphics g, int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
        }

        @Override
        public FrameMetrics metrics() {
            return metrics;
        }

        @Override
        public void setLayout(int left, int top, int w, int h, boolean drawBottomEdge, boolean hasBelow) {
            this.layout = new Layout(left, top, w, h, drawBottomEdge, hasBelow);
        }

        @Override
        public Layout layout() {
            return layout;
        }
    }

    private record ProxyFrameMetrics(int panelW) implements FrameMetrics {
        @Override public int texW() { return panelW; }
        @Override public int texH() { return panelW; }
        @Override public int titleStartH() { return 0; }
        @Override public int slotStartTopPx() { return 0; }
        @Override public int slotStartLeftPx() { return 0; }
        @Override public int slotSizePx() { return UiUnit.SLOT_PX; }
        @Override public int slotInnerStartY() { return 0; }
        @Override public int slotInnerHeight() { return UiUnit.SLOT_PX; }
        @Override public int topBorderY() { return 0; }
        @Override public int bottomBorderY() { return 0; }
        @Override public int bottomPadStartY() { return 0; }
        @Override public int bottomPadHeight() { return 0; }
        @Override public int bottomEdgeStartY() { return 0; }
        @Override public int bottomEdgeHeight() { return 0; }
        @Override public int buttomInvExtraStartY() { return 0; }
        @Override public int buttomInvExtraHeight() { return 0; }
    }
}
