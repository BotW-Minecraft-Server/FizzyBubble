package link.botwmcs.fizzy.client.elements;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class WidgetAbstractButton extends AbstractButton {
    private static final ResourceLocation WIDGETS_TEXTURE = Fizzy.resourceLocation("textures/gui/ui/widgets.png");
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
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(
                WIDGETS_TEXTURE,
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                sprite.u(),
                sprite.v(),
                spriteWidth,
                spriteHeight,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active && this.visible) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
        }
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
