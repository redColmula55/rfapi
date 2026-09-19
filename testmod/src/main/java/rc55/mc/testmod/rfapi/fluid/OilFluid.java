package rc55.mc.testmod.rfapi.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import rc55.mc.testmod.rfapi.block.TestModBlocks;
import rc55.mc.testmod.rfapi.item.TestModItems;

public abstract class OilFluid extends FlowableFluid {
    @Override
    public Fluid getFlowing() {
        return TestModFluids.FLOWING_OIL;
    }

    @Override
    public Fluid getStill() {
        return TestModFluids.STILL_OIL;
    }

    @Override
    public Item getBucketItem() {
        return TestModItems.OIL_BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return TestModBlocks.OIL_FLUID_BLOCK.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return this.getStill() == fluid || this.getFlowing() == fluid;
    }

    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 100f;
    }


    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    public static class Still extends OilFluid {
        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends OilFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder.add(LEVEL));
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }
    }
}
