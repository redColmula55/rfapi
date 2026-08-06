package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.fluidlib.FluidLibConfigs;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {
    // Only catch fish in fluids with #c:has_fishes
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"), method = "tick"
    )
    public boolean fluidlib$modifyBobberTickingPredicate(FluidState instance, TagKey<Fluid> tag) {
        return instance.isIn(FluidLibFluidTags.HAS_FISHES);
    }

    @Inject(at = @At("HEAD"), method = "isOpenOrWaterAround", cancellable = true)
    public void fluidlib$modifyOpenWaterCheck(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (FluidLibConfigs.getInstance().disableOpenWaterCheck) {
            cir.setReturnValue(true);
        }
    }
}
