package link.botwmcs.fizzy.ui.element.animate.vector;

import link.botwmcs.fizzy.ui.element.animate.ElementAnimation;
import link.botwmcs.fizzy.ui.element.animate.ElementAnimationContext;
import link.botwmcs.fizzy.ui.element.animate.ElementTransform;
import net.minecraft.util.Mth;

public final class FreeFallAnimation implements ElementAnimation {
    private final float gravity;
    private float velocity;
    private float offsetY;
    private float terminalVelocity = Float.NaN;
    private float minY = Float.NaN;
    private float maxY = Float.NaN;
    private float bounce = 0f;

    public FreeFallAnimation(float gravity) {
        this.gravity = gravity;
    }

    public FreeFallAnimation startAt(float y) {
        this.offsetY = y;
        return this;
    }

    public FreeFallAnimation velocity(float v) {
        this.velocity = v;
        return this;
    }

    public FreeFallAnimation terminalVelocity(float v) {
        this.terminalVelocity = v;
        return this;
    }

    public FreeFallAnimation clampY(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
        return this;
    }

    public FreeFallAnimation bounce(float factor) {
        this.bounce = Mth.clamp(factor, 0f, 1f);
        return this;
    }

    @Override
    public void tick(ElementAnimationContext context) {
        float dt = context.deltaTicks();
        if (dt == 0f) {
            return;
        }
        velocity += gravity * dt;
        if (Float.isFinite(terminalVelocity)) {
            velocity = Mth.clamp(velocity, -terminalVelocity, terminalVelocity);
        }
        offsetY += velocity * dt;

        if (Float.isFinite(minY) && offsetY < minY) {
            offsetY = minY;
            velocity = bounce > 0f ? -velocity * bounce : 0f;
        }
        if (Float.isFinite(maxY) && offsetY > maxY) {
            offsetY = maxY;
            velocity = bounce > 0f ? -velocity * bounce : 0f;
        }
    }

    @Override
    public void apply(ElementAnimationContext context, ElementTransform transform) {
        transform.offset(0f, offsetY);
    }
}
