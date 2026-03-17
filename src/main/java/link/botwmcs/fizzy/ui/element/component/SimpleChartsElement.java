package link.botwmcs.fizzy.ui.element.component;

import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A chart/table element that splits its assigned bounds into a grid of built-in pads (cells).
 * Each cell accepts elements directly, and every cell is clipped with scissor while rendering.
 */
public final class SimpleChartsElement implements ElementPainter {
    private final ContentSpec contentSpec;
    private final ElementType resolvedType;

    private final List<CellRenderSpec> resolvedCells = new ArrayList<>();
    private final List<AbstractWidget> childWidgets = new ArrayList<>();
    private final List<List<AbstractWidget>> widgetsByCell = new ArrayList<>();
    private final Map<AbstractWidget, Integer> widgetCellIndex = new IdentityHashMap<>();
    private final ChildInitContext childInitContext = new ChildInitContext();

    private ChartWidget rootWidget;
    private AbstractWidget pressedChild;

    public SimpleChartsElement(ContentSpec contentSpec) {
        this(builder(contentSpec));
    }

    public SimpleChartsElement(Consumer<ContentBuilder> contentDsl) {
        this(content(contentDsl));
    }

    private SimpleChartsElement(Builder builder) {
        this.contentSpec = Objects.requireNonNull(builder.contentSpec, "contentSpec");
        this.resolvedType = resolveType(this.contentSpec);
    }

    public static Builder builder(ContentSpec contentSpec) {
        return new Builder(contentSpec);
    }

    public static Builder builder(Consumer<ContentBuilder> contentDsl) {
        return new Builder(content(contentDsl));
    }

    public static SimpleChartsElement of(ContentSpec contentSpec) {
        return builder(contentSpec).build();
    }

    public static SimpleChartsElement of(Consumer<ContentBuilder> contentDsl) {
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

        this.pressedChild = null;
        this.childWidgets.clear();
        this.widgetCellIndex.clear();
        this.widgetsByCell.clear();

        resolveCells(leftPx, topPx, widthPx, heightPx, this.resolvedCells);
        for (int i = 0; i < this.resolvedCells.size(); i++) {
            this.widgetsByCell.add(new ArrayList<>());
        }

        this.rootWidget = new ChartWidget(leftPx, topPx, widthPx, heightPx);
        context.addRenderableWidget(this.rootWidget);

        for (int i = 0; i < this.resolvedCells.size(); i++) {
            CellRenderSpec cell = this.resolvedCells.get(i);
            LocalBounds bounds = cell.bounds();
            this.childInitContext.currentCellIndex = i;
            for (ElementPainter element : cell.cellSpec().elements()) {
                element.init(this.childInitContext, bounds.left(), bounds.top(), bounds.width(), bounds.height());
            }
        }
        this.childInitContext.currentCellIndex = -1;
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        resolveCells(leftPx, topPx, widthPx, heightPx, this.resolvedCells);
        if (this.rootWidget != null) {
            this.rootWidget.setX(leftPx);
            this.rootWidget.setY(topPx);
            this.rootWidget.setWidth(Math.max(0, widthPx));
            this.rootWidget.setHeight(Math.max(0, heightPx));
        }
    }

    @Override
    public ElementType type() {
        return this.resolvedType;
    }

    @Override
    public List<AbstractWidget> widgets() {
        return List.copyOf(this.childWidgets);
    }

