package net.typho.lieutenant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public record CircleC2SPacket(BlockPos pos, BlockState fill, int radius, Optional<RegistryKey<Block>> replace) implements CustomPayload {
    public static final Id<CircleC2SPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "circle"));
    public static final MapCodec<CircleC2SPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(CircleC2SPacket::pos),
            BlockState.CODEC.fieldOf("fill").forGetter(CircleC2SPacket::fill),
            Codecs.rangedInt(0, 256).fieldOf("radius").forGetter(CircleC2SPacket::radius),
            RegistryKey.createCodec(RegistryKeys.BLOCK).optionalFieldOf("replace").forGetter(CircleC2SPacket::replace)
    ).apply(instance, CircleC2SPacket::new));
    public static final PacketCodec<PacketByteBuf, CircleC2SPacket> PACKET_CODEC = PacketCodecs.codec(CODEC.codec()).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
