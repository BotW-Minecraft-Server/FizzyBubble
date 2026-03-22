package link.botwmcs.fizzy.client.gui;

import link.botwmcs.fizzy.ImageServices;
import link.botwmcs.fizzy.client.elements.StartButton;
import link.botwmcs.fizzy.client.elements.VanillaLikeAbstractButton;
import link.botwmcs.fizzy.client.elements.VanillaLikeButton;
import link.botwmcs.fizzy.client.elements.iconbutton.AccessibilityButton;
import link.botwmcs.fizzy.client.elements.iconbutton.LangSelectButton;
import link.botwmcs.fizzy.client.util.ScreenshotManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.internal.BrandingControl;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class FizzyTitleScreen extends Screen {
    private static final Component TITLE = Component.translatable("narrator.screen.title");
    private static final float FADE_IN_TIME = 2000.0F;
    @Nullable
    private SplashRenderer splash;

    private float panoramaFade;
    private boolean fading;
    private long fadeInStart;
    private final LogoRenderer logoRenderer;

    private VanillaLikeButton likeBtn;

    // 用 Unicode 心形符号做“空心/实心”
    private static final Component HEART_EMPTY = Component.literal("♡"); // 未点赞
    private static final Component HEART_FILLED = Component.literal("♥"); // 已点赞


    public FizzyTitleScreen() {
        this(false);
    }

    public FizzyTitleScreen(boolean fading) {
        this(fading, (LogoRenderer)null);
    }

    public FizzyTitleScreen(boolean fading, @Nullable LogoRenderer logoRenderer) {
        super(TITLE);
        this.panoramaFade = 1.0F;
        this.fading = fading;
        this.logoRenderer = (LogoRenderer) Objects.requireNonNullElseGet(logoRenderer, () -> new LogoRenderer(false));
    }


    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        final Minecraft mc = this.minecraft;

        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }

        ScreenshotManager.INSTANCE.ensureLikedLoaded();

        // 右下角布局参数
        int paddingRight   = 20;
        int paddingBottom  = 20;
        int buttonSpacing  = 5;
        int buttonWidth    = 80;

        // 右下角三行的 Y
        int quitY     = this.height - paddingBottom - 20;       // Quit 高 20
        int settingsY = quitY - 20 - buttonSpacing;             // Settings 高 20
        int playY     = settingsY - 40 - buttonSpacing;         // Play 高 40
        int rightX    = this.width  - buttonWidth - paddingRight;

        // ---- Play（打开自定义 PlayScreen）----
        this.addRenderableWidget(
                StartButton.builder(Component.translatable("fizzy.gui.titlescreen.play_button"),btn ->
                        // mc.setScreen(new PlayScreen(this))               // 你的 PlayScreen
                        mc.setScreen(new PlaySelectorScreen(this))
                ).bounds(rightX, playY, buttonWidth, 40).build()
        );

        // 可根据需要决定是否禁用按钮与 Tooltip，这里简化为始终可用
        Tooltip emptyTip = Tooltip.create(Component.empty());

        // ---- Settings（原版设置）----
        var settingsBtn = VanillaLikeButton.builder(Component.translatable("menu.options"), btn ->
                        // mc.setScreen(new OptionsScreen(this, mc.options))
                mc.setScreen(new OptionsScreen(this, mc.options))
                ).bounds(rightX, settingsY, buttonWidth, 20)
                .tooltip(emptyTip)
                .colorTheme(VanillaLikeAbstractButton.ColorTheme.GRAY)
                .build();
        this.addRenderableWidget(settingsBtn);

        // ---- Quit（退出游戏）----
        var quitBtn = VanillaLikeButton.builder(Component.translatable("menu.quit"), btn ->
                        mc.stop()
                ).bounds(rightX, quitY, buttonWidth, 20)
                .tooltip(emptyTip)
                .colorTheme(VanillaLikeAbstractButton.ColorTheme.GRAY)
                .build();
        this.addRenderableWidget(quitBtn);

        // 左下角三个 20×20 的小按钮
        int paddingLeft   = 20;
        int buttonSpacing2 = 5;

        // Language
        var languageBtn = (LangSelectButton) this.addRenderableWidget(
                LangSelectButton.builder(Component.literal(" "), btn ->
                        mc.setScreen(new LanguageSelectScreen(this, mc.options, mc.getLanguageManager()))
                ).bounds(paddingLeft, this.height - 20 - paddingLeft, 20, 20).build()
        );

        // Accessibility（在 Language 上方 5px）
        var accessibilityBtn = (AccessibilityButton) this.addRenderableWidget(
                AccessibilityButton.builder(Component.literal(" "), btn ->
                        mc.setScreen(new AccessibilityOptionsScreen(this, mc.options))
                ).bounds(paddingLeft, languageBtn.getY() - buttonSpacing2 - 20, 20, 20).build()
        );

        int flexableY = accessibilityBtn.getY();
        if (ModList.get().isLoaded("mod_menu")) {
            try {
                Class<?> modsScreenCls = Class.forName("com.terraformersmc.mod_menu.gui.ModsScreen");
                Constructor<?> ctor = modsScreenCls.getConstructor(Screen.class);
                Screen modsScreen = (Screen) ctor.newInstance(this);

                var modBtn = (VanillaLikeButton) this.addRenderableWidget(
                        VanillaLikeButton.builder(Component.literal("M"), btn ->
                                mc.setScreen(modsScreen)
                        ).bounds(paddingLeft, accessibilityBtn.getY() - buttonSpacing2 - 20, 20, 20)
                                .colorTheme(VanillaLikeAbstractButton.ColorTheme.GRAY)
                                .build()
                );
                flexableY = modBtn.getY();
            } catch (Throwable t) {
                flexableY = accessibilityBtn.getY();
            }

        }

        var nextScreenshotBtn = (VanillaLikeButton) this.addRenderableWidget(
                VanillaLikeButton.builder(Component.literal("→"), btn -> {
                    ScreenshotManager.INSTANCE.next();
                }).bounds(paddingLeft, flexableY - buttonSpacing2 - 20, 20, 20)
                        .colorTheme(VanillaLikeAbstractButton.ColorTheme.GRAY)
                        .build()
        );

        var previousScreenshotBtn = (VanillaLikeButton) this.addRenderableWidget(
                VanillaLikeButton.builder(Component.literal("←"), btn -> {
                    ScreenshotManager.INSTANCE.prev();
                }).bounds(paddingLeft, nextScreenshotBtn.getY() - buttonSpacing2 - 20, 20, 20)
                        .colorTheme(VanillaLikeAbstractButton.ColorTheme.GRAY)
                        .build()
        );

        this.likeBtn = (VanillaLikeButton) this.addRenderableWidget(
                VanillaLikeButton.builder(HEART_EMPTY, btn -> {
                    onClickLike();
                }).bounds(paddingLeft, previousScreenshotBtn.getY() - buttonSpacing2 - 20, 20, 20)
                        .colorTheme(VanillaLikeAbstractButton.ColorTheme.GRAY)
                        .build()
        );

        updateLikeButtonState();

