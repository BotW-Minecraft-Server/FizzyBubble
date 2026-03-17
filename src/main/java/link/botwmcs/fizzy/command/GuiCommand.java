package link.botwmcs.fizzy.command;

import com.mojang.brigadier.CommandDispatcher;
import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.client.elements.WidgetAbstractButton;
import link.botwmcs.fizzy.client.util.TextRenderer;
import link.botwmcs.fizzy.menu.FizzyTestMenu;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.behind.VanillaBehind;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.animate.vector.ScaleAnimation;
import link.botwmcs.fizzy.ui.element.background.FizzyBackgroundElement;
import link.botwmcs.fizzy.ui.element.background.MapBackgroundElement;
import link.botwmcs.fizzy.ui.element.below.DoubleButtonBelow;
import link.botwmcs.fizzy.ui.element.button.ColoredButtonElement;
import link.botwmcs.fizzy.ui.element.button.IconButtonElement;
import link.botwmcs.fizzy.ui.element.button.TransparentButtonElement;
import link.botwmcs.fizzy.ui.element.button.WidgetButtonElement;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import link.botwmcs.fizzy.ui.element.component.FizzyTooltipElement;
import link.botwmcs.fizzy.ui.element.component.SimpleChartsElement;
import link.botwmcs.fizzy.ui.element.funstuff.slotstuff.SlotBlockerElement;
import link.botwmcs.fizzy.ui.element.funstuff.vector.ProgressElement;
import link.botwmcs.fizzy.ui.element.funstuff.vector.SimpleDraggableElement;
import link.botwmcs.fizzy.ui.element.icon.IconElement;
import link.botwmcs.fizzy.ui.element.slot.SlotElement;
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import link.botwmcs.fizzy.ui.split.SplitType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.Map;

