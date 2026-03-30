package link.botwmcs.fizzy.ui.element.funstuff.vector;

import link.botwmcs.fizzy.client.util.HostRenderSupport;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import link.botwmcs.fizzy.client.util.Gwen;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderLayer;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ContextMenuElement implements ElementPainter {
    private static final int DEFAULT_MIN_MENU_WIDTH_PX = 88;
    private static final int DEFAULT_BASE_ROW_HEIGHT_PX = 18;
    private static final int DEFAULT_ROW_PADDING_LEFT_PX = 5;
    private static final int DEFAULT_ROW_PADDING_RIGHT_PX = 5;
    private static final int DEFAULT_ROW_PADDING_Y_PX = 2;
    private static final int DEFAULT_SUBMENU_ARROW_SPACE_PX = 10;
    private static final int DEFAULT_ROOT_OPEN_ANIM_MS = 130;
    private static final int DEFAULT_SUBMENU_OPEN_ANIM_MS = 110;

    private static final int BORDER_SIZE_PX = 1;
    private static final int MIN_MENU_SIZE_PX = 4;
    private static final int MIN_INNER_CONTENT_PX = 1;
    private static final int SCREEN_MARGIN_PX = 1;

    private static final int PANEL_BG_COLOR = 0xFF000000;
    private static final int PANEL_BORDER_COLOR = 0xFFA0A0A0;
    private static final int HOVER_BG_COLOR = 0xFF2F2F2F;
    private static final int LABEL_COLOR = 0xFFE0E0E0;
    private static final int LABEL_DISABLED_COLOR = 0xFF777777;
    private static final int SEPARATOR_COLOR = 0xFF4D4D4D;
    private static final int SUBMENU_PARENT_OVERLAP_PX = 2;
    private static final float POPUP_DEPTH_STEP_Z = 256.0f;

    private final MenuSpec rootMenu;
    private final int minMenuWidthPx;
    private final int baseRowHeightPx;
    private final int rowPaddingLeftPx;
    private final int rowPaddingRightPx;
    private final int rowPaddingYPx;
    private final int submenuArrowSpacePx;
    private final int rootOpenAnimMs;
    private final int submenuOpenAnimMs;

    private final RuntimeChildInitContext childInitContext = new RuntimeChildInitContext();

    private @Nullable MenuWidget rootWidget;
    private @Nullable PopupState rootPopup;

    private int triggerLeftPx;
    private int triggerTopPx;
    private int triggerWidthPx;
    private int triggerHeightPx;

    private double lastMouseX;
    private double lastMouseY;

    private ContextMenuElement(Builder builder) {
        this.rootMenu = builder.root.buildSpec();
        this.minMenuWidthPx = builder.minMenuWidthPx;
        this.baseRowHeightPx = builder.baseRowHeightPx;
        this.rowPaddingLeftPx = builder.rowPaddingLeftPx;
        this.rowPaddingRightPx = builder.rowPaddingRightPx;
        this.rowPaddingYPx = builder.rowPaddingYPx;
        this.submenuArrowSpacePx = builder.submenuArrowSpacePx;
        this.rootOpenAnimMs = builder.rootOpenAnimMs;
        this.submenuOpenAnimMs = builder.submenuOpenAnimMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.triggerLeftPx = leftPx;
        this.triggerTopPx = topPx;
        this.triggerWidthPx = Math.max(0, widthPx);
        this.triggerHeightPx = Math.max(0, heightPx);
        closeMenu();

        MenuWidget widget = new MenuWidget(leftPx, topPx, widthPx, heightPx);
        this.rootWidget = widget;
        context.addRenderableWidget(widget);
    }

    @Override
    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        this.triggerLeftPx = leftPx;
        this.triggerTopPx = topPx;
        this.triggerWidthPx = Math.max(0, widthPx);
        this.triggerHeightPx = Math.max(0, heightPx);

        if (this.rootWidget != null) {
            this.rootWidget.setX(leftPx);
            this.rootWidget.setY(topPx);
            this.rootWidget.setWidth(this.triggerWidthPx);
            this.rootWidget.setHeight(this.triggerHeightPx);
        }

        if (this.rootPopup != null) {
            renderMenus(g, Mth.floor(this.lastMouseX), Mth.floor(this.lastMouseY), partialTick);
        }
    }

    @Override
    public ElementType type() {
        return ElementType.CUSTOM;
    }

    @Override
    public UiRenderLayer layer() {
        return UiRenderLayer.overlay(300);
    }

    @Override
    public int zIndex() {
        return 200;
    }

    @Override
    public boolean suppressesTooltips() {
        return this.rootPopup != null;
    }

    @Override
    public List<AbstractWidget> widgets() {
        if (this.rootWidget == null) {
            return List.of();
        }
        return List.of(this.rootWidget);
    }

    public void closeMenu() {
        if (this.rootPopup != null) {
            disposePopup(this.rootPopup);
            this.rootPopup = null;
        }
    }

    private void openRootMenu(double mouseX, double mouseY) {
        if (this.rootMenu.entries().isEmpty()) {
            return;
        }
        closeMenu();

        int screenW = screenWidthPx();
        int screenH = screenHeightPx();
        MenuMeasure measure = measureMenu(this.rootMenu, screenW);
        if (measure.totalHeightPx() <= 0 || measure.widthPx() <= 0) {
            return;
        }

        int clickX = Mth.floor(mouseX);
        int clickY = Mth.floor(mouseY);

        int x = clickX;
        int y = clickY;
        int maxX = Math.max(SCREEN_MARGIN_PX, screenW - measure.widthPx() - SCREEN_MARGIN_PX);
        int maxY = Math.max(SCREEN_MARGIN_PX, screenH - measure.totalHeightPx() - SCREEN_MARGIN_PX);

        if (x + measure.widthPx() > screenW - SCREEN_MARGIN_PX) {
            x = clickX - measure.widthPx();
        }
        if (y + measure.totalHeightPx() > screenH - SCREEN_MARGIN_PX) {
            y = clickY - measure.totalHeightPx();
        }
        x = Mth.clamp(x, SCREEN_MARGIN_PX, maxX);
        y = Mth.clamp(y, SCREEN_MARGIN_PX, maxY);

        int originMinX = x + BORDER_SIZE_PX;
        int originMaxX = x + measure.widthPx() - BORDER_SIZE_PX - 1;
        int originMinY = y + BORDER_SIZE_PX;
        int originMaxY = y + measure.totalHeightPx() - BORDER_SIZE_PX - 1;
        int originX = Mth.clamp(clickX, originMinX, Math.max(originMinX, originMaxX));
        int originY = Mth.clamp(clickY, originMinY, Math.max(originMinY, originMaxY));

        PopupState popup = new PopupState(
                this.rootMenu,
                null,
                -1,
                x,
                y,
                measure.widthPx(),
                measure.totalHeightPx(),
                true,
                true,
                originX,
                originY,
                Util.getMillis(),
                Math.max(1, this.rootOpenAnimMs)
        );
        populatePopupRows(popup, measure);
        this.rootPopup = popup;
        updateHoverAndSubmenus(mouseX, mouseY);
    }

    private void populatePopupRows(PopupState popup, MenuMeasure measure) {
        int rowLeft = popup.x() + BORDER_SIZE_PX;
        int rowTop = popup.y() + BORDER_SIZE_PX;
        int rowWidth = Math.max(0, popup.width() - BORDER_SIZE_PX * 2);
        int contentLeft = rowLeft + rowPaddingLeftPx;
        int contentWidth = Math.max(0, rowWidth - rowPaddingLeftPx - rowPaddingRightPx);

        List<MenuEntrySpec> entries = popup.menu().entries();
        List<Integer> rowHeights = measure.rowHeightsPx();
        for (int i = 0; i < entries.size(); i++) {
            int rowHeight = rowHeights.get(i);
            int contentTop = rowTop + rowPaddingYPx;
            int contentHeight = Math.max(0, rowHeight - rowPaddingYPx * 2);

            RowRuntime row = new RowRuntime(
                    entries.get(i),
                    rowLeft,
                    rowTop,
                    rowWidth,
                    rowHeight,
                    contentLeft,
                    contentTop,
                    contentWidth,
                    contentHeight
            );
            popup.rows().add(row);

            if (row.entry() instanceof ElementEntrySpec elementEntry) {
                childInitContext.setCurrentRow(row);
                try {
                    elementEntry.element().init(
                            childInitContext,
                            row.contentLeft(),
                            row.contentTop(),
                            row.contentWidth(),
                            row.contentHeight()
                    );
                } finally {
                    childInitContext.clearCurrentRow();
                }
            }

            rowTop += rowHeight;
        }
    }

    private @Nullable PopupState createSubmenuPopup(PopupState parent, int ownerRowIndex, SubmenuEntrySpec entry) {
        if (entry.submenu().entries().isEmpty()) {
            return null;
        }
        if (ownerRowIndex < 0 || ownerRowIndex >= parent.rows().size()) {
            return null;
        }

        int screenW = screenWidthPx();
        int screenH = screenHeightPx();
        MenuMeasure measure = measureMenu(entry.submenu(), screenW);
        if (measure.totalHeightPx() <= 0 || measure.widthPx() <= 0) {
            return null;
        }

        RowRuntime ownerRow = parent.rows().get(ownerRowIndex);
        int rightAnchorX = parent.x() + parent.width() - BORDER_SIZE_PX;
        int leftAnchorX = parent.x() + BORDER_SIZE_PX;

        boolean openToRight = true;
        int x = rightAnchorX - SUBMENU_PARENT_OVERLAP_PX;
        if (x + measure.widthPx() > screenW - SCREEN_MARGIN_PX) {
            openToRight = false;
            x = leftAnchorX - measure.widthPx() + SUBMENU_PARENT_OVERLAP_PX;
        }
        if (x < SCREEN_MARGIN_PX) {
            x = Mth.clamp(x, SCREEN_MARGIN_PX, Math.max(SCREEN_MARGIN_PX, screenW - measure.widthPx() - SCREEN_MARGIN_PX));
        }

        int y = ownerRow.top();
        int maxY = Math.max(SCREEN_MARGIN_PX, screenH - measure.totalHeightPx() - SCREEN_MARGIN_PX);
        y = Mth.clamp(y, SCREEN_MARGIN_PX, maxY);

        int originY = Mth.clamp(
                Mth.floor(this.lastMouseY),
                y + BORDER_SIZE_PX,
                Math.max(y + BORDER_SIZE_PX, y + measure.totalHeightPx() - BORDER_SIZE_PX - 1)
        );
        int originX = openToRight ? rightAnchorX : leftAnchorX;

        PopupState popup = new PopupState(
                entry.submenu(),
                parent,
                ownerRowIndex,
                x,
                y,
                measure.widthPx(),
                measure.totalHeightPx(),
                false,
                openToRight,
                originX,
                originY,
                Util.getMillis(),
                Math.max(1, this.submenuOpenAnimMs)
        );
        populatePopupRows(popup, measure);
        return popup;
    }

    private MenuMeasure measureMenu(MenuSpec menu, int screenWidthPx) {
        Font font = currentFont();

        int maxLabelWidth = 0;
        int maxLabelHeight = 0;
        int maxElementWidthHint = 0;
        boolean hasSubmenu = false;

        for (MenuEntrySpec entry : menu.entries()) {
            if (entry instanceof ActionEntrySpec actionEntry) {
                maxLabelWidth = Math.max(maxLabelWidth, actionEntry.label().measureWidthPx(font));
                maxLabelHeight = Math.max(maxLabelHeight, actionEntry.label().measureHeightPx(font));
                continue;
            }
            if (entry instanceof SubmenuEntrySpec submenuEntry) {
                maxLabelWidth = Math.max(maxLabelWidth, submenuEntry.label().measureWidthPx(font));
                maxLabelHeight = Math.max(maxLabelHeight, submenuEntry.label().measureHeightPx(font));
                hasSubmenu = true;
                continue;
            }
            if (entry instanceof ElementEntrySpec elementEntry) {
                int widthHint = elementPreferredWidthPx(elementEntry.element(), font);
                maxElementWidthHint = Math.max(maxElementWidthHint, widthHint);
            }
        }

        int rowHeight = Math.max(this.baseRowHeightPx, maxLabelHeight + this.rowPaddingYPx * 2);
        int arrowSpace = hasSubmenu ? this.submenuArrowSpacePx : 0;
        int requiredWidth = maxLabelWidth + rowPaddingLeftPx + rowPaddingRightPx + arrowSpace + BORDER_SIZE_PX * 2;
        if (maxElementWidthHint > 0) {
            requiredWidth = Math.max(requiredWidth, maxElementWidthHint + rowPaddingLeftPx + rowPaddingRightPx + BORDER_SIZE_PX * 2);
        }
        int width = Math.max(this.minMenuWidthPx, requiredWidth);
        if (screenWidthPx > 0) {
            width = Math.min(width, Math.max(MIN_MENU_SIZE_PX, screenWidthPx - SCREEN_MARGIN_PX * 2));
        }
        width = Math.max(MIN_MENU_SIZE_PX, width);

        int contentWidth = Math.max(
                MIN_INNER_CONTENT_PX,
                width - BORDER_SIZE_PX * 2 - rowPaddingLeftPx - rowPaddingRightPx
        );

        List<Integer> rowHeights = new ArrayList<>(menu.entries().size());
        int totalHeight = BORDER_SIZE_PX * 2;
        int fallbackElementHeight = Math.max(1, rowHeight - rowPaddingYPx * 2);
        for (MenuEntrySpec entry : menu.entries()) {
            int rowHeightPx = rowHeight;
            if (entry instanceof ElementEntrySpec elementEntry) {
                int elementHeightPx = elementPreferredHeightPx(
                        elementEntry.element(),
                        contentWidth,
                        fallbackElementHeight,
                        font
                );
                rowHeightPx = Math.max(rowHeight, elementHeightPx + rowPaddingYPx * 2);
            }
            rowHeights.add(rowHeightPx);
            totalHeight += rowHeightPx;
        }

        return new MenuMeasure(width, totalHeight, rowHeight, List.copyOf(rowHeights));
    }

    private int elementPreferredWidthPx(ElementPainter element, @Nullable Font font) {
        if (element instanceof MeasurableElement measurableElement) {
            int widthHint = measurableElement.preferredMenuWidthPx();
            if (widthHint > 0) {
                return widthHint;
            }
        }
        if (element instanceof FizzyComponentElement fce && font != null) {
            return measureFceWidthPx(fce, font);
        }
        return 0;
    }

    private int elementPreferredHeightPx(ElementPainter element, int contentWidthPx, int fallbackHeightPx, @Nullable Font font) {
        if (element instanceof MeasurableElement measurableElement) {
            int measuredHeight = measurableElement.preferredMenuHeightPx(contentWidthPx);
            if (measuredHeight > 0) {
                return measuredHeight;
            }
        }
        if (element instanceof FizzyComponentElement fce && font != null) {
            int measuredHeight = measureFceHeightPx(fce, font);
            if (measuredHeight > 0) {
                return measuredHeight;
            }
        }

        ProbeInitContext probeContext = new ProbeInitContext();
        try {
            element.init(probeContext, 0, 0, Math.max(1, contentWidthPx), Math.max(1, fallbackHeightPx));
        } catch (Throwable ignored) {
            return Math.max(1, fallbackHeightPx);
        }
        int probedHeight = probeContext.measuredHeightPx();
        if (probedHeight > 0) {
            return probedHeight;
        }
        return Math.max(1, fallbackHeightPx);
    }

    private static int measureFceWidthPx(FizzyComponentElement fce, Font font) {
        String raw = fce.plainText().getString();
        String[] lines = raw.split("\\n", -1);
        float scale = Math.max(0.01f, fce.textScale());
        int max = 0;
        for (String line : lines) {
            max = Math.max(max, Math.round(font.width(line) * scale));
        }
        return max;
    }

    private static int measureFceHeightPx(FizzyComponentElement fce, Font font) {
        String raw = fce.plainText().getString();
        String[] lines = raw.split("\\n", -1);
        int lineCount = Math.max(1, lines.length);
        float scale = Math.max(0.01f, fce.textScale());
        return Math.max(1, Math.round(lineCount * font.lineHeight * scale));
    }

    private void updateHoverAndSubmenus(double mouseX, double mouseY) {
        if (this.rootPopup == null) {
            return;
        }
        updateHoverRecursive(this.rootPopup, mouseX, mouseY);
    }

    private void updateHoverRecursive(PopupState popup, double mouseX, double mouseY) {
        int hoveredRow = popup.rowIndexAt(mouseX, mouseY);
        popup.setHoveredRowIndex(hoveredRow);

        int hoveredSubmenuRow = -1;
        if (hoveredRow >= 0 && hoveredRow < popup.rows().size()) {
            RowRuntime row = popup.rows().get(hoveredRow);
            if (row.entry() instanceof SubmenuEntrySpec submenuEntry && submenuEntry.enabled()) {
                hoveredSubmenuRow = hoveredRow;
                if (popup.child() == null || popup.child().ownerRowIndex() != hoveredSubmenuRow) {
                    if (popup.child() != null) {
                        disposePopup(popup.child());
                        popup.setChild(null);
                    }
                    PopupState child = createSubmenuPopup(popup, hoveredSubmenuRow, submenuEntry);
                    popup.setChild(child);
                }
            }
        }

        if (popup.child() == null) {
            return;
        }

        boolean childContains = popup.child().contains(mouseX, mouseY) || popup.child().containsDescendant(mouseX, mouseY);
        if (hoveredSubmenuRow < 0 && !childContains) {
            disposePopup(popup.child());
            popup.setChild(null);
            return;
        }

        if (!popup.contains(mouseX, mouseY) && childContains) {
            popup.setHoveredRowIndex(popup.child().ownerRowIndex());
        }
        updateHoverRecursive(popup.child(), mouseX, mouseY);
    }

    private @Nullable HitTestResult hitTest(double mouseX, double mouseY) {
        if (this.rootPopup == null) {
            return null;
        }
        return hitTestRecursive(this.rootPopup, mouseX, mouseY);
    }

    private @Nullable HitTestResult hitTestRecursive(PopupState popup, double mouseX, double mouseY) {
        if (popup.child() != null) {
            HitTestResult childHit = hitTestRecursive(popup.child(), mouseX, mouseY);
            if (childHit != null) {
                return childHit;
            }
        }
        if (!popup.contains(mouseX, mouseY)) {
            return null;
        }
        int rowIndex = popup.rowIndexAt(mouseX, mouseY);
        if (rowIndex < 0 || rowIndex >= popup.rows().size()) {
            return new HitTestResult(popup, -1, null);
        }
        return new HitTestResult(popup, rowIndex, popup.rows().get(rowIndex));
    }

    private boolean handlePrimaryClick(HitTestResult hit, double mouseX, double mouseY) {
        RowRuntime row = hit.row();
        if (row == null) {
            return true;
        }

        if (row.entry() instanceof SeparatorEntrySpec) {
            return true;
        }
        if (row.entry() instanceof SubmenuEntrySpec submenuEntry) {
            if (!submenuEntry.enabled()) {
                return true;
            }
            if (hit.popup().child() == null || hit.popup().child().ownerRowIndex() != hit.rowIndex()) {
                if (hit.popup().child() != null) {
                    disposePopup(hit.popup().child());
                }
                PopupState child = createSubmenuPopup(hit.popup(), hit.rowIndex(), submenuEntry);
                hit.popup().setChild(child);
            }
            return true;
        }
        if (row.entry() instanceof ActionEntrySpec actionEntry) {
            if (actionEntry.enabled()) {
                runAction(actionEntry.action());
                closeMenu();
            }
            return true;
        }
        if (row.entry() instanceof ElementEntrySpec elementEntry) {
            if (forwardRowMouseClicked(row, mouseX, mouseY, 0)) {
                return true;
            }
            if (elementEntry.enabled() && elementEntry.onClick() != null) {
                runAction(elementEntry.onClick());
                closeMenu();
            }
            return true;
        }
        return true;
    }

    private boolean forwardRowMouseClicked(RowRuntime row, double mouseX, double mouseY, int button) {
        MouseButtonEvent event = HostRenderSupport.createMouseButtonEvent(mouseX, mouseY, button);
        List<AbstractWidget> widgets = row.widgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = widgets.get(i);
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseClicked(event, false)) {
                row.setPressedWidget(widget);
                return true;
            }
        }
        return false;
    }

    private boolean releasePressedWidgets(PopupState popup, double mouseX, double mouseY, int button) {
        MouseButtonEvent event = HostRenderSupport.createMouseButtonEvent(mouseX, mouseY, button);
        boolean handled = false;
        for (RowRuntime row : popup.rows()) {
            if (row.pressedWidget() != null) {
                handled = row.pressedWidget().mouseReleased(event) || handled;
                row.setPressedWidget(null);
            }
        }
        if (popup.child() != null) {
            handled = releasePressedWidgets(popup.child(), mouseX, mouseY, button) || handled;
        }
        return handled;
    }

    private boolean dragPressedWidgets(PopupState popup, double mouseX, double mouseY, int button, double dragX, double dragY) {
        MouseButtonEvent event = HostRenderSupport.createMouseButtonEvent(mouseX, mouseY, button);
        boolean handled = false;
        for (RowRuntime row : popup.rows()) {
            if (row.pressedWidget() == null) {
                continue;
            }
            handled = row.pressedWidget().mouseDragged(event, dragX, dragY) || handled;
        }
        if (popup.child() != null) {
            handled = dragPressedWidgets(popup.child(), mouseX, mouseY, button, dragX, dragY) || handled;
        }
        return handled;
    }

    private boolean scrollWidgets(PopupState popup, double mouseX, double mouseY, double scrollX, double scrollY) {
        HitTestResult hit = hitTest(mouseX, mouseY);
        if (hit == null || hit.row() == null) {
            return false;
        }
        RowRuntime row = hit.row();
        for (int i = row.widgets().size() - 1; i >= 0; i--) {
            AbstractWidget widget = row.widgets().get(i);
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
    }

    private void renderMenus(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        if (this.rootPopup == null) {
            return;
        }
        updateHoverAndSubmenus(mouseX, mouseY);
        renderPopup(g, this.rootPopup, mouseX, mouseY, partialTick, 0);
    }

    private void renderPopup(GuiGraphicsExtractor g, PopupState popup, int mouseX, int mouseY, float partialTick, int depth) {
        ClipRect clipRect = revealClipFor(popup);
        if (clipRect.width() <= 0 || clipRect.height() <= 0) {
            return;
        }

        g.pose().pushMatrix();
        g.pose().translate(0.0f, 0.0f);
        try {
            Gwen.withScissor(
                    g,
                    clipRect.left(),
                    clipRect.top(),
                    clipRect.right(),
                    clipRect.bottom(),
                    () -> {
                        drawPanel(g, popup.x(), popup.y(), popup.width(), popup.height());
                        for (int i = 0; i < popup.rows().size(); i++) {
                            RowRuntime row = popup.rows().get(i);
                            boolean hovered = popup.hoveredRowIndex() == i;
                            renderRow(g, row, hovered, mouseX, mouseY, partialTick);
                        }
                    }
            );
        } finally {
            g.pose().popMatrix();
        }

        if (popup.child() != null) {
            renderPopup(g, popup.child(), mouseX, mouseY, partialTick, depth + 1);
        }
    }

    private void renderRow(GuiGraphicsExtractor g, RowRuntime row, boolean hovered, int mouseX, int mouseY, float partialTick) {
        boolean clickable = isClickableRow(row);
        if (hovered && clickable) {
            g.fill(row.left(), row.top(), row.right(), row.bottom(), HOVER_BG_COLOR);
        }

        if (row.entry() instanceof SeparatorEntrySpec) {
            int y = row.top() + row.height() / 2;
            g.fill(row.contentLeft(), y, row.contentRight(), y + 1, SEPARATOR_COLOR);
            return;
        }

        if (row.entry() instanceof ActionEntrySpec actionEntry) {
            actionEntry.label().render(
                    g,
                    row.contentLeft(),
                    row.contentTop(),
                    row.contentWidth(),
                    row.contentHeight(),
                    partialTick,
                    actionEntry.enabled()
            );
            return;
        }

        if (row.entry() instanceof SubmenuEntrySpec submenuEntry) {
            int labelWidth = Math.max(0, row.contentWidth() - this.submenuArrowSpacePx);
            submenuEntry.label().render(
                    g,
                    row.contentLeft(),
                    row.contentTop(),
                    labelWidth,
                    row.contentHeight(),
                    partialTick,
                    submenuEntry.enabled()
            );
            drawSubmenuArrow(g, row, submenuEntry.enabled());
            return;
        }

        if (row.entry() instanceof ElementEntrySpec elementEntry) {
            clipRender(
                    g,
                    row.contentLeft(),
                    row.contentTop(),
                    row.contentRight(),
                    row.contentTop() + row.contentHeight(),
                    () -> {
                        elementEntry.element().render(
                                g,
                                row.contentLeft(),
                                row.contentTop(),
                                row.contentWidth(),
                                row.contentHeight(),
                                partialTick
                        );
                        for (AbstractWidget widget : row.widgets()) {
                            if (!widget.visible) {
                                continue;
                            }
                            widget.extractRenderState(g, mouseX, mouseY, partialTick);
                        }
                    }
            );
        }
    }

    private static void clipRender(GuiGraphicsExtractor g, int left, int top, int right, int bottom, Runnable renderTask) {
        Gwen.withScissor(g, left, top, right, bottom, renderTask);
    }

    private void drawSubmenuArrow(GuiGraphicsExtractor g, RowRuntime row, boolean enabled) {
        Font font = currentFont();
        if (font == null) {
            return;
        }
        String arrow = ">";
        int color = enabled ? LABEL_COLOR : LABEL_DISABLED_COLOR;
        int arrowWidth = font.width(arrow);
        int x = row.contentRight() - arrowWidth;
        int y = row.top() + (row.height() - font.lineHeight) / 2;
        g.text(font, arrow, x, y, color, false);
    }

    private static void drawPanel(GuiGraphicsExtractor g, int left, int top, int width, int height) {
        int right = left + width;
        int bottom = top + height;
        if (width <= 1 || height <= 1) {
            g.fill(left, top, right, bottom, PANEL_BORDER_COLOR);
            return;
        }

        g.fill(left, top, right, bottom, PANEL_BG_COLOR);
        g.fill(left, top, right, top + 1, PANEL_BORDER_COLOR);
        g.fill(left, bottom - 1, right, bottom, PANEL_BORDER_COLOR);
        g.fill(left, top, left + 1, bottom, PANEL_BORDER_COLOR);
        g.fill(right - 1, top, right, bottom, PANEL_BORDER_COLOR);
    }

    private ClipRect revealClipFor(PopupState popup) {
        int screenW = Math.max(0, screenWidthPx());
        int screenH = Math.max(0, screenHeightPx());

        float progress = popup.revealProgress();
        if (progress <= 0.0f) {
            return new ClipRect(0, 0, 0, 0);
        }

        if (popup.isRoot()) {
            int originX = popup.revealOriginX();
            int originY = popup.revealOriginY();
            int left = Math.round(Mth.lerp(progress, originX, popup.x()));
            int top = Math.round(Mth.lerp(progress, originY, popup.y()));
            int right = Math.round(Mth.lerp(progress, originX + 1.0f, popup.x() + popup.width()));
            int bottom = Math.round(Mth.lerp(progress, originY + 1.0f, popup.y() + popup.height()));
            return clampClip(left, top, right, bottom, screenW, screenH);
        }

        int visibleWidth = Math.max(1, Math.round(popup.width() * progress));
        int left;
        int right;
        if (popup.openToRight()) {
            left = popup.x();
            right = popup.x() + visibleWidth;
        } else {
            left = popup.x() + popup.width() - visibleWidth;
            right = popup.x() + popup.width();
        }
        return clampClip(left, popup.y(), right, popup.y() + popup.height(), screenW, screenH);
    }

    private static ClipRect clampClip(int left, int top, int right, int bottom, int screenW, int screenH) {
        int clampedLeft = Mth.clamp(left, 0, Math.max(0, screenW));
        int clampedTop = Mth.clamp(top, 0, Math.max(0, screenH));
        int clampedRight = Mth.clamp(right, 0, Math.max(0, screenW));
        int clampedBottom = Mth.clamp(bottom, 0, Math.max(0, screenH));
        if (clampedRight <= clampedLeft || clampedBottom <= clampedTop) {
            return new ClipRect(0, 0, 0, 0);
        }
        return new ClipRect(clampedLeft, clampedTop, clampedRight, clampedBottom);
    }

    private boolean isClickableRow(RowRuntime row) {
        if (row.entry() instanceof ActionEntrySpec actionEntry) {
            return actionEntry.enabled();
        }
        if (row.entry() instanceof SubmenuEntrySpec submenuEntry) {
            return submenuEntry.enabled();
        }
        if (row.entry() instanceof ElementEntrySpec elementEntry) {
            if (!elementEntry.enabled()) {
                return false;
            }
            if (elementEntry.onClick() != null) {
                return true;
            }
            for (AbstractWidget widget : row.widgets()) {
                if (widget.visible && widget.active) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTriggerArea(double mouseX, double mouseY) {
        return mouseX >= this.triggerLeftPx
                && mouseX < this.triggerLeftPx + this.triggerWidthPx
                && mouseY >= this.triggerTopPx
                && mouseY < this.triggerTopPx + this.triggerHeightPx;
    }

    private static void runAction(@Nullable Runnable action) {
        if (action == null) {
            return;
        }
        action.run();
    }

    private int screenWidthPx() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return 0;
        }
        return mc.getWindow().getGuiScaledWidth();
    }

    private int screenHeightPx() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return 0;
        }
        return mc.getWindow().getGuiScaledHeight();
    }

    private @Nullable Font currentFont() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null ? null : mc.font;
    }

    private void disposePopup(PopupState popup) {
        if (popup.child() != null) {
            disposePopup(popup.child());
        }
        popup.setChild(null);
        popup.rows().clear();
    }

    private final class MenuWidget extends AbstractWidget {
        private MenuWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            if (rootPopup != null) {
                updateHoverAndSubmenus(mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            lastMouseX = event.x();
            lastMouseY = event.y();

            if (event.button() == 1) {
                if (rootPopup == null) {
                    if (!isTriggerArea(event.x(), event.y())) {
                        return false;
                    }
                    openRootMenu(event.x(), event.y());
                    return true;
                }
                if (isTriggerArea(event.x(), event.y())) {
                    openRootMenu(event.x(), event.y());
                } else {
                    closeMenu();
                }
                return true;
            }

            if (rootPopup == null) {
                return false;
            }

            if (event.button() != 0) {
                return true;
            }

            updateHoverAndSubmenus(event.x(), event.y());
            HitTestResult hit = hitTest(event.x(), event.y());
            if (hit == null) {
                closeMenu();
                return true;
            }
            return handlePrimaryClick(hit, event.x(), event.y());
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            lastMouseX = event.x();
            lastMouseY = event.y();
            if (rootPopup == null) {
                return false;
            }
            boolean handled = releasePressedWidgets(rootPopup, event.x(), event.y(), event.button());
            return handled || event.button() == 0 || event.button() == 1;
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
            lastMouseX = event.x();
            lastMouseY = event.y();
            if (rootPopup == null) {
                return false;
            }
            if (event.button() != 0) {
                return true;
            }
            return dragPressedWidgets(rootPopup, event.x(), event.y(), event.button(), dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            if (rootPopup == null) {
                return false;
            }
            scrollWidgets(rootPopup, mouseX, mouseY, scrollX, scrollY);
            return true;
        }
    }

    private final class RuntimeChildInitContext implements InitContext {
        private @Nullable RowRuntime currentRow;

        private void setCurrentRow(RowRuntime currentRow) {
            this.currentRow = currentRow;
        }

        private void clearCurrentRow() {
            this.currentRow = null;
        }

        @Override
        public <T extends AbstractWidget> T addRenderableWidget(T widget) {
            if (this.currentRow == null) {
                throw new IllegalStateException("Context menu child widget must be attached to a row.");
            }
            this.currentRow.widgets().add(widget);
            return widget;
        }
    }

    public interface MeasurableElement {
        default int preferredMenuWidthPx() {
            return -1;
        }

        int preferredMenuHeightPx(int contentWidthPx);
    }

    public static final class Builder {
        private final MenuBuilder root = new MenuBuilder();
        private int minMenuWidthPx = DEFAULT_MIN_MENU_WIDTH_PX;
        private int baseRowHeightPx = DEFAULT_BASE_ROW_HEIGHT_PX;
        private int rowPaddingLeftPx = DEFAULT_ROW_PADDING_LEFT_PX;
        private int rowPaddingRightPx = DEFAULT_ROW_PADDING_RIGHT_PX;
        private int rowPaddingYPx = DEFAULT_ROW_PADDING_Y_PX;
        private int submenuArrowSpacePx = DEFAULT_SUBMENU_ARROW_SPACE_PX;
        private int rootOpenAnimMs = DEFAULT_ROOT_OPEN_ANIM_MS;
        private int submenuOpenAnimMs = DEFAULT_SUBMENU_OPEN_ANIM_MS;

        private Builder() {
        }

        public Builder minMenuWidthPx(int minMenuWidthPx) {
            if (minMenuWidthPx < MIN_MENU_SIZE_PX) {
                throw new IllegalArgumentException("minMenuWidthPx must be >= " + MIN_MENU_SIZE_PX);
            }
            this.minMenuWidthPx = minMenuWidthPx;
            return this;
        }

        public Builder baseRowHeightPx(int baseRowHeightPx) {
            if (baseRowHeightPx <= 0) {
                throw new IllegalArgumentException("baseRowHeightPx must be > 0");
            }
            this.baseRowHeightPx = baseRowHeightPx;
            return this;
        }

        public Builder rowPaddingPx(int leftPx, int rightPx, int yPx) {
            if (leftPx < 0 || rightPx < 0 || yPx < 0) {
                throw new IllegalArgumentException("row paddings must be >= 0");
            }
            this.rowPaddingLeftPx = leftPx;
            this.rowPaddingRightPx = rightPx;
            this.rowPaddingYPx = yPx;
            return this;
        }

        public Builder submenuArrowSpacePx(int submenuArrowSpacePx) {
            if (submenuArrowSpacePx < 0) {
                throw new IllegalArgumentException("submenuArrowSpacePx must be >= 0");
            }
            this.submenuArrowSpacePx = submenuArrowSpacePx;
            return this;
        }

        public Builder animationDurationMs(int rootOpenAnimMs, int submenuOpenAnimMs) {
            if (rootOpenAnimMs <= 0 || submenuOpenAnimMs <= 0) {
                throw new IllegalArgumentException("animation durations must be > 0");
            }
            this.rootOpenAnimMs = rootOpenAnimMs;
            this.submenuOpenAnimMs = submenuOpenAnimMs;
            return this;
        }

        public Builder item(Component text, Runnable action) {
            this.root.item(text, action);
            return this;
        }

        public Builder item(FizzyComponentElement text, Runnable action) {
            this.root.item(text, action);
            return this;
        }

        public Builder item(Component text, boolean enabled, Runnable action) {
            this.root.item(text, enabled, action);
            return this;
        }

        public Builder item(FizzyComponentElement text, boolean enabled, Runnable action) {
            this.root.item(text, enabled, action);
            return this;
        }

        public Builder submenu(Component text, Consumer<MenuBuilder> submenuDsl) {
            this.root.submenu(text, submenuDsl);
            return this;
        }

        public Builder submenu(FizzyComponentElement text, Consumer<MenuBuilder> submenuDsl) {
            this.root.submenu(text, submenuDsl);
            return this;
        }

        public Builder submenu(Component text, boolean enabled, Consumer<MenuBuilder> submenuDsl) {
            this.root.submenu(text, enabled, submenuDsl);
            return this;
        }

        public Builder submenu(FizzyComponentElement text, boolean enabled, Consumer<MenuBuilder> submenuDsl) {
            this.root.submenu(text, enabled, submenuDsl);
            return this;
        }

        public Builder separator() {
            this.root.separator();
            return this;
        }

        public Builder element(ElementPainter element) {
            this.root.element(element);
            return this;
        }

        public Builder element(ElementPainter element, Runnable onClick) {
            this.root.element(element, onClick);
            return this;
        }

        public Builder element(ElementPainter element, boolean enabled, @Nullable Runnable onClick) {
            this.root.element(element, enabled, onClick);
            return this;
        }

        public ContextMenuElement build() {
            return new ContextMenuElement(this);
        }
    }

    public static final class MenuBuilder {
        private final List<MenuEntrySpec> entries = new ArrayList<>();

        public MenuBuilder item(Component text, Runnable action) {
            return item(text, true, action);
        }

        public MenuBuilder item(FizzyComponentElement text, Runnable action) {
            return item(text, true, action);
        }

        public MenuBuilder item(Component text, boolean enabled, Runnable action) {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(action, "action");
            this.entries.add(new ActionEntrySpec(new ComponentLabel(text), enabled, action));
            return this;
        }

        public MenuBuilder item(FizzyComponentElement text, boolean enabled, Runnable action) {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(action, "action");
            this.entries.add(new ActionEntrySpec(new FceLabel(text), enabled, action));
            return this;
        }

        public MenuBuilder submenu(Component text, Consumer<MenuBuilder> submenuDsl) {
            return submenu(text, true, submenuDsl);
        }

        public MenuBuilder submenu(FizzyComponentElement text, Consumer<MenuBuilder> submenuDsl) {
            return submenu(text, true, submenuDsl);
        }

        public MenuBuilder submenu(Component text, boolean enabled, Consumer<MenuBuilder> submenuDsl) {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(submenuDsl, "submenuDsl");
            MenuBuilder child = new MenuBuilder();
            submenuDsl.accept(child);
            this.entries.add(new SubmenuEntrySpec(new ComponentLabel(text), enabled, child.buildSpec()));
            return this;
        }

        public MenuBuilder submenu(FizzyComponentElement text, boolean enabled, Consumer<MenuBuilder> submenuDsl) {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(submenuDsl, "submenuDsl");
            MenuBuilder child = new MenuBuilder();
            submenuDsl.accept(child);
            this.entries.add(new SubmenuEntrySpec(new FceLabel(text), enabled, child.buildSpec()));
            return this;
        }

        public MenuBuilder separator() {
            this.entries.add(new SeparatorEntrySpec());
            return this;
        }

        public MenuBuilder element(ElementPainter element) {
            return element(element, false, null);
        }

        public MenuBuilder element(ElementPainter element, Runnable onClick) {
            return element(element, true, onClick);
        }

        public MenuBuilder element(ElementPainter element, boolean enabled, @Nullable Runnable onClick) {
            this.entries.add(new ElementEntrySpec(Objects.requireNonNull(element, "element"), enabled, onClick));
            return this;
        }

        private MenuSpec buildSpec() {
            return new MenuSpec(List.copyOf(this.entries));
        }
    }

    private interface MenuLabel {
        int measureWidthPx(@Nullable Font font);

        int measureHeightPx(@Nullable Font font);

        void render(GuiGraphicsExtractor g,
                    int leftPx,
                    int topPx,
                    int widthPx,
                    int heightPx,
                    float partialTick,
                    boolean enabled);
    }

    private static final class ComponentLabel implements MenuLabel {
        private final Component text;

        private ComponentLabel(Component text) {
            this.text = Objects.requireNonNull(text, "text");
        }

        @Override
        public int measureWidthPx(@Nullable Font font) {
            if (font == null) {
                return 0;
            }
            String[] lines = this.text.getString().split("\\n", -1);
            int max = 0;
            for (String line : lines) {
                max = Math.max(max, font.width(line));
            }
            return max;
        }

        @Override
        public int measureHeightPx(@Nullable Font font) {
            if (font == null) {
                return 0;
            }
            int lineCount = Math.max(1, this.text.getString().split("\\n", -1).length);
            return lineCount * font.lineHeight;
        }

        @Override
        public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick, boolean enabled) {
            if (widthPx <= 0 || heightPx <= 0) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return;
            }
            Font font = mc.font;
            String raw = this.text.getString();
            String firstLine = raw;
            int breakIndex = raw.indexOf('\n');
            if (breakIndex >= 0) {
                firstLine = raw.substring(0, breakIndex);
            }
            String clipped = font.plainSubstrByWidth(firstLine, Math.max(1, widthPx));
            int color = enabled ? LABEL_COLOR : LABEL_DISABLED_COLOR;
            int y = topPx + (heightPx - font.lineHeight) / 2;
            g.text(font, clipped, leftPx, y, color, false);
        }
    }

    private static final class FceLabel implements MenuLabel {
        private final FizzyComponentElement textElement;

        private FceLabel(FizzyComponentElement textElement) {
            this.textElement = Objects.requireNonNull(textElement, "textElement");
        }

        @Override
        public int measureWidthPx(@Nullable Font font) {
            if (font == null) {
                return 0;
            }
            return measureFceWidthPx(this.textElement, font);
        }

        @Override
        public int measureHeightPx(@Nullable Font font) {
            if (font == null) {
                return 0;
            }
            return measureFceHeightPx(this.textElement, font);
        }

        @Override
        public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick, boolean enabled) {
            if (widthPx <= 0 || heightPx <= 0) {
                return;
            }
            clipRender(
                    g,
                    leftPx,
                    topPx,
                    leftPx + widthPx,
                    topPx + heightPx,
                    () -> {
                        this.textElement.render(g, leftPx, topPx, widthPx, heightPx, partialTick);
                        if (!enabled) {
                            g.fill(leftPx, topPx, leftPx + widthPx, topPx + heightPx, 0x99000000);
                        }
                    }
            );
        }
    }

    private abstract static class MenuEntrySpec {
    }

    private static final class ActionEntrySpec extends MenuEntrySpec {
        private final MenuLabel label;
        private final boolean enabled;
        private final Runnable action;

        private ActionEntrySpec(MenuLabel label, boolean enabled, Runnable action) {
            this.label = label;
            this.enabled = enabled;
            this.action = action;
        }

        private MenuLabel label() {
            return label;
        }

        private boolean enabled() {
            return enabled;
        }

        private Runnable action() {
            return action;
        }
    }

    private static final class SubmenuEntrySpec extends MenuEntrySpec {
        private final MenuLabel label;
        private final boolean enabled;
        private final MenuSpec submenu;

        private SubmenuEntrySpec(MenuLabel label, boolean enabled, MenuSpec submenu) {
            this.label = label;
            this.enabled = enabled;
            this.submenu = submenu;
        }

        private MenuLabel label() {
            return label;
        }

        private boolean enabled() {
            return enabled;
        }

        private MenuSpec submenu() {
            return submenu;
        }
    }

    private static final class SeparatorEntrySpec extends MenuEntrySpec {
    }

    private static final class ElementEntrySpec extends MenuEntrySpec {
        private final ElementPainter element;
        private final boolean enabled;
        private final @Nullable Runnable onClick;

        private ElementEntrySpec(ElementPainter element, boolean enabled, @Nullable Runnable onClick) {
            this.element = element;
            this.enabled = enabled;
            this.onClick = onClick;
        }

        private ElementPainter element() {
            return element;
        }

        private boolean enabled() {
            return enabled;
        }

        private @Nullable Runnable onClick() {
            return onClick;
        }
    }

    private record MenuSpec(List<MenuEntrySpec> entries) {
        private MenuSpec {
            entries = List.copyOf(entries);
        }
    }

    private static final class PopupState {
        private final MenuSpec menu;
        private final @Nullable PopupState parent;
        private final int ownerRowIndex;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final boolean root;
        private final boolean openToRight;
        private final int revealOriginX;
        private final int revealOriginY;
        private final long openStartMs;
        private final int openAnimMs;
        private final List<RowRuntime> rows = new ArrayList<>();

        private int hoveredRowIndex = -1;
        private @Nullable PopupState child;

        private PopupState(MenuSpec menu,
                           @Nullable PopupState parent,
                           int ownerRowIndex,
                           int x,
                           int y,
                           int width,
                           int height,
                           boolean root,
                           boolean openToRight,
                           int revealOriginX,
                           int revealOriginY,
                           long openStartMs,
                           int openAnimMs) {
            this.menu = menu;
            this.parent = parent;
            this.ownerRowIndex = ownerRowIndex;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.root = root;
            this.openToRight = openToRight;
            this.revealOriginX = revealOriginX;
            this.revealOriginY = revealOriginY;
            this.openStartMs = openStartMs;
            this.openAnimMs = openAnimMs;
        }

        private MenuSpec menu() {
            return menu;
        }

        private int ownerRowIndex() {
            return ownerRowIndex;
        }

        private int x() {
            return x;
        }

        private int y() {
            return y;
        }

        private int width() {
            return width;
        }

        private int height() {
            return height;
        }

        private boolean isRoot() {
            return root;
        }

        private boolean openToRight() {
            return openToRight;
        }

        private int revealOriginX() {
            return revealOriginX;
        }

        private int revealOriginY() {
            return revealOriginY;
        }

        private List<RowRuntime> rows() {
            return rows;
        }

        private int hoveredRowIndex() {
            return hoveredRowIndex;
        }

        private void setHoveredRowIndex(int hoveredRowIndex) {
            this.hoveredRowIndex = hoveredRowIndex;
        }

        private @Nullable PopupState child() {
            return child;
        }

        private void setChild(@Nullable PopupState child) {
            this.child = child;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.x
                    && mouseX < this.x + this.width
                    && mouseY >= this.y
                    && mouseY < this.y + this.height;
        }

        private boolean containsDescendant(double mouseX, double mouseY) {
            if (this.child == null) {
                return false;
            }
            if (this.child.contains(mouseX, mouseY)) {
                return true;
            }
            return this.child.containsDescendant(mouseX, mouseY);
        }

        private int rowIndexAt(double mouseX, double mouseY) {
            if (!contains(mouseX, mouseY)) {
                return -1;
            }
            for (int i = 0; i < this.rows.size(); i++) {
                RowRuntime row = this.rows.get(i);
                if (mouseY >= row.top() && mouseY < row.bottom()) {
                    return i;
                }
            }
            return -1;
        }

        private float revealProgress() {
            long elapsed = Math.max(0L, Util.getMillis() - this.openStartMs);
            float t = Mth.clamp(elapsed / (float) Math.max(1, this.openAnimMs), 0.0f, 1.0f);
            return easeOutCubic(t);
        }
    }

    private static final class RowRuntime {
        private final MenuEntrySpec entry;
        private final int left;
        private final int top;
        private final int width;
        private final int height;
        private final int contentLeft;
        private final int contentTop;
        private final int contentWidth;
        private final int contentHeight;
        private final List<AbstractWidget> widgets = new ArrayList<>();
        private @Nullable AbstractWidget pressedWidget;

        private RowRuntime(MenuEntrySpec entry,
                           int left,
                           int top,
                           int width,
                           int height,
                           int contentLeft,
                           int contentTop,
                           int contentWidth,
                           int contentHeight) {
            this.entry = entry;
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.contentLeft = contentLeft;
            this.contentTop = contentTop;
            this.contentWidth = contentWidth;
            this.contentHeight = contentHeight;
        }

        private MenuEntrySpec entry() {
            return entry;
        }

        private int left() {
            return left;
        }

        private int top() {
            return top;
        }

        private int width() {
            return width;
        }

        private int height() {
            return height;
        }

        private int right() {
            return this.left + this.width;
        }

        private int bottom() {
            return this.top + this.height;
        }

        private int contentLeft() {
            return contentLeft;
        }

        private int contentTop() {
            return contentTop;
        }

        private int contentWidth() {
            return contentWidth;
        }

        private int contentHeight() {
            return contentHeight;
        }

        private int contentRight() {
            return this.contentLeft + this.contentWidth;
        }

        private List<AbstractWidget> widgets() {
            return widgets;
        }

        private @Nullable AbstractWidget pressedWidget() {
            return pressedWidget;
        }

        private void setPressedWidget(@Nullable AbstractWidget pressedWidget) {
            this.pressedWidget = pressedWidget;
        }
    }

    private record MenuMeasure(int widthPx, int totalHeightPx, int rowHeightPx, List<Integer> rowHeightsPx) {
        private MenuMeasure {
            rowHeightsPx = List.copyOf(rowHeightsPx);
        }
    }

    private record ClipRect(int left, int top, int right, int bottom) {
        private int width() {
            return right - left;
        }

        private int height() {
            return bottom - top;
        }
    }

    private record HitTestResult(PopupState popup, int rowIndex, @Nullable RowRuntime row) {
    }

    private static final class ProbeInitContext implements InitContext {
        private final List<AbstractWidget> widgets = new ArrayList<>();

        @Override
        public <T extends AbstractWidget> T addRenderableWidget(T widget) {
            this.widgets.add(widget);
            return widget;
        }

        private int measuredHeightPx() {
            if (this.widgets.isEmpty()) {
                return 0;
            }
            int minTop = Integer.MAX_VALUE;
            int maxBottom = Integer.MIN_VALUE;
            for (AbstractWidget widget : this.widgets) {
                minTop = Math.min(minTop, widget.getY());
                maxBottom = Math.max(maxBottom, widget.getY() + Math.max(0, widget.getHeight()));
            }
            if (maxBottom <= minTop) {
                return 0;
            }
            return maxBottom - minTop;
        }
    }

    private static float easeOutCubic(float t) {
        float clamped = Mth.clamp(t, 0.0f, 1.0f);
        float inv = 1.0f - clamped;
        return 1.0f - inv * inv * inv;
    }
}
