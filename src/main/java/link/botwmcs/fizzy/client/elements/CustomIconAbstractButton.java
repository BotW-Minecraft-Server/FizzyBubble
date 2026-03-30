package link.botwmcs.fizzy.client.elements;

import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.Objects;
import javax.annotation.Nullable;

public abstract class CustomIconAbstractButton extends AbstractButton {
    private final Identifier texture;
    private final boolean stretchToFit;
    private final boolean allowUpscale;
    private @Nullable SoundEvent pressSound;

    public CustomIconAbstractButton(int x, int y, int width, int height, Component message,
                                    Identifier texture, boolean stretchToFit, boolean allowUpscale) {
        super(x, y, width, height, message);
        this.texture = Objects.requireNonNull(texture, "texture");
        this.stretchToFit = stretchToFit;
        this.allowUpscale = allowUpscale;
    }

    @Override
    public final void onPress(InputWithModifiers input) {
        this.onPress();
    }

    public abstract void onPress();

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        float alpha = this.active ? 1.0F : 0.5F;
        FizzyGuiUtils.drawTextureFit(
                g,
                texture,
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                stretchToFit,
                allowUpscale,
                alpha
        );
    }

    public Identifier texture() {
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

}
