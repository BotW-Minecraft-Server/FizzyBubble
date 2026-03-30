package link.botwmcs.fizzy.client.formatting.inline;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;

public final class FizzyInlineImageGlyph implements BakedGlyph {
    private final InlineImageSource source;
    private final GlyphInfo info;
    private final float width;
    private final float height;
    private final float yOffset;

    public FizzyInlineImageGlyph(InlineImageSource source, float advance) {
        this.source = source;
        this.info = GlyphInfo.simple(Math.max(0.0F, advance));
        this.width = this.info.getAdvance();
        this.height = FizzyInlineImageRenderer.measureDrawHeight(source);
        this.yOffset = (FizzyInlineImageRenderer.LINE_HEIGHT - this.height) * 0.5F;
    }

    @Override
    public GlyphInfo info() {
        return this.info;
    }

    @Override
    public TextRenderable.Styled createGlyph(
            float x,
            float y,
            int color,
            int shadowColor,
            Style style,
            float boldOffset,
            float shadowOffset
    ) {
        return new FizzyInlineImageRenderable(
                this.source,
                x,
                y,
                ARGB.color(ARGB.alpha(color), 0xFFFFFF),
                ARGB.color(ARGB.alpha(shadowColor), 0xFFFFFF),
                style,
                shadowOffset,
                this.width,
                this.height,
                this.yOffset
        );
    }
}
