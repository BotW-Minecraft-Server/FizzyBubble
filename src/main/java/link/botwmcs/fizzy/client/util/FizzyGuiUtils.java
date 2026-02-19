package link.botwmcs.fizzy.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class FizzyGuiUtils {
    private static ResourceLocation blankTextureLocation;
    public static ResourceLocation getBlankTexture() {
        if (blankTextureLocation == null) {
            NativeImage img = new NativeImage(1, 1, false);
            img.setPixelRGBA(0, 0, 0xFFFFFFFF);
            blankTextureLocation = Minecraft.getInstance().getTextureManager().register("blank", new DynamicTexture(img));
        }

        return blankTextureLocation;
    }

    public static boolean useWhiteOrBlackForeColor(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        double luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        return luminance < 0.5;
    }

    /**
     * @see <a href="http://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/content/trains/schedule/ScheduleScreen.java">...</a>
     */
    public static void startStencil(GuiGraphics g, float x, float y, float w, float h) {
        RenderSystem.clear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(~0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);

        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(w, h, 1);
        g.fillGradient(0, 0, -100, 1, 1, 0xff000000, 0xff000000);
        g.pose().popPose();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
    }

    public static void endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }


}
