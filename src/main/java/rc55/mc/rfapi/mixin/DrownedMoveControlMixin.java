package rc55.mc.rfapi.mixin;

import net.minecraft.entity.mob.DrownedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.entity.mob.DrownedEntity$DrownedMoveControl")
public class DrownedMoveControlMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/DrownedEntity;isTouchingWater()Z"
            ),
            method = "tick"
    )
    public boolean rfapi$fixDrownedSwimming(DrownedEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
