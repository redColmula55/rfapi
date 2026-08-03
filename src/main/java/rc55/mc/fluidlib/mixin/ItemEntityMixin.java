package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;isTouchingWater()Z"),
            method = "tick"
    )
    public boolean fluidlib$hookPhysicsInLavaLikeFluid(ItemEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;isInLava()Z"),
            method = "tick"
    )
    public boolean fluidlib$hookPhysicsInWaterLikeFluid(ItemEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_LAVA_PHYSICS_TAG) > 0.;
    }
}
