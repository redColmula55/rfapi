package rc55.mc.fluidlib.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.*;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.fluidlib.FluidLibConfigs;
import rc55.mc.fluidlib.fluid.reaction.FluidReactionType;
import rc55.mc.fluidlib.fluid.reaction.IFluidReaction;
import rc55.mc.fluidlib.mixin.FlowableFluidAccessor;

import java.util.Random;

/**
 * This holds some methods used for triggering fluid reactions
 * Do not call them outside of fluid flowing logics
 */
public class FluidHelper {
    /**
     * Calculates if a fluid can flow "into" another fluid(treat that fluid like air and flow through)
     * This handles {@link FluidReactionType#FLOW_INTO} logics so it needs to be hooked to vanilla fluids
     * Here we extract it into a static method so we can perform the hook with {@linkplain rc55.mc.fluidlib.mixin.FluidStateMixin#fluidlib$hookCustomFlowIntoLogic(Fluid, FluidState, BlockView, BlockPos, Fluid, Direction) mixins}
     * @param self The fluid instance which tries to call this method
     * @param state Fluid state to flow into
     * @param world World instance
     * @param pos Pos to flow into
     * @param fluid The fluid type to flow into
     * @param direction Flowing direction of the fluid
     * @return Whether the fluid to flow into can be replaced
     * @see FlowableFluid#canBeReplacedWith(FluidState, BlockView, BlockPos, Fluid, Direction) 
     */
    @ApiStatus.Internal
    public static boolean canBeReplacedWith(Fluid self, FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        if (state.isEmpty()) return true;
        if (self.matchesType(fluid)) return false;
        if (world instanceof WorldAccess) {
            IFluidReaction.triggerReaction(FluidReactionType.FLOW_INTO, (WorldAccess) world, pos.offset(direction.getOpposite()), pos);
        }
        return false;
    }

    /**
     * Calculates new fluid state while fluids tries to flow to another position
     * This also handles custom source conversion logics so it needs to be hooked to vanilla fluids
     * Here we extract it into a static method so we can perform the hook with {@linkplain rc55.mc.fluidlib.mixin.FlowableFluidMixin#fluidlib$hookCustomSourceConversion(World, BlockPos, BlockState, CallbackInfoReturnable) mixins}
     * @param self The fluid instance which tries to call this method
     * @param world World instance
     * @param pos Pos to flow into
     * @param state Block state used to be there in the pos to flow into
     * @param levelDecreasePerBlock See {@link FlowableFluid#getLevelDecreasePerBlock(WorldView)}
     * @return New fluid state
     * @see FlowableFluid#getUpdatedState(World, BlockPos, BlockState)
     */
    @ApiStatus.Internal
    public static FluidState getUpdatedState(FlowableFluid self, World world, BlockPos pos, BlockState state, int levelDecreasePerBlock) {
        final FluidSettings settings = FluidSettings.get(self);
        int i = 0;
        int neighboringSourceCount = 0;

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos sidePos = pos.offset(direction);
            BlockState sideState = world.getBlockState(sidePos);
            FluidState sideFluidState = sideState.getFluidState();
            if (((FlowableFluidAccessor) self).invoke_receivesFlow(direction, world, pos, state, sidePos, sideState)) {
                if (sideFluidState.getFluid().matchesType(self)) {
                    if (sideFluidState.isStill()) {
                        neighboringSourceCount++;
                    }

                    i = Math.max(i, sideFluidState.getLevel());
                } else if (self.isStill(state.getFluidState()) && sideFluidState.isStill()) {
                    BlockState conversionResult = IFluidReaction.triggerReaction(FluidReactionType.SOURCE_CONVERSION, world, sidePos, pos);
                    if (conversionResult != null) {
                        return conversionResult.getFluidState();
                    }
                }
            }
        }

        if (settings.isInfinite(world) && neighboringSourceCount >= 2) {
            BlockState bottomState = world.getBlockState(settings.flowsUp() ? pos.up() : pos.down());
            FluidState bottomFluidState = bottomState.getFluidState();
            if (bottomState.isSolid() || (self.matchesType(bottomFluidState.getFluid()) && bottomFluidState.isStill())) {
                //新水源方块（无限水）
                return self.getStill(false);
            }
        }

        BlockPos upPos = settings.flowsUp() ? pos.down() : pos.up();
        BlockState topState = world.getBlockState(upPos);
        FluidState topFluidState = topState.getFluidState();

