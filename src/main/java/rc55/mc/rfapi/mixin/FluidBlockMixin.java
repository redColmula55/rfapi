package rc55.mc.rfapi.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.rfapi.RFApiConfigs;
import rc55.mc.rfapi.event.EntityTouchFluidEvent;
import rc55.mc.rfapi.event.FluidBlockNeighborUpdateEvent;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;
import rc55.mc.rfapi.fluid.reaction.IFluidReaction;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin extends Block {
    private FluidBlockMixin(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        EntityTouchFluidEvent.EVENT.invoker().onEntityCollision(state, world, pos, entity);
        super.onEntityCollision(state, world, pos, entity);
    }

    @Inject(at = @At("HEAD"), method = "receiveNeighborFluids", cancellable = true)
    public void rfapi$hookFluidReaction(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (IFluidReaction.triggerReaction(FluidReactionType.SIMPLE, world, pos, pos) != null) {
            cir.setReturnValue(false);
        } else if (IFluidReaction.triggerReaction(FluidReactionType.FLOWABLE, world, pos, pos) != null) {
            cir.setReturnValue(false);
        } else if (RFApiConfigs.getInstance().blockVanillaFluidReactions) {
            // Cancel this so vanilla fluid reactions won't happen
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "neighborUpdate", cancellable = true)
    public void rfapi$triggerFluidNeighborUpdateEvent(
            BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci
    ) {
        if (FluidBlockNeighborUpdateEvent.EVENT.invoker().onNeighborUpdate(
                (FluidBlock)(Object) this, state, world, pos, sourceBlock, sourcePos, notify
        )) {
            ci.cancel();
        }
    }
}
