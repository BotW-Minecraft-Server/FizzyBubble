package link.botwmcs.fizzy.client.elements;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public abstract class CustomIconAbstractButton extends AbstractButton {
    private final ResourceLocation texture;
    private final boolean stretchToFit;
    private final boolean allowUpscale;
    private @Nullable SoundEvent pressSound;

    public CustomIconAbstractButton(int x, int y, int width, int height, Component message,
                                    ResourceLocation texture, boolean stretchToFit, boolean allowUpscale) {
        super(x, y, width, height, message);
        this.texture = Objects.requireNonNull(texture, "texture");
        this.stretchToFit = stretchToFit;
        this.allowUpscale = allowUpscale;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        TextureSize size = SIZE_CACHE.computeIfAbsent(texture, CustomIconAbstractButton::resolveTextureSize);
        int texW = Math.max(1, size.w());
        int texH = Math.max(1, size.h());

        int drawX = this.getX();
        int drawY = this.getY();
        int drawW = this.getWidth();
        int drawH = this.getHeight();

        if (!stretchToFit) {
            float scale = Math.min(drawW / (float) texW, drawH / (float) texH);
            if (!allowUpscale) {
                scale = Math.min(scale, 1f);
            }
            if (scale <= 0f) {
                return;
            }

            drawW = Math.max(1, Math.round(texW * scale));
            drawH = Math.max(1, Math.round(texH * scale));
            drawX = this.getX() + (this.getWidth() - drawW) / 2;
            drawY = this.getY() + (this.getHeight() - drawH) / 2;
        }

        float alpha = this.active ? 1.0F : 0.5F;
        g.setColor(1.0F, 1.0F, 1.0F, alpha);
        g.blit(texture, drawX, drawY, 0, 0, drawW, drawH, texW, texH);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active && this.visible) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
        }
    }

    public ResourceLocation texture() {
        return this.texture;
    }

    public boolean stretchToFit() {
        return this.stretchToFit;
    }

    public boolean allowUpscale() {
        return this.allowUpscale;
    }

    public void setPressSound(@Nullable SoundEvent sound) {
        this.pressSound = sound;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (pressSound != null) {
            soundManager.play(SimpleSoundInstance.forUI(pressSound, 1.0F));
            return;
        }
        super.playDownSound(soundManager);
    }

    private record TextureSize(int w, int h) {
        static final TextureSize FALLBACK = new TextureSize(16, 16);
    }

    private static final Map<ResourceLocation, TextureSize> SIZE_CACHE = new ConcurrentHashMap<>();

    private static TextureSize resolveTextureSize(ResourceLocation tex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return TextureSize.FALLBACK;
        }
        try {
            var resourceOpt = mc.getResourceManager().getResource(tex);
            if (resourceOpt.isEmpty()) {
                return TextureSize.FALLBACK;
            }

            Resource resource = resourceOpt.get();
            try (InputStream stream = resource.open(); NativeImage image = NativeImage.read(stream)) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width <= 0 || height <= 0) {
                    return TextureSize.FALLBACK;
                }
                return new TextureSize(width, height);
            }
        } catch (IOException e) {
            return TextureSize.FALLBACK;
        }
    }
}
