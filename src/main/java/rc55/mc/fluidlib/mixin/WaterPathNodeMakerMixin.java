package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.ai.pathing.WaterPathNodeMaker;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidHelper;

@Mixin(WaterPathNodeMaker.class)
public class WaterPathNodeMakerMixin {
    // Changes how entity seeks path inside fluid
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = "getNodeType"
    )
    public boolean fluidlib$fixNavigationInFluid(FluidState state, TagKey<Fluid> tag) {
        return FluidHelper.checkEntityMoveAction(state.getFluid(), tag);
    }
}
