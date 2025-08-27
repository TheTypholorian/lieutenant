package net.typho.lieutenant.packets;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.typho.lieutenant.Lieutenant;

import java.util.Optional;

public record CircleC2SPacket(BlockPos pos, RegistryKey<Block> fill, int radius, Optional<RegistryKey<Block>> replace) implements CustomPayload {
    public static final Id<CircleC2SPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "circle"));
    public static final MapCodec<CircleC2SPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(CircleC2SPacket::pos),
            RegistryKey.createCodec(RegistryKeys.BLOCK).fieldOf("fill").forGetter(CircleC2SPacket::fill),
            Codecs.rangedInt(0, 256).fieldOf("radius").forGetter(CircleC2SPacket::radius),
            RegistryKey.createCodec(RegistryKeys.BLOCK).optionalFieldOf("replace").forGetter(CircleC2SPacket::replace)
    ).apply(instance, CircleC2SPacket::new));
    public static final PacketCodec<PacketByteBuf, CircleC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, CircleC2SPacket::pos,
            RegistryKey.createPacketCodec(RegistryKeys.BLOCK), CircleC2SPacket::fill,
            PacketCodecs.INTEGER, CircleC2SPacket::radius,
            PacketCodecs.optional(RegistryKey.createPacketCodec(RegistryKeys.BLOCK)), CircleC2SPacket::replace,
            CircleC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
