package rc55.mc.fluidlib.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.fluidlib.data.FluidReaction;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin {
    @Shadow
    protected @Final FlowableFluid fluid;

    @Inject(at = @At("HEAD"), method = "receiveNeighborFluids", cancellable = true)
    public void fluidlib$hookFluidReaction(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (FluidReaction.reaction(FluidReaction.Type.REACTION, world, pos, state)) {
            cir.setReturnValue(false);
        } else if (FluidReaction.reaction(FluidReaction.Type.INFECTION, world, pos, state)) {
            cir.setReturnValue(false);
        }
        cir.setReturnValue(true);
    }
}
