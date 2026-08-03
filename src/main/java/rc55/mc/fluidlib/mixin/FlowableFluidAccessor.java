package rc55.mc.fluidlib.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@ApiStatus.Internal
@Mixin(FlowableFluid.class)
public interface FlowableFluidAccessor {
    @Invoker("receivesFlow")
    boolean invoke_receivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState);

    @Invoker("canFill")
    boolean invoke_canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid);
}
