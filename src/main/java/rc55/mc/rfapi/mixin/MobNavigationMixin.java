package rc55.mc.rfapi.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.MobNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.rfapi.fluid.FluidSettings;

@Mixin(MobNavigation.class)
public abstract class MobNavigationMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"),
            method = "getPathfindingY"
    )
    public boolean rfapi$fixNavigationInFluid(BlockState instance, Block block) {
        return FluidSettings.get(instance.getFluidState()).canSwim();
    }
}
