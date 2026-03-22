package link.botwmcs.fizzy.ui.element.funstuff.vector;

import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.client.util.Gwen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class SimpleDraggableElement implements ElementPainter {
    private static final int DEFAULT_WHEEL_STEP_PX = UiUnit.SLOT_PX;
    private static final int DEFAULT_SCROLLBAR_WIDTH_PX = 6;
    private static final int DEFAULT_SCROLLBAR_GAP_PX = 2;
    private static final int DEFAULT_MIN_THUMB_HEIGHT_PX = 14;

    private static final int SCROLLBAR_TRACK_COLOR = 0xFF2B2B2B;
    private static final int SCROLLBAR_BORDER_DARK = 0xFF111111;
    private static final int SCROLLBAR_BORDER_LIGHT = 0xFF4C4C4C;
    private static final int SCROLLBAR_THUMB_COLOR = 0xFF8A8A8A;
    private static final int SCROLLBAR_THUMB_HOVER_COLOR = 0xFFB5B5B5;
    private static final int SCROLLBAR_THUMB_DISABLED_COLOR = 0xFF4C4C4C;

    private final ContentSpec contentSpec;
    private final int wheelStepPx;
    private final int scrollbarWidthPx;
    private final int scrollbarGapPx;
    private final int minThumbHeightPx;

    private final List<AbstractWidget> childWidgets = new ArrayList<>();
    private final ChildInitContext childInitContext = new ChildInitContext();

    private ScrollWidget scrollWidget;
    private double scrollOffsetPx;

    private int viewportLeft;
    private int viewportTop;
    private int viewportWidth;
    private int viewportHeight;
    private int contentHeightPx;
    private boolean showScrollbar;

    private boolean draggingScrollbar;
    private int scrollbarDragGrabOffsetPx;
    private AbstractWidget pressedChild;

    public SimpleDraggableElement(ContentSpec contentSpec) {
        this(builder(contentSpec));
    }

    public SimpleDraggableElement(Consumer<ContentBuilder> contentDsl) {
        this(content(contentDsl));
    }

    private SimpleDraggableElement(Builder builder) {
        this.contentSpec = Objects.requireNonNull(builder.contentSpec, "contentSpec");
        this.wheelStepPx = builder.wheelStepPx;
        this.scrollbarWidthPx = builder.scrollbarWidthPx;
        this.scrollbarGapPx = builder.scrollbarGapPx;
        this.minThumbHeightPx = builder.minThumbHeightPx;
    }

    public static Builder builder(ContentSpec contentSpec) {
        return new Builder(contentSpec);
    }

    public static Builder builder(Consumer<ContentBuilder> contentDsl) {
        return new Builder(content(contentDsl));
    }

    public static SimpleDraggableElement of(ContentSpec contentSpec) {
        return builder(contentSpec).build();
    }

    public static SimpleDraggableElement of(Consumer<ContentBuilder> contentDsl) {
        return builder(contentDsl).build();
    }

    public static ContentBuilder contentBuilder() {
        return new ContentBuilder();
    }

    public static ContentSpec content(Consumer<ContentBuilder> contentDsl) {
        Objects.requireNonNull(contentDsl, "contentDsl");
        ContentBuilder builder = new ContentBuilder();
        contentDsl.accept(builder);
        return builder.build();
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        Objects.requireNonNull(context, "context");

        this.draggingScrollbar = false;
        this.scrollbarDragGrabOffsetPx = 0;
        this.pressedChild = null;
        this.childWidgets.clear();

        updateViewport(leftPx, topPx, widthPx, heightPx);

        this.scrollWidget = new ScrollWidget(leftPx, topPx, widthPx, heightPx);
        context.addRenderableWidget(this.scrollWidget);

        int scrollOffset = scrollOffsetInt();
        for (ContentPadSpec pad : contentSpec.pads()) {
            LocalBounds bounds = pad.bounds();
            int elementLeft = viewportLeft + bounds.left();
            int elementTop = viewportTop + bounds.top() - scrollOffset;
            for (ElementPainter element : pad.elements()) {
                element.init(childInitContext, elementLeft, elementTop, bounds.width(), bounds.height());
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        updateViewport(leftPx, topPx, widthPx, heightPx);
        if (scrollWidget != null) {
            scrollWidget.setX(leftPx);
            scrollWidget.setY(topPx);
            scrollWidget.setWidth(Math.max(0, widthPx));
            scrollWidget.setHeight(Math.max(0, heightPx));
        }
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }

    private void updateViewport(int leftPx, int topPx, int widthPx, int heightPx) {
        int safeWidth = Math.max(0, widthPx);
        int safeHeight = Math.max(0, heightPx);

        int totalScrollbarCost = scrollbarWidthPx + scrollbarGapPx;
        this.showScrollbar = totalScrollbarCost > 0 && safeWidth > totalScrollbarCost;

        this.viewportLeft = leftPx;
        this.viewportTop = topPx;
        this.viewportHeight = safeHeight;
        this.viewportWidth = showScrollbar ? Math.max(0, safeWidth - totalScrollbarCost) : safeWidth;

        this.contentHeightPx = Math.max(contentSpec.contentHeightPx(), viewportHeight);
        clampScroll();
    }

    private void renderContentAndScrollbar(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            if (showScrollbar) {
                drawScrollbar(g, mouseX, mouseY);
            }
            return;
        }

        Gwen.withScissor(
                g,
                viewportLeft,
                viewportTop,
                viewportLeft + viewportWidth,
                viewportTop + viewportHeight,
                () -> {
                    int scrollOffset = scrollOffsetInt();
                    for (ContentPadSpec pad : contentSpec.pads()) {
                        LocalBounds bounds = pad.bounds();
                        int elementLeft = viewportLeft + bounds.left();
                        int elementTop = viewportTop + bounds.top() - scrollOffset;
                        for (ElementPainter element : pad.elements()) {
                            element.render(g, elementLeft, elementTop, bounds.width(), bounds.height(), partialTick);
                        }
                    }

                    for (AbstractWidget widget : childWidgets) {
                        widget.render(g, mouseX, mouseY, partialTick);
                    }
                }
        );

        if (showScrollbar) {
            drawScrollbar(g, mouseX, mouseY);
        }
    }

    private void drawScrollbar(GuiGraphics g, int mouseX, int mouseY) {
        int trackLeft = scrollbarLeft();
        int trackTop = viewportTop;
        int trackRight = trackLeft + scrollbarWidthPx;
        int trackBottom = trackTop + viewportHeight;
        if (scrollbarWidthPx <= 0 || viewportHeight <= 0) {
            return;
        }

        g.fill(trackLeft, trackTop, trackRight, trackBottom, SCROLLBAR_TRACK_COLOR);
        g.fill(trackLeft, trackTop, trackRight, trackTop + 1, SCROLLBAR_BORDER_LIGHT);
        g.fill(trackLeft, trackBottom - 1, trackRight, trackBottom, SCROLLBAR_BORDER_DARK);
        g.fill(trackLeft, trackTop, trackLeft + 1, trackBottom, SCROLLBAR_BORDER_DARK);
        g.fill(trackRight - 1, trackTop, trackRight, trackBottom, SCROLLBAR_BORDER_LIGHT);

        int thumbHeight = thumbHeightPx();
        int thumbTop = thumbTopPx();
        int thumbBottom = thumbTop + thumbHeight;

        int thumbColor;
        if (maxScrollPx() <= 0) {
            thumbColor = SCROLLBAR_THUMB_DISABLED_COLOR;
        } else {
            boolean hover = isPointInsideThumb(mouseX, mouseY);
            thumbColor = (draggingScrollbar || hover) ? SCROLLBAR_THUMB_HOVER_COLOR : SCROLLBAR_THUMB_COLOR;
        }

        g.fill(trackLeft + 1, thumbTop + 1, trackRight - 1, thumbBottom - 1, thumbColor);
        g.fill(trackLeft, thumbTop, trackRight, thumbTop + 1, SCROLLBAR_BORDER_LIGHT);
        g.fill(trackLeft, thumbBottom - 1, trackRight, thumbBottom, SCROLLBAR_BORDER_DARK);
        g.fill(trackLeft, thumbTop, trackLeft + 1, thumbBottom, SCROLLBAR_BORDER_DARK);
        g.fill(trackRight - 1, thumbTop, trackRight, thumbBottom, SCROLLBAR_BORDER_LIGHT);
    }

    private void scrollBy(double deltaPx) {
        if (deltaPx == 0.0D) {
            return;
        }
        this.scrollOffsetPx += deltaPx;
        clampScroll();
    }

    private void setScrollFromThumbTop(int thumbTop) {
        int maxScroll = maxScrollPx();
        int travel = thumbTravelPx();
        if (maxScroll <= 0 || travel <= 0) {
            this.scrollOffsetPx = 0.0D;
            return;
        }
        int clampedTop = Mth.clamp(thumbTop, viewportTop, viewportTop + travel);
        double ratio = (clampedTop - viewportTop) / (double) travel;
        this.scrollOffsetPx = ratio * maxScroll;
        clampScroll();
    }

    private void clampScroll() {
        this.scrollOffsetPx = Mth.clamp(this.scrollOffsetPx, 0.0D, maxScrollPx());
    }

    private int maxScrollPx() {
        return Math.max(0, contentHeightPx - viewportHeight);
    }

    private int scrollOffsetInt() {
        return Mth.floor(scrollOffsetPx + 0.5D);
    }

    private int scrollbarLeft() {
        return viewportLeft + viewportWidth + scrollbarGapPx;
    }

    private int thumbHeightPx() {
        if (viewportHeight <= 0) {
            return 0;
        }
        int maxScroll = maxScrollPx();
        if (maxScroll <= 0) {
            return viewportHeight;
        }
        int ideal = Mth.floor((double) viewportHeight * (double) viewportHeight / Math.max(1.0D, contentHeightPx));
        return Mth.clamp(ideal, Math.min(minThumbHeightPx, viewportHeight), viewportHeight);
    }

    private int thumbTravelPx() {
        return Math.max(0, viewportHeight - thumbHeightPx());
    }

    private int thumbTopPx() {
        int maxScroll = maxScrollPx();
        int travel = thumbTravelPx();
        if (maxScroll <= 0 || travel <= 0) {
            return viewportTop;
        }
        double ratio = scrollOffsetPx / (double) maxScroll;
        return viewportTop + Mth.floor(ratio * travel + 0.5D);
    }

    private boolean isPointInsideViewport(double mouseX, double mouseY) {
        return mouseX >= viewportLeft
                && mouseX < viewportLeft + viewportWidth
                && mouseY >= viewportTop
                && mouseY < viewportTop + viewportHeight;
    }

    private boolean isPointInsideScrollbar(double mouseX, double mouseY) {
        if (!showScrollbar || scrollbarWidthPx <= 0 || viewportHeight <= 0) {
            return false;
        }
        int left = scrollbarLeft();
        return mouseX >= left
                && mouseX < left + scrollbarWidthPx
                && mouseY >= viewportTop
                && mouseY < viewportTop + viewportHeight;
    }

    private boolean isPointInsideThumb(double mouseX, double mouseY) {
        if (!isPointInsideScrollbar(mouseX, mouseY)) {
            return false;
        }
        int thumbTop = thumbTopPx();
        int thumbHeight = thumbHeightPx();
        return mouseY >= thumbTop && mouseY < thumbTop + thumbHeight;
    }

    private final class ScrollWidget extends AbstractWidget {
        private ScrollWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            updateViewport(getX(), getY(), getWidth(), getHeight());
            renderContentAndScrollbar(g, mouseX, mouseY, partialTick);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.visible || button != 0) {
                return false;
            }
            updateViewport(getX(), getY(), getWidth(), getHeight());
            pressedChild = null;

            if (isPointInsideScrollbar(mouseX, mouseY)) {
                if (maxScrollPx() <= 0) {
                    return true;
                }
                int mouseYPx = Mth.floor(mouseY);
                int thumbTop = thumbTopPx();
                int thumbHeight = thumbHeightPx();
                if (mouseYPx >= thumbTop && mouseYPx < thumbTop + thumbHeight) {
                    draggingScrollbar = true;
                    scrollbarDragGrabOffsetPx = mouseYPx - thumbTop;
                } else {
                    draggingScrollbar = true;
                    scrollbarDragGrabOffsetPx = Math.max(1, thumbHeight / 2);
                    setScrollFromThumbTop(mouseYPx - scrollbarDragGrabOffsetPx);
                }
                return true;
            }

            if (!isPointInsideViewport(mouseX, mouseY)) {
                return false;
            }

            for (int i = childWidgets.size() - 1; i >= 0; i--) {
                AbstractWidget widget = childWidgets.get(i);
                if (!widget.visible) {
                    continue;
                }
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    pressedChild = widget;
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (!this.visible || button != 0) {
                return false;
            }
            boolean handled = false;
            if (draggingScrollbar) {
                draggingScrollbar = false;
                handled = true;
            }

            if (pressedChild != null) {
                handled = pressedChild.mouseReleased(mouseX, mouseY, button) || handled;
                pressedChild = null;
                return handled;
            }

            if (isPointInsideViewport(mouseX, mouseY)) {
                for (int i = childWidgets.size() - 1; i >= 0; i--) {
                    AbstractWidget widget = childWidgets.get(i);
                    if (!widget.visible) {
                        continue;
                    }
                    if (widget.mouseReleased(mouseX, mouseY, button)) {
                        handled = true;
                        break;
                    }
                }
            }

            return handled;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.active || !this.visible || button != 0) {
                return false;
            }
            if (draggingScrollbar) {
                int mouseYPx = Mth.floor(mouseY);
                setScrollFromThumbTop(mouseYPx - scrollbarDragGrabOffsetPx);
                return true;
            }
            if (pressedChild != null) {
                return pressedChild.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (!this.active || !this.visible) {
                return false;
            }
            updateViewport(getX(), getY(), getWidth(), getHeight());
            if (!isPointInsideViewport(mouseX, mouseY) && !isPointInsideScrollbar(mouseX, mouseY)) {
                return false;
            }

            double delta = scrollY != 0.0D ? scrollY : scrollX;
            if (delta == 0.0D) {
                return false;
            }
            scrollBy(-delta * wheelStepPx);
            return true;
        }
    }

    private final class ChildInitContext implements InitContext {
        @Override
        public <T extends AbstractWidget> T addRenderableWidget(T widget) {
            childWidgets.add(widget);
            return widget;
        }
    }

    public static final class Builder {
        private final ContentSpec contentSpec;
        private int wheelStepPx = DEFAULT_WHEEL_STEP_PX;
        private int scrollbarWidthPx = DEFAULT_SCROLLBAR_WIDTH_PX;
        private int scrollbarGapPx = DEFAULT_SCROLLBAR_GAP_PX;
        private int minThumbHeightPx = DEFAULT_MIN_THUMB_HEIGHT_PX;

        private Builder(ContentSpec contentSpec) {
            this.contentSpec = Objects.requireNonNull(contentSpec, "contentSpec");
        }

        public Builder wheelStepPx(int wheelStepPx) {
            if (wheelStepPx <= 0) {
                throw new IllegalArgumentException("wheelStepPx must be > 0");
            }
            this.wheelStepPx = wheelStepPx;
            return this;
        }

        public Builder scrollbarWidthPx(int scrollbarWidthPx) {
            if (scrollbarWidthPx < 0) {
                throw new IllegalArgumentException("scrollbarWidthPx must be >= 0");
            }
            this.scrollbarWidthPx = scrollbarWidthPx;
            return this;
        }

        public Builder scrollbarGapPx(int scrollbarGapPx) {
            if (scrollbarGapPx < 0) {
                throw new IllegalArgumentException("scrollbarGapPx must be >= 0");
            }
            this.scrollbarGapPx = scrollbarGapPx;
            return this;
        }

        public Builder minThumbHeightPx(int minThumbHeightPx) {
            if (minThumbHeightPx <= 0) {
                throw new IllegalArgumentException("minThumbHeightPx must be > 0");
            }
            this.minThumbHeightPx = minThumbHeightPx;
            return this;
        }

        public SimpleDraggableElement build() {
            return new SimpleDraggableElement(this);
        }
    }

    public static final class ContentBuilder {
        private final List<ContentPadSpec> pads = new ArrayList<>();
        private int minContentHeightPx;

        public ContentPadBuilder pad(int rowStart, int colStart, int rowEnd, int colEnd) {
            if (rowStart < 1) {
                throw new IllegalArgumentException("rowStart must be >= 1");
            }
            if (colStart < 1) {
                throw new IllegalArgumentException("colStart must be >= 1");
            }
            if (rowEnd < rowStart) {
                throw new IllegalArgumentException("rowEnd must be >= rowStart");
            }
            if (colEnd < colStart) {
                throw new IllegalArgumentException("colEnd must be >= colStart");
            }
            return new ContentPadBuilder(this, PadKind.SLOT, rowStart, colStart, rowEnd, colEnd);
        }

        public ContentPadBuilder padByPx(int leftPx, int topPx, int widthPx, int heightPx) {
            if (widthPx < 0) {
                throw new IllegalArgumentException("widthPx must be >= 0");
            }
            if (heightPx < 0) {
                throw new IllegalArgumentException("heightPx must be >= 0");
            }
            return new ContentPadBuilder(this, PadKind.PIXEL, leftPx, topPx, widthPx, heightPx);
        }

        public ContentBuilder contentHeightPx(int minContentHeightPx) {
            if (minContentHeightPx < 0) {
                throw new IllegalArgumentException("minContentHeightPx must be >= 0");
            }
            this.minContentHeightPx = minContentHeightPx;
            return this;
        }

        public ContentSpec build() {
            int autoHeight = 0;
            for (ContentPadSpec pad : pads) {
                autoHeight = Math.max(autoHeight, pad.bounds().bottom());
            }
            int finalHeight = Math.max(autoHeight, minContentHeightPx);
            return new ContentSpec(List.copyOf(pads), finalHeight);
        }

        private void addPad(ContentPadSpec padSpec) {
            this.pads.add(Objects.requireNonNull(padSpec, "padSpec"));
        }
    }

    public static final class ContentPadBuilder {
        private final ContentBuilder parent;
        private final PadKind kind;
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final List<ElementPainter> elements = new ArrayList<>();

        private boolean inner;

        private ContentPadBuilder(ContentBuilder parent, PadKind kind, int a, int b, int c, int d) {
            this.parent = parent;
            this.kind = kind;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public ContentPadBuilder element(ElementPainter element) {
            this.elements.add(Objects.requireNonNull(element, "element"));
            return this;
        }

        public ContentPadBuilder elements(ElementPainter... elements) {
            Objects.requireNonNull(elements, "elements");
            for (ElementPainter element : elements) {
                this.elements.add(Objects.requireNonNull(element, "element"));
            }
            return this;
        }

        public ContentPadBuilder inner() {
            if (this.kind != PadKind.SLOT) {
                throw new IllegalStateException("inner() is only supported for slot pads.");
            }
            this.inner = true;
            return this;
        }

        public ContentBuilder done() {
            LocalBounds bounds = switch (kind) {
                case SLOT -> resolveSlotBounds();
                case PIXEL -> new LocalBounds(a, b, c, d);
            };
            parent.addPad(new ContentPadSpec(bounds, List.copyOf(elements)));
            return parent;
        }

        private LocalBounds resolveSlotBounds() {
            int rowStart = a;
            int colStart = b;
            int rowEnd = c;
            int colEnd = d;

            int left = (colStart - 1) * UiUnit.SLOT_PX;
            int top = (rowStart - 1) * UiUnit.SLOT_PX;
            int width = (colEnd - colStart + 1) * UiUnit.SLOT_PX;
            int height = (rowEnd - rowStart + 1) * UiUnit.SLOT_PX;

            if (!inner) {
                return new LocalBounds(left, top, width, height);
            }
            int inset = UiUnit.SLOT_PAD_INNER_INSET_PX;
            int insetX = Math.min(inset, width / 2);
            int insetY = Math.min(inset, height / 2);
            return new LocalBounds(left + insetX, top + insetY, width - insetX * 2, height - insetY * 2);
        }
    }

    public static final class ContentSpec {
        private final List<ContentPadSpec> pads;
        private final int contentHeightPx;

        private ContentSpec(List<ContentPadSpec> pads, int contentHeightPx) {
            this.pads = Objects.requireNonNull(pads, "pads");
            this.contentHeightPx = Math.max(0, contentHeightPx);
        }

        private List<ContentPadSpec> pads() {
            return pads;
        }

        private int contentHeightPx() {
            return contentHeightPx;
        }
    }

    private record LocalBounds(int left, int top, int width, int height) {
        private LocalBounds {
            if (width < 0) {
                throw new IllegalArgumentException("width must be >= 0");
            }
            if (height < 0) {
                throw new IllegalArgumentException("height must be >= 0");
            }
        }

        private int bottom() {
            return top + height;
        }
    }

    private record ContentPadSpec(LocalBounds bounds, List<ElementPainter> elements) {
        private ContentPadSpec {
            Objects.requireNonNull(bounds, "bounds");
            elements = List.copyOf(elements);
        }
    }

    private enum PadKind {
        SLOT,
        PIXEL
    }
}
