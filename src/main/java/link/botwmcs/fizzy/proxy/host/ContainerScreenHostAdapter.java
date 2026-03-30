package link.botwmcs.fizzy.proxy.host;

import link.botwmcs.fizzy.mixin.client.AbstractContainerScreenAccessor;
import link.botwmcs.fizzy.proxy.api.HostRenderStage;
import link.botwmcs.fizzy.proxy.api.HostStageCapabilities;
import link.botwmcs.fizzy.ui.core.UiUnit;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ContainerScreenHostAdapter implements HostAdapter {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("fizzy", "container_screen");
    private static final HostStageCapabilities STAGES = HostStageCapabilities.of(
            HostRenderStage.SCREEN_PRE,
            HostRenderStage.SOURCE_BG_PRE,
            HostRenderStage.SOURCE_BG_POST,
            HostRenderStage.SOURCE_CONTENT_PRE,
            HostRenderStage.SOURCE_CONTENT_POST,
            HostRenderStage.SOURCE_TOOLTIP_PRE,
            HostRenderStage.SOURCE_TOOLTIP_POST,
            HostRenderStage.SCREEN_POST
    );
    private static final int SLOT_PIXEL_SIZE = 16;
    private static final int AXIS_CLUSTER_TOLERANCE = 3;

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public int priority() {
        return 1_000;
    }

    @Override
    public boolean supports(Screen screen) {
        return screen instanceof AbstractContainerScreen<?>;
    }

    @Override
    public HostGeometry resolveGeometry(Screen screen) {
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) screen;
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) container;

        int panelLeft = accessor.fizzy$getLeftPos();
        int panelTop = accessor.fizzy$getTopPos();
        int panelWidth = Math.max(0, accessor.fizzy$getImageWidth());
        int panelHeight = Math.max(0, accessor.fizzy$getImageHeight());

        Rect visible = Rect.of(panelLeft, panelTop, panelWidth, panelHeight);
        Rect slotBounds = resolveSlotBounds(container, panelLeft, panelTop);
        if (slotBounds != null) {
            visible = visible.union(slotBounds);
        }

        Rect widgetBounds = resolveWidgetBounds(container);
        if (widgetBounds != null) {
            visible = visible.union(widgetBounds);
        }

        visible = visible.clampToScreen(screen.width, screen.height);
        SlotGridGeometry slotGrid = resolveSlotGridGeometry(container, panelLeft, panelTop);
        return new HostGeometry(
                visible.left(),
                visible.top(),
                visible.width(),
                visible.height(),
                slotGrid
        );
    }

    @Override
    public HostStageCapabilities stageCapabilities(Screen screen) {
        return STAGES;
    }

    private static @Nullable Rect resolveSlotBounds(AbstractContainerScreen<?> container, int panelLeft, int panelTop) {
        AbstractContainerMenu menu = container.getMenu();
        if (menu == null || menu.slots.isEmpty()) {
            return null;
        }

        Rect out = null;
        for (Slot slot : menu.slots) {
            int absX = panelLeft + slot.x;
            int absY = panelTop + slot.y;
            Rect slotRect = Rect.of(absX, absY, SLOT_PIXEL_SIZE, SLOT_PIXEL_SIZE);
            out = out == null ? slotRect : out.union(slotRect);
        }
        return out;
    }

    private static @Nullable Rect resolveWidgetBounds(AbstractContainerScreen<?> container) {
        Rect out = null;
        for (GuiEventListener child : container.children()) {
            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }
            if (!widget.visible) {
                continue;
            }
            Rect widgetRect = Rect.of(
                    widget.getX(),
                    widget.getY(),
                    Math.max(0, widget.getWidth()),
                    Math.max(0, widget.getHeight())
            );
            out = out == null ? widgetRect : out.union(widgetRect);
        }
        return out;
    }

    private static @Nullable SlotGridGeometry resolveSlotGridGeometry(AbstractContainerScreen<?> container, int panelLeft, int panelTop) {
        AbstractContainerMenu menu = container.getMenu();
        if (menu == null || menu.slots.isEmpty()) {
            return null;
        }

        List<Integer> xs = new ArrayList<>(menu.slots.size());
        List<Integer> ys = new ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            xs.add(panelLeft + slot.x);
            ys.add(panelTop + slot.y);
        }
        if (xs.isEmpty() || ys.isEmpty()) {
            return null;
        }

        AxisClusters xAxis = clusterAxis(xs);
        AxisClusters yAxis = clusterAxis(ys);
        if (xAxis == null || yAxis == null) {
            return null;
        }

        return new SlotGridGeometry(
                xAxis.origin(),
                yAxis.origin(),
                Math.max(1, xAxis.count()),
                Math.max(1, yAxis.count()),
                UiUnit.SLOT_PX
        );
    }

    private static @Nullable AxisClusters clusterAxis(List<Integer> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }
        Collections.sort(coordinates);

        int origin = coordinates.get(0);
        int clusters = 1;
        int anchor = origin;
        for (int i = 1; i < coordinates.size(); i++) {
            int value = coordinates.get(i);
            if (Math.abs(value - anchor) <= AXIS_CLUSTER_TOLERANCE) {
                continue;
            }
            clusters++;
            anchor = value;
        }
        return new AxisClusters(origin, clusters);
    }

    private record Rect(int left, int top, int right, int bottom) {
        static Rect of(int left, int top, int width, int height) {
            int safeWidth = Math.max(0, width);
            int safeHeight = Math.max(0, height);
            return new Rect(left, top, left + safeWidth, top + safeHeight);
        }

        int width() {
            return Math.max(0, right - left);
        }

        int height() {
            return Math.max(0, bottom - top);
        }

        Rect union(Rect other) {
            return new Rect(
                    Math.min(this.left, other.left),
                    Math.min(this.top, other.top),
                    Math.max(this.right, other.right),
                    Math.max(this.bottom, other.bottom)
            );
        }

        Rect clampToScreen(int screenWidth, int screenHeight) {
            int clampedLeft = Math.max(0, Math.min(this.left, Math.max(0, screenWidth)));
            int clampedTop = Math.max(0, Math.min(this.top, Math.max(0, screenHeight)));
            int clampedRight = Math.max(clampedLeft, Math.min(this.right, Math.max(0, screenWidth)));
            int clampedBottom = Math.max(clampedTop, Math.min(this.bottom, Math.max(0, screenHeight)));
            return new Rect(clampedLeft, clampedTop, clampedRight, clampedBottom);
        }
    }

    private record AxisClusters(int origin, int count) {
    }
}
