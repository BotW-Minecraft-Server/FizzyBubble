package link.botwmcs.fizzy.client.util;

import link.botwmcs.fizzy.ui.element.component.FizzyTooltipElement;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.Nullable;

public final class FizzyTooltipWidgetUtil {
    private FizzyTooltipWidgetUtil() {
    }

    public static void hide(@Nullable FizzyTooltipElement tooltipElement) {
        setVisible(tooltipElement, false);
    }

    public static void show(@Nullable FizzyTooltipElement tooltipElement) {
        setVisible(tooltipElement, true);
    }

    public static void setVisible(@Nullable FizzyTooltipElement tooltipElement, boolean visible) {
        if (tooltipElement == null) {
            return;
        }
        for (AbstractWidget widget : tooltipElement.widgets()) {
            widget.visible = visible;
            widget.active = false;
        }
    }
}
