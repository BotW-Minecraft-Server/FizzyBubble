package link.botwmcs.fizzy.ui.behind;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.util.Optional;

public class ImageBehind implements BehindPainter {
    private ResourceLocation behindTexture = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/title/background/default.png");
    private int texW = 0;
    private int texH = 0;
    private boolean sizeChecked = false;

    public ImageBehind(ResourceLocation behindTexture) {
        if (behindTexture != null) {
            this.behindTexture = behindTexture;
        }
    }

    @Override
    public void paint(GuiGraphics g, FramePainter painter, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        final int sw = mc.getWindow().getGuiScaledWidth();
        final int sh = mc.getWindow().getGuiScaledHeight();

        ensureTextureSize(mc); // 首帧读取贴图尺寸；失败则给个兜底

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        final int tw = texW > 0 ? texW : 256;
        final int th = texH > 0 ? texH : 256;

        // 任一边小于屏幕：按原始大小平铺（从左到右、从上到下）
        if (tw < sw || th < sh) {
            for (int y = 0; y < sh; y += th) {
                int drawH = Math.min(th, sh - y); // 处理边缘裁切
                for (int x = 0; x < sw; x += tw) {
                    int drawW = Math.min(tw, sw - x);
                    // 采样贴图的左上区域 [0,0, drawW, drawH]，绘制到屏幕 (x,y)
                    g.blit(behindTexture, x, y, 0.0f, 0.0f, drawW, drawH, tw, th);
                }
            }
            return;
        }

        // 两边都 >= 屏幕：cover 等比铺满（不变形，可能被裁掉）
        float scale = Math.max(sw / (float) tw, sh / (float) th);
        int dw = Math.round(tw * scale);
        int dh = Math.round(th * scale);
        int dx = (sw - dw) / 2;
        int dy = (sh - dh) / 2;

        g.blit(behindTexture, dx, dy, 0.0f, 0.0f, dw, dh, tw, th);

    }

    private void ensureTextureSize(Minecraft mc) {
        if (sizeChecked) return;
        sizeChecked = true;

        Optional<Resource> opt = mc.getResourceManager().getResource(this.behindTexture);
        if (opt.isEmpty()) {
            // 回退：给个常见方图尺寸，足以让 blit 工作
            texW = 256;
            texH = 256;
            return;
        }

        try (var in = opt.get().open()) {
            NativeImage img = NativeImage.read(in);
            texW = img.getWidth();
            texH = img.getHeight();
            img.close();
        } catch (IOException e) {
            // 读取失败则回退
            texW = 256;
            texH = 256;
        }
    }

    @Override
    public BehindType type() {
        return BehindType.IMAGE;
    }
}
