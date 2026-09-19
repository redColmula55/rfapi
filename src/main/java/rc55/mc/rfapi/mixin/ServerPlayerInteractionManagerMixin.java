package rc55.mc.rfapi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import rc55.mc.rfapi.RFApiConfigs;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    protected @Final ServerPlayerEntity player;

    @WrapOperation(
            method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z")
    )
    public boolean rfapi$breakFluidBlockForCreativePlayer(ServerWorld instance, BlockPos pos, boolean move, Operation<Boolean> original) {
        if (RFApiConfigs.getInstance().drawFluidBlockOutline
                && this.player.isCreative()
                && (instance.getBlockState(pos).getBlock() instanceof FluidBlock)
        ) {
            return instance.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | (move ? Block.MOVED : 0));
        }
        return original.call(instance, pos, move);
    }
}
