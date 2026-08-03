package rc55.mc.fluidlib.fluid;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import rc55.mc.fluidlib.FluidLib;

public class FluidLibFluidTags {
    /**
     * The vanilla water tag
     */
    public static final TagKey<Fluid> WATER = of(Identifier.tryParse("water"));

    /**
     * The vanilla lava tag
     */
    public static final TagKey<Fluid> LAVA = of(Identifier.tryParse("lava"));

    /**
     * Whether corals can keep alive in these fluids
     */
    public static final TagKey<Fluid> CORAL_SURVIVES = ofConventional("coral_survives");

    /**
     * Whether players can catch fish in these fluids
     */
    public static final TagKey<Fluid> HAS_FISHES = ofConventional("has_fishes");

    /**
     * Marks fluids that cannot be placed in ultrawarm worlds(e.g. vanilla nether)
     */
    public static final TagKey<Fluid> DISAPPEAR_IN_ULTRAWARM = ofConventional("disappear_in_ultrawarm");

    public static TagKey<Fluid> of(Identifier id) {
        return TagKey.of(RegistryKeys.FLUID, id);
    }

    public static TagKey<Fluid> ofConventional(String id) {
        return of(Identifier.of("c", id));
    }

    private static TagKey<Fluid> ofDummy(String name) {
        return of(Identifier.of(FluidLib.MODID, "dummy/" + name));
    }

    @ApiStatus.Internal
    public static final TagKey<Fluid> DUMMY_WATER_PHYSICS_TAG = ofDummy("water_physics");

    @ApiStatus.Internal
    public static final TagKey<Fluid> DUMMY_LAVA_PHYSICS_TAG = ofDummy("lava_physics");

    @ApiStatus.Internal
    public static final TagKey<Fluid> DUMMY_UNBREATHABLE_TAG = ofDummy("unbreathable");
}
