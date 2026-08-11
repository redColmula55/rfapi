package rc55.mc.rfapi.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.rfapi.fluid.FluidHelper;

@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin extends Fluid {
    @Shadow protected abstract int getLevelDecreasePerBlock(WorldView world);

    @Inject(at = @At("HEAD"), method = "getUpdatedState", cancellable = true)
    public void rfapi$hookCustomSourceConversion(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<FluidState> cir) {
        cir.setReturnValue(FluidHelper.getUpdatedState((FlowableFluid)(Object) this, world, pos, state, this.getLevelDecreasePerBlock(world)));
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return FluidHelper.canBeReplacedWith(this, state, world, pos, fluid, direction);
    }

    @Override
    public void onScheduledTick(World world, BlockPos pos, FluidState state) {
        FluidHelper.onScheduledTick((FlowableFluid)(Object) this, world, pos, state, this.getLevelDecreasePerBlock(world));
    }
}