//        var likeBtn = (FizzyButton) this.addRenderableWidget(
//                FizzyButton.builder(Component.literal("♥"), btn -> {
//                    onClickLike();
//                }).bounds(paddingLeft, previousScreenshotBtn.getY() - buttonSpacing2 - 20, 20, 20).build()
//        );

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float f = 1.0F;
        if (this.fading) {
            float f1 = (float)(Util.getMillis() - this.fadeInStart) / 2000.0F;
            if (f1 > 1.0F) {
                this.fading = false;
                this.panoramaFade = 1.0F;
            } else {
                f1 = Mth.clamp(f1, 0.0F, 1.0F);
                f = Mth.clampedMap(f1, 0.5F, 1.0F, 0.0F, 1.0F);
                this.panoramaFade = Mth.clampedMap(f1, 0.0F, 0.5F, 0.0F, 1.0F);
            }

            this.fadeWidgets(f);
        }

        this.renderPanorama(guiGraphics, partialTick);
        int i = Mth.ceil(f * 255.0F) << 24;
        if ((i & -67108864) != 0) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            updateLikeButtonState();
            this.logoRenderer.renderLogo(guiGraphics, this.width, f);
            ClientHooks.renderMainMenu((TitleScreen) new TitleScreen(), guiGraphics, this.font, this.width, this.height, i);
            if (this.splash != null && !(Boolean)this.minecraft.options.hideSplashTexts().get()) {
                this.splash.render(guiGraphics, this.width, this.font, i);
            }

