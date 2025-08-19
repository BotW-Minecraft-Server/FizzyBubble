package link.botwmcs.samchai.mixin.client;

import link.botwmcs.samchai.Fizzy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.*;

/**
 * 这个 Mixin 的作用（高层说明）：
 * 1) 覆写（Overwrite）LogoRenderer#renderLogo，使标题界面的 logo 支持：
 *    - 按语言为简体中文时，使用自定义中文 Logo（支持白色版本）
 *    - 可选渲染 “EDITION” 子标识（支持白色版本）
 *    - 根据 keepLogoThroughFade 控制在淡入淡出阶段是否始终全不透明
 * 2) 逻辑入口保留与原版相近的参数：屏宽、透明度（alpha）、纵向偏移（yOffset）。
 * 3) 继续兼容用户配置（ConfigManager），包括：是否显示中文版、是否显示 edition、是否使用白色主题。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
    @Shadow
    @Final
    private boolean keepLogoThroughFade;

    @Unique
    private final boolean showEasterEgg = Minecraft.getInstance().level != null
            ? Minecraft.getInstance().level.random.nextDouble() < 1.0E-4
            : RandomSource.create().nextDouble() < 1.0E-4;

    @Unique private static final int LOGO_HEIGHT = 44;
    @Unique private static final ResourceLocation MINECRAFT_LOGO_WHITE = Fizzy.resourceLocation("textures/gui/title/minecraft_white.png");
    @Unique private static final ResourceLocation EDITION_WHITE = Fizzy.resourceLocation("textures/gui/title/edition_white.png");
    @Unique private static final ResourceLocation CHINESE_MINECRAFT_LOGO = Fizzy.resourceLocation("textures/gui/title/minecraft_chinese.png");
    @Unique private static final ResourceLocation CHINESE_MINECRAFT_LOGO_WHITE = Fizzy.resourceLocation("textures/gui/title/minecraft_chinese_white.png");

    @Unique private static final int CHINESE_LOGO_WIDTH = 189;
    @Unique private static final int CHINESE_LOGO_HEIGHT = 65;
    @Unique private static final int CHINESE_LOGO_TEX_W = 189;
    @Unique private static final int CHINESE_LOGO_TEX_H = 71;

    @Unique private static final int EDITION_TEX_W = 128;
    @Unique private static final int EDITION_TEX_H = 14;
    @Unique private static final int WHITE_EDITION_WIDTH = 128;
    @Unique private static final int WHITE_EDITION_HEIGHT = 14;

    @Unique private static final int EDITION_LOGO_OVERLAP = 7; // 与主 Logo 的重叠高度


    /**
     * @author Sam_Chai
     * @reason
     *      覆写原版的渲染逻辑：
     *      - 计算不透明度（若 keepLogoThroughFade=true 则强制 alpha=1）
     *      - 判断是否中文与用户配置，决定使用哪套 Logo 资源
     *      - 绘制主 Logo 与 Edition 子标
     */
    @Overwrite
    public void renderLogo(GuiGraphics gg, int screenWidth, float alpha, int yOffset) {
        // 1) 最终不透明度：如果 keepLogoThroughFade 为真，就在过场阶段也保持 1.0F
        final float opacity = this.keepLogoThroughFade ? 1.0F : alpha;

        // 2) 判断是否选择中文 Logo（需要语言为 zh）
        final boolean isChinese = this.fizzy_template_1_21_1$isChinese();

        // 3) 选取主 Logo 纹理资源
        final ResourceLocation titleLogo = this.fizzy_template_1_21_1$selectTitleLogo(isChinese);

        // 4) 用 GuiGraphics 的全局颜色控制透明度（相当于你原来的颜色 ABGR）
        gg.setColor(1.0F, 1.0F, 1.0F, opacity);
        this.fizzy_template_1_21_1$renderMainLogo(gg, screenWidth, yOffset, isChinese, titleLogo);

        // 5) 渲染 Edition 子标识（如果开启）
        final ResourceLocation editionLogo = this.fizzy_template_1_21_1$selectEditionLogo();
        this.fizzy_template_1_21_1$renderEditionLogo(gg, screenWidth, yOffset, editionLogo, isChinese);

        // 6) 恢复颜色状态（以防影响后续 GUI 绘制）
        gg.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // =========================
    // 工具方法（Unique）
    // =========================

    /**
     * 是否为“简体中文”语言。
     * 等价于：Minecraft.getInstance().getLanguageManager().getSelected().getCode().equals("zh_cn")
     */
    @Unique
    private boolean fizzy_template_1_21_1$isChinese() {
        String code = Minecraft.getInstance()
                .getLanguageManager()
                .getSelected(); // 已经是 "en_us" / "zh_cn" / "zh_tw" / "zh_hk" 这样的代码

        // 判断前缀为 zh，或者明确的繁体标签
        return code.startsWith("zh");

    }

    /**
     * 绘制主 Logo：
     * - 中文 Logo：尺寸 189x65、贴图 189x71，位置左偏 94 且整体上移 15像素
     * - 非中文（原版）：尺寸 256x44、贴图 256x64，位置左偏 128（保持原版布局）
     */
    @Unique
    private void fizzy_template_1_21_1$renderMainLogo(GuiGraphics gg, int screenWidth, int yOffset,
                                                      boolean isChinese, ResourceLocation logo) {
        if (isChinese) {
            final int x = screenWidth / 2 - (CHINESE_LOGO_WIDTH / 2); // 189/2=94
            gg.blit(logo, x, yOffset - 15, 0, 0, CHINESE_LOGO_WIDTH, CHINESE_LOGO_HEIGHT,
                    CHINESE_LOGO_TEX_W, CHINESE_LOGO_TEX_H);
        } else {
            final int x = screenWidth / 2 - 128;
            gg.blit(logo, x, yOffset, 0, 0, 256, LOGO_HEIGHT, 256, 64);
        }
    }

    /**
     * 绘制 Edition 子标识：
     * - 默认/白色主题尺寸均为 128x14（保持你原逻辑）
     * - 与主 Logo 有 7 像素的重叠（EDITION_LOGO_OVERLAP）
     * - 使用中文 Logo 时整体下移额外 7 像素，避免相互遮挡
     */
    @Unique
    private void fizzy_template_1_21_1$renderEditionLogo(GuiGraphics gg, int screenWidth, int yOffset,
                                                         ResourceLocation edition, boolean isChinese) {
        final int editionWidth  = EDITION_TEX_W;
        final int editionHeight = EDITION_TEX_H;
        final int x = screenWidth / 2 - (editionWidth / 2);
        final int baseY = yOffset + LOGO_HEIGHT - EDITION_LOGO_OVERLAP;
        final int y = isChinese ? baseY + 7 : baseY;

        gg.blit(edition, x, y, 0, 0, editionWidth, editionHeight, EDITION_TEX_W, EDITION_TEX_H);
    }

    /**
     * 选择主 Logo：
     * *   （如果需要使用“彩蛋”Logo，可在这里扩展 showEasterEgg 的判定）
     */
    @Unique
    private ResourceLocation fizzy_template_1_21_1$selectTitleLogo(boolean isChinese) {
        if (isChinese) {
            return CHINESE_MINECRAFT_LOGO;
        } else {
            // 直接采用你自带的白色资源，或也可改为引用原版 LogoRenderer 的常量
            return LogoRenderer.MINECRAFT_LOGO; // 原版默认 Logo（1.21.x 为公开常量）
        }
    }

    /**
     * 选择 Edition 子标识：
     * - whiteLogo=true 时使用自定义白色 Edition
     * - 否则使用原版的 Edition 资源
     */
    @Unique
    private ResourceLocation fizzy_template_1_21_1$selectEditionLogo() {
        return LogoRenderer.MINECRAFT_EDITION;
    }

}
