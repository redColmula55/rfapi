package rc55.mc.fluidlib.fluid;

import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.MapColor;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import rc55.mc.fluidlib.FluidLib;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <h1>Fluid Lib fluid registry helper</h1>
 * <p>
 * FluidLib provides its own way to register/manage fluids.
 * </p>
 * <p>
 * In vanilla, you should override some methods in
 * {@link Fluid} to determine certain property for the fluid.
 * Normally it's acceptable if your mod only has a few simple fluids,
 * but may be tricky when your mod needs to register a lot of fluids,
 * or your fluids has custom logics in multiple ways.
 * </p>
 * <p>
 * So in FluidLib, we will instead use our own {@link ExtendedFluid}
 * and pass in a {@link FluidSettings} which will automatically determine
 * properties for the fluid and handles both still/flowing factor.
 * <br/>
 * Here we use a {@link FluidReference} to evaluate fluids lazily by caching their id and get
 * them from the registry when necessary.
 * </p>
 * <p>
 * Here is a simple example for registering a fluid with custom fluid block and given name:
 * <pre>{@code
 * // In your common init code
 * FluidReference<ExtendedFluid> TEST_FLUID = FluidRegistry.register(
 *     new Identifier(MODID, "test_fluid"),
 *     ExtendedFluid::ofStill,
 *     ExtendedFluid::ofFlowing,
 *     CustomFluidBlock::new,
 *     ref -> FluidSettings.waterLike().bucketItem(() -> TEST_FLUID_BUCKET)
 * );
 *
 * // In your client init code
 * // In this case still texture will be assets/modid/textures/block/test_fluid_still.png
 * TEST_STILL_TEX_ID = new Identifier(MODID, "block/test_fluid_still");
 * TEST_FLOW_TEX_ID = new Identifier(MODID, "blocks/test_fluid_flowing");
 * FluidRenderRegistry.register(TEST_FLUID, TEST_STILL_TEX_ID, TEST_FLOW_TEX_ID, null);
 * }</pre>
 * </p>
 * @see FluidSettings
 */
public final class FluidRegistry {
    private FluidRegistry() {
    }

    public static FluidReference<ExtendedFluid> registerSimple(Identifier id, FluidSettings.Builder settings) {
        return register(id, ExtendedFluid::ofStill, ExtendedFluid::ofFlowing, ref -> settings);
    }

    public static <T extends FlowableFluid> FluidReference<T> register(
            Identifier id,
            BiFunction<FluidSettings, FluidReference<T>, T> stillFactory,
            BiFunction<FluidSettings, FluidReference<T>, T> flowingFactory,
            Function<FluidReference<T>, FluidSettings.Builder> settings
    ) {
        return register(id, stillFactory, flowingFactory, FluidBlock::new, settings);
    }

    public static <F extends FlowableFluid, B extends FluidBlock> FluidReference<F> register(
            Identifier id,
            BiFunction<FluidSettings, FluidReference<F>, F> stillFactory,
            BiFunction<FluidSettings, FluidReference<F>, F> flowingFactory,
            BiFunction<F, AbstractBlock.Settings, B> blockFactory,
            Function<FluidReference<F>, FluidSettings.Builder> settings
    ) {
        final Identifier stillId = Identifier.of(id.getNamespace(), id.getPath() + "/still");
        final Identifier flowId = Identifier.of(id.getNamespace(), id.getPath() + "/flowing");
        return register(stillId, flowId, id, stillFactory, flowingFactory, blockFactory, settings);
    }

    public static <F extends FlowableFluid, B extends FluidBlock> FluidReference<F> register(
            Identifier stillId,
            Identifier flowId,
            Identifier blockId,
            BiFunction<FluidSettings, FluidReference<F>, F> stillFactory,
            BiFunction<FluidSettings, FluidReference<F>, F> flowingFactory,
            BiFunction<F, AbstractBlock.Settings, B> blockFactory,
            Function<FluidReference<F>, FluidSettings.Builder> settings
    ) {
        final FluidReference<F> reference = new FluidReference<>(stillId, flowId, blockId, settings);
        F still = register(stillId, stillFactory.apply(reference.getSettings(), reference));
        F flow = register(flowId, flowingFactory.apply(reference.getSettings(), reference));
        FluidSettings.register(reference);
        B block = registerFluidBlock(blockId, reference, blockFactory);

        reference.onRegister(still, flow, block);

        LandPathNodeTypesRegistry.register(block, (state, neighbor) -> switch (reference.getSettings().getMovementType()) {
            case WATER -> PathNodeType.WATER;
            case LAVA -> PathNodeType.LAVA;
            default -> PathNodeType.OPEN;
        });

        return reference;
    }

    private static <T extends Fluid> T register(Identifier id, T fluid) {
        return Registry.register(Registries.FLUID, id, fluid);
    }

    private static <F extends FlowableFluid, B extends FluidBlock> B registerFluidBlock(
            Identifier id,
            FluidReference<F> fluid,
            BiFunction<F, AbstractBlock.Settings, B> blockFactory
    ) {
        return Registry.register(Registries.BLOCK, id, blockFactory.apply(fluid.getStill(), fluid.createBlockSettings()));
    }

    public static RegistryKey<Fluid> keyFor(Identifier id) {
        return RegistryKey.of(RegistryKeys.FLUID, id);
    }

    public static Fluid get(Identifier id) {
        return Registries.FLUID.get(id);
    }

    public static Fluid get(String id) {
        return get(Identifier.tryParse(id));
    }

    public static Fluid get(int rawId) {
        return Registries.FLUID.get(rawId);
    }

    public static Fluid get(RegistryKey<Fluid> key) {
        return Registries.FLUID.get(key);
    }

    public static Identifier getId(Fluid fluid) {
        return Registries.FLUID.getId(fluid);
    }

    public static int getRawId(Fluid fluid) {
        return Registries.FLUID.getRawId(fluid);
    }
}