public class GuiCommand {
    private static final int DEFAULT_ROWS = 4;
    private static final int PAGE_ONE = 1;
    private static final int PAGE_TWO = 2;

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(
                Commands.literal("fizzygui")
                        .then(Commands.literal("open")
                                .executes(ctx -> openPanel(DEFAULT_ROWS))
                        )
                        .then(Commands.literal("menu")
                                .executes(ctx -> openTestMenu(ctx.getSource()))
                        )
        );
    }

    private static int openTestMenu(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This subcommand can only be used by a player."));
            return 0;
        }
        return player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new FizzyTestMenu(containerId, inventory),
                Component.literal("Fizzy Test Menu")
        )).isPresent() ? 1 : 0;
    }

    private static int openPanel(int rows) {
        return openPanelPage(rows, PAGE_ONE);
    }

    private static int openPanelPage(int rows, int page) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return 0;

        var painter = new FizzyFrame(Component.literal("Test Panel"));
        var behind = new VanillaBehind();
        var elementBg = new FizzyBackgroundElement(BgType.STONE);

        int pageRows = Math.max(rows, minRowsForPage(page));
        int wPx = painter.panelWidthPx();
        int hPx = painter.computeHeightPx(pageRows, /*includeBottomEdge*/ true);

        FizzyGuiBuilder builder = switch (page) {
            case PAGE_TWO -> buildPage2(mc, rows, pageRows, elementBg);
            case PAGE_ONE -> buildPage1(mc, rows, pageRows, elementBg);
            default -> buildPage1(mc, rows, pageRows, elementBg);
        };

        FizzyGui gui = builder
                .host(HostType.SCREEN)
                .behind(behind)
                .frame(painter)
                .overrideSizePx(wPx, hPx)
                .build();

        int effectivePage = page == PAGE_TWO ? PAGE_TWO : PAGE_ONE;
        String title1 = "Fizzy Panel (" + pageRows + " rows) - Page " + effectivePage;
        mc.tell(() -> {
            mc.setScreen(new FizzyScreenHost(gui) {
                @Override public Component getTitle() {
                    return Component.literal(title1);
                }
            });
        });

        return 1;
    }

    private static int minRowsForPage(int page) {
        return page == PAGE_TWO ? 3 : 1;
    }

    private static FizzyGuiBuilder buildPage1(Minecraft mc, int rows, int pageRows, FizzyBackgroundElement elementBg) {
        var appleBlocker = new SlotBlockerElement(false);
        var appleIcon = IconButtonElement.builder(
                        Component.empty(),
                        btn -> mc.player.sendSystemMessage(Component.literal("Apple icon clicked!")),
                        ResourceLocation.withDefaultNamespace("textures/item/apple.png")
                )
                .build();

        var transparentBtn = TransparentButtonElement.builder(
                Component.empty(),
                btn -> mc.player.sendSystemMessage(Component.literal("Transparent clicked!"))
                )
                .build();

        var nuggetIcon = IconElement.builder(
                ResourceLocation.withDefaultNamespace("textures/item/gold_nugget.png")
                )
                .build()
                .animated(ScaleAnimation.pulse(1.0f, 0.2f, 0.1f));

        var headerText = FizzyComponentElement.builder()
//                .addText(Component.literal("How about 2nd text"))
                .addText(Component.literal("Fizzy Component"))
                .shadow(true)
                .wrap(false)
                .textScale(1.0f)
                .align(TextRenderer.Align.CENTER)
                .rainbow(0.01f, '6', 'e')
//                .rainbowStatic(true)
//                .color(0xE6EEF7)                 // 全局颜色
//                .bold(true)                       // 全局加粗
//                .lineSpacing(2.0f)                // 行间距
//                .letterSpacing(0.5f)              // 字间距
//                .floating(false, 0.01f)
//                .t2c(Map.of(
//                        "[0:5]", 0x57D7FF,        // “Hello” 上色
//                        "Text", 0xFFFFFF          // “Text” 上色
//                ))
//                .t2g(Map.of(
//                        "Fizzy", new int[]{0xFF5F6D, 0xFFC371} // “Fizzy” 渐变
//                ))
//                .t2b(Map.of(
//                        "Hello", true             // “Hello” 加粗
//                ))
//                .t2u(Map.of(
//                        "Hello", true              // “Text” 下划线
//                ))
//                .t2f(Map.of(
//                        "Hello", 0.1f,
//                        "Fizzy", 0.1f,
//                        "Text", 0.1f
//                ), true, true) // 总开关, 像素感
                // 测试 t2g/t2c 是否越界影响后续字符（应只有 Fizzy 渐变，Text 保持白色）
//                .text(Component.literal("How about 2nd text\nFizzy Text"))
//                .lineSpacing(5f)
//                .t2c(Map.of("Text", 0xFFFFFF))
//                .t2r(Map.of("Fizzy", TextRenderer.RainbowConfig.of()))
//                .t2b(Map.of("Fizzy", true))
                .build();

        var tooltip1 = FizzyTooltipElement.builder()
                .addText(Component.literal("This is a tooltip"))
                .addText(Component.literal("1. line 1"))
                .addText(Component.literal("2. line 2"))
                .addText(Component.literal(""))
                .addText(Component.literal("Ofcourse you can RAINBOW"))
                .wrap(true)
//                .textScale(1f)
                .color(0xFFFFFF)
                .shadow(true)
//                .maxWidthPx(180)
                .tooltipColors(0xB31B1F2A, 0xB312161F, 0xB36FC2FF, 0xB3408FD4)
                .t2r(Map.of("Ofcourse you can RAINBOW", TextRenderer.RainbowConfig.of().rainbowStatic(true)))
                .build();

        var button0 = ColoredButtonElement.builder(Component.literal("Button 0"), btn -> {
            boolean open = !appleBlocker.isOpen();
            appleBlocker.setOpen(open);
            mc.player.sendSystemMessage(Component.literal(open ? "Blocker opened!" : "Blocker closed!"));
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

        var belowBtn = new DoubleButtonBelow(
                Component.literal("Exit"),
                btn -> mc.setScreen(null),
                Component.literal("Next"),
                btn -> openPanelPage(rows, PAGE_TWO)
        );

        var widget0 = WidgetButtonElement.builder(Component.empty(), btn -> {
            mc.player.sendSystemMessage(Component.literal("Widget clicked!"));
        }).type(WidgetAbstractButton.WidgetType.TRIANGLE)
                .color(WidgetAbstractButton.WidgetColor.VANILLA)
                .direction(WidgetAbstractButton.ArrowDirection.LEFT).build();

        var widget1 = WidgetButtonElement.builder(Component.empty(), btn -> {
            mc.player.sendSystemMessage(Component.literal("Widget clicked!"));
        }).type(WidgetAbstractButton.WidgetType.TRIANGLE)
                .color(WidgetAbstractButton.WidgetColor.VANILLA)
                .direction(WidgetAbstractButton.ArrowDirection.RIGHT).build();

        var progressBar = ProgressElement.builder()
                .progress(0.68f)
                .color(ProgressElement.Color.YELLOW)
                .autoNotches(true)
                .build();

        FizzyGuiBuilder builder = FizzyGuiBuilder.start()
                .sizeSlots(pageRows)
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
                .pad(1, 4, 1, 9)
                .element(headerText).done()
                .pad(2, 5, 3, 8)
                .element(new SlotElement()).done()
                .pad(2, 5, 2, 5)
                .element(appleIcon)
                .element(appleBlocker).done()
                .pad(2, 6,2,6)
                .element(nuggetIcon)
                .element(transparentBtn)
                .element(tooltip1).done()
                .padByPx(UiUnit.SLOT_PX * 4 - UiUnit.SLOT_PX / 2, UiUnit.SLOT_PX * 3 + 1, UiUnit.SLOT_PX, UiUnit.SLOT_PX)
                .element(widget0).done()
                .padByPx(UiUnit.SLOT_PX * 9 - UiUnit.SLOT_PX / 2 - 3, UiUnit.SLOT_PX * 3 + 1, UiUnit.SLOT_PX, UiUnit.SLOT_PX)
                .element(widget1).done()
                .pad(4, 5, 4, 8).inner()
                .element(progressBar)
                .done()
                .below(belowBtn);

        builder.splitByPx(UiUnit.SLOT_PX * 3 - 1, 0, UiUnit.SLOT_PX * 4, SplitType.VERTICAL);

        return builder;
    }

    private static FizzyGuiBuilder buildPage2(Minecraft mc, int rows, int pageRows, FizzyBackgroundElement elementBg) {
        var map = new MapBackgroundElement();
        var mapBg = new FizzyBackgroundElement(BgType.BARRIER);

        var denseCharts = SimpleChartsElement.builder(content -> {
            content.grid(13, 2)
                    .cell(1, 1, 1, 2)
                    .inner()
                    .element(FizzyComponentElement.builder()
                            .addText(Component.literal("Charts in Draggable"))
                            .align(TextRenderer.Align.CENTER)
                            .shadow(true)
                            .color(0xFFFFFF)
                            .build())
                    .done();

            for (int i = 0; i < 12; i++) {
                int index = i + 1;
                ColoredAbstractButton.Color leftColor = switch (index % 4) {
                    case 1 -> ColoredAbstractButton.Color.BLUE;
                    case 2 -> ColoredAbstractButton.Color.RED;
                    case 3 -> ColoredAbstractButton.Color.LIME;
                    default -> ColoredAbstractButton.Color.YELLOW;
                };
                ColoredAbstractButton.Color rightColor = switch ((index + 2) % 4) {
                    case 1 -> ColoredAbstractButton.Color.BLUE;
                    case 2 -> ColoredAbstractButton.Color.RED;
                    case 3 -> ColoredAbstractButton.Color.LIME;
                    default -> ColoredAbstractButton.Color.YELLOW;
                };

                content.cell(index + 1, 1)
                        .inner()
                        .element(ColoredButtonElement.builder(Component.literal("L" + index), btn -> {
                            if (mc.player != null) {
                                mc.player.sendSystemMessage(Component.literal("Charts left #" + index));
                            }
                        }).color(leftColor).build())
                        .done()
                        .cell(index + 1, 2)
                        .inner()
                        .element(ColoredButtonElement.builder(Component.literal("R" + index), btn -> {
                            if (mc.player != null) {
                                mc.player.sendSystemMessage(Component.literal("Charts right #" + index));
                            }
                        }).color(rightColor).build())
                        .done();
            }
        }).build();

        SimpleDraggableElement.ContentSpec draggedPad = SimpleDraggableElement.contentBuilder()
                .pad(1, 1, 16, 4)
                .inner()
                .element(denseCharts)
                .done()
                .build();

        var draggableList = SimpleDraggableElement.builder(draggedPad)
                .wheelStepPx(UiUnit.SLOT_PX)
                .build();

        var page2Below = new DoubleButtonBelow(
                Component.literal("Close"),
                btn -> mc.setScreen(null),
                Component.literal("Prev"),
                btn -> openPanelPage(rows, PAGE_ONE)
        );

        FizzyGuiBuilder builder = FizzyGuiBuilder.start()
                .sizeSlots(pageRows)
                .padByFrame()
                .element(elementBg).done()
                .pad(1, 1, 4, 4)
                .element(mapBg)
                .done()
                .pad(1, 1, 4,4).inner()
                .element(map)
                .done()
                .pad(1, 5, 4, 9).inner()
//                .element(new SlotElement())
                .element(draggableList)
                .done()
                .below(page2Below);

        builder.splitByPx(UiUnit.SLOT_PX * 4 - 1, 0, UiUnit.SLOT_PX * 4, SplitType.VERTICAL);

        return builder;
    }
}
