package rc55.mc.rfapi.mixin;

import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AquaticMoveControl.class)
public abstract class AquaticMoveControlMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/MobEntity;isTouchingWater()Z"
            ),
            method = "tick"
    )
    public boolean rfapi$fixAquaticMobPathFinding(MobEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
