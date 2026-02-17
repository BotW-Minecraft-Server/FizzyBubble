package link.botwmcs.fizzy.ui.element.animate;

import link.botwmcs.fizzy.ui.element.ElementPainter;

public final class ElementAnimationContext {
    private final ElementPainter element;
    private final int leftPx;
    private final int topPx;
    private final int widthPx;
    private final int heightPx;
    private final float partialTick;
    private final float deltaSeconds;
    private final float deltaTicks;
    private final float ageSeconds;
    private final float ageTicks;
    private final boolean paused;

    public ElementAnimationContext(ElementPainter element,
                                   int leftPx, int topPx, int widthPx, int heightPx,
                                   float partialTick,
                                   float deltaSeconds, float deltaTicks,
                                   float ageSeconds, float ageTicks,
                                   boolean paused) {
        this.element = element;
        this.leftPx = leftPx;
        this.topPx = topPx;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        this.partialTick = partialTick;
        this.deltaSeconds = deltaSeconds;
        this.deltaTicks = deltaTicks;
        this.ageSeconds = ageSeconds;
        this.ageTicks = ageTicks;
        this.paused = paused;
    }

    public ElementPainter element() {
        return element;
    }

    public int leftPx() {
        return leftPx;
    }

    public int topPx() {
        return topPx;
    }

    public int widthPx() {
        return widthPx;
    }

    public int heightPx() {
        return heightPx;
    }

    public float partialTick() {
        return partialTick;
    }

    public float deltaSeconds() {
        return deltaSeconds;
    }

    public float deltaTicks() {
        return deltaTicks;
    }

    public float ageSeconds() {
        return ageSeconds;
    }

    public float ageTicks() {
        return ageTicks;
    }

    public boolean paused() {
        return paused;
    }

    public float centerX() {
        return leftPx + widthPx * 0.5f;
    }

    public float centerY() {
        return topPx + heightPx * 0.5f;
    }
}
