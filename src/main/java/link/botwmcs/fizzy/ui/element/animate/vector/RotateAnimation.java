package link.botwmcs.fizzy.ui.element.animate.vector;

import link.botwmcs.fizzy.ui.element.animate.ElementAnimation;
import link.botwmcs.fizzy.ui.element.animate.ElementAnimationContext;
import link.botwmcs.fizzy.ui.element.animate.ElementTransform;

public final class RotateAnimation implements ElementAnimation {
    private final float degreesPerTick;
    private float angleDeg;
    private boolean pivotSet;
    private float pivotX;
    private float pivotY;

    public RotateAnimation(float degreesPerTick) {
        this.degreesPerTick = degreesPerTick;
    }

    public RotateAnimation startAt(float degrees) {
        this.angleDeg = degrees;
        return this;
    }

    public RotateAnimation pivot(float x, float y) {
        this.pivotSet = true;
        this.pivotX = x;
        this.pivotY = y;
        return this;
    }

    @Override
    public void tick(ElementAnimationContext context) {
        float delta = context.deltaTicks();
        if (delta == 0f) {
            return;
        }
        angleDeg += degreesPerTick * delta;
    }

    @Override
    public void apply(ElementAnimationContext context, ElementTransform transform) {
        transform.rotateDeg(angleDeg);
        if (pivotSet) {
            transform.pivot(pivotX, pivotY);
        }
    }
}
