package rc55.mc.rfapi.block;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class RFApiBlockTags {
    /**
     * Marks blocks that represents a fluid block
     */
    public static final TagKey<Block> FLUID = ofConventional("fluids");

    /**
     * Marks blocks like ice, which can convert to a type of fluid
     */
    public static final TagKey<Block> ICY = ofConventional("icy");

    /**
     * Marks blocks like sponge, which can absorb certain types of fluid
     */
    public static final TagKey<Block> SPONGE_LIKE = ofConventional("sponge_like");

    public static TagKey<Block> of(Identifier id) {
        return TagKey.of(RegistryKeys.BLOCK, id);
    }

    public static TagKey<Block> ofConventional(String id) {
        return of(Identifier.of("c", id));
    }

    public static TagKey<Block> fromFluidTag(TagKey<Fluid> tag) {
        return of(tag.id());
    }
}
