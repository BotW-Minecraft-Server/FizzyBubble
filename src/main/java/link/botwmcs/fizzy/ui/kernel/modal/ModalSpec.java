package link.botwmcs.fizzy.ui.kernel.modal;

import link.botwmcs.fizzy.client.overlay.Anchor;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class ModalSpec {
    private final Component title;
    private final Component message;
    private final int widthPx;
    private final int heightPx;
    private final Anchor anchor;

    private ModalSpec(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, Component.empty());
        this.message = Objects.requireNonNullElse(builder.message, Component.empty());
        this.widthPx = Math.max(120, builder.widthPx);
        this.heightPx = Math.max(64, builder.heightPx);
        this.anchor = Objects.requireNonNullElse(builder.anchor, Anchor.TOP_LEFT);
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

    public int widthPx() {
        return widthPx;
    }

    public int heightPx() {
        return heightPx;
    }

    public Anchor anchor() {
        return anchor;
    }

    public static final class Builder {
        private Component title = Component.empty();
        private Component message = Component.empty();
        private int widthPx = 220;
        private int heightPx = 96;
        private Anchor anchor = Anchor.TOP_LEFT;

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

        public Builder widthPx(int widthPx) {
            this.widthPx = widthPx;
            return this;
        }

        public Builder heightPx(int heightPx) {
            this.heightPx = heightPx;
            return this;
        }

        public Builder anchor(Anchor anchor) {
            this.anchor = anchor;
            return this;
        }

        public ModalSpec build() {
            return new ModalSpec(this);
        }
    }
}
