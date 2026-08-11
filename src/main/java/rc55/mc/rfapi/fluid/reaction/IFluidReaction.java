package rc55.mc.rfapi.fluid.reaction;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.RFApiMain;
import rc55.mc.rfapi.data.FluidIngredient;
import rc55.mc.rfapi.data.ResourceMap;
import rc55.mc.rfapi.data.ResourceReloadListenerImpl;
import rc55.mc.rfapi.fluid.FluidHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a data-driven system which determines how fluids behave when they match certain block/fluid
 * <p>
 *     To add a new reaction type, first register it through {@link FluidReactionType#register(Identifier, FluidReactionType)}.
 *     After that, pick a random pos and trigger your new reaction type
 *     manually by {@link #triggerReaction(FluidReactionType, WorldAccess, BlockPos, BlockPos)}.
 * </p>
 * @param <T> The type of this reaction
 */
public interface IFluidReaction<T extends FluidReactionType<?>> {
    Codec<IFluidReaction<?>> BASE_CODEC = new Codec<>() {
        @Override
        public <P> DataResult<Pair<IFluidReaction<?>, P>> decode(DynamicOps<P> ops, P prefix) {
            return ops.get(prefix, "type")
                    .flatMap(p -> FluidReactionType.REGISTRY.getCodec().parse(ops, p))
                    .flatMap(type -> type.codec().parse(ops, prefix))
                    .map(reaction -> Pair.of(reaction, prefix));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <P> DataResult<P> encode(IFluidReaction<? extends FluidReactionType> reaction, DynamicOps<P> ops, P prefix) {
            return FluidReactionType.REGISTRY.getCodec().encodeStart(ops, reaction.getType())
                    .map(p -> ops.set(prefix, "type", p))
                    .flatMap(p -> ((Codec<IFluidReaction<?>>)reaction.getType().codec()).encode(reaction, ops, p));
        }
    };

    ResourceMap<Fluid, IFluidReaction<?>> CACHE = new ResourceMap<>(true, r -> r.getSource().stream()
            .flatMap(FluidIngredient.Entry::stream).collect(Collectors.toList())
    );

    ResourceReloadListenerImpl<IFluidReaction<?>> RELOAD_LISTENER = ResourceReloadListenerImpl.ofServer(
            Identifier.of(RFApiMain.MODID, "fluid_reaction"), IFluidReaction.BASE_CODEC, IFluidReaction.CACHE
    );

    @ApiStatus.Internal
    static void initDataListener() {
    }

    /**
     * Triggers fluid reaction
     * @param type Type of reaction to trigger, {@code null} to dismiss type check
     * @param world The world
     * @param sourcePos Where the fluid triggers it from
     * @param targetPos Where the result will be placed
     * @return The result of the reaction, {@code null} for no reaction performed
     */
    static @Nullable BlockState triggerReaction(@Nullable FluidReactionType<?> type, WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        final Fluid fluid = world.getFluidState(sourcePos).getFluid();
        for (final IFluidReaction<?> reaction : CACHE.get(fluid)) {
            if (type == null || reaction.getType() == type) {
                if (reaction.checkReaction(world, sourcePos, targetPos)) {
                    final BlockState result = reaction.performReaction(world, sourcePos, targetPos);
                    if (result == null) {
                        continue;
                    }
                    if (fluid.getSettings().canSetFire()) {
                        FluidHelper.playExtinguishEvent(world, sourcePos);
                    } else if (world.getFluidState(targetPos).getFluid().getSettings().canSetFire()) {
                        FluidHelper.playExtinguishEvent(world, targetPos);
                    }
                    return result;
                }
            }
        }
        return null;
    }

    static <T extends IFluidReaction<?>> Collection<T> allFor(@Nullable FluidReactionType<T> type, Fluid fluid) {
        if (type == null) {
            return (Collection<T>) ImmutableSet.copyOf(CACHE.get(fluid));
        }
        List<T> list = new ArrayList<>(5);
        for (final IFluidReaction<?> reaction : CACHE.get(fluid)) {
            if (reaction.getType() == type) {
                list.add((T) reaction);
            }
        }
        return ImmutableSet.copyOf(list);
    }

    /**
     * Check if the fluid to reaction with is not a waterlogged block
     * @param world The world
     * @param pos Pos to perform reaction
     * @param allowEmpty Whether allow the pos to be air
     */
    static boolean validateFluidBlock(BlockView world, BlockPos pos, boolean allowEmpty) {
        return (allowEmpty && world.getBlockState(pos).isAir()) || world.getBlockState(pos).getBlock() instanceof FluidBlock;
    }

    T getType();

    /**
     * Fluids able to trigger this reaction
     */
    FluidIngredient getSource();

    /**
     * Chance of this reaction happens, within {@code [0,1]}
     * Set to 1 for always, 0 for never
     */
    float getChance();

    /**
     * What this reaction produces
     */
    BlockState getResult();

    /**
     * Checks the chance for this reaction to happen
     * @param world The world
     */
    default boolean checkRandom(WorldAccess world) {
        return world.getRandom().nextFloat() < this.getChance();
    }

    /**
     * Check if this reaction can happen
     * This is also responsible for {@linkplain #checkRandom(WorldAccess) checking the random}
     * @param world The world
     * @param sourcePos Where the reaction was triggered
     * @param targetPos Where the result will be placed
     */
    boolean checkReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos);

    /**
     * Performs the reaction
     * Call {@link #checkReaction(WorldAccess, BlockPos, BlockPos)} before calling this!
     * @param world The world
     * @param sourcePos Where the reaction was triggered
     * @param targetPos Where the result will be placed
     * @return The result, {@code null} for not performed
     * @see #triggerReaction(FluidReactionType, WorldAccess, BlockPos, BlockPos)
     */
    @Nullable
    BlockState performReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos);
}
