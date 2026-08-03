package rc55.mc.fluidlib.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import rc55.mc.fluidlib.fluid.FluidReference;
import rc55.mc.fluidlib.fluid.FluidRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Util class used to match fluid reactions while touching each other
 */
public class FluidIngredient implements Predicate<Fluid> {
    private final Collection<Entry> matching;

    public FluidIngredient(Collection<Entry> matching) {
        this.matching = matching;
    }

    public FluidIngredient(Stream<Entry> stream) {
        this(stream.collect(Collectors.toUnmodifiableSet()));
    }

    public static FluidIngredient of(Fluid... fluids) {
        return new FluidIngredient(Stream.of(fluids).map(Entry::new));
    }

    public static FluidIngredient of(TagKey<Fluid> tag) {
        return builder().add(tag).build();
    }

    public static FluidIngredient of(FluidReference<?> fluid) {
        return of(fluid.getFlowing(), fluid.getStill());
    }

    /**
     * @return A builder for FluidIngredient used for data generation
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final FluidIngredient EMPTY = new FluidIngredient(ImmutableList.of());
    public static final Codec<FluidIngredient> CODEC = Entry.CODEC.listOf().xmap(FluidIngredient::new, FluidIngredient::list);

    @Override
    public boolean test(Fluid fluid) {
        return this.matching.stream().anyMatch(entry -> entry.test(fluid));
    }

    public boolean test(FluidState state) {
        return this.test(state.getFluid());
    }

    public List<Entry> list() {
        return ImmutableList.copyOf(this.matching);
    }

    public Stream<Entry> stream() {
        return this.matching.stream();
    }

    public static class Entry implements Predicate<Fluid> {
        private final String id;
        private Fluid fluid;
        private TagKey<Fluid> tag;

        public static final Codec<Entry> CODEC = Codec.STRING.xmap(Entry::new, Entry::toString);

        public Entry(String id) {
            this.id = id;
            if (id.startsWith("#")) {
                this.tag = TagKey.of(RegistryKeys.FLUID, Identifier.tryParse(id.substring(1)));
            } else {
                this.fluid = FluidRegistry.get(id);
            }
        }

        public Entry(Fluid fluid) {
            this.id = FluidRegistry.getId(fluid).toString();
            this.fluid = fluid;
        }

        public Entry(TagKey<Fluid> tag) {
            this.id = tag.toString();
            this.tag = tag;
        }

        @Override
        public boolean test(Fluid fluid) {
            if (this.tag != null) {
                return fluid.isIn(this.tag);
            } else {
                return fluid == this.fluid;
            }
        }

        @Override
        public String toString() {
            return this.id;
        }

        public Stream<Fluid> stream() {
            if (this.tag != null) {
                return Registries.FLUID.getOrCreateEntryList(this.tag).stream().map(RegistryEntry::value);
            } else {
                return Stream.of(this.fluid);
            }
        }
    }

    public static class Builder {
        private final Collection<Entry> entries = new ArrayList<>();

        public Builder add(Fluid fluid) {
            this.entries.add(new Entry(fluid));
            return this;
        }

        public Builder add(TagKey<Fluid> tag) {
            this.entries.add(new Entry(tag));
            return this;
        }

        public FluidIngredient build() {
            return new FluidIngredient(this.entries);
        }
    }
}
