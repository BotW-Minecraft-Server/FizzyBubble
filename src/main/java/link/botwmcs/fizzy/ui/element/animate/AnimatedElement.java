package link.botwmcs.fizzy.ui.element.animate;

import link.botwmcs.fizzy.client.util.AnimationClock;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AnimatedElement implements ElementPainter {
    private final ElementPainter delegate;
    private final List<ElementAnimation> animations;
    private final AnimationClock animationClock = new AnimationClock();
    private float ageSeconds;
    private float ageTicks;

    public AnimatedElement(ElementPainter delegate, List<ElementAnimation> animations) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.animations = new ArrayList<>(Objects.requireNonNull(animations, "animations"));
    }

    public static Builder builder(ElementPainter delegate) {
        return new Builder(delegate);
    }

    public ElementPainter delegate() {
        return delegate;
    }

    public List<ElementAnimation> animations() {
        return Collections.unmodifiableList(animations);
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        delegate.init(context, leftPx, topPx, widthPx, heightPx);
        for (ElementAnimation animation : animations) {
            animation.init(context, leftPx, topPx, widthPx, heightPx);
        }
    }

    @Override
    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (animations.isEmpty()) {
            delegate.render(g, leftPx, topPx, widthPx, heightPx, partialTick);
            return;
        }

        FrameTime frameTime = updateTime();
        ElementAnimationContext ctx = new ElementAnimationContext(
                delegate,
                leftPx, topPx, widthPx, heightPx,
                partialTick,
                frameTime.deltaSeconds, frameTime.deltaTicks,
                ageSeconds, ageTicks,
                frameTime.paused
        );

        for (ElementAnimation animation : animations) {
            animation.tick(ctx);
        }

        ElementTransform transform = new ElementTransform();
        for (ElementAnimation animation : animations) {
            animation.apply(ctx, transform);
        }

        float exactLeft = leftPx + transform.offsetX();
        float exactTop = topPx + transform.offsetY();
        int renderLeft = Math.round(exactLeft);
        int renderTop = Math.round(exactTop);
        float fracX = exactLeft - renderLeft;
        float fracY = exactTop - renderTop;
        transform.clearOffset();

        g.pose().pushMatrix();
        if (fracX != 0.0f || fracY != 0.0f) {
            g.pose().translate(fracX, fracY);
        }
        transform.applyToPose(g.pose(), renderLeft, renderTop, widthPx, heightPx);
        boolean colorApplied = transform.applyColor(g);

        for (ElementAnimation animation : animations) {
            animation.beforeRender(ctx, g);
        }
        delegate.render(g, renderLeft, renderTop, widthPx, heightPx, partialTick);
        for (int i = animations.size() - 1; i >= 0; i--) {
            animations.get(i).afterRender(ctx, g);
        }

        if (colorApplied) {
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        g.pose().popMatrix();
    }

    @Override
    public ElementType type() {
        return delegate.type();
    }

    private FrameTime updateTime() {
        Minecraft mc = Minecraft.getInstance();
        boolean paused = mc != null && mc.isPaused();
        AnimationClock.TickDelta delta = animationClock.tick(paused);
        float deltaSeconds = delta.seconds();
        float deltaTicks = delta.ticks();
        ageSeconds += deltaSeconds;
        ageTicks += deltaTicks;
        return new FrameTime(deltaSeconds, deltaTicks, paused);
    }

    private record FrameTime(float deltaSeconds, float deltaTicks, boolean paused) {
        private static final FrameTime ZERO = new FrameTime(0f, 0f, false);
    }

    public static final class Builder {
        private final ElementPainter delegate;
        private final List<ElementAnimation> animations = new ArrayList<>();

        private Builder(ElementPainter delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        public Builder add(ElementAnimation animation) {
            animations.add(Objects.requireNonNull(animation, "animation"));
            return this;
        }

        public Builder addAll(ElementAnimation... animations) {
            if (animations == null || animations.length == 0) {
                return this;
            }
            for (ElementAnimation animation : animations) {
                if (animation != null) {
                    this.animations.add(animation);
                }
            }
            return this;
        }

        public Builder addAll(Iterable<? extends ElementAnimation> animations) {
            if (animations == null) {
                return this;
            }
            for (ElementAnimation animation : animations) {
                if (animation != null) {
                    this.animations.add(animation);
                }
            }
            return this;
        }

        public AnimatedElement build() {
            return new AnimatedElement(delegate, animations);
        }
    }
}
