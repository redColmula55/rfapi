package rc55.mc.rfapi.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.fluid.FluidTags;

/**
 * An ice like block that allows customizing melted state
 */
public class FluidConvertibleBlock extends Block {
    protected final FluidReference<?> fluid;

    public FluidConvertibleBlock(FluidReference<?> fluid, Settings settings) {
        super(settings);
        this.fluid = fluid;
    }

    public FluidReference<?> getFluid() {
        return fluid;
    }

    public BlockState getMeltedState() {
        return this.getFluid().getBlock().getDefaultState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
            if (world.getDimension().ultrawarm()) {
                world.removeBlock(pos, false);
                return;
            }

            BlockState blockState = world.getBlockState(pos.down());
            if (blockState.blocksMovement() || blockState.isLiquid()) {
                world.setBlockState(pos, this.getMeltedState());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 11 - state.getOpacity(world, pos)) {
            this.melt(state, world, pos);
        }
    }

    protected void melt(BlockState state, World world, BlockPos pos) {
        if (this.fluid.getStill().isIn(FluidTags.DISAPPEAR_IN_ULTRAWARM) && world.getDimension().ultrawarm()) {
            world.removeBlock(pos, false);
        } else {
            world.setBlockState(pos, this.getMeltedState());
            world.updateNeighbor(pos, this.getFluid().getBlock(), pos);
        }
    }
}
