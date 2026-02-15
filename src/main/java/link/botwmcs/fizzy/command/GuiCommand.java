package link.botwmcs.fizzy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.client.elements.WidgetAbstractButton;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.behind.BlurBehind;
import link.botwmcs.fizzy.ui.core.FizzyGui;
import link.botwmcs.fizzy.ui.core.FizzyGuiBuilder;
import link.botwmcs.fizzy.ui.core.HostType;
import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.background.FizzyBackgroundElement;
import link.botwmcs.fizzy.ui.element.background.MapBackgroundElement;
import link.botwmcs.fizzy.ui.element.below.DoubleButtonBelow;
import link.botwmcs.fizzy.ui.element.button.ColoredButtonElement;
import link.botwmcs.fizzy.ui.element.button.IconButtonElement;
import link.botwmcs.fizzy.ui.element.button.WidgetButtonElement;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import link.botwmcs.fizzy.ui.element.funstuff.slotstuff.SlotBlockerElement;
import link.botwmcs.fizzy.ui.element.slot.SlotElement;
import link.botwmcs.fizzy.ui.frame.FizzyFrame;
import link.botwmcs.fizzy.ui.host.FizzyScreenHost;
import link.botwmcs.fizzy.ui.split.SplitType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
        );
    }

    private static int openPanel(int rows) {
        return openPanelPage(rows, PAGE_ONE);
    }

    private static int openPanelPage(int rows, int page) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return 0;

        var painter = new FizzyFrame(Component.literal("Test Panel"));
        var behind = new BlurBehind();
        var elementBg = new FizzyBackgroundElement(BgType.BARRIER);

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

        var headerText = FizzyComponentElement.builder(Component.literal("Fizzy Text Demo"))
                .singleLine()
                .align(FizzyComponentElement.Align.CENTER)
                .textScale(1.05f)
                .shadow(true)
                .rainbow(0.1f,'f','8').rainbowStatic()
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
                .padByPx(UiUnit.SLOT_PX * 4 - UiUnit.SLOT_PX / 2, UiUnit.SLOT_PX * 3 + 1, UiUnit.SLOT_PX, UiUnit.SLOT_PX)
                .element(widget0).done()
                .padByPx(UiUnit.SLOT_PX * 9 - UiUnit.SLOT_PX / 2 - 3, UiUnit.SLOT_PX * 3 + 1, UiUnit.SLOT_PX, UiUnit.SLOT_PX)
                .element(widget1).done()
                .below(belowBtn);

        builder.splitByPx(UiUnit.SLOT_PX * 3 - 1, 0, UiUnit.SLOT_PX * 4, SplitType.VERTICAL);

        return builder;
    }

    private static FizzyGuiBuilder buildPage2(Minecraft mc, int rows, int pageRows, FizzyBackgroundElement elementBg) {
        var mapBg = new MapBackgroundElement();
        var mapSlots = new SlotElement();
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
                .pad(1, 1, 3, 3)
                .element(mapBg)
                .done()
                .pad(4,1,4,3)
                .element(mapSlots)
                .done()
                .below(page2Below);

        builder.splitByPx(UiUnit.SLOT_PX * 3 - 1, 0, UiUnit.SLOT_PX * 4, SplitType.VERTICAL);

        return builder;
    }
}