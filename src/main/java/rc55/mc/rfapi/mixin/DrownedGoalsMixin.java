package rc55.mc.rfapi.mixin;

import net.minecraft.entity.mob.DrownedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {
        "net.minecraft.entity.mob.DrownedEntity$LeaveWaterGoal",
        "net.minecraft.entity.mob.DrownedEntity$TargetAboveWaterGoal",
        //"net.minecraft.entity.mob.DrownedEntity$WanderAroundOnSurfaceGoal"
})
public class DrownedGoalsMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/DrownedEntity;isTouchingWater()Z"
            ),
            method = "canStart"
    )
    public boolean rfapi$fixDrownedSwimming(DrownedEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
