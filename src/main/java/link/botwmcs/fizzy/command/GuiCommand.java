package link.botwmcs.fizzy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.background.SoildColorBg;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.behind.ImageBehind;
import link.botwmcs.fizzy.ui.behind.SoildColorBehind;
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiCommand {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(
                Commands.literal("fizzygui")
                        .then(Commands.literal("open")
                                .executes(ctx -> openPanel(/*rows*/3)) // 默认 3 行
                                .then(Commands.argument("rows", IntegerArgumentType.integer(1, 6))
                                        .executes(ctx -> openPanel(IntegerArgumentType.getInteger(ctx, "rows")))
                                )
                        )
        );
    }

    /** 实际打开 GUI（Screen 版本，带底边） */
    private static int openPanel(int rows) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return 0;

        var painter = new FizzyFrame(Component.literal("Test Panel"));
        var background = new FizzyBg(BgType.STONE);
//        var background = new SoildColorBg(0xFF202020);
//        var behind = new SoildColorBehind(0xFF202020);
//        var behind = new ImageBehind(ResourceLocation.withDefaultNamespace("textures/block/gold_block.png"));
        var behind = new BlurBehind();
        int wPx = painter.panelWidthPx();
        int hPx = painter.computeHeightPx(rows, /*includeBottomEdge*/ true);

        FizzyGui gui = FizzyGuiBuilder.start()
                .sizeSlots(rows)          // 记录网格尺寸（后续 split/region/elements 会用）
                .pad(1,1,1,9)
                .element((g, leftPx, topPx, widthPx, heightPx, pT) -> {
                    g.fill(leftPx, topPx, leftPx + widthPx, topPx + heightPx, 0x6640C4FF);
                }).done()
                .host(HostType.SCREEN)
                .behind(behind)
                .background(background)
                .frame(painter)
                .overrideSizePx(wPx, hPx)    // 用真实像素覆盖 BG 尺寸
                .build();
        mc.tell(() -> {
            mc.setScreen(new FizzyScreenHost(gui) {
                @Override public Component getTitle() {
                    return Component.literal("Fizzy Panel (" + rows + " rows)");
                }
            });
        });

        return 1;
    }
}
