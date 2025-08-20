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

public abstract class SingleplayerAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;

    // 三态 sprite（正常/悬停/禁用）——这里示例为同一张，你可以替换成各自不同的路径
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("selector/singleplayer"),
            Fizzy.resourceLocation("selector/singleplayer")
    );

    public SingleplayerAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        // 如果需要自定义初始 alpha：
        // this.setAlpha(1.0f);
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

        // 文字渲染（使用 AbstractButton 自带的布局逻辑）
        Font font = Minecraft.getInstance().font;
        this.renderString(gg, font, color);
    }

    /** 鼠标点击：父类已处理为 onPress()，无需改动；若要拦截可在此 override 后再调 super */
    // @Override
    // public boolean mouseClicked(double mouseX, double mouseY, int button) { ... }

    /** 键盘回车/空格触发与叙述播放：等价于你原来的 key 处理 */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isActive() && this.isFocused() && CommonInputs.selected(keyCode)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** 点击音效（可保留默认，也可自定义） */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }
}
