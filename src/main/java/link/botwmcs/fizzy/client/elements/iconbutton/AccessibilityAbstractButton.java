package link.botwmcs.fizzy.client.elements.iconbutton;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AccessibilityAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;

    /** 三态贴图（sprite 体系） */
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("accessibility"),
            Fizzy.resourceLocation("accessibility_highlighted")
    );

    public AccessibilityAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** 子类实现点击逻辑 */
    @Override
    public abstract void onPress();

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        // 背板：按状态绘制 sprite
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        // 应用 alpha 以配合父级淡入淡出
        gg.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        gg.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), x, y, w, h);
        gg.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 文本：居中绘制（悬停时 +1 像素下沉）
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        Component msg = this.getMessage();
        int textWidth = font.width(msg);
        int textX = x + (w - textWidth) / 2;
        int textY = y + (h - 8) / 2 + (this.isHoveredOrFocused() ? 1 : 0);

        int baseRgb = this.active ? 0xFFFFFF : 0x9A9A9A;
        int argb = (Math.round(this.alpha * 255.0f) << 24) | baseRgb;

        gg.drawString(font, msg, textX, textY, argb, true);
    }
    /** Narration 更新：保持原版格式 */
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // handler.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, 1.0F));
        handler.play(SimpleSoundInstance.forUI(SoundEvents.BONE_BLOCK_BREAK, 1.0F));
    }

}
