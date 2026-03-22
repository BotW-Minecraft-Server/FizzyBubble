package link.botwmcs.fizzy.client.util;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.host.FizzyMenuScreenHost;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class BlockingElementSupport {
    private BlockingElementSupport() {
    }

    public static List<ElementPainter> elementsAtCurrentScreenPx(int x, int y) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof FizzyScreenHost host) {
            return host.elementsAtPx(x, y);
        }
        if (screen instanceof FizzyMenuScreenHost<?> host) {
            return host.elementsAtPx(x, y);
        }
        return List.of();
    }

    public static void disableUnderlyingWidgets(
            List<ElementPainter> elements,
            ElementPainter owner,
            Map<AbstractWidget, Boolean> storedActive
    ) {
        for (ElementPainter element : elements) {
            if (element == owner) {
                continue;
            }
            List<AbstractWidget> widgets = element.widgets();
            if (widgets.isEmpty()) {
                continue;
            }
            FizzyGuiUtils.disableWidgets(widgets, storedActive);
        }
    }

    public static void restoreWidgets(Map<AbstractWidget, Boolean> storedActive) {
        FizzyGuiUtils.restoreWidgetStates(storedActive);
    }

    public static Map<AbstractWidget, Boolean> newWidgetStateMap() {
        return new IdentityHashMap<>();
    }
}
