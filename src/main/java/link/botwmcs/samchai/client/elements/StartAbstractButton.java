package link.botwmcs.samchai.client.elements;

import link.botwmcs.samchai.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class StartAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("play_button"),
            Fizzy.resourceLocation("button"),
            Fizzy.resourceLocation("play_button_highlighted")
    );

    public StartAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** 子类必须实现点击逻辑 */
    @Override
    public abstract void onPress();

    /** 渲染主体 */
    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.blitSprite(
                SPRITES.get(this.isActive(), this.isHoveredOrFocused()),
                this.getX(), this.getY(), this.getWidth(), this.getHeight()
        );

        int color = this.isActive() ? 0xFFFFFFFF : 0xFFA0A0A0;
        renderString(g, Minecraft.getInstance().font, color);
    }

    /** 渲染按钮文字，y 偏移与 GreatAbstractButton 不同：hover 时下移 2px */
    public void renderString(GuiGraphics g, Font font, int color) {
        int yOffset = this.isHoveredOrFocused() ? 2 : 0;
        Component msg = this.getMessage();
        int textWidth = font.width(msg);

        int textX = this.getX() + (this.getWidth() - textWidth) / 2;
        int textY = this.getY() + (this.getHeight() - 8) / 2 + yOffset;

        g.drawString(font, msg, textX, textY, color, true);
    }

    /** 鼠标点击 */
    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active && this.visible) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
        }
    }

    /** 键盘触发 */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible) {
            if (isValidClickButton(keyCode)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onPress();
                return true;
            }
        }
        return false;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // handler.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, 1.0F));
        handler.play(SimpleSoundInstance.forUI(SoundEvents.BONE_BLOCK_BREAK, 1.0F));
    }

    public void playHoverSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, 1.0F));
    }

}
