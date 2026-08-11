package rc55.mc.rfapi.mixin;

import net.minecraft.entity.mob.WaterCreatureEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.rfapi.fluid.FluidTags;

@Mixin(WaterCreatureEntity.class)
public class WaterCreatureEntityMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/WaterCreatureEntity;isInsideWaterOrBubbleColumn()Z"),
            method = "tickWaterBreathingAir"
    )
    public boolean rfapi$modifyWaterCreatureBreathing(WaterCreatureEntity instance) {
        return instance.isSubmergedIn(FluidTags.DUMMY_UNBREATHABLE_TAG) || instance.isInsideWaterOrBubbleColumn();
    }
}
