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

public record PlaceFeatureC2SPacket(BlockPos target, RegistryKey<ConfiguredFeature<?, ?>> feature) implements CustomPayload {
    public static final Id<PlaceFeatureC2SPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "place_feature"));
    public static final MapCodec<PlaceFeatureC2SPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("target").forGetter(PlaceFeatureC2SPacket::target),
            RegistryKey.createCodec(RegistryKeys.CONFIGURED_FEATURE).fieldOf("feature").forGetter(PlaceFeatureC2SPacket::feature)
    ).apply(instance, PlaceFeatureC2SPacket::new));
    public static final PacketCodec<PacketByteBuf, PlaceFeatureC2SPacket> PACKET_CODEC = PacketCodecs.codec(CODEC.codec()).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
