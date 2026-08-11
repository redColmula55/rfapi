package rc55.mc.rfapi.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.rfapi.fluid.FluidHelper;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin extends FlowableFluid {
    @Inject(at = @At("HEAD"), method = "canBeReplacedWith", cancellable = true)
    public void rfapi$hookCustomFlowIntoLogic(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(FluidHelper.canBeReplacedWith(this, state, world, pos, fluid, direction));
    }
}
