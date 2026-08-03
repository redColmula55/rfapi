package rc55.mc.fluidlib.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.fluidlib.fluid.FluidHelper;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin extends FlowableFluid {
    @Inject(at = @At("HEAD"), method = "canBeReplacedWith", cancellable = true)
    public void fluidlib$hookCustomFlowIntoLogic(
            FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction, CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(FluidHelper.canBeReplacedWith(this, state, world, pos, fluid, direction));
    }

    /**
     * Disables vanilla lava create stone
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"), method = "flow")
    public boolean fluidlib$disableVanillaStoneCreation(FluidState instance, TagKey<Fluid> tag) {
        return false;
    }
}
