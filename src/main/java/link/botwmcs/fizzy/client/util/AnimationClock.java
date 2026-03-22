package link.botwmcs.fizzy.client.util;

import net.minecraft.Util;

public final class AnimationClock {
    private long lastUpdateMs;

    public TickDelta tick(boolean paused) {
        long now = Util.getMillis();
        if (lastUpdateMs == 0L) {
            lastUpdateMs = now;
            return TickDelta.ZERO;
        }

        float deltaSeconds = (now - lastUpdateMs) / 1000.0f;
        lastUpdateMs = now;
        if (deltaSeconds < 0.0f) {
            deltaSeconds = 0.0f;
        }
        if (paused) {
            return TickDelta.ZERO;
        }
        return new TickDelta(deltaSeconds, deltaSeconds * 20.0f);
    }

    public record TickDelta(float seconds, float ticks) {
        public static final TickDelta ZERO = new TickDelta(0.0f, 0.0f);
    }
}
