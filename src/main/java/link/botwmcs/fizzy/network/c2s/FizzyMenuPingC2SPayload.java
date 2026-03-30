package link.botwmcs.fizzy.network.c2s;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FizzyMenuPingC2SPayload(int containerId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Fizzy.MODID, "fizzy_menu_ping");
    public static final Type<FizzyMenuPingC2SPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, FizzyMenuPingC2SPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FizzyMenuPingC2SPayload::containerId,
            FizzyMenuPingC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
