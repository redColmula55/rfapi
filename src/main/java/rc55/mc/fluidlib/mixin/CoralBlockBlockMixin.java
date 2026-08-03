package rc55.mc.fluidlib.mixin;

import net.minecraft.block.CoralBlockBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(CoralBlockBlock.class)
public abstract class CoralBlockBlockMixin {
    // Only survives in fluids that has #c:coral_survives tag
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"), method = "isInWater")
    public boolean fluidlib_checkCanSurvive(FluidState instance, TagKey<Fluid> tag) {
        return instance.isIn(FluidLibFluidTags.CORAL_SURVIVES);
    }
}
