package rc55.mc.rfapi.mixin;

import net.minecraft.entity.passive.AxolotlEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AxolotlEntity.class)
public class AxolotlEntityMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/AxolotlEntity;isTouchingWater()Z"
            ),
            method = "travel"
    )
    public boolean rfapi$fixAxolotlSwimming(AxolotlEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
