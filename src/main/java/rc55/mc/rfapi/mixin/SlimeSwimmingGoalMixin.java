package rc55.mc.rfapi.mixin;

import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.rfapi.fluid.FluidSettings;

@Mixin(targets = "net.minecraft.entity.mob.SlimeEntity$SwimmingGoal")
public abstract class SlimeSwimmingGoalMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/SlimeEntity;isTouchingWater()Z"
            ),
            method = "canStart"
    )
    public boolean rfapi$fixSlimeSwimming(SlimeEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/SlimeEntity;isInLava()Z"
            ),
            method = "canStart"
    )
    public boolean rfapi$fixSlimeSwimming2(SlimeEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().getMovementType() == FluidSettings.EntityMovementType.LAVA);
    }
}
