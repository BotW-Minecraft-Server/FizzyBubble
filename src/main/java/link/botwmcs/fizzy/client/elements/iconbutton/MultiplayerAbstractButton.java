package link.botwmcs.fizzy.client.elements.iconbutton;

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
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public abstract class MultiplayerAbstractButton extends AbstractButton {
    protected static final int TEXT_MARGIN = 2;

    private static final WidgetSprites SPRITES = new WidgetSprites(
            Fizzy.resourceLocation("selector/multiplayer"),
            Fizzy.resourceLocation("selector/multiplayer")
    );

    public MultiplayerAbstractButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public final void onPress(InputWithModifiers input) {
        this.onPress();
    }

    public abstract void onPress();

    @Override
    protected void extractContents(GuiGraphicsExtractor gg, int mouseX, int mouseY, float partialTick) {
        var sprite = SPRITES.get(this.isActive(), this.isHoveredOrFocused());
        gg.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        int color = this.isActive() ? 0xFFFFFFFF : 0xFF9E9E9E;
        Font font = Minecraft.getInstance().font;
        FizzyGuiUtils.drawCenteredLabel(gg, font, this.getMessage(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), color, true, 0);
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }
}
