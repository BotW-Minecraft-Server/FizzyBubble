package link.botwmcs.fizzy.ui.element.animate.vector;

import link.botwmcs.fizzy.ui.element.animate.ElementAnimation;
import link.botwmcs.fizzy.ui.element.animate.ElementAnimationContext;
import link.botwmcs.fizzy.ui.element.animate.ElementTransform;
import net.minecraft.util.Mth;

public final class ScaleAnimation implements ElementAnimation {
    private final float baseX;
    private final float baseY;
    private final float ampX;
    private final float ampY;
    private final float speed;
    private boolean pivotSet;
    private float pivotX;
    private float pivotY;

    public static ScaleAnimation fixed(float scale) {
        return new ScaleAnimation(scale, scale, 0f, 0f, 0f);
    }

    public static ScaleAnimation fixed(float scaleX, float scaleY) {
        return new ScaleAnimation(scaleX, scaleY, 0f, 0f, 0f);
    }

    public static ScaleAnimation pulse(float base, float amplitude, float speed) {
        return new ScaleAnimation(base, base, amplitude, amplitude, speed);
    }

    public static ScaleAnimation pulse(float baseX, float baseY, float ampX, float ampY, float speed) {
        return new ScaleAnimation(baseX, baseY, ampX, ampY, speed);
    }

    public ScaleAnimation(float baseX, float baseY, float ampX, float ampY, float speed) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.ampX = ampX;
        this.ampY = ampY;
        this.speed = speed;
    }

    public ScaleAnimation pivot(float x, float y) {
        this.pivotSet = true;
        this.pivotX = x;
        this.pivotY = y;
        return this;
    }

    @Override
    public void apply(ElementAnimationContext context, ElementTransform transform) {
        float phase = speed == 0f ? 0f : context.ageTicks() * speed;
        float s = speed == 0f ? 0f : Mth.sin(phase);
        float sx = baseX + ampX * s;
        float sy = baseY + ampY * s;
        transform.scale(sx, sy);
        if (pivotSet) {
            transform.pivot(pivotX, pivotY);
        }
    }
}
