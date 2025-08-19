package link.botwmcs.samchai.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import link.botwmcs.samchai.Fizzy;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mixin(PanoramaRenderer.class)
public class PanoramaRendererMixin {
    // ============ 你的自定义背景（作为兜底） ============
    @Unique
    private static final ResourceLocation CUSTOM_OVERLAY = Fizzy.resourceLocation("textures/gui/title/background/default.png");


    // 16:9 固定基准（按此等比适配）
    @Unique private static final int  TEXTURE_WIDTH  = 1920;
    @Unique private static final int  TEXTURE_HEIGHT = 1080;
    @Unique private static final float ASPECT_RATIO  = 1920f / 1080f;

    // 原版 overlay 资源（用于识别当前这次 blit 是否是标题遮罩层）
    @Unique
    private static final ResourceLocation VANILLA_OVERLAY =
            ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");

    // ============ 截图封面（一次性加载，不轮询） ============
    @Unique private static final String SCREENSHOT_DIR_NAME = "screenshots";
    @Unique private static final String[] IMAGE_GLOBS = new String[]{"*.png","*.PNG","*.jpg","*.JPG","*.jpeg","*.JPEG"};

    @Unique private ResourceLocation screenshotTextureRL; // 动态注册句柄
    @Unique private DynamicTexture screenshotTexture;   // 动态纹理（便于释放）
    @Unique
    private int screenshotW, screenshotH;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CubeMap cubeMap, CallbackInfo ci) {
        loadScreenshotOnce(true);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V")
    )
    private void onRenderOverlay(GuiGraphics guiGraphics, ResourceLocation atlasLocation, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        if (!atlasLocation.equals(VANILLA_OVERLAY)) {
            guiGraphics.blit(atlasLocation, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
            return;
        }

        ResourceLocation finalTex = this.screenshotTextureRL != null ? this.screenshotTextureRL : CUSTOM_OVERLAY;
        // 如果还是原版 overlay，就走原调用参数（sprite 条按原版拉伸）
        if (finalTex.equals(VANILLA_OVERLAY)) {
            guiGraphics.blit(atlasLocation, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
            return;
        }
        // 4) 对“截图/自定义背景”按 16:9 等比居中显示（不拉伸变形）
        float screenAspect = (float) width / (float) height;
        int renderW, renderH;
        if (screenAspect > ASPECT_RATIO) {
            // 屏幕更“宽” → 以宽度为基准
            float scale = (float) width / (float) TEXTURE_WIDTH;
            renderW = width;
            renderH = (int) (TEXTURE_HEIGHT * scale);
        } else {
            // 屏幕更“窄”或等于 → 以高度为基准
            float scale = (float) height / (float) TEXTURE_HEIGHT;
            renderH = height;
            renderW = (int) (TEXTURE_WIDTH * scale);
        }
        int ox = x + (width  - renderW) / 2;
        int oy = y + (height - renderH) / 2;

        // 5) 源纹理采样区域：若是截图，用截图原始尺寸；否则用 1920x1080 基准
        int srcW = (finalTex.equals(this.screenshotTextureRL) && this.screenshotW > 0) ? this.screenshotW : TEXTURE_WIDTH;
        int srcH = (finalTex.equals(this.screenshotTextureRL) && this.screenshotH > 0) ? this.screenshotH : TEXTURE_HEIGHT;

        // 6) 实际绘制：采整幅贴图（u/v 偏移设 0），按计算后的等比大小/居中位置渲染
        guiGraphics.blit(finalTex, ox, oy, renderW, renderH,
                0.0F, 0.0F,           // uOffset, vOffset
                srcW, srcH,           // uWidth, vHeight（采样宽高）
                srcW, srcH);          // textureWidth, textureHeight（纹理实际宽高）
    }







    // =================== 工具方法 ===================

    /** 按配置一次性加载（随机或最新）一张截图为动态纹理 */
    @Unique
    private void loadScreenshotOnce(boolean randomize) {
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve(SCREENSHOT_DIR_NAME);
        if (!Files.isDirectory(dir)) {
            releaseScreenshotTexture();
            return;
        }


        // 收集可用图片文件
        List<Path> images = listImages(dir);
        if (images.isEmpty()) {
            releaseScreenshotTexture();
            return;
        }

        // 选择文件：随机 or 最新
        Path chosen;
        if (randomize) {
            RandomSource rng = RandomSource.create();        // 每次回到主菜单随机一次
            chosen = images.get(rng.nextInt(images.size()));
        } else {
            // 最新：按最后修改时间降序
            chosen = images.stream()
                    .max(Comparator.comparingLong(this::safeMtime))
                    .orElse(null);
        }
        if (chosen == null) {
            releaseScreenshotTexture();
            return;
        }

        try (InputStream in = Files.newInputStream(chosen)) {
            NativeImage img = NativeImage.read(in);

            // 释放旧纹理
            releaseScreenshotTexture();

            // 创建并注册新纹理
            this.screenshotTexture = new DynamicTexture(img);
            this.screenshotW = img.getWidth();
            this.screenshotH = img.getHeight();

            // 用文件名做 RL，保证每次相同文件名得到相同句柄，避免无限增长
            String fileBase = chosen.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
            this.screenshotTextureRL = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "screens/cover/" + fileBase);

            TextureManager tm = Minecraft.getInstance().getTextureManager();
            tm.register(this.screenshotTextureRL, this.screenshotTexture);

        } catch (Exception e) {
            e.printStackTrace();
            releaseScreenshotTexture();
        }
    }

    /** 列出目录下所有支持的图片（不递归） */
    @Unique
    private List<Path> listImages(Path dir) {
        List<Path> out = new ArrayList<>();
        for (String glob : IMAGE_GLOBS) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
                for (Path p : stream) {
                    if (Files.isRegularFile(p)) out.add(p);
                }
            } catch (Exception ignore) {}
        }
        return out;
    }

    /** 安全获取文件最后修改时间（失败时返回 -1） */
    @Unique
    private long safeMtime(Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (Exception e) {
            return -1L;
        }
    }

    /** 释放已注册的动态纹理，避免显存泄漏 */
    @Unique
    private void releaseScreenshotTexture() {
        if (this.screenshotTextureRL != null) {
            try {
                Minecraft.getInstance().getTextureManager().release(this.screenshotTextureRL);
            } catch (Exception ignore) {}
            this.screenshotTextureRL = null;
        }
        if (this.screenshotTexture != null) {
            try {
                this.screenshotTexture.close();
            } catch (Exception ignore) {}
            this.screenshotTexture = null;
        }
    }
}
