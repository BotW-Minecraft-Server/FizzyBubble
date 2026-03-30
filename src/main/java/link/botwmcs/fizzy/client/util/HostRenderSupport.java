package link.botwmcs.fizzy.client.util;

import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.component.FizzyTooltipElement;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderLayer;
import link.botwmcs.fizzy.ui.kernel.render.UiRenderPhase;
import link.botwmcs.fizzy.ui.pad.PadSpec;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HostRenderSupport {
    private HostRenderSupport() {
    }

    public static List<ElementPlacement> collectElementPlacements(
            FizzyGui gui,
            FramePainter frame,
            @Nullable FramePainter.SlotArea slotArea
    ) {
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

    public static List<ElementPainter> elementsInRect(
            FizzyGui gui,
            FramePainter frame,
            @Nullable FramePainter.SlotArea slotArea,
            int x,
            int y,
            int w,
            int h
    ) {
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

    public static List<ElementPainter> elementsAtSlot(
            FizzyGui gui,
            FramePainter frame,
            int row,
            int col
    ) {
        FramePainter.SlotArea slotArea = frame.currentSlotArea();
        if (slotArea == null) {
            return List.of();
        }
        int slotX = slotArea.x() + (col - 1) * UiUnit.SLOT_PX;
        int slotY = slotArea.y() + (row - 1) * UiUnit.SLOT_PX;
        return elementsInRect(gui, frame, slotArea, slotX, slotY, UiUnit.SLOT_PX, UiUnit.SLOT_PX);
    }

    public static List<ElementPainter> elementsAtPixel(
            FizzyGui gui,
            FramePainter frame,
            int x,
            int y
    ) {
        return elementsInRect(gui, frame, frame.currentSlotArea(), x, y, 1, 1);
    }

    public static boolean shouldSuppressTooltips(List<ElementPlacement> placements) {
        for (ElementPlacement placement : placements) {
            if (placement.element().suppressesTooltips()) {
                return true;
            }
        }
        return false;
    }

    public static void renderElement(GuiGraphicsExtractor graphics, ElementPlacement placement, float partialTick) {
        ElementPainter element = placement.element();
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0f, 0.0f);
        try {
            element.render(
                    graphics,
                    placement.left(),
                    placement.top(),
                    placement.width(),
                    placement.height(),
                    partialTick
            );
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static void renderManagedWidget(GuiGraphicsExtractor graphics, ManagedWidget managedWidget, int mouseX, int mouseY, float partialTick) {
        AbstractWidget widget = managedWidget.widget();
        if (!widget.visible) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0f, 0.0f);
        try {
            widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static UiRenderLayer resolveWidgetLayer(@Nullable ElementPainter owner, AbstractWidget widget) {
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

    public static int resolveWidgetZIndex(@Nullable ElementPainter owner, AbstractWidget widget) {
        if (FizzyTooltipElement.isTooltipWidget(widget)) {
            return FizzyTooltipElement.defaultTooltipZIndex();
        }
        return owner != null ? owner.zIndex() : 0;
    }

    public static MouseButtonEvent createMouseButtonEvent(double mouseX, double mouseY, int button) {
        return new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
    }

    public static boolean dispatchOverlayMouseClicked(List<ManagedWidget> managedWidgets, MouseButtonEvent event, boolean doubleClick) {
        for (AbstractWidget widget : overlayWidgetsTopDown(managedWidgets)) {
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseClicked(event, doubleClick)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dispatchOverlayMouseReleased(List<ManagedWidget> managedWidgets, MouseButtonEvent event) {
        for (AbstractWidget widget : overlayWidgetsTopDown(managedWidgets)) {
            if (!widget.visible) {
                continue;
            }
            if (widget.mouseReleased(event)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dispatchOverlayMouseDragged(
            List<ManagedWidget> managedWidgets,
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        for (AbstractWidget widget : overlayWidgetsTopDown(managedWidgets)) {
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseDragged(event, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dispatchOverlayMouseScrolled(
            List<ManagedWidget> managedWidgets,
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        for (AbstractWidget widget : overlayWidgetsTopDown(managedWidgets)) {
            if (!widget.visible || !widget.active) {
                continue;
            }
            if (widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
    }

    private static List<AbstractWidget> overlayWidgetsTopDown(List<ManagedWidget> managedWidgets) {
        if (managedWidgets.isEmpty()) {
            return List.of();
        }

        List<ManagedWidget> overlayWidgets = new ArrayList<>();
        for (ManagedWidget managedWidget : managedWidgets) {
            if (managedWidget.layer().phase() != UiRenderPhase.OVERLAY) {
                continue;
            }
            overlayWidgets.add(managedWidget);
        }
        if (overlayWidgets.isEmpty()) {
            return List.of();
        }

        overlayWidgets.sort(Comparator
                .comparingInt((ManagedWidget widget) -> widget.layer().order())
                .thenComparingInt(ManagedWidget::zIndex)
                .thenComparingInt(ManagedWidget::serial));

        List<AbstractWidget> out = new ArrayList<>(overlayWidgets.size());
        for (int i = overlayWidgets.size() - 1; i >= 0; i--) {
            out.add(overlayWidgets.get(i).widget());
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

    public record ManagedWidget(AbstractWidget widget, UiRenderLayer layer, int zIndex, int serial) {
    }

    public record ElementPlacement(ElementPainter element, int left, int top, int width, int height, int order) {
    }
}