    private void renderCellsAndWidgets(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (this.rootWidget == null) {
            return;
        }
        resolveCells(this.rootWidget.getX(), this.rootWidget.getY(), this.rootWidget.getWidth(), this.rootWidget.getHeight(), this.resolvedCells);

        for (int i = 0; i < this.resolvedCells.size(); i++) {
            CellRenderSpec cell = this.resolvedCells.get(i);
            LocalBounds bounds = cell.bounds();
            if (bounds.width() <= 0 || bounds.height() <= 0) {
                continue;
            }

            g.enableScissor(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
            try {
                for (ElementPainter element : cell.cellSpec().elements()) {
                    element.render(g, bounds.left(), bounds.top(), bounds.width(), bounds.height(), partialTick);
                }
                if (i < this.widgetsByCell.size()) {
                    for (AbstractWidget widget : this.widgetsByCell.get(i)) {
                        widget.render(g, mouseX, mouseY, partialTick);
                    }
                }
            } finally {
                g.disableScissor();
            }
        }
    }

    private boolean forwardMouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        this.pressedChild = null;
        for (int i = this.childWidgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = this.childWidgets.get(i);
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (!isInsideWidgetCell(widget, mouseX, mouseY)) {
                continue;
            }
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                this.pressedChild = widget;
                return true;
            }
        }
        return isInsideAnyCell(mouseX, mouseY);
    }

    private boolean forwardMouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (this.pressedChild != null) {
            boolean handled = this.pressedChild.mouseReleased(mouseX, mouseY, button);
            this.pressedChild = null;
            return handled;
        }

        for (int i = this.childWidgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = this.childWidgets.get(i);
            if (!widget.visible) {
                continue;
            }
            if (!isInsideWidgetCell(widget, mouseX, mouseY)) {
                continue;
            }
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    private boolean forwardMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) {
            return false;
        }
        if (this.pressedChild == null) {
            return false;
        }
        return this.pressedChild.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean forwardMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (int i = this.childWidgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = this.childWidgets.get(i);
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (!isInsideWidgetCell(widget, mouseX, mouseY)) {
                continue;
            }
            if (widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideAnyCell(double mouseX, double mouseY) {
        for (CellRenderSpec cell : this.resolvedCells) {
            if (cell.bounds().contains(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideWidgetCell(AbstractWidget widget, double mouseX, double mouseY) {
        Integer cellIndex = this.widgetCellIndex.get(widget);
        if (cellIndex == null || cellIndex < 0 || cellIndex >= this.resolvedCells.size()) {
            return false;
        }
        return this.resolvedCells.get(cellIndex).bounds().contains(mouseX, mouseY);
    }

    private void resolveCells(int leftPx, int topPx, int widthPx, int heightPx, List<CellRenderSpec> out) {
        out.clear();
        int safeWidth = Math.max(0, widthPx);
        int safeHeight = Math.max(0, heightPx);

        for (CellSpec cell : this.contentSpec.cells()) {
            int rawLeft = leftPx + partitionOffset(cell.colStart() - 1, safeWidth, this.contentSpec.cols());
            int rawRight = leftPx + partitionOffset(cell.colEnd(), safeWidth, this.contentSpec.cols());
            int rawTop = topPx + partitionOffset(cell.rowStart() - 1, safeHeight, this.contentSpec.rows());
            int rawBottom = topPx + partitionOffset(cell.rowEnd(), safeHeight, this.contentSpec.rows());

            int cellLeft = rawLeft;
            int cellTop = rawTop;
            int cellWidth = Math.max(0, rawRight - rawLeft);
            int cellHeight = Math.max(0, rawBottom - rawTop);

            if (cell.inner()) {
                int inset = UiUnit.SLOT_PAD_INNER_INSET_PX;
                int insetX = Math.min(inset, cellWidth / 2);
                int insetY = Math.min(inset, cellHeight / 2);
                cellLeft += insetX;
                cellTop += insetY;
                cellWidth -= insetX * 2;
                cellHeight -= insetY * 2;
            }

            out.add(new CellRenderSpec(cell, new LocalBounds(cellLeft, cellTop, cellWidth, cellHeight)));
        }
    }

    private static int partitionOffset(int index, int total, int parts) {
        if (parts <= 0 || total <= 0) {
            return 0;
        }
        int clamped = Math.max(0, Math.min(index, parts));
        return (int) ((long) clamped * (long) total / (long) parts);
    }

    private static ElementType resolveType(ContentSpec contentSpec) {
        for (CellSpec cell : contentSpec.cells()) {
            for (ElementPainter element : cell.elements()) {
                if (element.type() == ElementType.BUTTON) {
                    return ElementType.BUTTON;
                }
            }
        }
        return ElementType.CUSTOM;
    }

    private final class ChartWidget extends AbstractWidget {
        private ChartWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            renderCellsAndWidgets(g, mouseX, mouseY, partialTick);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.visible) {
                return false;
            }
            return forwardMouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (!this.visible) {
                return false;
            }
            return forwardMouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.active || !this.visible) {
                return false;
            }
            return forwardMouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (!this.active || !this.visible) {
                return false;
            }
            return forwardMouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
    }

    private final class ChildInitContext implements InitContext {
        private int currentCellIndex = -1;

        @Override
        public <T extends AbstractWidget> T addRenderableWidget(T widget) {
            if (this.currentCellIndex < 0 || this.currentCellIndex >= widgetsByCell.size()) {
                throw new IllegalStateException("Cell index is not available while initializing child widget.");
            }
            childWidgets.add(widget);
            widgetsByCell.get(this.currentCellIndex).add(widget);
            widgetCellIndex.put(widget, this.currentCellIndex);
            return widget;
        }
    }

    public static final class Builder {
        private final ContentSpec contentSpec;

        private Builder(ContentSpec contentSpec) {
            this.contentSpec = Objects.requireNonNull(contentSpec, "contentSpec");
        }

        public SimpleChartsElement build() {
            return new SimpleChartsElement(this);
        }
    }

    public static final class ContentBuilder {
        private int rows = -1;
        private int cols = -1;
        private final List<CellSpec> explicitCells = new ArrayList<>();

        public ContentBuilder grid(int rows, int cols) {
            if (rows <= 0) {
                throw new IllegalArgumentException("rows must be > 0");
            }
            if (cols <= 0) {
                throw new IllegalArgumentException("cols must be > 0");
            }
            this.rows = rows;
            this.cols = cols;
            return this;
        }

        public CellBuilder cell(int row, int col) {
            return cell(row, col, row, col);
        }

        public CellBuilder cell(int rowStart, int colStart, int rowEnd, int colEnd) {
            if (rows <= 0 || cols <= 0) {
                throw new IllegalStateException("grid(rows, cols) must be configured before adding cells.");
            }
            return new CellBuilder(this, rowStart, colStart, rowEnd, colEnd);
        }

        public ContentSpec build() {
            if (rows <= 0 || cols <= 0) {
                throw new IllegalStateException("grid(rows, cols) must be configured before build().");
            }

            int[][] owner = new int[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    owner[r][c] = -1;
                }
            }

            for (int i = 0; i < explicitCells.size(); i++) {
                CellSpec cell = explicitCells.get(i);
                validateCellRange(cell.rowStart(), cell.colStart(), cell.rowEnd(), cell.colEnd(), rows, cols);
                for (int r = cell.rowStart(); r <= cell.rowEnd(); r++) {
                    for (int c = cell.colStart(); c <= cell.colEnd(); c++) {
                        if (owner[r - 1][c - 1] != -1) {
                            throw new IllegalStateException("Cell overlap detected at row " + r + ", col " + c);
                        }
                        owner[r - 1][c - 1] = i;
                    }
                }
            }

            List<CellSpec> finalized = new ArrayList<>(rows * cols);
            boolean[] appended = new boolean[explicitCells.size()];
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    int idx = owner[r - 1][c - 1];
                    if (idx >= 0) {
                        if (!appended[idx]) {
                            finalized.add(explicitCells.get(idx));
                            appended[idx] = true;
                        }
                    } else {
                        finalized.add(CellSpec.empty(r, c));
                    }
                }
            }

            return new ContentSpec(rows, cols, List.copyOf(finalized));
        }

        private void addCell(CellSpec spec) {
            explicitCells.add(Objects.requireNonNull(spec, "spec"));
        }

        private static void validateCellRange(int rowStart, int colStart, int rowEnd, int colEnd, int rows, int cols) {
            if (rowStart < 1 || rowStart > rows) {
                throw new IllegalArgumentException("rowStart must be in [1, rows]");
            }
            if (colStart < 1 || colStart > cols) {
                throw new IllegalArgumentException("colStart must be in [1, cols]");
            }
            if (rowEnd < rowStart || rowEnd > rows) {
                throw new IllegalArgumentException("rowEnd must be in [rowStart, rows]");
            }
            if (colEnd < colStart || colEnd > cols) {
                throw new IllegalArgumentException("colEnd must be in [colStart, cols]");
            }
        }
    }

    public static final class CellBuilder {
        private final ContentBuilder parent;
        private final int rowStart;
        private final int colStart;
        private final int rowEnd;
        private final int colEnd;
        private final List<ElementPainter> elements = new ArrayList<>();

        private boolean inner;

        private CellBuilder(ContentBuilder parent, int rowStart, int colStart, int rowEnd, int colEnd) {
            this.parent = parent;
            this.rowStart = rowStart;
            this.colStart = colStart;
            this.rowEnd = rowEnd;
            this.colEnd = colEnd;
        }

        public CellBuilder element(ElementPainter element) {
            this.elements.add(Objects.requireNonNull(element, "element"));
            return this;
        }

        public CellBuilder elements(ElementPainter... elements) {
            Objects.requireNonNull(elements, "elements");
            for (ElementPainter element : elements) {
                this.elements.add(Objects.requireNonNull(element, "element"));
            }
            return this;
        }

        public CellBuilder inner() {
            this.inner = true;
            return this;
        }

        public ContentBuilder done() {
            parent.addCell(new CellSpec(rowStart, colStart, rowEnd, colEnd, inner, List.copyOf(elements)));
            return parent;
        }
    }

    public static final class ContentSpec {
        private final int rows;
        private final int cols;
        private final List<CellSpec> cells;

        private ContentSpec(int rows, int cols, List<CellSpec> cells) {
            if (rows <= 0) {
                throw new IllegalArgumentException("rows must be > 0");
            }
            if (cols <= 0) {
                throw new IllegalArgumentException("cols must be > 0");
            }
            this.rows = rows;
            this.cols = cols;
            this.cells = List.copyOf(Objects.requireNonNull(cells, "cells"));
        }

        public int rows() {
            return rows;
        }

        public int cols() {
            return cols;
        }

        private List<CellSpec> cells() {
            return cells;
        }
    }

    private record CellRenderSpec(CellSpec cellSpec, LocalBounds bounds) {
        private CellRenderSpec {
            Objects.requireNonNull(cellSpec, "cellSpec");
            Objects.requireNonNull(bounds, "bounds");
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

        private int right() {
            return left + width;
        }

        private int bottom() {
            return top + height;
        }

        private boolean contains(double x, double y) {
            return x >= left && x < right() && y >= top && y < bottom();
        }
    }

    private record CellSpec(int rowStart,
                            int colStart,
                            int rowEnd,
                            int colEnd,
                            boolean inner,
                            List<ElementPainter> elements) {
        private CellSpec {
            elements = List.copyOf(Objects.requireNonNull(elements, "elements"));
        }

        private static CellSpec empty(int row, int col) {
            return new CellSpec(row, col, row, col, false, List.of());
        }
    }
}
