package link.botwmcs.fizzy.network.s2c;

import link.botwmcs.fizzy.Fizzy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AnnouncePayload(String context, int ticks) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Fizzy.MODID, "announce");
    public static final Type<AnnouncePayload> TYPE = new Type<>(ID);


    public static final StreamCodec<FriendlyByteBuf, AnnouncePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, AnnouncePayload::context,
                    ByteBufCodecs.VAR_INT, AnnouncePayload::ticks,
                    AnnouncePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

