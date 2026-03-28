package link.botwmcs.fizzy.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int fizzy$getLeftPos();

    @Accessor("topPos")
    int fizzy$getTopPos();

    @Accessor("imageWidth")
    int fizzy$getImageWidth();

    @Accessor("imageHeight")
    int fizzy$getImageHeight();
}

