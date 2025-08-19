package link.botwmcs.samchai.client.elements;

import link.botwmcs.samchai.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class FizzyAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;
    private static final WidgetSprites SPRITES = new WidgetSprites(
        Fizzy.resourceLocation("button"),
        Fizzy.resourceLocation("button_highlighted")
    );

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
        Component msg = this.getMessage();
        int textWidth = font.width(msg);

        int textX = this.getX() + (this.getWidth() - textWidth) / 2;
        int textY = this.getY() + (this.getHeight() - 8) / 2 + yOffset;

        g.drawString(font, msg, textX, textY, color, true);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active && this.visible) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
        }
    }


}