        if (!topFluidState.isEmpty()
                && topFluidState.getFluid().matchesType(self)
                && ((FlowableFluidAccessor) self).invoke_receivesFlow(settings.flowsUp() ? Direction.DOWN : Direction.UP, world, pos, state, upPos, topState)
        ) {
            //向下
            return self.getFlowing(8, true);
        } else {
            //向四周
            int k = i - levelDecreasePerBlock;
            return k <= 0 ? Fluids.EMPTY.getDefaultState() : self.getFlowing(k, false);
        }
    }

    /**
     * Called when the fluid receives
     * This handles {@link FluidReactionType#INFECTION} logics so it needs to be hooked to vanilla fluids
     * Here we extract it into a static method so we can perform the hook with {@linkplain rc55.mc.fluidlib.mixin.FlowableFluidMixin#onScheduledTick(World, BlockPos, FluidState) mixins}
     * @param self The fluid instance which tries to call this method
     * @param world World instance
     * @param pos Pos to update
     * @param state Fluid state used to be in the pos
     * @param levelDecreasePerBlock See {@link FlowableFluid#getLevelDecreasePerBlock(WorldView)}
     * @see Fluid#onScheduledTick(World, BlockPos, FluidState)
     */
    @ApiStatus.Internal
    public static void onScheduledTick(FlowableFluid self, World world, BlockPos pos, FluidState state, int levelDecreasePerBlock) {
        // Do nothing if fluid updates are not allowed
        if (!FluidLibConfigs.getInstance().fluidUpdates) {
            return;
        }

        boolean infection = false;
        for (Direction direction : Direction.values()) {
            final BlockPos targetPos = pos.offset(direction);
            BlockState infected = IFluidReaction.triggerReaction(FluidReactionType.INFECTION, world, pos, targetPos);
            if (infected != null) {
                infection = true;
                world.scheduleFluidTick(targetPos, infected.getFluidState().getFluid(), ((FlowableFluidAccessor)self).invoke_getNextTickDelay(world, targetPos, state, infected.getFluidState()));
                world.updateNeighborsAlways(targetPos, infected.getBlock());
            }
        }
        if (infection) return;

        // Vanilla tick
        if (!state.isStill()) {
            FluidState fluidState = getUpdatedState(self, world, pos, world.getBlockState(pos), levelDecreasePerBlock);
            int i = ((FlowableFluidAccessor)self).invoke_getNextTickDelay(world, pos, state, fluidState);
            if (fluidState.isEmpty()) {
                state = fluidState;
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            } else if (!fluidState.equals(state)) {
                state = fluidState;
                BlockState blockState = fluidState.getBlockState();
                world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
                world.scheduleFluidTick(pos, fluidState.getFluid(), i);
                world.updateNeighborsAlways(pos, blockState.getBlock());
            }
        }

        ((FlowableFluidAccessor)self).invoke_tryFlow(world, pos, state);
    }

    public static void playExtinguishEvent(WorldAccess world, BlockPos pos) {
        world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
    }

    /**
     * Applied to entity movement/navigation checks
     * @param fluid The fluid
     * @param tag What vanilla tag should be applied by where it's called from
     * @return If the check is success
     */
    @ApiStatus.Internal
    public static boolean checkEntityMoveAction(Fluid fluid, TagKey<Fluid> tag) {
        final FluidSettings settings = fluid.getSettings();
        if (tag == FluidTags.WATER || tag == FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) {
            return settings.getMovementType() == FluidSettings.EntityMovementType.WATER;
        } else if (tag == FluidTags.LAVA || tag == FluidLibFluidTags.DUMMY_LAVA_PHYSICS_TAG) {
            return settings.getMovementType() == FluidSettings.EntityMovementType.LAVA;
        }
        return fluid.isIn(tag);
    }

    public static boolean checkCanMobSwim(MobEntity mob) {
        return mob.getFluidHeight(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) > mob.getSwimHeight()
                || mob.getFluidHeight(FluidLibFluidTags.DUMMY_LAVA_PHYSICS_TAG) > 0.;
    }

    /**
     * Gets the still form of the given fluid
     */
    public static Fluid trim(Fluid fluid) {
        if (fluid instanceof FlowableFluid) {
            return ((FlowableFluid) fluid).getStill();
        }
        return fluid;
    }

    public static <T> T[] shuffle(T[] arr) {
        Random random = new Random();
        for (int i = 0; i < arr.length; i++) {
            swap(arr, random.nextInt(arr.length), random.nextInt(arr.length));
        }
        return arr;
    }

    private static <T> void swap(T[] arr, int a, int b) {
        T tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }
}
