package rc55.mc.fluidlib.mixin;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidHelper;

@Mixin(FluidState.class)
public class FluidStateMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/fluid/Fluid;canBeReplacedWith(Lnet/minecraft/fluid/FluidState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;Lnet/minecraft/util/math/Direction;)Z"
            ),
            method = "canBeReplacedWith"
    )
    public boolean fluidlib$hookCustomFlowIntoLogic(Fluid instance, FluidState state, BlockView blockView, BlockPos pos, Fluid fluid, Direction direction) {
        return FluidHelper.canBeReplacedWith(instance, state, blockView, pos, fluid, direction);
    }
}
