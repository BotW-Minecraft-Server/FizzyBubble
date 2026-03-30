package link.botwmcs.fizzy.client.elements.iconbutton;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AccessibilityAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;

    private static final WidgetSprites SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("accessibility"),
            Fizzy.resourceLocation("accessibility_highlighted")
    );

    public AccessibilityAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public final void onPress(InputWithModifiers input) {
        this.onPress();
    }

    public abstract void onPress();

    @Override
    protected void extractContents(GuiGraphicsExtractor gg, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        gg.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()), x, y, w, h, ARGB.white(this.alpha));

        int baseRgb = this.active ? 0xFFFFFF : 0x9A9A9A;
        int argb = FizzyGuiUtils.withAlpha(baseRgb, this.alpha);
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.BONE_BLOCK_BREAK, 1.0F));
    }
}
