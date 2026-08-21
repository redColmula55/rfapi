package rc55.mc.rfapi.fluid;

import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <h1>Fluid registry helper</h1>
 * <p>
 * Reservoir API provides its own way to register/manage fluids.
 * </p>
 * <p>
 * In vanilla, you should override some methods in
 * {@link Fluid} to determine certain property for the fluid.
 * Normally it's acceptable if your mod only has a few simple fluids,
 * but may be tricky when your mod needs to register a lot of fluids,
 * or your fluids has custom logics in multiple ways.
 * </p>
 * <p>
 * So in Reservoir, we will instead use our own {@link ExtendedFluid}
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
        return register(stillId, flowId, blockId, stillFactory, flowingFactory, blockFactory, settings, Function.identity());
    }

    /**
     * Register a fluid with a customized block, still, flowing factor and block settings
     * @param stillId ID for the still factor
     * @param flowId ID for the flowing factor
     * @param blockId ID for the fluid block
     * @param stillFactory Object factory used to create customized still fluid object
     * @param flowingFactory Object factory used to create customized flowing fluid object
     * @param blockFactory Object factory used to create customized fluid block
     * @param settings The fluid settings builder
     * @param blockSettingsAdapter A function that allows you to customize some
     *                             block settings on top of the automatically
     *                             created one, see {@linkplain FluidReference#createBlockSettings() how the settings was created}
     * @return The fluid reference
     * @param <F> The fluid type
     * @param <B> The fluid block type
     */
    public static <F extends FlowableFluid, B extends FluidBlock> FluidReference<F> register(
            Identifier stillId,
            Identifier flowId,
            Identifier blockId,
            BiFunction<FluidSettings, FluidReference<F>, F> stillFactory,
            BiFunction<FluidSettings, FluidReference<F>, F> flowingFactory,
            BiFunction<F, AbstractBlock.Settings, B> blockFactory,
            Function<FluidReference<F>, FluidSettings.Builder> settings,
            Function<AbstractBlock.Settings, ? extends AbstractBlock.Settings> blockSettingsAdapter
    ) {
        final FluidReference<F> reference = new FluidReference<>(stillId, flowId, blockId, settings);
        F still = register(stillId, stillFactory.apply(reference.getSettings(), reference));
        F flow = register(flowId, flowingFactory.apply(reference.getSettings(), reference));
        FluidSettings.register(reference);
        B block = registerFluidBlock(blockId, reference, blockFactory, blockSettingsAdapter);

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
            BiFunction<F, AbstractBlock.Settings, B> blockFactory,
            Function<AbstractBlock.Settings, ? extends AbstractBlock.Settings> settingsAdapter
    ) {
        return Registry.register(Registries.BLOCK, id, blockFactory.apply(fluid.getStill(), settingsAdapter.apply(fluid.createBlockSettings())));
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

    public static Stream<Fluid> stream() {
        return Registries.FLUID.stream();
    }

    public static Stream<Fluid> stream(Predicate<? super Fluid> predicate) {
        return stream().filter(predicate);
    }
}
