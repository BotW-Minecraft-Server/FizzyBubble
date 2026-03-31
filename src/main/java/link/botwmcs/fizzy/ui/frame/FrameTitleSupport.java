package link.botwmcs.fizzy.ui.frame;

import link.botwmcs.fizzy.client.util.TextRenderer;
import link.botwmcs.fizzy.ui.element.component.FizzyComponentElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

final class FrameTitleSupport {
    private static final int DEFAULT_TITLE_MARGIN_PX = 8;

    private FrameTitleSupport() {
    }

    static FizzyComponentElement defaultTitle(Component title, boolean dark) {
        return defaultTitle(title, dark, builder -> {});
    }

    static FizzyComponentElement defaultTitle(
            Component title,
            boolean dark,
            Consumer<FizzyComponentElement.Builder> titleCustomizer
    ) {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(titleCustomizer, "titleCustomizer");

        FizzyComponentElement.Builder builder = FizzyComponentElement.builder()
                .addText(title)
                .wrap(false)
                .autoEllipsis()
                .align(TextRenderer.Align.CENTER)
                .color(dark ? 0xE6E6E6 : 0xFFFFFF)
                .shadow(true)
                .clipToPad(false);
        titleCustomizer.accept(builder);
        return builder.build();
    }

    static void render(
            GuiGraphicsExtractor graphics,
            FrameMetrics metrics,
            int left,
            int top,
            int width,
            FizzyComponentElement titleElement
    ) {
        Objects.requireNonNull(graphics, "graphics");
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(titleElement, "titleElement");

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        Font font = minecraft.font;
        int marginPx = Math.max(DEFAULT_TITLE_MARGIN_PX, metrics.slotStartLeftPx());
        int titleLeft = left + marginPx;
        int titleWidth = Math.max(1, width - marginPx * 2);
        int titleTop = top + metrics.titleStartH();
        int titleHeight = Math.max(1, titleElement.measureHeightPx(font, titleWidth));
        titleElement.render(graphics, titleLeft, titleTop, titleWidth, titleHeight, 0.0f);
    }
}
