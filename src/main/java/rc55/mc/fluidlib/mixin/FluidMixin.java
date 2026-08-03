package rc55.mc.fluidlib.mixin;

import net.minecraft.fluid.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import rc55.mc.fluidlib.fluid.IFluidWithSettings;

@Mixin(Fluid.class)
public class FluidMixin implements IFluidWithSettings {
}
