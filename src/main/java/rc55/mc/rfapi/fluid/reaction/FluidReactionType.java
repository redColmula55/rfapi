package rc55.mc.rfapi.fluid.reaction;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import rc55.mc.rfapi.RFApiMain;
import rc55.mc.rfapi.data.ByteBufSerializer;
import rc55.mc.rfapi.data.ITranslatable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Type for a fluid reaction
 * @param codec Codec for (de)serializing reaction data from/to datapack
 * @param byteBufCodec Codec for (de)serializing reaction data on network layer(for C/S syncing)
 * @param <T> The reaction class
 * @see IFluidReaction
 */
public record FluidReactionType<T extends IFluidReaction<?>>(Codec<T> codec, ByteBufSerializer<T> byteBufCodec) implements ITranslatable {
    /// For compatibility only
    @Deprecated
    public FluidReactionType(Codec<T> codec) {
        this(codec, ByteBufSerializer.fromCodec(codec));
    }

    public int getRawId() {
        return REGISTRY.getRawId(this);
    }

    // Dirty hack to cache translation keys since record classes dont allow extra private fields
    private static final Map<FluidReactionType<?>, String> translationKeys = new IdentityHashMap<>();

    @Override
    public String getTranslationKey() {
        return translationKeys.computeIfAbsent(this,
                r -> Optional.ofNullable(REGISTRY.getId(r))
                        .map(id -> id.toTranslationKey("fluid_reaction"))
                        .orElse("fluid_reaction.unregistered")
        );
    }

    public static final Registry<FluidReactionType<?>> REGISTRY = FabricRegistryBuilder.<FluidReactionType<?>>createDefaulted(
            RegistryKey.ofRegistry(new Identifier(RFApiMain.MODID, "fluid_reaction_type")), new Identifier("simple")
    ).attribute(RegistryAttribute.SYNCED).attribute(RegistryAttribute.MODDED).buildAndRegister();

    public static FluidReactionType<?> byRawId(int id) {
        return REGISTRY.get(id);
    }

    /**
     * Normal fluid reactions(e.g. vanilla basalt generator)
     */
    public static final FluidReactionType<SimpleFluidReaction> SIMPLE = register(
            new Identifier("simple"), new FluidReactionType<>(SimpleFluidReaction.CODEC, SimpleFluidReaction.BUF_CODEC)
    );

    /**
     * Normal fluid reactions, but creates different block when the source is still/flowing
     * (e.g. in vanilla, when flowing/still lava mets water creates cobblestone/obsidian)
     */
    public static final FluidReactionType<SimpleFlowableFluidReaction> FLOWABLE = register(
            new Identifier("flowable"), new FluidReactionType<>(SimpleFlowableFluidReaction.CODEC, SimpleFlowableFluidReaction.BUF_CODEC)
    );

    /**
     * Happens when a fluid flows into another fluid(e.g. vanilla stone generator)
     */
    public static final FluidReactionType<FlowIntoReaction> FLOW_INTO = register(
            new Identifier("flow_into"), new FluidReactionType<>(FlowIntoReaction.CODEC, FlowIntoReaction.BUF_CODEC)
    );

    /**
     * Happens when 2 fluid source blocks try to create a new source block
     */
    public static final FluidReactionType<SourceConversionReaction> SOURCE_CONVERSION = register(
            new Identifier(RFApiMain.MODID, "source_conversion"), new FluidReactionType<>(SourceConversionReaction.CODEC, SourceConversionReaction.BUF_CODEC)
    );

    /**
     * Turns the target fluid into the source fluid when they met each other
     */
    public static final FluidReactionType<InfectionReaction> INFECTION = register(
            new Identifier(RFApiMain.MODID, "infection"), new FluidReactionType<>(InfectionReaction.CODEC, InfectionReaction.BUF_CODEC)
    );

    public static <T extends IFluidReaction<?>> FluidReactionType<T> register(Identifier id, FluidReactionType<T> type) {
        return Registry.register(REGISTRY, id, type);
    }

    @ApiStatus.Internal
    public static void init() {
        RFApiMain.LOGGER.info("Loaded {} internal reaction types.", REGISTRY.getKeys().size());
        IFluidReaction.initDataListener();
    }
}
