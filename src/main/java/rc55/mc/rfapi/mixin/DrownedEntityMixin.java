package rc55.mc.rfapi.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.DrownedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DrownedEntity.class)
public abstract class DrownedEntityMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/DrownedEntity;isTouchingWater()Z"
            ),
            method = {"travel", "updateSwimming"}
    )
    public boolean rfapi$fixDrownedSwimming(DrownedEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isTouchingWater()Z"
            ),
            method = {"isTargetingUnderwater", "canDrownedAttackTarget"}
    )
    public boolean rfapi$fixDrownedAttackTargeting(LivingEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
