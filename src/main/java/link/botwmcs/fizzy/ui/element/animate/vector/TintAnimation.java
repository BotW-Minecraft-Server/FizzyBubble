package link.botwmcs.fizzy.ui.element.animate.vector;

import link.botwmcs.fizzy.ui.element.animate.ElementAnimation;
import link.botwmcs.fizzy.ui.element.animate.ElementAnimationContext;
import link.botwmcs.fizzy.ui.element.animate.ElementTransform;
import net.minecraft.util.Mth;

public final class TintAnimation implements ElementAnimation {
    private final int colorA;
    private final int colorB;
    private final float speed;

    public static TintAnimation fixed(int rgb) {
        return new TintAnimation(rgb, rgb, 0f);
    }

    public static TintAnimation pulse(int fromRgb, int toRgb, float speed) {
        return new TintAnimation(fromRgb, toRgb, speed);
    }

    public TintAnimation(int colorA, int colorB, float speed) {
        this.colorA = colorA;
        this.colorB = colorB;
        this.speed = speed;
    }

    @Override
    public void apply(ElementAnimationContext context, ElementTransform transform) {
        float t = speed == 0f ? 0f : (Mth.sin(context.ageTicks() * speed) * 0.5f + 0.5f);
        int color = lerpRgb(colorA, colorB, t);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        transform.setColor(r, g, b, 1.0f);
    }

    private static int lerpRgb(int c1, int c2, float t) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        int r = (int) Mth.lerp(t, r1, r2);
        int g = (int) Mth.lerp(t, g1, g2);
        int b = (int) Mth.lerp(t, b1, b2);
        return (r << 16) | (g << 8) | b;
    }
}
