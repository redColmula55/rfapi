package rc55.mc.fluidlib.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.fluidlib.FluidLibConfigs;
import rc55.mc.fluidlib.fluid.reaction.FluidReactionType;
import rc55.mc.fluidlib.fluid.reaction.IFluidReaction;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin {
    @Inject(at = @At("HEAD"), method = "receiveNeighborFluids", cancellable = true)
    public void fluidlib$hookFluidReaction(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (IFluidReaction.triggerReaction(FluidReactionType.SIMPLE, world, pos, pos) != null) {
            cir.setReturnValue(false);
        } else if (IFluidReaction.triggerReaction(FluidReactionType.FLOWABLE, world, pos, pos) != null) {
            cir.setReturnValue(false);
        } else if (FluidLibConfigs.getInstance().blockVanillaFluidReactions) {
            // Cancel this so vanilla fluid reactions won't happen
            cir.setReturnValue(true);
        }
    }
}
