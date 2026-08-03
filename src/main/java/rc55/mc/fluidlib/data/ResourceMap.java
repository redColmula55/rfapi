package rc55.mc.fluidlib.data;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple class used for data-driven resource cache
 * @see ResourceReloadListenerImpl
 * @param <K> Index (aka key) type
 * @param <R> Resource type
 */
public class ResourceMap<K, R> {
    private final boolean lazy;
    private final Map<K, Collection<R>> valuesMap;
    private final Collection<R> values;
    private final Function<R, Iterable<? extends K>> valueMapper;

    // values used in lazy versions
    private boolean requiresReload = false;
    private Collection<R> reloadCache;

    /**
     * Creates a ResourceMap
     * @param lazy Whether to load the resources lazily(only load them while they're used)
     * @param valueMapper How resources are mapped to their indexes(aka keys)
     */
    public ResourceMap(boolean lazy, Function<R, Iterable<? extends K>> valueMapper) {
        this.lazy = lazy;
        this.valueMapper = valueMapper;
        this.values = new ArrayList<>();
        this.valuesMap = new HashMap<>();
    }

    public static <T> ResourceMap<ItemConvertible, T> fromIngredient(Function<T, Ingredient> ingredientProvider) {
        return new ResourceMap<>(false, t -> Stream.of(ingredientProvider.apply(t).getMatchingStacks()).map(ItemStack::getItem).collect(Collectors.toList()));
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

    public boolean contains(K key) {
        return this.valuesMap.containsKey(key);
    }

    public Collection<R> get(K key) {
        // In normal conditions this will always be false
        if (this.requiresReload) {
            this.reload(this.reloadCache);
        }
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

    public Collection<R> getAll() {
        return this.values;
    }

    public void clear() {
        this.values.clear();
        this.valuesMap.clear();
    }

    /**
     * ResourceMap that directly uses the resource identifier as their index
     * @param <R> Resource type
     */
    public static class Direct<R> extends ResourceMap<Identifier, R> {

        private final Map<Identifier, R> resources = new HashMap<>();
        public Direct() {
            super(false, null);
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
    }
}
