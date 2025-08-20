package net.typho.lieutenant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public record FeatureC2SPacket(BlockPos target, RegistryKey<ConfiguredFeature<?, ?>> feature) implements CustomPayload {
    public static final Id<FeatureC2SPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "feature"));
    public static final MapCodec<FeatureC2SPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("target").forGetter(FeatureC2SPacket::target),
            RegistryKey.createCodec(RegistryKeys.CONFIGURED_FEATURE).fieldOf("feature").forGetter(FeatureC2SPacket::feature)
    ).apply(instance, FeatureC2SPacket::new));
    public static final PacketCodec<PacketByteBuf, FeatureC2SPacket> PACKET_CODEC = PacketCodecs.codec(CODEC.codec()).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
