package rc55.mc.rfapi.data;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A util interface for (de)serializing objects from/to {@link PacketByteBuf}
 * @param <T> Object type
 */
public interface ByteBufSerializer<T> {
    PacketByteBuf toByteBuf(T obj);

    T fromByteBuf(PacketByteBuf buf);

    default boolean hasPredictableSize() {
        return false;
    }

    /**
     * Convert {@link Codec}s to a network friendlier variant
     * @param codec The {@link Codec} to parse from
     * @return The serializer instance
     * @param <T> Object variant to work with
     * @deprecated Due to a huge waste of bandwidth, do **<strong>NOT</strong>** use this in your code. Preserved for compatibility only.
     */
    @Deprecated
    static <T> ByteBufSerializer<T> fromCodec(Codec<T> codec) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.encodeAsJson(codec, obj);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                return buf.decodeAsJson(codec);
            }
        };
    }

    /**
     * Creates a serializer instance which always returns a fixed result
     */
    static <T> ByteBufSerializer<T> unit(T obj) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                return PacketByteBufs.empty();
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                return obj;
            }

            @Override
            public boolean hasPredictableSize() {
                return true;
            }
        };
    }

    static <I, O> ByteBufSerializer<O> adapt(ByteBufSerializer<I> input, Function<? super I, ? extends O> adapter, Function<? super O, ? extends I> adapter2) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(O obj) {
                return input.toByteBuf(adapter2.apply(obj));
            }

            @Override
            public O fromByteBuf(PacketByteBuf buf) {
                return adapter.apply(input.fromByteBuf(buf));
            }

            @Override
            public boolean hasPredictableSize() {
                return input.hasPredictableSize();
            }
        };
    }

    static <T> ByteBufSerializer<Optional<T>> optional(ByteBufSerializer<T> codec, T defaultVal) {
        return adapt(codec, Optional::of, op -> op.orElse(defaultVal));
    }

    static <T> ByteBufSerializer<T> direct(Function<? super PacketByteBuf, ? extends T> getter, BiConsumer<? super PacketByteBuf, ? super T> setter) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                final var buf = PacketByteBufs.create();
                setter.accept(buf, obj);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                return getter.apply(buf);
            }

            @Override
            public boolean hasPredictableSize() {
                return true;
            }
        };
    }

    static <T> ByteBufSerializer<T> byRegistry(Registry<T> registry) {
        return adapt(INT, registry::get, registry::getRawId);
    }

    ByteBufSerializer<Boolean> BOOLEAN = direct(PacketByteBuf::readBoolean, PacketByteBuf::writeBoolean);
    ByteBufSerializer<Integer> INT = direct(PacketByteBuf::readVarInt, PacketByteBuf::writeVarInt);
    ByteBufSerializer<Float> FLOAT = direct(PacketByteBuf::readFloat, PacketByteBuf::writeFloat);
    ByteBufSerializer<String> STRING = direct(PacketByteBuf::readString, PacketByteBuf::writeString);
    ByteBufSerializer<Identifier> IDENTIFIER = adapt(STRING, Identifier::new, Identifier::toString);
    ByteBufSerializer<Item> ITEM = byRegistry(Registries.ITEM);
    ByteBufSerializer<Block> BLOCK = byRegistry(Registries.BLOCK);
    ByteBufSerializer<BlockState> BLOCK_STATE = adapt(INT, Block::getStateFromRawId, Block::getRawIdFromState);
    ByteBufSerializer<Fluid> FLUID = byRegistry(Registries.FLUID);

    // ========== Custom serializer creator utils ==========

    static <T, P1> ByteBufSerializer<T> create(
            Function<P1, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                return builder.apply(p1);
            }
        };
    }

    static <T, P1, P2> ByteBufSerializer<T> create(
            BiFunction<P1, P2, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                return builder.apply(p1, p2);
            }
        };
    }

    static <T, P1, P2, P3> ByteBufSerializer<T> create(
            Function3<P1, P2, P3, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                return builder.apply(p1, p2, p3);
            }
        };
    }

    static <T, P1, P2, P3, P4> ByteBufSerializer<T> create(
            Function4<P1, P2, P3, P4, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3,
            Function<T, P4> getter4, ByteBufSerializer<P4> codec4
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                write(getter4.apply(obj), codec4, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                P4 p4 = read(codec4, buf);
                return builder.apply(p1, p2, p3, p4);
            }
        };
    }

    static <T, P1, P2, P3, P4, P5> ByteBufSerializer<T> create(
            Function5<P1, P2, P3, P4, P5, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3,
            Function<T, P4> getter4, ByteBufSerializer<P4> codec4,
            Function<T, P5> getter5, ByteBufSerializer<P5> codec5
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                write(getter4.apply(obj), codec4, buf);
                write(getter5.apply(obj), codec5, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                P4 p4 = read(codec4, buf);
                P5 p5 = read(codec5, buf);
                return builder.apply(p1, p2, p3, p4, p5);
            }
        };
    }

    static <T, P1, P2, P3, P4, P5, P6> ByteBufSerializer<T> create(
            Function6<P1, P2, P3, P4, P5, P6, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3,
            Function<T, P4> getter4, ByteBufSerializer<P4> codec4,
            Function<T, P5> getter5, ByteBufSerializer<P5> codec5,
            Function<T, P6> getter6, ByteBufSerializer<P6> codec6
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                write(getter4.apply(obj), codec4, buf);
                write(getter5.apply(obj), codec5, buf);
                write(getter6.apply(obj), codec6, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                P4 p4 = read(codec4, buf);
                P5 p5 = read(codec5, buf);
                P6 p6 = read(codec6, buf);
                return builder.apply(p1, p2, p3, p4, p5, p6);
            }
        };
    }

    static <T, P1, P2, P3, P4, P5, P6, P7> ByteBufSerializer<T> create(
            Function7<P1, P2, P3, P4, P5, P6, P7, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3,
            Function<T, P4> getter4, ByteBufSerializer<P4> codec4,
            Function<T, P5> getter5, ByteBufSerializer<P5> codec5,
            Function<T, P6> getter6, ByteBufSerializer<P6> codec6,
            Function<T, P7> getter7, ByteBufSerializer<P7> codec7
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                write(getter4.apply(obj), codec4, buf);
                write(getter5.apply(obj), codec5, buf);
                write(getter6.apply(obj), codec6, buf);
                write(getter7.apply(obj), codec7, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                P4 p4 = read(codec4, buf);
                P5 p5 = read(codec5, buf);
                P6 p6 = read(codec6, buf);
                P7 p7 = read(codec7, buf);
                return builder.apply(p1, p2, p3, p4, p5, p6, p7);
            }
        };
    }

    static <T, P1, P2, P3, P4, P5, P6, P7, P8> ByteBufSerializer<T> create(
            Function8<P1, P2, P3, P4, P5, P6, P7, P8, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3,
            Function<T, P4> getter4, ByteBufSerializer<P4> codec4,
            Function<T, P5> getter5, ByteBufSerializer<P5> codec5,
            Function<T, P6> getter6, ByteBufSerializer<P6> codec6,
            Function<T, P7> getter7, ByteBufSerializer<P7> codec7,
            Function<T, P8> getter8, ByteBufSerializer<P8> codec8
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                write(getter4.apply(obj), codec4, buf);
                write(getter5.apply(obj), codec5, buf);
                write(getter6.apply(obj), codec6, buf);
                write(getter7.apply(obj), codec7, buf);
                write(getter8.apply(obj), codec8, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                P4 p4 = read(codec4, buf);
                P5 p5 = read(codec5, buf);
                P6 p6 = read(codec6, buf);
                P7 p7 = read(codec7, buf);
                P8 p8 = read(codec8, buf);
                return builder.apply(p1, p2, p3, p4, p5, p6, p7, p8);
            }
        };
    }

    static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9> ByteBufSerializer<T> create(
            Function9<P1, P2, P3, P4, P5, P6, P7, P8, P9, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3,
            Function<T, P4> getter4, ByteBufSerializer<P4> codec4,
            Function<T, P5> getter5, ByteBufSerializer<P5> codec5,
            Function<T, P6> getter6, ByteBufSerializer<P6> codec6,
            Function<T, P7> getter7, ByteBufSerializer<P7> codec7,
            Function<T, P8> getter8, ByteBufSerializer<P8> codec8,
            Function<T, P9> getter9, ByteBufSerializer<P9> codec9
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                write(getter4.apply(obj), codec4, buf);
                write(getter5.apply(obj), codec5, buf);
                write(getter6.apply(obj), codec6, buf);
                write(getter7.apply(obj), codec7, buf);
                write(getter8.apply(obj), codec8, buf);
                write(getter9.apply(obj), codec9, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                P4 p4 = read(codec4, buf);
                P5 p5 = read(codec5, buf);
                P6 p6 = read(codec6, buf);
                P7 p7 = read(codec7, buf);
                P8 p8 = read(codec8, buf);
                P9 p9 = read(codec9, buf);
                return builder.apply(p1, p2, p3, p4, p5, p6, p7, p8, p9);
            }
        };
    }

    static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> ByteBufSerializer<T> create(
            Function10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, T> builder,
            Function<T, P1> getter1, ByteBufSerializer<P1> codec1,
            Function<T, P2> getter2, ByteBufSerializer<P2> codec2,
            Function<T, P3> getter3, ByteBufSerializer<P3> codec3,
            Function<T, P4> getter4, ByteBufSerializer<P4> codec4,
            Function<T, P5> getter5, ByteBufSerializer<P5> codec5,
            Function<T, P6> getter6, ByteBufSerializer<P6> codec6,
            Function<T, P7> getter7, ByteBufSerializer<P7> codec7,
            Function<T, P8> getter8, ByteBufSerializer<P8> codec8,
            Function<T, P9> getter9, ByteBufSerializer<P9> codec9,
            Function<T, P10> getter10, ByteBufSerializer<P10> codec10
    ) {
        return new ByteBufSerializer<>() {
            @Override
            public PacketByteBuf toByteBuf(T obj) {
                PacketByteBuf buf = PacketByteBufs.create();
                write(getter1.apply(obj), codec1, buf);
                write(getter2.apply(obj), codec2, buf);
                write(getter3.apply(obj), codec3, buf);
                write(getter4.apply(obj), codec4, buf);
                write(getter5.apply(obj), codec5, buf);
                write(getter6.apply(obj), codec6, buf);
                write(getter7.apply(obj), codec7, buf);
                write(getter8.apply(obj), codec8, buf);
                write(getter9.apply(obj), codec9, buf);
                write(getter10.apply(obj), codec10, buf);
                return buf;
            }

            @Override
            public T fromByteBuf(PacketByteBuf buf) {
                P1 p1 = read(codec1, buf);
                P2 p2 = read(codec2, buf);
                P3 p3 = read(codec3, buf);
                P4 p4 = read(codec4, buf);
                P5 p5 = read(codec5, buf);
                P6 p6 = read(codec6, buf);
                P7 p7 = read(codec7, buf);
                P8 p8 = read(codec8, buf);
                P9 p9 = read(codec9, buf);
                P10 p10 = read(codec10, buf);
                return builder.apply(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
            }
        };
    }

    static <T> void write(T obj, ByteBufSerializer<? super T> codec, PacketByteBuf buf) {
        if (codec.hasPredictableSize()) {
            buf.writeBytes(codec.toByteBuf(obj));
        } else {
            PacketByteBuf tmp = codec.toByteBuf(obj);
            buf.writeVarInt(tmp.readableBytes());
            buf.writeBytes(tmp);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T read(ByteBufSerializer<? super T> codec, PacketByteBuf buf) {
        if (codec.hasPredictableSize()) {
            return (T) codec.fromByteBuf(buf);
        }
        int size = buf.readVarInt();
        return (T) codec.fromByteBuf(PacketByteBufs.copy(buf.readBytes(size)));
    }
}
