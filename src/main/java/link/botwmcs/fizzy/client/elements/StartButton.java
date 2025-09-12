package link.botwmcs.fizzy.client.elements;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class StartButton extends StartAbstractButton {
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int BIG_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int DEFAULT_SPACING = 8;

    protected static final CreateNarration DEFAULT_NARRATION = supplier -> supplier.get().copy();

    protected final OnPress onPress;
    protected final CreateNarration createNarration;

    public static Builder builder(Component component, OnPress onPress) {
        return new Builder(component, onPress);
    }

    public StartButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.createNarration = createNarration;
    }


    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    /** 等价于 method_25360：用于辅助功能的旁白文本 */
    @Override
    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> StartButton.super.createNarrationMessage());
    }

    /** 等价于 method_47399：更新旁白内容 */
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output); // 等价 method_37021
    }

    // ---------------------------
    // Builder
    // ---------------------------
    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private @Nullable Tooltip tooltip;
        private int x;
        private int y;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private CreateNarration createNarration;

        public Builder(Component component, OnPress onPress) {
            this.createNarration = StartButton.DEFAULT_NARRATION;
            this.message = component;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int w) {
            this.width = w;
            return this;
        }

        public Builder size(int w, int h) {
            this.width = w;
            this.height = h;
            return this;
        }

        public Builder bounds(int x, int y, int w, int h) {
            return this.pos(x, y).size(w, h);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public StartButton build() {
            StartButton button = new StartButton(
                    this.x, this.y, this.width, this.height,
                    this.message, this.onPress, this.createNarration
            );
            button.setTooltip(this.tooltip); // 等价 method_47400
            return button;
        }
    }

    // ---------------------------
    // 接口
    // ---------------------------
    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> defaultMessage);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(StartButton button);
    }
}
