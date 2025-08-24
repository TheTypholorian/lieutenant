package net.typho.lieutenant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public record CloneC2SPacket(BlockBox copy, BlockPos paste, CloneType type) implements CustomPayload {
    public static final Id<CloneC2SPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "clone"));
    public static final MapCodec<CloneC2SPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockBox.CODEC.fieldOf("copy").forGetter(CloneC2SPacket::copy),
            BlockPos.CODEC.fieldOf("paste").forGetter(CloneC2SPacket::paste),
            CloneType.CODEC.optionalFieldOf("type", CloneType.INSERT).forGetter(CloneC2SPacket::type)
    ).apply(instance, CloneC2SPacket::new));
    public static final PacketCodec<PacketByteBuf, CloneC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            Lieutenant.BLOCK_BOX_PACKET_CODEC, CloneC2SPacket::copy,
            BlockPos.PACKET_CODEC, CloneC2SPacket::paste,
            CloneType.PACKET_CODEC, CloneC2SPacket::type,
            CloneC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
