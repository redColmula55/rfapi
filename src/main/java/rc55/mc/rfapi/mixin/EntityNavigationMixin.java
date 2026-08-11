package rc55.mc.rfapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import rc55.mc.rfapi.fluid.FluidTags;

@Mixin(EntityNavigation.class)
public abstract class EntityNavigationMixin {
    @Shadow
    protected @Final MobEntity entity;

    @ModifyReturnValue(at = @At("RETURN"), method = "isInLiquid")
    public boolean rfapi$fixNavigationInFluid(boolean original) {
        return original
                || this.entity.getFluidHeight(FluidTags.DUMMY_WATER_PHYSICS_TAG) > 0
                || this.entity.getFluidHeight(FluidTags.DUMMY_LAVA_PHYSICS_TAG) > 0;
    }
}
