package rc55.mc.rfapi.data;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple class used for data-driven resource cache
 * @see ResourceReloadListenerImpl
 * @param <K> Index (aka key) type
 * @param <R> Resource type
 */
public class ResourceMap<K, R> implements ByteBufSerializer<ResourceMap<K, R>> {
    private final boolean lazy;
    private final Map<K, Collection<R>> valuesMap;
    private final Collection<R> values;
    private final Function<? super R, Iterable<? extends K>> valueMapper;
    @Nullable
    protected final ByteBufSerializer<? super R> valueBufCodec;

    // values used in lazy versions
    private boolean requiresReload = false;
    private Collection<R> reloadCache;

    /**
     * Creates a ResourceMap
     * @param lazy Whether to load the resources lazily(only load them when requires a  while they're used)
     * @param valueMapper How resources are mapped to their indexes(aka keys)
     * @param valueBufCodec How resources are synced through client/server, {@code null} for resources only on one side
     */
    public ResourceMap(boolean lazy, Function<? super R, Iterable<? extends K>> valueMapper, @Nullable ByteBufSerializer<? super R> valueBufCodec) {
        this.lazy = lazy;
        this.valueMapper = valueMapper;
        this.values = new ArrayList<>();
        this.valuesMap = new HashMap<>();
        this.valueBufCodec = valueBufCodec;
    }

    /**
     * Creates a ResourceMap that is only on one side(or C/S sync logics handled manually)
     * @param lazy Whether to load the resources lazily(only load them while they're used)
     * @param valueMapper How resources are mapped to their indexes(aka keys)
     */
    public ResourceMap(boolean lazy, Function<? super R, Iterable<? extends K>> valueMapper) {
        this(lazy, valueMapper, null);
    }

    public static <T> ResourceMap<ItemConvertible, T> fromIngredient(Function<T, Ingredient> ingredientProvider) {
        return new ResourceMap<>(false, t -> Stream.of(ingredientProvider.apply(t).getMatchingStacks()).map(ItemStack::getItem).collect(Collectors.toList()), null);
    }

    /**
     * Reloads resources
     * @param map Parsed resources with their identifier
     */
    public void reload(Map<Identifier, R> map) {
        /* Tag will load too early in this, causing it unable to match stuff that uses tags
         * So in this case we load these data lazily by only marking them as
         * requires reload and load them while we actually use them
         */
        if (this.lazy) {
            this.requiresReload = true;
            this.reloadCache = map.values();
        } else {
            this.reload(map.values());
        }
    }

    protected void reload(Collection<R> newValues) {
        this.clear();
        this.values.addAll(newValues);
        newValues.forEach(resource -> {
            for (K indirectKey : this.valueMapper.apply(resource)) {
                this.valuesMap.computeIfAbsent(indirectKey, k -> new HashSet<>()).add(resource);
            }
        });

        this.requiresReload = false;
        this.reloadCache = null;
    }

    /**
     * If the value mapping is ready
     */
    public boolean isReady() {
        return !this.requiresReload;
    }

    /**
     * Requires a value refresh for lazy variants
     * In normal variants({@linkplain #lazy} = false) or when
     * values are ready({@linkplain #requiresReload} = false) this does nothing
     */
    public void loadLazyValues() {
        // In normal conditions(lazy = false) this will always be false
        if (this.requiresReload) {
            this.reload(this.reloadCache);
        }
    }

    public boolean contains(K key) {
        return this.valuesMap.containsKey(key);
    }

    public Collection<R> get(K key) {
        this.loadLazyValues();
        return this.valuesMap.getOrDefault(key, List.of());
    }

    public Optional<@Nullable R> getFirstMatch(K key) {
        return this.get(key).stream().findFirst();
    }

    public Optional<@Nullable R> getAnyMatch(K key) {
        return this.get(key).stream().findAny();
    }

    public R getOrThrow(K key) {
        return this.getAnyMatch(key).orElseThrow();
    }

    public R getOrDefault(K key, R defaultValue) {
        return this.getAnyMatch(key).orElse(defaultValue);
    }

    /**
     * Returns all values present
     * This will **<strong>NOT</strong>** automatically load the values for lazy variants
     * @return All values when {@linkplain #isReady() ready}, otherwise empty
     */
    public Collection<R> getAll() {
        return this.values;
    }

    public void clear() {
        this.values.clear();
        this.valuesMap.clear();
    }

    @Override
    public PacketByteBuf toByteBuf(ResourceMap<K, R> obj) {
        if (this.valueBufCodec == null) return PacketByteBufs.empty();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.values.size());
        for (final R val : this.values) {
            ByteBufSerializer.write(val, this.valueBufCodec, buf);
        }
        return buf;
    }

    public PacketByteBuf toByteBuf() {
        return this.toByteBuf(this);
    }

    @Override
    public ResourceMap<K, R> fromByteBuf(PacketByteBuf buf) {
        return this.fromByteBuf(buf, Runnable::run);
    }

    public ResourceMap<K, R> fromByteBuf(PacketByteBuf buf, Consumer<Runnable> taskExecutor) {
        if (this.valueBufCodec == null) return this;
        int size = buf.readVarInt();
        final List<R> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(ByteBufSerializer.read(this.valueBufCodec, buf));
        }
        taskExecutor.accept(() -> {
            if (this.lazy) {
                this.reloadCache = list;
                this.requiresReload = true;
            } else {
                this.reload(list);
            }
        });
        return this;
    }

    /**
     * ResourceMap that directly uses the resource identifier as their index
     * @param <R> Resource type
     */
    public static class Direct<R> extends ResourceMap<Identifier, R> {

        private final Map<Identifier, R> resources = new HashMap<>();
        public Direct(@Nullable ByteBufSerializer<R> valueBufCodec) {
            super(false, null, valueBufCodec);
        }
        public Direct() {
            this(null);
        }

        @Override
        protected void reload(Collection<R> newValues) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reload(Map<Identifier, R> map) {
            this.clear();
            this.resources.putAll(map);
        }

        @Override
        public Collection<R> get(Identifier key) {
            return this.resources.containsKey(key) ? ImmutableList.of(this.resources.get(key)) : ImmutableList.of();
        }

        @Override
        public Collection<R> getAll() {
            return this.resources.values();
        }

        @Override
        public void clear() {
            this.resources.clear();
        }

        @Override
        public PacketByteBuf toByteBuf() {
            if (this.valueBufCodec == null) {
                return PacketByteBufs.empty();
            }
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(this.resources.size());
            for (final Map.Entry<Identifier, R> entry : this.resources.entrySet()) {
                ByteBufSerializer.write(entry.getKey(), ByteBufSerializer.IDENTIFIER, buf);
                ByteBufSerializer.write(entry.getValue(), this.valueBufCodec, buf);
            }
            return buf;
        }

        @Override
        public ResourceMap<Identifier, R> fromByteBuf(PacketByteBuf buf, Consumer<Runnable> taskExecutor) {
            if (this.valueBufCodec == null) {
                return this;
            }
            this.clear();
            int size = buf.readVarInt();
            final Map<Identifier, R> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(ByteBufSerializer.read(ByteBufSerializer.IDENTIFIER, buf), ByteBufSerializer.read(this.valueBufCodec, buf));
            }
            taskExecutor.accept(() -> this.reload(map));
            return this;
        }
    }
}
