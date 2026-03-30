package link.botwmcs.fizzy.client.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public abstract class VanillaLikeAbstractButton extends AbstractButton {
    private static final int TEXT_HIGHLIGHT = rgb(0xE5, 0xE5, 0xE5);
    private static final int TEXT_DISABLED = rgb(0x75, 0x75, 0x75);
    private static final int TEXT_SHADOW = rgb(0x18, 0x18, 0x18);

    private @Nullable SoundEvent pressSound;
    private ColorTheme colorTheme;
    private boolean drawTextShadow = true;

    protected VanillaLikeAbstractButton(int x, int y, int width, int height, Component message, ColorTheme colorTheme) {
        super(x, y, width, height, message);
        this.colorTheme = Objects.requireNonNullElse(colorTheme, ColorTheme.GRAY);
    }

    @Override
    public final void onPress(InputWithModifiers input) {
        this.onPress();
    }

    public abstract void onPress();

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Style style = this.currentStyle();
        renderCodeBackground(guiGraphics, style);
        renderText(guiGraphics, style);
    }

    private Style currentStyle() {
        StyleTriplet triplet = this.colorTheme.styles();
        if (!this.active) {
            return triplet.disabled();
        }
        return this.isHoveredOrFocused() ? triplet.hover() : triplet.normal();
    }

    private void renderCodeBackground(GuiGraphicsExtractor guiGraphics, Style style) {
        if (this.getWidth() <= 2 || this.getHeight() <= 2) {
            return;
        }

        int alphaInt = clampToByte(Math.round(this.alpha * 255.0F));
        int left = this.getX() + 1;
        int top = this.getY() + 1;
        int right = this.getX() + this.getWidth() - 1;
        int bottom = this.getY() + this.getHeight() - 1;

        int base = argb(alphaInt, style.buttonColor());
        int highlight = argb(alphaInt, style.highlightColor());
        int shadow = argb(Math.round(alphaInt * 0.5F), darkerTwice(style.buttonColor()));
        int outline = argb(alphaInt, style.outlineColor());

        guiGraphics.fill(left, top, right, bottom, base);

        guiGraphics.fill(left, top, left + 1, bottom, highlight);
        guiGraphics.fill(left + 1, top, right, top + 1, highlight);

        guiGraphics.fill(right - 1, top, right, bottom, shadow);
        guiGraphics.fill(left, Math.max(top, bottom - 2), right - 1, bottom, shadow);

        guiGraphics.fill(left - 1, top - 1, right + 1, top, outline);
        guiGraphics.fill(left - 1, bottom, right + 1, bottom + 1, outline);
        guiGraphics.fill(left - 1, top, left, bottom, outline);
        guiGraphics.fill(right, top, right + 1, bottom, outline);
    }

    private void renderText(GuiGraphicsExtractor guiGraphics, Style style) {
        Font font = Minecraft.getInstance().font;
        Component message = this.getMessage();
        int alphaInt = clampToByte(Math.round(this.alpha * 255.0F));
        int textColor = argb(alphaInt, style.textColor());
        int shadowColor = argb(alphaInt, style.textShadowColor());

        int textX = this.getX() + (this.getWidth() - font.width(message)) / 2;
        int textY = this.getY() + (this.getHeight() - 8) / 2;

        if (this.drawTextShadow) {
            guiGraphics.text(font, message, textX + 1, textY + 1, shadowColor, false);
        }
        guiGraphics.text(font, message, textX, textY, textColor, false);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (this.pressSound != null) {
            handler.play(SimpleSoundInstance.forUI(this.pressSound, 1.0F));
            return;
        }
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public void setPressSound(@Nullable SoundEvent pressSound) {
        this.pressSound = pressSound;
    }

    public void setColorTheme(ColorTheme colorTheme) {
        this.colorTheme = Objects.requireNonNullElse(colorTheme, ColorTheme.GRAY);
    }

    public ColorTheme getColorTheme() {
        return this.colorTheme;
    }

    public void setDrawTextShadow(boolean drawTextShadow) {
        this.drawTextShadow = drawTextShadow;
    }

    public boolean isDrawTextShadow() {
        return this.drawTextShadow;
    }

    protected static int rgb(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private static int argb(int alpha, int rgb) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    private static int clampToByte(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static int darkerTwice(int rgb) {
        Color c = new Color(rgb);
        Color darker = c.darker().darker();
        return rgb(darker.getRed(), darker.getGreen(), darker.getBlue());
    }

    private static int brighter(int rgb) {
        Color c = new Color(rgb);
        Color brighter = c.brighter();
        return rgb(brighter.getRed(), brighter.getGreen(), brighter.getBlue());
    }

    private static Style style(int textColor, int buttonColor, int outlineColor) {
        return new Style(textColor, buttonColor, outlineColor, brighter(buttonColor), TEXT_SHADOW);
    }

    public enum ColorTheme {
        GRAY(
                new StyleTriplet(
                        style(TEXT_HIGHLIGHT, rgb(56, 56, 56), rgb(0, 4, 0)),
                        style(TEXT_HIGHLIGHT, rgb(89, 89, 89), rgb(255, 255, 255)),
                        style(TEXT_DISABLED, rgb(37, 37, 37), rgb(0, 0, 0))
                )
        ),
        NOTICE_GREEN(
                new StyleTriplet(
                        style(TEXT_HIGHLIGHT, rgb(50, 123, 68), rgb(255, 255, 255)),
                        style(TEXT_HIGHLIGHT, rgb(45, 121, 65), rgb(255, 255, 255)),
                        style(TEXT_DISABLED, rgb(17, 60, 28), rgb(0, 4, 0))
                )
        ),
        GREEN(
                new StyleTriplet(
                        style(TEXT_HIGHLIGHT, rgb(34, 97, 50), rgb(0, 4, 0)),
                        style(TEXT_HIGHLIGHT, rgb(45, 121, 65), rgb(255, 255, 255)),
                        style(TEXT_DISABLED, rgb(17, 60, 28), rgb(0, 4, 0))
                )
        ),
        BLUE(
                new StyleTriplet(
                        style(TEXT_HIGHLIGHT, rgb(39, 70, 115), rgb(0, 4, 0)),
                        style(TEXT_HIGHLIGHT, rgb(48, 115, 212), rgb(255, 255, 255)),
                        style(TEXT_DISABLED, rgb(30, 42, 60), rgb(0, 4, 0))
                )
        ),
        RED(
                new StyleTriplet(
                        style(TEXT_HIGHLIGHT, rgb(159, 68, 68), rgb(0, 4, 0)),
                        style(TEXT_HIGHLIGHT, rgb(192, 37, 37), rgb(255, 255, 255)),
                        style(TEXT_DISABLED, rgb(64, 27, 27), rgb(0, 4, 0))
                )
        );

        private final StyleTriplet styles;

        ColorTheme(StyleTriplet styles) {
            this.styles = styles;
        }

        public StyleTriplet styles() {
            return this.styles;
        }
    }

    public record StyleTriplet(Style normal, Style hover, Style disabled) {
    }

    public record Style(int textColor, int buttonColor, int outlineColor, int highlightColor, int textShadowColor) {
    }
}
