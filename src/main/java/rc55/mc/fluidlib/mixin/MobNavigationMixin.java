package rc55.mc.fluidlib.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.MobNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidSettings;

@Mixin(MobNavigation.class)
public class MobNavigationMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"),
            method = "getPathfindingY"
    )
    public boolean fluidlib$fixNavigationInFluid(BlockState instance, Block block) {
        return FluidSettings.get(instance.getFluidState()).canSwim();
    }
}
