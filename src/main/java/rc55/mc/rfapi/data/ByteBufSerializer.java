package rc55.mc.rfapi.data;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

/**
 * A util interface for (de)serializing objects from/to {@link PacketByteBuf}
 * @param <T> Object type
 */
public interface ByteBufSerializer<T> {
    PacketByteBuf toByteBuf(T obj);

    T fromByteBuf(PacketByteBuf buf);

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
}
