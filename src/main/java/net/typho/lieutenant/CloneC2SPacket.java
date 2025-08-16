package net.typho.lieutenant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public record CloneC2SPacket(BlockBox copy, BlockPos paste) implements CustomPayload {
    public static final Id<CloneC2SPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "clone"));
    public static final MapCodec<CloneC2SPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockBox.CODEC.fieldOf("copy").forGetter(CloneC2SPacket::copy),
            BlockPos.CODEC.fieldOf("paste").forGetter(CloneC2SPacket::paste)
    ).apply(instance, CloneC2SPacket::new));
    public static final PacketCodec<PacketByteBuf, CloneC2SPacket> PACKET_CODEC = PacketCodecs.codec(CODEC.codec()).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
