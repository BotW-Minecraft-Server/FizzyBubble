package link.botwmcs.fizzy.client.elements;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

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

    @Override
    public final void onPress(InputWithModifiers input) {
        this.onPress();
    }

    public abstract void onPress();

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SPRITES.get(this.isActive(), this.isHoveredOrFocused()),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight()
        );

        int color = this.isActive() ? 0xFFFFFFFF : 0xFFA0A0A0;
        renderString(g, Minecraft.getInstance().font, color);
    }

    public void renderString(GuiGraphicsExtractor g, Font font, int color) {
        int yOffset = this.isHoveredOrFocused() ? 2 : 0;
        FizzyGuiUtils.drawCenteredLabel(
                g,
                font,
                this.getMessage(),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                color,
                true,
                yOffset
        );
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.BONE_BLOCK_BREAK, 1.0F));
    }

    public void playHoverSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, 1.0F));
    }
}
