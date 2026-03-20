package link.botwmcs.fizzy.ui.kernel.notification;

import link.botwmcs.fizzy.client.overlay.Anchor;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class NotificationSpec {
    private final Component title;
    private final Component message;
    private final NotificationLevel level;
    private final int durationTicks;
    private final Anchor anchor;

    private NotificationSpec(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, Component.empty());
        this.message = Objects.requireNonNullElse(builder.message, Component.empty());
        this.level = Objects.requireNonNullElse(builder.level, NotificationLevel.INFO);
        this.durationTicks = Math.max(20, builder.durationTicks);
        this.anchor = Objects.requireNonNullElse(builder.anchor, Anchor.TOP_RIGHT);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Component title() {
        return title;
    }

    public Component message() {
        return message;
    }

    public NotificationLevel level() {
        return level;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public Anchor anchor() {
        return anchor;
    }

    public static final class Builder {
        private Component title = Component.empty();
        private Component message = Component.empty();
        private NotificationLevel level = NotificationLevel.INFO;
        private int durationTicks = 80;
        private Anchor anchor = Anchor.TOP_RIGHT;

        private Builder() {
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder level(NotificationLevel level) {
            this.level = level;
            return this;
        }

        public Builder durationTicks(int durationTicks) {
            this.durationTicks = durationTicks;
            return this;
        }

        public Builder anchor(Anchor anchor) {
            this.anchor = anchor;
            return this;
        }

        public NotificationSpec build() {
            return new NotificationSpec(this);
        }
    }
}
