package link.botwmcs.fizzy.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class ScreenshotCycler {
    public static final ScreenshotCycler INSTANCE = new ScreenshotCycler();

    private ScreenshotCycler() {}
    private static final ResourceLocation FALLBACK_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/title/background/default.png");

    // 16:9 基准
    private static final int  TEXTURE_WIDTH  = 1920;
    private static final int  TEXTURE_HEIGHT = 1080;
    private static final float ASPECT_RATIO  = 1920f / 1080f;

    private static final String SCREENSHOT_DIR_NAME = "screenshots";
    private static final String[] IMAGE_GLOBS = new String[]{"*.png","*.PNG","*.jpg","*.JPG","*.jpeg","*.JPEG"};

    // 渐变时长（tick）
    private static final int FADE_TICKS = 20;

    // ============ 资源状态 ============
    private List<Path> images = List.of();
    private int index = -1;

    // 当前帧
    private ResourceLocation curRL;
    private DynamicTexture curTex;
    private int curW, curH;

    // 目标帧（做渐变用）
    private ResourceLocation nextRL;
    private DynamicTexture  nextTex;
    private int nextW, nextH;

    private int fadeTicks = 0;     // 0..FADE_TICKS
    private boolean fading = false;

    private boolean prepared = false;

    // ============ 外部控制 API ============
    public void ensurePrepared(boolean randomizeFirst) {
        if (prepared) return;
        prepared = true;
        reloadList();
        if (images.isEmpty()) {
            // 无截图：加载 fallback
            uploadFallbackAsCurrent();
        } else {
            // 选一个初始项
            index = randomizeFirst ? RandomSource.create().nextInt(images.size()) : findLatestIndex();
            loadAsCurrent(images.get(index));
        }
    }

    public void next() {
        if (images.isEmpty()) return;
        int target = (index + 1 + images.size()) % images.size();
        startTransitionTo(target);
    }

    public void prev() {
        if (images.isEmpty()) return;
        int target = (index - 1 + images.size()) % images.size();
        startTransitionTo(target);
    }

    public void refreshListKeepCurrent() {
        // 可给外部调用：当你怀疑有新截图生成时
        Path old = (index >= 0 && index < images.size()) ? images.get(index) : null;
        reloadList();
        if (old != null) {
            int i = images.indexOf(old);
            if (i >= 0) index = i;
        }
    }

    // 渲染（由 Mixin 调用）
    public void renderBackground(GuiGraphics gg, int x, int y, int width, int height) {
        // 保障至少有一张
        if (curRL == null && nextRL == null) {
            ensurePrepared(true);
        }

        // 进度推进
        if (fading) {
            fadeTicks++;
            if (fadeTicks >= FADE_TICKS) {
                // 结束：next → current
                dropCurrent();
                curRL  = nextRL;  nextRL  = null;
                curTex = nextTex; nextTex = null;
                curW   = nextW;   nextW   = 0;
                curH   = nextH;   nextH   = 0;
                index  = nextIndexWhenFading; // 记录我们要切到哪个索引
                nextIndexWhenFading = -1;

                fading = false;
                fadeTicks = 0;
            }
        }

        // 计算绘制区域（等比居中）
        int[] rect = fit16x9(width, height);
        int ox = x + rect[0], oy = y + rect[1], rw = rect[2], rh = rect[3];

        // 绘制：如果在渐变，则叠两层
        if (fading && nextRL != null && curRL != null) {
            float t = (float) fadeTicks / (float) FADE_TICKS;
            // 先绘制当前（旧）图，alpha = (1 - t)
            blitWithAlpha(gg, curRL, ox, oy, rw, rh, curW, curH, 1.0f - t);
            // 再绘制目标（新）图，alpha = t
            blitWithAlpha(gg, nextRL, ox, oy, rw, rh, nextW, nextH, t);
        } else {
            // 单层
            if (curRL != null) {
                blitWithAlpha(gg, curRL, ox, oy, rw, rh, (curW > 0 ? curW : TEXTURE_WIDTH), (curH > 0 ? curH : TEXTURE_HEIGHT), 1.0f);
            } else if (nextRL != null) {
                blitWithAlpha(gg, nextRL, ox, oy, rw, rh, (nextW > 0 ? nextW : TEXTURE_WIDTH), (nextH > 0 ? nextH : TEXTURE_HEIGHT), 1.0f);
            } else {
                // 兜底
                blitWithAlpha(gg, FALLBACK_OVERLAY, ox, oy, rw, rh, TEXTURE_WIDTH, TEXTURE_HEIGHT, 1.0f);
            }
        }
    }


    // ============ 内部：文件 & 选择 ============
    private void reloadList() {
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve(SCREENSHOT_DIR_NAME);
        if (!Files.isDirectory(dir)) {
            images = List.of();
            return;
        }
        List<Path> out = new ArrayList<>();
        for (String glob : IMAGE_GLOBS) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, glob)) {
                for (Path p : ds) if (Files.isRegularFile(p)) out.add(p);
            } catch (Exception ignore) {}
        }
        // 稳定排序（按时间/再按文件名）
        images = out.stream()
                .sorted(Comparator.<Path>comparingLong(this::mtimeSafe).thenComparing(p -> p.getFileName().toString()))
                .collect(Collectors.toList());
    }

    private int findLatestIndex() {
        if (images.isEmpty()) return -1;
        long best = Long.MIN_VALUE;
        int bi = 0;
        for (int i = 0; i < images.size(); i++) {
            long m = mtimeSafe(images.get(i));
            if (m > best) { best = m; bi = i; }
        }
        return bi;
    }

    private long mtimeSafe(Path p) {
        try { return Files.getLastModifiedTime(p).toMillis(); } catch (Exception e) { return -1L; }
    }

    // ============ 内部：加载/释放 ============
    private void uploadFallbackAsCurrent() {
        dropCurrent();
        curRL = FALLBACK_OVERLAY;
        curTex = null; // 静态纹理由资源包提供，无需注册/释放
        curW = TEXTURE_WIDTH;
        curH = TEXTURE_HEIGHT;
        index = -1;
    }

    private void loadAsCurrent(Path file) {
        // 读入字节（避免阻塞渲染线程）
        byte[] data;
        try (InputStream in = Files.newInputStream(file)) {
            data = in.readAllBytes();
        } catch (Exception e) {
            uploadFallbackAsCurrent();
            return;
        }

        RenderSystem.recordRenderCall(() -> {
            try (InputStream bin = new java.io.ByteArrayInputStream(data)) {
                NativeImage img = NativeImage.read(bin);
                if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
                    uploadFallbackAsCurrent();
                    return;
                }
                // 替换 current
                dropCurrent();
                DynamicTexture tex = new DynamicTexture(img);
                String safeName = file.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "screens/cover/" + safeName);

                TextureManager tm = Minecraft.getInstance().getTextureManager();
                tm.register(rl, tex);

                curRL = rl; curTex = tex;
                curW = img.getWidth(); curH = img.getHeight();
            } catch (Exception ex) {
                uploadFallbackAsCurrent();
            }
        });
    }

    private int nextIndexWhenFading = -1;

    private void startTransitionTo(int targetIndex) {
        if (targetIndex == index || images.isEmpty()) return;

        Path file = images.get(targetIndex);

        // 先把文件读到内存
        byte[] data;
        try (InputStream in = Files.newInputStream(file)) {
            data = in.readAllBytes();
        } catch (Exception e) {
            return; // 忽略这次切换
        }

        // 在渲染线程上传为 next
        RenderSystem.recordRenderCall(() -> {
            try (InputStream bin = new java.io.ByteArrayInputStream(data)) {
                NativeImage img = NativeImage.read(bin);
                if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) return;

                // 替换 next（先释放旧的 next）
                dropNext();

                DynamicTexture tex = new DynamicTexture(img);
                String safeName = file.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "screens/cover/" + safeName);

                Minecraft.getInstance().getTextureManager().register(rl, tex);

                nextRL = rl; nextTex = tex;
                nextW = img.getWidth(); nextH = img.getHeight();

                // 启动渐变
                fading = true;
                fadeTicks = 0;
                nextIndexWhenFading = targetIndex;

            } catch (Exception ignore) {}
        });
    }

    private void dropCurrent() {
        if (curRL != null && curTex != null) {
            try { Minecraft.getInstance().getTextureManager().release(curRL); } catch (Exception ignore) {}
            try { curTex.close(); } catch (Exception ignore) {}
        }
        curRL = null; curTex = null; curW = 0; curH = 0;
    }

    private void dropNext() {
        if (nextRL != null && nextTex != null) {
            try { Minecraft.getInstance().getTextureManager().release(nextRL); } catch (Exception ignore) {}
            try { nextTex.close(); } catch (Exception ignore) {}
        }
        nextRL = null; nextTex = null; nextW = 0; nextH = 0;
    }

    // ============ 内部：绘制工具 ============
    private static void blitWithAlpha(GuiGraphics gg, ResourceLocation rl, int x, int y, int w, int h, int srcW, int srcH, float alpha) {
        // 保存/恢复 shader 颜色
        float[] prev = getShaderColor();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        gg.blit(rl, x, y, w, h, 0f, 0f, srcW, srcH, srcW, srcH);
        RenderSystem.setShaderColor(prev[0], prev[1], prev[2], prev[3]);
    }

    private static float[] getShaderColor() {
        // Moj 内部没有直接 getter，这里用一个小 hack：自己维护也行。为了稳妥，恢复为默认 1,1,1,1 前先保存。
        // 这里简单地返回默认色，调用者上层如果全程不用自定义颜色，也没问题。
        return new float[]{1f,1f,1f,1f};
    }

    /** 返回 [ox, oy, rw, rh] */
    private static int[] fit16x9(int width, int height) {
        float screenAspect = (float) width / (float) height;
        int rw, rh;
        if (screenAspect > ASPECT_RATIO) {
            float scale = (float) width / (float) TEXTURE_WIDTH;
            rw = width;
            rh = (int) (TEXTURE_HEIGHT * scale);
        } else {
            float scale = (float) height / (float) TEXTURE_HEIGHT;
            rh = height;
            rw = (int) (TEXTURE_WIDTH * scale);
        }
        int ox = (width  - rw) / 2;
        int oy = (height - rh) / 2;
        return new int[]{ox, oy, rw, rh};
    }

}
