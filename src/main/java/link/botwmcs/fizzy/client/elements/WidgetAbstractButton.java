package link.botwmcs.fizzy.client.elements;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;

import javax.annotation.Nullable;

public abstract class WidgetAbstractButton extends AbstractButton {
    private static final Identifier WIDGETS_TEXTURE = Fizzy.resourceLocation("textures/gui/ui/widgets.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int SPRITE_WIDTH = 15;
    private static final int SPRITE_HEIGHT = 14;
    private static final int INTERNAL_GAP = 2;
    private static final int GROUP_SPACING = 8;
    private static final int ROW_SPACING = 3;
    private static final int GROUP_SIZE = 4;

    private final WidgetType type;
    private final WidgetColor color;
    private final ArrowDirection direction;
    private final boolean stretchToFit;
    private final Sprite sprite;
    private @Nullable SoundEvent pressSound;

    public WidgetAbstractButton(int x, int y, int width, int height, Component message,
                                WidgetType type, WidgetColor color, ArrowDirection direction,
                                boolean stretchToFit) {
        super(x, y, width, height, message);
        this.type = type;
        this.color = color;
        this.direction = direction;
        this.stretchToFit = stretchToFit;
        this.sprite = spriteFor(type, color, direction);
    }

    @Override
    public final void onPress(InputWithModifiers input) {
        this.onPress();
    }

    public abstract void onPress();

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Sprite sprite = this.sprite;
        if (sprite == null) {
            return;
        }

        int spriteWidth = sprite.width();
        int spriteHeight = sprite.height();

        int drawWidth = this.stretchToFit ? this.getWidth() : spriteWidth;
        int drawHeight = this.stretchToFit ? this.getHeight() : spriteHeight;

        int drawX = this.getX() + (this.getWidth() - drawWidth) / 2;
        int drawY = this.getY() + (this.getHeight() - drawHeight) / 2;

        float alpha = this.active ? 1.0F : 0.5F;
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                WIDGETS_TEXTURE,
                drawX,
                drawY,
                sprite.u(),
                sprite.v(),
                drawWidth,
                drawHeight,
                spriteWidth,
                spriteHeight,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                ARGB.white(alpha)
        );
    }

    // tools
    public WidgetType type() {
        return this.type;
    }

    public WidgetColor color() {
        return this.color;
    }

    public ArrowDirection direction() {
        return this.direction;
    }

    public boolean stretchToFit() {
        return this.stretchToFit;
    }

    public Sprite sprite() {
        return this.sprite;
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

    protected static Sprite spriteFor(WidgetType type, WidgetColor color, ArrowDirection direction) {
        int colorIndex = color.ordinal();
        int column = colorIndex % GROUP_SIZE;
        int rowGroup = colorIndex / GROUP_SIZE;
        int rowIndex = type.rowOffset() + rowGroup;

        int baseU = column * (SPRITE_WIDTH * 2 + INTERNAL_GAP + GROUP_SPACING);
        if (direction == ArrowDirection.RIGHT) {
            baseU += SPRITE_WIDTH + INTERNAL_GAP;
        }

        int v = rowIndex * (SPRITE_HEIGHT + ROW_SPACING);
        return new Sprite(baseU, v, SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    // content
    public enum WidgetType {
        LONG_ARROW(0),
        SHORT_ARROW(2),
        TRIANGLE(4);

        private final int rowOffset;

        WidgetType(int rowOffset) {
            this.rowOffset = rowOffset;
        }

        int rowOffset() {
            return this.rowOffset;
        }
    }

    public enum WidgetColor {
        GRAY,
        WOOD,
        GREEN,
        YELLOW,
        CYAN,
        RED,
        ORANGE,
        VANILLA
    }

    public enum ArrowDirection {
        LEFT,
        RIGHT
    }

    public record Sprite(int u, int v, int width, int height) {
    }
}
