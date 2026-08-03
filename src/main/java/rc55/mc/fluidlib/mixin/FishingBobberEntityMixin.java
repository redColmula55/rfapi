package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
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
}
