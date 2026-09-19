package rc55.mc.rfapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private static final VoxelShape AIR_COLLISION_SHAPE = VoxelShapes.cuboid(0, 0.5, 0, 1, 1, 1);

    @SuppressWarnings("deprecation")
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        EntityTouchFluidEvent.EVENT.invoker().onEntityCollision(state, world, pos, entity);
        super.onEntityCollision(state, world, pos, entity);
    }

    @Shadow
    public @Final FlowableFluid fluid;

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = this.fluid.getSettings().flowsUp() ? AIR_COLLISION_SHAPE : FluidBlock.COLLISION_SHAPE;
        return context.isAbove(shape, pos, true)
                && state.get(FluidBlock.LEVEL) == 0
                && context.canWalkOnFluid(world.getFluidState(pos.up()), state.getFluidState())
                ? shape : VoxelShapes.empty();
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(this.fluid.getBucketItem());
    }

    @ModifyReturnValue(
            method = "getOutlineShape", at = @At("RETURN")
    )
    public VoxelShape rfapi$drawFullOutlineOnConfig(VoxelShape original) {
        return RFApiConfigs.getInstance().drawFluidBlockOutline ? VoxelShapes.fullCube() : original;
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
