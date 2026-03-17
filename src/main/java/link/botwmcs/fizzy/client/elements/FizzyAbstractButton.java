package link.botwmcs.fizzy.client.elements;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class FizzyAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;
    private static final WidgetSprites SPRITES = new WidgetSprites(
        Fizzy.resourceLocation("button"),
        Fizzy.resourceLocation("button_highlighted")
    );
    private @Nullable SoundEvent pressSound;

    public FizzyAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(
                SPRITES.get(this.isActive(), this.isHoveredOrFocused()),
                this.getX(), this.getY(), this.getWidth(), this.getHeight()
        );

        int color = this.isActive() ? 0xFFFFFFFF : 0xFFA0A0A0;
        renderString(guiGraphics, Minecraft.getInstance().font, color);

    }

    public void renderString(GuiGraphics g, Font font, int color) {
        int yOffset = this.isHoveredOrFocused() ? 1 : 0;
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
    public void onClick(double mouseX, double mouseY) {
        if (this.active && this.visible) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (pressSound != null) {
            handler.play(SimpleSoundInstance.forUI(pressSound, 1.0F));
            return;
        }
        handler.play(SimpleSoundInstance.forUI(SoundEvents.BONE_BLOCK_BREAK, 1.0F));
    }

    public void setPressSound(@Nullable SoundEvent sound) {
        this.pressSound = sound;
    }

    public void playHoverSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, 1.0F));
    }


}
