package link.botwmcs.fizzy.client.elements.iconbutton;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class AccessibilityButton extends AccessibilityAbstractButton {
    public static final int SMALL_WIDTH     = 120;
    public static final int DEFAULT_WIDTH   = 150;
    public static final int BIG_WIDTH       = 200;
    public static final int DEFAULT_HEIGHT  = 20;
    public static final int DEFAULT_SPACING = 8;

    /** 默认旁白：直接使用父类的旁白文本 */
    protected static final CreateNarration DEFAULT_NARRATION = supplier -> supplier.get().copy();

    protected final OnPress onPress;
    protected final CreateNarration createNarration;

    public static Builder builder(Component component, OnPress onPress) {
        return new Builder(component, onPress);
    }

    public AccessibilityButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.createNarration = createNarration;
    }

    /** 点击回调：转发给外部提供的 OnPress */
    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    /** 旁白文本：允许通过 CreateNarration 包装/替换父类默认旁白 */
    @Override
    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> AccessibilityButton.super.createNarrationMessage());
    }

    /** Narration 更新：保持原版按钮的默认描述格式 */
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    // ====================== Builder ======================

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        public final OnPress onPress;

        private @Nullable Tooltip tooltip;
        private int x;
        private int y;
        private int width  = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private CreateNarration createNarration = AccessibilityButton.DEFAULT_NARRATION;

        public Builder(Component component, OnPress onPress) {
            this.message = component;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public AccessibilityButton build() {
            AccessibilityButton button = new AccessibilityButton(
                    this.x, this.y, this.width, this.height, this.message, this.onPress, this.createNarration
            );
            if (this.tooltip != null) {
                button.setTooltip(this.tooltip);
            }
            return button;
        }
    }

    // ====================== 接口 ======================

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<Component> parentSupplier);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(AccessibilityButton button);
    }
}