//            String s = "Minecraft " + SharedConstants.getCurrentVersion().getName();
//            if (this.minecraft.isDemo()) {
//                s = s + " Demo";
//            } else {
//                s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
//            }
//
//            if (Minecraft.checkModStatus().shouldReportAsModified()) {
//                s = s + I18n.get("menu.modded", new Object[0]);
//            }

            BrandingControl.forEachLine(true, true, (brdline, brd) -> {
                Font var10001 = this.font;
                int var10004 = this.height;
                int var10006 = brdline;
                Objects.requireNonNull(this.font);
                guiGraphics.drawString(var10001, brd, 2, var10004 - (10 + var10006 * (9 + 1)), 16777215 | i);
            });
            BrandingControl.forEachAboveCopyrightLine((brdline, brd) -> {
                Font var10001 = this.font;
                int var10003 = this.width - this.font.width(brd);
                int var10004 = this.height;
                int var10006 = brdline + 1;
                Objects.requireNonNull(this.font);
                guiGraphics.drawString(var10001, brd, var10003, var10004 - (10 + var10006 * (9 + 1)), 16777215 | i);
            });
        }

    }

    private void fadeWidgets(float alpha) {
        for(GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener instanceof AbstractWidget abstractwidget) {
                abstractwidget.setAlpha(alpha);
            }
        }
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    protected void renderPanorama(GuiGraphics guiGraphics, float partialTick) {
        PANORAMA.render(guiGraphics, this.width, this.height, this.panoramaFade, partialTick);
    }

    // private methods
    private void onClickLike() {
        ScreenshotManager.ImageInfo info = ScreenshotManager.INSTANCE.getDisplayedImage();
        Path file = info.file;
        if (file == null || !Files.exists(file)) {
            Minecraft.getInstance().getToasts().addToast(SystemToast.multiline(
                    Minecraft.getInstance(),
                    SystemToast.SystemToastId.NARRATOR_TOGGLE,
                    Component.translatable("fizzy.gui.titlescreen.like_button.upload.failure"),
                    Component.translatable("fizzy.gui.titlescreen.like_button.upload.failure.not_local")
            ));
            return;
        }

        try {
            byte[] data = Files.readAllBytes(file);
            String filename = file.getFileName().toString();
            String mime = guessMimeType(filename);

            ImageServices.IMAGES.uploadAsync(data, filename, mime)
                    .thenAccept(result -> {
                        if (result.success) {
                            ScreenshotManager.INSTANCE.markLiked(file);
                            updateLikeButtonState();

                            Minecraft mc = Minecraft.getInstance();
                            mc.execute(() -> {
                                playUISound(SoundEvents.EXPERIENCE_ORB_PICKUP);
                                mc.getToasts().addToast(SystemToast.multiline(
                                        mc,
                                        SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                        Component.translatable("fizzy.gui.titlescreen.like_button.upload.success"),
                                        Component.translatable("fizzy.gui.titlescreen.like_button.upload.success.notice")
//                                        Component.literal(result.rawResponse)
//                                        Component.literal(result.url != null ? result.url : "(no url)")
                                ));
                            });
                        } else {
                            Minecraft mc = Minecraft.getInstance();
                            mc.execute(() -> {
                                playUISound(SoundEvents.NOTE_BLOCK_BASS.value());
                                mc.getToasts().addToast(SystemToast.multiline(
                                        mc,
                                        SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                        Component.translatable("fizzy.gui.titlescreen.like_button.upload.failure"),
                                        Component.literal("HTTP " + result.httpCode)
                                ));
                            });
                        }
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        Minecraft.getInstance().execute(() -> {
                            playUISound(SoundEvents.NOTE_BLOCK_BASS.value());
                            Minecraft.getInstance().getToasts().addToast(SystemToast.multiline(
                                    Minecraft.getInstance(),
                                    SystemToast.SystemToastId.NARRATOR_TOGGLE,
                                    Component.translatable("fizzy.gui.titlescreen.like_button.upload.failure"),
                                    Component.literal(ex.getMessage())
                            ));
                        });
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Minecraft.getInstance().getToasts().addToast(SystemToast.multiline(
                    Minecraft.getInstance(),
                    SystemToast.SystemToastId.NARRATOR_TOGGLE,
                    Component.translatable("fizzy.gui.titlescreen.like_button.upload.failure.error_loading_file"),
                    Component.literal(e.getMessage())
            ));
        }

    }

    private static String guessMimeType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private static void playUISound(SoundEvent sound) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }


    private void updateLikeButtonState() {
        if (likeBtn == null) return;

        ScreenshotManager.ImageInfo info = ScreenshotManager.INSTANCE.getDisplayedImage();
        boolean hasFile = info != null && !info.isFallback && info.file != null;
        boolean liked = hasFile && ScreenshotManager.INSTANCE.isLiked(info.file);

        // 1) 文本：♡ / ♥
        likeBtn.setMessage(liked ? HEART_FILLED : HEART_EMPTY);

        // 2) 颜色（可选）：已点赞显示红色
        if (liked) {
            likeBtn.setMessage(Component.literal("♥"));
        }

        // 3) 可点状态：已点赞 or 无有效文件 → 禁用
        likeBtn.active = hasFile && !liked;

        // 4) Tooltip：已点赞时提示“已收藏”，否则“收藏/上传…”
        likeBtn.setTooltip(Tooltip.create(
                Component.translatable(liked ? "fizzy.gui.titlescreen.like_button.tooltip.already_uploaded" : "fizzy.gui.titlescreen.like_button.tooltip.upload")
        ));
    }


}
