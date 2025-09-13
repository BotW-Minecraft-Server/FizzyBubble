package link.botwmcs.fizzy.client.elements.iconbutton;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public abstract class MultiplayerAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;

    // 三态 sprite（正常/悬停/禁用）——此处三张相同，你可替换为实际三张
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("selector/multiplayer"),
            Fizzy.resourceLocation("selector/multiplayer")
    );

    public MultiplayerAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** 点击行为交给子类实现 */
    @Override
    public abstract void onPress();

    /** 渲染：底图 + 文本 */
    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        // 绘制按钮底图（根据 active/hovered 取对应 sprite）
        var sprite = SPRITES.get(this.isActive(), this.isHoveredOrFocused());
        gg.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        // 文本颜色：启用白色、禁用灰色
        int color = this.isActive() ? 0xFFFFFF : 0x9E9E9E;

        Font font = Minecraft.getInstance().font;
        this.renderString(gg, font, color);
    }

    /** 键盘触发（空格/回车） */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isActive() && this.isFocused() && CommonInputs.selected(keyCode)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** 点击音效 */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }

}
