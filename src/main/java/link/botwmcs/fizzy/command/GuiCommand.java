package link.botwmcs.fizzy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.background.FizzyBg;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.element.below.LeftButtonBelow;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.background.FizzyBackgroundElement;
import link.botwmcs.fizzy.ui.element.button.ColoredButtonElement;
import link.botwmcs.fizzy.ui.element.slot.SlotElement;
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import link.botwmcs.fizzy.ui.split.SplitType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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

        var button0 = ColoredButtonElement.builder(Component.literal("Button 0"), btn -> {
            mc.player.sendSystemMessage(Component.literal("Button 0 clicked!"));
        }).color(ColoredAbstractButton.Color.BLUE).build();
        var button1 = ColoredButtonElement.builder(Component.literal("Button 1"), btn -> {
            mc.player.sendSystemMessage(Component.literal("Button 1 clicked!"));
        }).color(ColoredAbstractButton.Color.RED).build();
        var button2 = ColoredButtonElement.builder(Component.literal("Button 2"), btn -> {
            mc.player.sendSystemMessage(Component.literal("Button 2 clicked!"));
        }).color(ColoredAbstractButton.Color.LIME).build();
        var button3 = ColoredButtonElement.builder(Component.literal("Button 3"), btn -> {
            mc.player.sendSystemMessage(Component.literal("Button 3 clicked!"));
        }).color(ColoredAbstractButton.Color.YELLOW).build();
        var elementBg = new FizzyBackgroundElement(BgType.BARRIER);
//        var belowBtn = new DoubleButtonBelow(Component.literal("Confirm"), btn -> {
//            mc.player.sendSystemMessage(Component.literal("Confirm clicked!"));
//        }, Component.literal("Cancel"), btn -> {
//            mc.player.sendSystemMessage(Component.literal("Cancel clicked!"));
//        });
        var belowBtn = new LeftButtonBelow(Component.literal("Close"), btn -> {
            mc.player.sendSystemMessage(Component.literal("Close clicked!"));
            mc.setScreen(null);
        });

        int wPx = painter.panelWidthPx();
        int hPx = painter.computeHeightPx(rows, /*includeBottomEdge*/ true);

        FizzyGuiBuilder builder = FizzyGuiBuilder.start()
                .sizeSlots(rows)          // 记录网格尺寸（后续 split/region/elements 会用）
//                .pad(1,1,1,9)
//                .element((g, leftPx, topPx, widthPx, heightPx, pT) -> {
//                    g.fill(leftPx, topPx, leftPx + widthPx, topPx + heightPx, 0x6640C4FF);
//                }).done()
//                .pad(1, 1, 4, 9)
//                .element(elementBg).done()
                .padByFrame()
                .element(elementBg).done()
                .pad(1, 1, 1, 3)
                .element(button0).done()
                .pad(2, 1, 2, 3)
                .element(button1).done()
                .pad(3, 1, 3, 3)
                .element(button2).done()
                .pad(4, 1, 4, 3)
                .element(button3).done()
                .pad(2,5,3, 8)
                .element(new SlotElement()).done()
                .below(belowBtn);

        if (rows > 1) {
//            builder.split(1, 3, rows, 3);
        }
        builder.splitByPx(UiUnit.SLOT_PX * 3 - 1, 0, UiUnit.SLOT_PX * 4, SplitType.VERTICAL);

        FizzyGui gui = builder
                .host(HostType.SCREEN)
                .behind(behind)
//                .background(background)
                .frame(painter)
                .overrideSizePx(wPx, hPx)
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
