package link.botwmcs.fizzy.client.elements;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;

public abstract class ColoredAbstractButton extends AbstractButton {
    private static final WidgetSprites BLUE_SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("colored_button/colored_button_blue"),
            Fizzy.resourceLocation("colored_button/colored_button_blue")
    );

    private WidgetSprites sprites = BLUE_SPRITES;
    private @Nullable SoundEvent pressSound;

    public enum Color {
        BLUE,
        ORANGE,
        YELLOW,
        LIME,
        RED,
        PINK,
        CYAN
    }

    protected static WidgetSprites getSprites(Color color) {
        switch (color) {
            case ORANGE:
                return new WidgetSprites(
                        Fizzy.resourceLocation("colored_button/colored_button_orange"),
                        Fizzy.resourceLocation("colored_button/colored_button_orange")
                );
            case YELLOW:
                return new WidgetSprites(
                        Fizzy.resourceLocation("colored_button/colored_button_yellow"),
                        Fizzy.resourceLocation("colored_button/colored_button_yellow")
                );
            case LIME:
                return new WidgetSprites(
                        Fizzy.resourceLocation("colored_button/colored_button_lime"),
                        Fizzy.resourceLocation("colored_button/colored_button_lime")
                );
            case RED:
                return new WidgetSprites(
                        Fizzy.resourceLocation("colored_button/colored_button_red"),
                        Fizzy.resourceLocation("colored_button/colored_button_red")
                );
            case PINK:
                return new WidgetSprites(
                        Fizzy.resourceLocation("colored_button/colored_button_pink"),
                        Fizzy.resourceLocation("colored_button/colored_button_pink")
                );
            case CYAN:
                return new WidgetSprites(
                        Fizzy.resourceLocation("colored_button/colored_button_cyan"),
                        Fizzy.resourceLocation("colored_button/colored_button_cyan")
                );
            case BLUE:
            default:
                return BLUE_SPRITES;
        }
    }

    public ColoredAbstractButton(int x, int y, int width, int height, Component message, Color color) {
        super(x, y, width, height, message);
        this.sprites = getSprites(color);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var sprite = sprites.get(this.isActive(), this.isHoveredOrFocused());
        guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());

//        guiGraphics.blitSprite(
//                sprites.get(this.isActive(), this.isHoveredOrFocused()),
//                this.getX(), this.getY(), this.getWidth(), this.getHeight()
//        );

        int color = this.isActive() ? 0xFFFFFFFF : 0xFFA0A0A0;
        renderString(guiGraphics, Minecraft.getInstance().font, color);
    }

    public void renderString(GuiGraphics g, Font font, int color) {
        Component msg = this.getMessage();
        int textWidth = font.width(msg);
        int textX = this.getX() + (this.getWidth() - textWidth) / 2;
        int textY = this.getY() + (this.getHeight() - 8) / 2;
        g.drawString(font, msg, textX, textY, color, true);
    }

    public void setPressSound(@Nullable SoundEvent sound) {
        this.pressSound = sound;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (pressSound != null) {
            handler.play(SimpleSoundInstance.forUI(pressSound, 1.0F));
            return;
        }
        super.playDownSound(handler);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active && this.visible) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
        }
    }
}
