package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidHelper;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {
    // Make boats float in water-like fluids
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = {"checkBoatInWater", "getWaterHeightBelow", "getUnderWaterLocation"}
    )
    public boolean fluidlib$hookInWaterCheck(FluidState state, TagKey<Fluid> tag) {
        return FluidHelper.checkEntityMoveAction(state.getFluid(), tag);
    }
}
