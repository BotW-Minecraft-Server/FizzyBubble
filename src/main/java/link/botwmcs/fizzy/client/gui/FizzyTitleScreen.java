package link.botwmcs.fizzy.client.gui;

import link.botwmcs.fizzy.client.elements.FizzyButton;
import link.botwmcs.fizzy.client.elements.StartButton;
import link.botwmcs.fizzy.client.elements.iconbutton.AccessibilityButton;
import link.botwmcs.fizzy.client.elements.iconbutton.LangSelectButton;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.internal.BrandingControl;

import javax.annotation.Nullable;
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
        final Minecraft mc = this.minecraft;

        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }

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
        var settingsBtn = FizzyButton.builder(Component.translatable("menu.options"), btn ->
                        // mc.setScreen(new OptionsScreen(this, mc.options))
                mc.setScreen(new OptionsScreen(this, mc.options))
                ).bounds(rightX, settingsY, buttonWidth, 20)
                .tooltip(emptyTip)
                .build();
        this.addRenderableWidget(settingsBtn);

        // ---- Quit（退出游戏）----
        var quitBtn = FizzyButton.builder(Component.translatable("menu.quit"), btn ->
                        mc.stop()
                ).bounds(rightX, quitY, buttonWidth, 20)
                .tooltip(emptyTip)
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
//
//        // 自定义 Auui Settings（在 Accessibility 上方 5px）
//        this.addRenderableWidget(
//                AuuiSettingsButton.builder(Component.literal(" "), btn ->
//                        mc.setScreen(new AuuiSettingsScreen(this, mc.options))
//                ).bounds(paddingLeft, accessibilityBtn.getY() - buttonSpacing2 - 20, 20, 20).build()
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
}
