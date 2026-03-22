package link.botwmcs.fizzy.ui.kernel.render;

import java.util.Objects;

public record UiRenderLayer(UiRenderPhase phase, int order) implements Comparable<UiRenderLayer> {
    public UiRenderLayer {
        phase = Objects.requireNonNull(phase, "phase");
    }

    public static UiRenderLayer behind(int order) {
        return new UiRenderLayer(UiRenderPhase.BEHIND, order);
    }

    public static UiRenderLayer background(int order) {
        return new UiRenderLayer(UiRenderPhase.BACKGROUND, order);
    }

    public static UiRenderLayer frame(int order) {
        return new UiRenderLayer(UiRenderPhase.FRAME, order);
    }

    public static UiRenderLayer elements(int order) {
        return new UiRenderLayer(UiRenderPhase.ELEMENT, order);
    }

    public static UiRenderLayer split(int order) {
        return new UiRenderLayer(UiRenderPhase.SPLIT, order);
    }

    public static UiRenderLayer widgets(int order) {
        return new UiRenderLayer(UiRenderPhase.WIDGET, order);
    }

    public static UiRenderLayer tooltip(int order) {
        return new UiRenderLayer(UiRenderPhase.TOOLTIP, order);
    }

    public static UiRenderLayer overlay(int order) {
        return new UiRenderLayer(UiRenderPhase.OVERLAY, order);
    }

    @Override
    public int compareTo(UiRenderLayer other) {
        int phaseCompare = Integer.compare(this.phase.ordinal(), other.phase.ordinal());
        if (phaseCompare != 0) {
            return phaseCompare;
        }
        return Integer.compare(this.order, other.order);
    }
}
