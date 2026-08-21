package rc55.mc.rfapi.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import rc55.mc.rfapi.fluid.FluidRegistry;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A sponge like block that allows customizing acceptable fluids
 */
public class FluidAbsorbingBlock extends Block {
    private final BlockState wetState;
    private final Predicate<Fluid> acceptableFluids;
    private final int maxAbsorptionDistance, maxAbsorptionAmount;

    public FluidAbsorbingBlock(Predicate<Fluid> acceptableFluids, int maxAbsorptionDistance, int maxAbsorptionAmount, BlockState wetState, Settings settings) {
        super(settings);
        this.wetState = wetState;
        this.acceptableFluids = acceptableFluids;
        this.maxAbsorptionDistance = maxAbsorptionDistance;
        this.maxAbsorptionAmount = maxAbsorptionAmount;
    }

    public FluidAbsorbingBlock(Predicate<Fluid> acceptableFluids, BlockState wetState, Settings settings) {
        this(acceptableFluids, 6, 65, wetState, settings);
    }

    public BlockState getWetState() {
        return wetState;
    }

    public Predicate<Fluid> getFluidPredicate() {
        return this.acceptableFluids;
    }

    public int getMaxAbsorptionDistance() {
        return maxAbsorptionDistance;
    }

    public int getMaxAbsorptionAmount() {
        return maxAbsorptionAmount;
    }

    public Stream<Fluid> getAcceptableFluids() {
        return FluidRegistry.stream(this.acceptableFluids);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.update(world, pos);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        this.update(world, pos);
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    protected void update(World world, BlockPos pos) {
        if (this.absorbWater(world, pos)) {
            world.setBlockState(pos, this.getWetState(), Block.NOTIFY_LISTENERS);
            world.syncWorldEvent(2001, pos, Block.getRawIdFromState(Blocks.WATER.getDefaultState()));
        }
    }

    private boolean absorbWater(World world, BlockPos pos) {
        return BlockPos.iterateRecursively(pos, this.getMaxAbsorptionDistance(), this.getMaxAbsorptionAmount(), (currentPos, queuer) -> {
            for (Direction direction : Direction.values()) {
                queuer.accept(currentPos.offset(direction));
            }
        }, (currentPos) -> {
            if (currentPos.equals(pos)) {
                return true;
            } else {
                BlockState state = world.getBlockState(currentPos);
                FluidState fluidState = world.getFluidState(currentPos);
                if (fluidState.isEmpty() || !this.getFluidPredicate().test(fluidState.getFluid())) {
                    return false;
                } else {
                    Block block = state.getBlock();
                    if (block instanceof FluidDrainable) {
                        if (!((FluidDrainable)block).tryDrainFluid(world, currentPos, state).isEmpty()) {
                            return true;
                        }
                    }

                    if (block instanceof FluidBlock) {
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    } else if (fluidState.isOf(Fluids.WATER)) {
                        if (!state.isOf(Blocks.KELP) && !state.isOf(Blocks.KELP_PLANT) && !state.isOf(Blocks.SEAGRASS) && !state.isOf(Blocks.TALL_SEAGRASS)) {
                            return false;
                        }

                        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(currentPos) : null;
                        dropStacks(state, world, currentPos, blockEntity);
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    }

                    return true;
                }
            }
        }) > 1;
    }
}
