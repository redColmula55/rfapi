package rc55.mc.fluidlib.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockIngredient implements Predicate<Block> {
    private final Collection<Entry> matching;

    public BlockIngredient(Collection<Entry> matching) {
        this.matching = matching;
    }

    public BlockIngredient(Stream<Entry> stream) {
        this(stream.collect(Collectors.toUnmodifiableSet()));
    }

    public static BlockIngredient of(Block... blocks) {
        return new BlockIngredient(Stream.of(blocks).map(Entry::new));
    }

    public static BlockIngredient of(TagKey<Block> tag) {
        return builder().add(tag).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final BlockIngredient EMPTY = new BlockIngredient(ImmutableList.of());
    public static final Codec<BlockIngredient> CODEC = Entry.CODEC.listOf().xmap(BlockIngredient::new, BlockIngredient::list);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockIngredient) {
            return this.stream().allMatch(e -> ((BlockIngredient) obj).matching.stream().anyMatch(e::equals));
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.matching.stream().findAny());
    }

    @Override
    public boolean test(Block block) {
        return this.matching.stream().anyMatch(entry -> entry.test(block));
    }

    public boolean test(BlockState state) {
        return this.test(state.getBlock());
    }

    public List<Entry> list() {
        return ImmutableList.copyOf(this.matching);
    }

    public Stream<Entry> stream() {
        return this.matching.stream();
    }

    public static class Entry implements Predicate<Block> {
        private final String id;
        private Block block;
        private TagKey<Block> tag;

        public static final Codec<Entry> CODEC = Codec.STRING.xmap(Entry::new, Entry::toString);

        public Entry(String id) {
            this.id = id;
            if (id.startsWith("#")) {
                this.tag = TagKey.of(RegistryKeys.BLOCK, Identifier.tryParse(id.substring(1)));
            } else {
                this.block = Registries.BLOCK.get(Identifier.tryParse(id));
            }
        }

        public Entry(Block block) {
            this.id = Registries.BLOCK.getId(block).toString();
            this.block = block;
        }

        public Entry(TagKey<Block> tag) {
            this.id = "#" + tag.id();
            this.tag = tag;
        }

        @Override
        public boolean test(Block block) {
            if (this.tag != null) {
                return block.getDefaultState().isIn(this.tag);
            } else {
                return block == this.block;
            }
        }

        @Override
        public String toString() {
            return this.id;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                return this.id.equals(((Entry) obj).id);
            } else return false;
        }

        public Stream<Block> stream() {
            if (this.tag != null) {
                return Registries.BLOCK.getOrCreateEntryList(this.tag).stream().map(RegistryEntry::value);
            } else {
                return Stream.of(this.block);
            }
        }
    }

    public static class Builder {
        private final Collection<Entry> entries = new ArrayList<>();

        public Builder add(Block block) {
            this.entries.add(new Entry(block));
            return this;
        }

        public Builder add(TagKey<Block> tag) {
            this.entries.add(new Entry(tag));
            return this;
        }

        public BlockIngredient build() {
            return new BlockIngredient(this.entries);
        }
    }
}
