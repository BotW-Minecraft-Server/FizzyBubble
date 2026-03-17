package link.botwmcs.fizzy.client.elements.iconbutton;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import net.minecraft.client.Minecraft;
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
public abstract class LangSelectAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;

    /** 三态 sprite（放置于 assets/auui/textures/gui/sprites/title/...） */
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("language"),
            Fizzy.resourceLocation("language_highlighted")
    );

    public LangSelectAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** 子类实现：点击逻辑 */
    @Override
    public abstract void onPress();

    /** 渲染：底板 sprite + 文本 */
    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        final int x = getX();
        final int y = getY();
        final int w = getWidth();
        final int h = getHeight();

        // 背板（按 active/hovered 选 sprite），带 alpha
        gg.setColor(1f, 1f, 1f, this.alpha);
        gg.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), x, y, w, h);
        gg.setColor(1f, 1f, 1f, 1f);

        // 文本：居中 + 悬停下沉 1px
        int rgb = this.active ? 0xFFFFFF : 0x9A9A9A;
        int argb = FizzyGuiUtils.withAlpha(rgb, this.alpha);
        FizzyGuiUtils.drawCenteredLabel(
                gg,
                Minecraft.getInstance().font,
                this.getMessage(),
                x,
                y,
                w,
                h,
                argb,
                true,
                this.isHoveredOrFocused() ? 1 : 0
        );
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
