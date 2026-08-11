package rc55.mc.rfapi.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.rfapi.fluid.FluidTags;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;isTouchingWater()Z"),
            method = "tick"
    )
    public boolean rfapi$hookPhysicsInLavaLikeFluid(ItemEntity instance) {
        return instance.getFluidHeight(FluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;isInLava()Z"),
            method = "tick"
    )
    public boolean rfapi$hookPhysicsInWaterLikeFluid(ItemEntity instance) {
        return instance.getFluidHeight(FluidTags.DUMMY_LAVA_PHYSICS_TAG) > 0.;
    }
}
