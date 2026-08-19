package rc55.mc.rfapi.mixin;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.entity.passive.FrogEntity$FrogSwimPathNodeMaker")
public class FrogSwimPathNodeMakerMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;isTouchingWater()Z"), method = "getStart")
    public boolean rfapi$fixFrogSwimmingPathFinding(MobEntity instance) {
        return instance.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
