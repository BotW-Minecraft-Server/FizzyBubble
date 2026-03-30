package link.botwmcs.fizzy.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ScreenshotManager {
    public static final ScreenshotManager INSTANCE = new ScreenshotManager();

    private ScreenshotManager() {}
    private static final Identifier FALLBACK_OVERLAY =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/title/background/default.png");

    // 16:9 基准
    private static final int  TEXTURE_WIDTH  = 1920;
    private static final int  TEXTURE_HEIGHT = 1080;
    private static final float ASPECT_RATIO  = 1920f / 1080f;

    private static final String SCREENSHOT_DIR_NAME = "screenshots";
    private static final String[] IMAGE_GLOBS = new String[]{"*.png","*.PNG","*.jpg","*.JPG","*.jpeg","*.JPEG"};

    private final Set<String> liked = ConcurrentHashMap.newKeySet();
    private volatile boolean likedLoaded = false;

    // 渐变时长（tick）
    private static final int FADE_TICKS = 20;

    // ============ 资源状态 ============
    private List<Path> images = List.of();
    private int index = -1;

    // 当前帧
    private Identifier curRL;
    private DynamicTexture curTex;
    private int curW, curH;

    // 目标帧（做渐变用）
    private Identifier nextRL;
    private DynamicTexture  nextTex;
    private int nextW, nextH;

    private int fadeTicks = 0;     // 0..FADE_TICKS
    private boolean fading = false;

    private boolean prepared = false;

    private volatile Path curFile;   // 当前显示图对应的文件（可能为 null：fallback 或尚未准备）
    private volatile Path nextFile;  // 渐变目标图对应的文件

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

    public void ensureLikedLoaded() {
        if (likedLoaded) return;
        synchronized (this) {
            if (likedLoaded) return;
            try {
                Path p = likedJsonPath();
                if (Files.isRegularFile(p)) {
                    String txt = Files.readString(p, StandardCharsets.UTF_8).trim();
                    List<String> raw = new java.util.ArrayList<>();

                    if (txt.startsWith("[")) {
                        // 简单 JSON 数组解析
                        txt = txt.substring(1, txt.endsWith("]") ? txt.length() - 1 : txt.length());
                        for (String part : txt.split(",")) {
                            String s = part.trim();
                            if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
                            if (!s.isEmpty()) raw.add(s);
                        }
                    } else {
                        // 逐行
                        for (String line : txt.split("\\R")) {
                            String s = line.trim();
                            if (!s.isEmpty()) raw.add(s);
                        }
                    }

                    for (String s0 : raw) {
                        String s = s0.replace('\\', '/').trim();

                        // 兼容旧数据：把绝对路径或含有 ".../screenshots/..." 的裁剪成相对
                        int k = s.toLowerCase(java.util.Locale.ROOT).lastIndexOf("/screenshots/");
                        if (k >= 0) s = s.substring(k + "/screenshots/".length());
                        if (s.startsWith("./")) s = s.substring(2);
                        if (s.startsWith("/")) s = s.substring(1);

                        // 过滤明显不是图片文件的项（目录名、空字符串等）
                        String lower = s.toLowerCase(java.util.Locale.ROOT);
                        boolean looksImage = lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
                        if (!looksImage) continue;

                        liked.add(s);
                    }
                }
            } catch (Exception ignore) {}
            likedLoaded = true;
        }

    }

    public boolean isLiked(@org.jetbrains.annotations.Nullable Path file) {
        ensureLikedLoaded();
        if (file == null) return false;
        String key = keyFor(file);
        return key != null && liked.contains(key);

    }

    public boolean markLiked(@org.jetbrains.annotations.Nullable Path file) {
        ensureLikedLoaded();
        if (file == null) return false;
        String key = keyFor(file);
        if (key == null) return false;
        boolean added = liked.add(key);
        if (added) {
            java.util.concurrent.CompletableFuture.runAsync(this::saveLikedQuiet);
        }
        return added;
    }



    // 渲染（由 Mixin 调用）
    public void renderBackground(GuiGraphicsExtractor gg, int x, int y, int width, int height) {
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

                curFile = nextFile; nextFile = null;

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

    // ===== 快照查询 API =====
    /** 不可变的图片快照 */
    public static final class ImageInfo {
        public final Identifier rl;
        public final int width, height;
        public final boolean isFallback;
        public final @Nullable Path file; // 可能为 null（fallback 或未就绪）
        public final float visibleAlpha;   // 在当前帧里这张图的可见 alpha（0..1）
        public final boolean isFading;     // 是否处于渐变过程

        private ImageInfo(Identifier rl, int w, int h, boolean fallback, @Nullable Path file, float alpha, boolean fading) {
            this.rl = rl; this.width = w; this.height = h;
            this.isFallback = fallback; this.file = file;
            this.visibleAlpha = alpha; this.isFading = fading;
        }
    }

    /**
     * 获取“此刻屏幕上最显眼”的那张背景图快照（不阻塞，不保证在渲染线程调用）。
     * - 若处于渐变：返回 alpha 较大的那张（t>=0.5 返回 next，否则返回 current）。
     * - 若无图：返回 fallback。
     * 本方法不触发 IO/GL 操作。
     */
    public ImageInfo getDisplayedImage() {
        final boolean fadingLocal = this.fading;
        final int fadeTicksLocal = this.fadeTicks;
        final Identifier cur = this.curRL, nxt = this.nextRL;
        final int cW = this.curW, cH = this.curH, nW = this.nextW, nH = this.nextH;
        final Path cF = this.curFile, nF = this.nextFile;

        if (cur == null && nxt == null) {
            return new ImageInfo(FALLBACK_OVERLAY, TEXTURE_WIDTH, TEXTURE_HEIGHT, true, null, 1f, false);
        }

        if (fadingLocal && cur != null && nxt != null) {
            float t = clamp01((float) fadeTicksLocal / (float) FADE_TICKS);
            if (t >= 0.5f) {
                return new ImageInfo(nxt, (nW > 0 ? nW : TEXTURE_WIDTH), (nH > 0 ? nH : TEXTURE_HEIGHT), false, nF, t, true);
            } else {
                boolean curIsFallback = (this.index < 0);
                return new ImageInfo(cur, (cW > 0 ? cW : TEXTURE_WIDTH), (cH > 0 ? cH : TEXTURE_HEIGHT), curIsFallback, cF, 1f - t, true);
            }
        }

        if (cur != null) {
            boolean curIsFallback = (this.index < 0);
            return new ImageInfo(cur, (cW > 0 ? cW : TEXTURE_WIDTH), (cH > 0 ? cH : TEXTURE_HEIGHT), curIsFallback, cF, 1f, false);
        }
        // 罕见兜底
        return new ImageInfo(nxt, (nW > 0 ? nW : TEXTURE_WIDTH), (nH > 0 ? nH : TEXTURE_HEIGHT), false, nF, 1f, fadingLocal);
    }

    /** 不论是否在渐变，返回当前层（旧图层）的信息；不存在时返回 fallback。 */
    public ImageInfo getCurrentLayerImage() {
        final Identifier cur = this.curRL;
        if (cur != null) {
            float alpha = this.fading ? clamp01(1f - ((float)this.fadeTicks / (float)FADE_TICKS)) : 1f;
            return new ImageInfo(cur, (this.curW > 0 ? this.curW : TEXTURE_WIDTH), (this.curH > 0 ? this.curH : TEXTURE_HEIGHT),
                    this.index < 0, this.curFile, alpha, this.fading);
        }
        return new ImageInfo(FALLBACK_OVERLAY, TEXTURE_WIDTH, TEXTURE_HEIGHT, true, null, 1f, false);
    }

    /** 不论是否在渐变，返回目标层（新图层）的信息；不存在时返回 fallback。 */
    public ImageInfo getNextLayerImage() {
        final Identifier nxt = this.nextRL;
        if (nxt != null) {
            float alpha = this.fading ? clamp01((float)this.fadeTicks / (float)FADE_TICKS) : 1f;
            return new ImageInfo(nxt, (this.nextW > 0 ? this.nextW : TEXTURE_WIDTH), (this.nextH > 0 ? this.nextH : TEXTURE_HEIGHT),
                    false, this.nextFile, alpha, this.fading);
        }
        return new ImageInfo(FALLBACK_OVERLAY, TEXTURE_WIDTH, TEXTURE_HEIGHT, true, null, 1f, false);
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

    private void saveLikedQuiet() {
        try {
            Path p = likedJsonPath();
            if (!Files.isDirectory(p.getParent())) Files.createDirectories(p.getParent());
            // 写成简单 JSON 数组
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (String s : liked) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(s.replace("\\","\\\\").replace("\"","\\\"")).append('"');
            }
            sb.append(']');
            Files.writeString(p, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (Exception ignore) {}
    }

    private static Path likedJsonPath() {
        Path cfgDir = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("fizzy");
        if (!Files.isDirectory(cfgDir)) {
            try { Files.createDirectories(cfgDir); } catch (Exception ignore) {}
        }
        return cfgDir.resolve("liked.json");
    }

    // 获取 <gameDir>/screenshots 目录
    private static Path screenshotsDir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve(SCREENSHOT_DIR_NAME);
    }

    /**
     * 将文件转成存储用 key：相对于 screenshots/ 的相对路径（统一 / 分隔）
     * 若不在 screenshots 目录下，则退化为只用文件名。
     * 返回 null 表示无法生成有效 key（例如 "." 之类）
     */
    private static @org.jetbrains.annotations.Nullable String keyFor(Path file) {
        try {
            Path abs = file.toAbsolutePath().normalize();
            Path dir = screenshotsDir().toAbsolutePath().normalize();
            String key;
            if (abs.startsWith(dir)) {
                key = dir.relativize(abs).toString();
            } else {
                // 理论上你的图片都来自 screenshots，这里兜底一下
                key = abs.getFileName().toString();
            }
            key = key.replace('\\', '/');
            if (key.isEmpty() || ".".equals(key)) return null;
            return key;
        } catch (Exception e) {
            return null;
        }
    }



    // ============ 内部：加载/释放 ============
    private void uploadFallbackAsCurrent() {
        dropCurrent();
        curRL = FALLBACK_OVERLAY;
        curTex = null; // 静态纹理由资源包提供，无需注册/释放
        curW = TEXTURE_WIDTH;
        curH = TEXTURE_HEIGHT;
        index = -1;
        curFile = null;
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
                Identifier rl = Identifier.fromNamespaceAndPath(Fizzy.MODID, "screens/cover/" + safeName);

                TextureManager tm = Minecraft.getInstance().getTextureManager();
                tm.register(rl, tex);

                curRL = rl; curTex = tex;
                curW = img.getWidth(); curH = img.getHeight();
                curFile = file;
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
                Identifier rl = Identifier.fromNamespaceAndPath(Fizzy.MODID, "screens/cover/" + safeName);

                Minecraft.getInstance().getTextureManager().register(rl, tex);

                nextRL = rl; nextTex = tex;
                nextW = img.getWidth(); nextH = img.getHeight();
                nextFile = file;

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
        curFile = null;
    }

    private void dropNext() {
        if (nextRL != null && nextTex != null) {
            try { Minecraft.getInstance().getTextureManager().release(nextRL); } catch (Exception ignore) {}
            try { nextTex.close(); } catch (Exception ignore) {}
        }
        nextRL = null; nextTex = null; nextW = 0; nextH = 0;
        nextFile = null;
    }

    // ============ 内部：绘制工具 ============
    private static void blitWithAlpha(GuiGraphicsExtractor gg, Identifier rl, int x, int y, int w, int h, int srcW, int srcH, float alpha) {
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

    private static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }


}
