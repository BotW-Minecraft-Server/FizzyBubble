package link.botwmcs.fizzy.network.s2c;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FizzyMenuSyncS2CPayload(int containerId, int progress, String text) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Fizzy.MODID, "fizzy_menu_sync");
    public static final Type<FizzyMenuSyncS2CPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, FizzyMenuSyncS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FizzyMenuSyncS2CPayload::containerId,
            ByteBufCodecs.VAR_INT, FizzyMenuSyncS2CPayload::progress,
            ByteBufCodecs.STRING_UTF8, FizzyMenuSyncS2CPayload::text,
            FizzyMenuSyncS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
