package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.mob.WaterCreatureEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(WaterCreatureEntity.class)
public class WaterCreatureEntityMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/WaterCreatureEntity;isInsideWaterOrBubbleColumn()Z"),
            method = "tickWaterBreathingAir"
    )
    public boolean fluidlib$modifyWaterCreatureBreathing(WaterCreatureEntity instance) {
        return instance.isSubmergedIn(FluidLibFluidTags.DUMMY_UNBREATHABLE_TAG) || instance.isInsideWaterOrBubbleColumn();
    }
}
