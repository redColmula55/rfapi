package rc55.mc.rfapi.mixin;

import net.minecraft.block.CoralParentBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.rfapi.fluid.FluidTags;

@Mixin(CoralParentBlock.class)
public abstract class CoralParentBlockMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = "isInWater"
    )
    private static boolean rfapi$checkCanSurvive(FluidState instance, TagKey<Fluid> tag) {
        return instance.isIn(FluidTags.CORAL_SURVIVES);
    }
}
