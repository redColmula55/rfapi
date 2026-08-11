package rc55.mc.rfapi.fluid.reaction;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import rc55.mc.rfapi.RFApiMain;

/**
 * Type for a fluid reaction
 * @param codec Codec for data files
 * @param <T> The reaction class
 * @see IFluidReaction
 */
public record FluidReactionType<T extends IFluidReaction<?>>(Codec<T> codec) {
    public static final Registry<FluidReactionType<?>> REGISTRY = FabricRegistryBuilder.<FluidReactionType<?>>createDefaulted(
            RegistryKey.ofRegistry(new Identifier(RFApiMain.MODID, "fluid_reaction_type")), new Identifier("simple")
    ).attribute(RegistryAttribute.SYNCED).attribute(RegistryAttribute.MODDED).buildAndRegister();

    /**
     * Normal fluid reactions(e.g. vanilla basalt generator)
     */
    public static final FluidReactionType<SimpleFluidReaction> SIMPLE = register(
            new Identifier("simple"), new FluidReactionType<>(SimpleFluidReaction.CODEC)
    );

    /**
     * Normal fluid reactions, but creates different block when the source is still/flowing
     * (e.g. in vanilla, when flowing/still lava mets water creates cobblestone/obsidian)
     */
    public static final FluidReactionType<SimpleFlowableFluidReaction> FLOWABLE = register(
            new Identifier("flowable"), new FluidReactionType<>(SimpleFlowableFluidReaction.CODEC)
    );

    /**
     * Happens when a fluid flows into another fluid(e.g. vanilla stone generator)
     */
    public static final FluidReactionType<FlowIntoReaction> FLOW_INTO = register(
            new Identifier("flow_into"), new FluidReactionType<>(FlowIntoReaction.CODEC)
    );

    /**
     * Happens when 2 fluid source blocks try to create a new source block
     */
    public static final FluidReactionType<SourceConversionReaction> SOURCE_CONVERSION = register(
            new Identifier(RFApiMain.MODID, "source_conversion"), new FluidReactionType<>(SourceConversionReaction.CODEC)
    );

    /**
     * Turns the target fluid into the source fluid when they met each other
     */
    public static final FluidReactionType<InfectionReaction> INFECTION = register(
            new Identifier(RFApiMain.MODID, "infection"), new FluidReactionType<>(InfectionReaction.CODEC)
    );

    public static <T extends IFluidReaction<?>> FluidReactionType<T> register(Identifier id, FluidReactionType<T> type) {
        return Registry.register(REGISTRY, id, type);
    }

    @ApiStatus.Internal
    public static void init() {
        RFApiMain.LOGGER.info("Loaded {} reaction types.", REGISTRY.getKeys().size());
        IFluidReaction.initDataListener();
    }
}
