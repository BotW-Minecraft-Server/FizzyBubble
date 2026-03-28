package link.botwmcs.fizzy.client.gui;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.elements.VanillaLikeAbstractButton;
import link.botwmcs.fizzy.proxy.api.KernelAttachSpec;
import link.botwmcs.fizzy.proxy.api.KernelUiSpec;
import link.botwmcs.fizzy.proxy.host.HostGeometry;
import link.botwmcs.fizzy.proxy.rule.ProxyBuildContext;
import link.botwmcs.fizzy.proxy.rule.ProxyRule;
import link.botwmcs.fizzy.proxy.rule.ProxyRuleRegistry;
import link.botwmcs.fizzy.ui.element.button.VanillaLikeButtonElement;
import link.botwmcs.fizzy.ui.pad.PixelPadSpec;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class TitleScreenProxyDemo {
    private TitleScreenProxyDemo() {
    }

    public static void register(ProxyRuleRegistry registry) {
        registry.register(new TitleScreenRightButtonsRule());
    }

    private static final class TitleScreenRightButtonsRule implements ProxyRule {
        private static final ResourceLocation ID =
                ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "demo/title_right_vanilla_like_buttons");

        private static final int BUTTON_WIDTH = 124;
        private static final int BUTTON_HEIGHT = 20;
        private static final int BUTTON_GAP = 6;
        private static final int RIGHT_MARGIN = 16;
        private static final int TOP_MIN_MARGIN = 24;

        @Override
        public ResourceLocation id() {
            return ID;
        }

        @Override
        public int priority() {
            return 100;
        }

        @Override
        public boolean matches(ProxyBuildContext context) {
            return context.screen() instanceof TitleScreen;
        }

        @Override
        public KernelAttachSpec build(ProxyBuildContext context) {
            HostGeometry geometry = context.geometry();
            int totalHeight = BUTTON_HEIGHT * 3 + BUTTON_GAP * 2;
            int startX = Math.max(0, geometry.rootWidth() - RIGHT_MARGIN - BUTTON_WIDTH);
            int centeredY = (geometry.rootHeight() - totalHeight) / 2;
            int startY = Math.max(TOP_MIN_MARGIN, centeredY);

            VanillaLikeButtonElement button1 = createDemoButton("Demo Button A", VanillaLikeAbstractButton.ColorTheme.GRAY);
            VanillaLikeButtonElement button2 = createDemoButton("Demo Button B", VanillaLikeAbstractButton.ColorTheme.BLUE);
            VanillaLikeButtonElement button3 = createDemoButton("Demo Button C", VanillaLikeAbstractButton.ColorTheme.GREEN);

            KernelUiSpec uiSpec = KernelUiSpec.builder()
                    .addPad(new PixelPadSpec(startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT, List.of(button1)))
                    .addPad(new PixelPadSpec(startX, startY + (BUTTON_HEIGHT + BUTTON_GAP), BUTTON_WIDTH, BUTTON_HEIGHT, List.of(button2)))
                    .addPad(new PixelPadSpec(startX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 2, BUTTON_WIDTH, BUTTON_HEIGHT, List.of(button3)))
                    .build();

            return new KernelAttachSpec(uiSpec, null, null, null);
        }

        private static VanillaLikeButtonElement createDemoButton(String text, VanillaLikeAbstractButton.ColorTheme theme) {
            return VanillaLikeButtonElement.builder(button -> {
                // Demo only: no actual behavior.
            })
                    .text(Component.literal(text))
                    .colorTheme(theme)
                    .build();
        }
    }
}

