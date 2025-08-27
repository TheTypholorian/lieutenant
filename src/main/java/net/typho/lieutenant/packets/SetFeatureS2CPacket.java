package net.typho.lieutenant.packets;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.typho.lieutenant.Lieutenant;

public record SetFeatureS2CPacket(RegistryKey<ConfiguredFeature<?, ?>> feature) implements CustomPayload {
    public static final Id<SetFeatureS2CPacket> ID = new Id<>(Identifier.of(Lieutenant.MOD_ID, "set_feature"));
    public static final MapCodec<SetFeatureS2CPacket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.CONFIGURED_FEATURE).fieldOf("feature").forGetter(SetFeatureS2CPacket::feature)
    ).apply(instance, SetFeatureS2CPacket::new));
    public static final PacketCodec<PacketByteBuf, SetFeatureS2CPacket> PACKET_CODEC = PacketCodec.tuple(
            RegistryKey.createPacketCodec(RegistryKeys.CONFIGURED_FEATURE), SetFeatureS2CPacket::feature,
            SetFeatureS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
