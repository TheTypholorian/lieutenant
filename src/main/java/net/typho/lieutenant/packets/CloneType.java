package net.typho.lieutenant.packets;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.function.Predicate;

public enum CloneType {
    REPLACE(state -> true),
    INSERT(state -> !state.isAir());

    public static final Codec<CloneType> CODEC = Codec.STRING.xmap(CloneType::valueOf, Enum::name);
    public static final PacketCodec<ByteBuf, CloneType> PACKET_CODEC = PacketCodecs.STRING.xmap(CloneType::valueOf, Enum::name);

    private final Predicate<BlockState> predicate;

    CloneType(Predicate<BlockState> predicate) {
        this.predicate = predicate;
    }

    public boolean test(BlockState state) {
        return predicate.test(state);
    }

    public CloneType prev() {
        if (ordinal() == 0) {
            return values()[values().length - 1];
        }

        return values()[ordinal() - 1];
    }

    public CloneType next() {
        if (ordinal() == values().length - 1) {
            return values()[0];
        }

        return values()[ordinal() + 1];
    }

    public String getTranslationKey() {
        return "clone_type.beryllium." + name().toLowerCase();
    }
}
