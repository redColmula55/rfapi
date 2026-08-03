package rc55.mc.fluidlib.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodecHelper {
    /**
     * A block state codec which will either accepts a simple block name, or full {@code {"Name": "", "Properties": {}}} declaration
     * In the former case, the default state will be used
     * Note that when serializing, this will always use the right side(which is the blockstate based codec)
     */
    public static final Codec<BlockState> BLOCK_STATE = Codec.either(
            Registries.BLOCK.getCodec().xmap(Block::getDefaultState, BlockState::getBlock),
            BlockState.CODEC
    ).xmap(
            e -> e.map(Function.identity(), Function.identity()),
            e -> e == e.getBlock().getDefaultState() ? Either.left(e) : Either.right(e)
    );

    public static final Codec<Float> CHANCE = rangedFloat(0f, 1f, f -> String.format("Must be a float within 0~1, but found %.4f", f));

    /**
     * Create a codec that accepts a list
     * @param registry
     * @param constructor
     * @param valueGetter
     * @return
     * @param <T>
     * @param <I>
     */
    @Deprecated
    public static <T, I> Codec<I> ingredientCodec(
            Registry<T> registry,
            Function<Stream<T>, ? extends I> constructor,
            Function<? super I, Stream<T>> valueGetter
    ) {
        return Codec.either(
                registry.getCodec(),
                TagKey.codec(registry.getKey())
        ).listOf().xmap(
                list -> list.stream().flatMap(either -> either
                        .mapBoth(Function.identity(), registry::getOrCreateEntryList)
                        .map(Stream::of, named -> named.stream().map(RegistryEntry::value))
                ),
                stream -> stream.map(Either::<T, TagKey<T>>left).collect(Collectors.toList())
        ).xmap(constructor, valueGetter);
    }

    private static Codec<Float> rangedFloat(float min, float max, Function<Float, String> messageFactory) {
        return Codecs.validate(
                Codec.FLOAT,
                value -> value.compareTo(min) > 0 && value.compareTo(max) <= 0 ? DataResult.success(value) : DataResult.error(() -> (String)messageFactory.apply(value))
        );
    }
}
