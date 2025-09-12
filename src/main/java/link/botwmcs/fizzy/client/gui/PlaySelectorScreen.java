package link.botwmcs.fizzy.client.gui;

import link.botwmcs.fizzy.client.elements.FizzyButton;
import link.botwmcs.fizzy.client.elements.iconbutton.MultiplayerButton;
import link.botwmcs.fizzy.client.elements.iconbutton.SingleplayerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class PlaySelectorScreen extends Screen {
    private final Screen lastScreen;

    protected PlaySelectorScreen(Screen lastScreen) {
        super(Component.literal("PLAYSELECTOR"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        initButtons();
    }

    private void initButtons() {
        // 按原版布局的计算方式，保持与旧 UI 近似的纵向位置
        int verticalOffset = (this.height - 250) / 2 + 50;

        // 只保留“大按钮”的原始尺寸
        final int BTN_W = 80;
        final int BTN_H = 120;

        // 两颗按钮在屏幕中线两侧对称分布，保持不拉伸
        final int GAP = 20; // 两按钮之间的水平间距
        int centerX = this.width / 2;
        int leftX  = centerX - GAP / 2 - BTN_W; // 左按钮 X
        int rightX = centerX + GAP / 2;         // 右按钮 X
        int btnY   = verticalOffset;            // 与原大按钮相同的 Y

        // 计算多人模式可用性与（仅在禁用时）tooltip
        Component reason = getMultiplayerDisabledReason();
        boolean mpEnabled = (reason == null);
        Tooltip emptyTip = Tooltip.create(Component.empty());
        Tooltip mpTooltip = (reason != null) ? Tooltip.create(reason) : emptyTip;

        // --- Singleplayer（左）---
        var singleBtn = this.addRenderableWidget(
                SingleplayerButton.builder(Component.empty(), (b) ->
                                this.minecraft.setScreen(new SelectWorldScreen(this))
                        )
                        .bounds(leftX, btnY, BTN_W, BTN_H)
                        // 单人按钮无需 tooltip，保持原行为（不传或传 null 均可）
                        .build()
        );
        singleBtn.active = true; // 单人永远可用

        // --- Multiplayer（右）---
        var multiBtn = this.addRenderableWidget(
                MultiplayerButton.builder(Component.empty(), (b) ->
                                this.minecraft.setScreen(new JoinMultiplayerScreen(this))
                        )
                        .bounds(rightX, btnY, BTN_W, BTN_H)
                        .tooltip(mpTooltip) // 只有禁用时才会是非空 Tooltip，避免 NPE
                        .build()
        );
        multiBtn.active = mpEnabled;

        // 小按钮（保持 80×20）
        final int SMALL_W = 80;
        final int SMALL_H = 20;
        int smallY = btnY + BTN_H + 10; // 在大按钮下面留 10px 间隔

        // 左边小按钮
        var singleSmall = this.addRenderableWidget(
                FizzyButton.builder(Component.translatable("fizzy.gui.playselector.singleplayer"), (button ->
                        this.minecraft.setScreen(new SelectWorldScreen(this))
                )).bounds(leftX, smallY, SMALL_W, SMALL_H).tooltip(emptyTip).build()
        );
        singleSmall.active = true;

        // 右边小按钮
        var multiSmall = this.addRenderableWidget(
                FizzyButton.builder(Component.translatable("fizzy.gui.playselector.multiplayer"), (button ->
//                        this.minecraft.setScreen(new JoinMultiplayerScreen(this))
                        this.minecraft.setScreen(new ServerSelectorScreen(this))
                )).bounds(rightX, smallY, SMALL_W, SMALL_H).tooltip(emptyTip).build()
        );
        multiSmall.active = mpEnabled;
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
//        if (this.minecraft.level == null) {
//            this.renderDirtBackground(gg, 0);
//        }

        int panelWidth  = 420;
        int panelHeight = 220;
        int panelX = (this.width  - panelWidth)  / 2;
        int panelY = (this.height - panelHeight) / 2;

        // 背景面板
        gg.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80222222);
        gg.fill(panelX, panelY - 1, panelX + panelWidth, panelY, Integer.MIN_VALUE);
        gg.fill(panelX, panelY + panelHeight, panelX + panelWidth, panelY + panelHeight + 1, Integer.MIN_VALUE);
        gg.fill(panelX - 1, panelY - 1, panelX, panelY + panelHeight + 1, Integer.MIN_VALUE);
        gg.fill(panelX + panelWidth, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, Integer.MIN_VALUE);

        super.render(gg, mouseX, mouseY, partialTick);

        // 标题
        gg.drawCenteredString(this.font, Component.translatable("fizzy.gui.playselector.title"), this.width / 2, panelY + 14, 0xFFFFFF);
//
//        // 两个标签文字（可选）：按钮下方的小标题
//        int gap = 20;
//        int btnWidth  = (panelWidth - gap - 2 * 24) / 2;
//        int btnHeight = 130;
//        int btnY = panelY + 50;
//
//        // 左按钮标题
//        gg.drawCenteredString(this.font, this.minecraft.font.plainSubstrByWidth(Component.translatable("fizzy.gui.playselector.singleplayer").getString(), 200),
//                panelX + 24 + btnWidth / 2, btnY + btnHeight + 8, 0xFFFFFFFF);
//
//        // 右按钮标题
//        gg.drawCenteredString(this.font, this.minecraft.font.plainSubstrByWidth(Component.translatable("fizzy.gui.playselector.multiplayer").getString(), 200),
//                panelX + 24 + btnWidth + gap + btnWidth / 2, btnY + btnHeight + 8, 0xFFFFFFFF);
    }



    private @Nullable Component getMultiplayerDisabledReason() {
        Minecraft mc = this.minecraft;

        if (mc.allowsMultiplayer()) {
            return null;
        } else if (mc.isNameBanned()) {
            return Component.translatable("title.multiplayer.disabled.banned.name");
        } else {
            var ban = mc.multiplayerBan();
            if (ban != null) {
                return ban.expires() != null
                        ? Component.translatable("title.multiplayer.disabled.banned.temporary")
                        : Component.translatable("title.multiplayer.disabled.banned.permanent");
            } else {
                return Component.translatable("title.multiplayer.disabled");
            }
        }
    }
}
