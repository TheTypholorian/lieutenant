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
import net.minecraft.util.math.BlockBox;

import java.util.Optional;

public record FillC2SPacket(BlockBox box, BlockState fill, Optional<RegistryKey<Block>> replace) implements CustomPayload {
    public static final Id<FillC2SPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "fill"));
    public static final MapCodec<FillC2SPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockBox.CODEC.fieldOf("box").forGetter(FillC2SPacket::box),
            BlockState.CODEC.fieldOf("fill").forGetter(FillC2SPacket::fill),
            RegistryKey.createCodec(RegistryKeys.BLOCK).optionalFieldOf("replace").forGetter(FillC2SPacket::replace)
    ).apply(instance, FillC2SPacket::new));
    public static final PacketCodec<PacketByteBuf, FillC2SPacket> PACKET_CODEC = PacketCodecs.codec(CODEC.codec()).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
