package link.botwmcs.fizzy.client.overlay.content;

import link.botwmcs.fizzy.api.IOverlayContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SimpleTextPage implements IOverlayContent {
    private final Component text;

    public SimpleTextPage(Component text) {
        this.text = text;
    }

    @Override
    public void render(GuiGraphics g, int x, int y, int width, int height, float partialTick) {
        g.drawString(Minecraft.getInstance().font, text, x + 4, y + 4, 0xFFFFFFFF, false);
    }
}
