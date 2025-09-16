package link.botwmcs.fizzy.mixin.client;

import link.botwmcs.fizzy.util.EnvDetector;
import link.botwmcs.fizzy.client.util.ScreenshotManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(PanoramaRenderer.class)
public class PanoramaRendererMixin {
    @Unique
    private static final ResourceLocation VANILLA_OVERLAY =
            ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void fizzy$onRenderHead(GuiGraphics gg, int width, int height, float fade, float partialTick, CallbackInfo ci) {
        // 延迟初始化（一次性）
        ScreenshotManager.INSTANCE.ensurePrepared(true);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"
            )
    )
    private void fizzy$onOverlayBlit(GuiGraphics gg,
                                     ResourceLocation atlasLocation,
                                     int x, int y, int width, int height,
                                     float uOffset, float vOffset,
                                     int uWidth, int vHeight,
                                     int textureWidth, int textureHeight) {
        // 非原版 overlay → 原样绘制
        if (!atlasLocation.equals(VANILLA_OVERLAY)) {
            gg.blit(atlasLocation, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
            return;
        }

        // 替换为我们的背景绘制（包含渐变/等比居中）
        if (EnvDetector.isLTSX()) {
            ScreenshotManager.INSTANCE.renderBackground(gg, x, y, width, height);
        } else {
            gg.blit(atlasLocation, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
        }

    }

    /* Old Code **/
//    // ============ 你的自定义背景（作为兜底） ============
//    @Unique
//    private static final ResourceLocation CUSTOM_OVERLAY = Fizzy.resourceLocation("textures/gui/title/background/default.png");
//
//
//    // 16:9 固定基准（按此等比适配）
//    @Unique private static final int  TEXTURE_WIDTH  = 1920;
//    @Unique private static final int  TEXTURE_HEIGHT = 1080;
//    @Unique private static final float ASPECT_RATIO  = 1920f / 1080f;
//
//    // 原版 overlay 资源（用于识别当前这次 blit 是否是标题遮罩层）
//    @Unique
//    private static final ResourceLocation VANILLA_OVERLAY =
//            ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");
//
//    // ============ 截图封面（一次性加载，不轮询） ============
//    @Unique private static final String SCREENSHOT_DIR_NAME = "screenshots";
//    @Unique private static final String[] IMAGE_GLOBS = new String[]{"*.png","*.PNG","*.jpg","*.JPG","*.jpeg","*.JPEG"};
//
//    @Unique private ResourceLocation screenshotTextureRL; // 动态注册句柄
//    @Unique private DynamicTexture screenshotTexture;   // 动态纹理（便于释放）
//    @Unique
//    private int screenshotW, screenshotH;
//
//    @Unique private boolean screenshotPrepared = false;
//
//
//    //    @Inject(method = "<initImageClient>", at = @At("TAIL"))
////    private void onInit(CubeMap cubeMap, CallbackInfo ci) {
////        loadScreenshotOnce(true);
////    }
//    @Inject(method = "render", at = @At("HEAD"))
//    private void onRenderHead(GuiGraphics guiGraphics, int width, int height, float fade, float partialTick, CallbackInfo ci) {
//        if (!screenshotPrepared) {
//            screenshotPrepared = true;
//            prepareScreenshotTextureAsync(/*randomize=*/true);
//        }
//    }
//
//    @Redirect(
//            method = "render",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V")
//    )
//    private void onRenderOverlay(GuiGraphics guiGraphics, ResourceLocation atlasLocation, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
//        if (!atlasLocation.equals(VANILLA_OVERLAY)) {
//            guiGraphics.blit(atlasLocation, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
//            return;
//        }
//
//        ResourceLocation finalTex = this.screenshotTextureRL != null ? this.screenshotTextureRL : CUSTOM_OVERLAY;
////        ResourceLocation finalTex = (this.screenshotTextureRL != null) ? this.screenshotTextureRL : CUSTOM_OVERLAY;
//        // 若是 fallback 的 CUSTOM_OVERLAY，还没加载成功也没关系（静态资源会存在）
//
//        // 如果还是原版 overlay，就走原调用参数（sprite 条按原版拉伸）
//        if (finalTex.equals(VANILLA_OVERLAY)) {
//            guiGraphics.blit(atlasLocation, x, y, width, height, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
//            return;
//        }
//        // 4) 对“截图/自定义背景”按 16:9 等比居中显示（不拉伸变形）
//        float screenAspect = (float) width / (float) height;
//        int renderW, renderH;
//        if (screenAspect > ASPECT_RATIO) {
//            // 屏幕更“宽” → 以宽度为基准
//            float scale = (float) width / (float) TEXTURE_WIDTH;
//            renderW = width;
//            renderH = (int) (TEXTURE_HEIGHT * scale);
//        } else {
//            // 屏幕更“窄”或等于 → 以高度为基准
//            float scale = (float) height / (float) TEXTURE_HEIGHT;
//            renderH = height;
//            renderW = (int) (TEXTURE_WIDTH * scale);
//        }
//        int ox = x + (width  - renderW) / 2;
//        int oy = y + (height - renderH) / 2;
//
//        // 5) 源纹理采样区域：若是截图，用截图原始尺寸；否则用 1920x1080 基准
//        int srcW = (finalTex.equals(this.screenshotTextureRL) && this.screenshotW > 0) ? this.screenshotW : TEXTURE_WIDTH;
//        int srcH = (finalTex.equals(this.screenshotTextureRL) && this.screenshotH > 0) ? this.screenshotH : TEXTURE_HEIGHT;
//
//        // 6) 实际绘制：采整幅贴图（u/v 偏移设 0），按计算后的等比大小/居中位置渲染
//        guiGraphics.blit(finalTex, ox, oy, renderW, renderH,
//                0.0F, 0.0F,           // uOffset, vOffset
//                srcW, srcH,           // uWidth, vHeight（采样宽高）
//                srcW, srcH);          // textureWidth, textureHeight（纹理实际宽高）
//    }
//
//
//
//
//
//
//
//    // =================== 工具方法 ===================
//
//    @Unique
//    private void prepareScreenshotTextureAsync(boolean randomize) {
//        // 先在当前线程挑选文件并打开字节流到 byte[]，避免 IO 阻塞渲染线程
//        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve(SCREENSHOT_DIR_NAME);
//        List<Path> images = Files.isDirectory(dir) ? listImages(dir) : List.of();
//        Path chosen = pickOne(images, randomize);
//        if (chosen == null) { releaseScreenshotTexture(); return; }
//
//        byte[] data;
//        try (InputStream in = Files.newInputStream(chosen)) {
//            data = in.readAllBytes();
//        } catch (Exception e) {
//            releaseScreenshotTexture();
//            return;
//        }
//
//        // 在渲染线程中：解码为 NativeImage + 创建 DynamicTexture + 注册
//        RenderSystem.recordRenderCall(() -> {
//            try (InputStream bin = new java.io.ByteArrayInputStream(data)) {
//                NativeImage img = NativeImage.read(bin); // 此处不要 try-with-resources 关闭 img，由 DynamicTexture 管
//                if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
//                    releaseScreenshotTexture();
//                    return;
//                }
//
//                // 释放旧纹理（同样在渲染线程中释放，避免与上传并发）
//                releaseScreenshotTexture();
//
//                this.screenshotTexture = new DynamicTexture(img);
//                this.screenshotW = img.getWidth();
//                this.screenshotH = img.getHeight();
//
//                String fileBase = chosen.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
//                this.screenshotTextureRL = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "screens/cover/" + fileBase);
//
//                Minecraft.getInstance().getTextureManager().register(this.screenshotTextureRL, this.screenshotTexture);
//            } catch (Exception e) {
//                releaseScreenshotTexture();
//            }
//        });
//    }
//
//    @Unique
//    private Path pickOne(List<Path> images, boolean randomize) {
//        if (images.isEmpty()) return null;
//        if (randomize) {
//            return images.get(RandomSource.create().nextInt(images.size()));
//        } else {
//            return images.stream().max(Comparator.comparingLong(this::safeMtime)).orElse(null);
//        }
//    }
//
//    /** 列出目录下所有支持的图片（不递归） */
//    @Unique
//    private List<Path> listImages(Path dir) {
//        List<Path> out = new ArrayList<>();
//        for (String glob : IMAGE_GLOBS) {
//            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
//                for (Path p : stream) {
//                    if (Files.isRegularFile(p)) out.add(p);
//                }
//            } catch (Exception ignore) {}
//        }
//        return out;
//    }
//
//    /** 安全获取文件最后修改时间（失败时返回 -1） */
//    @Unique
//    private long safeMtime(Path p) {
//        try {
//            return Files.getLastModifiedTime(p).toMillis();
//        } catch (Exception e) {
//            return -1L;
//        }
//    }
//
//    /** 释放已注册的动态纹理，避免显存泄漏 */
//    @Unique
//    private void releaseScreenshotTexture() {
//        if (this.screenshotTextureRL != null) {
//            try {
//                Minecraft.getInstance().getTextureManager().release(this.screenshotTextureRL);
//            } catch (Exception ignore) {}
//            this.screenshotTextureRL = null;
//        }
//        if (this.screenshotTexture != null) {
//            try {
//                this.screenshotTexture.close();
//            } catch (Exception ignore) {}
//            this.screenshotTexture = null;
//        }
//    }
}
