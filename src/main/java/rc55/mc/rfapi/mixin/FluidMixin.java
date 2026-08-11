package rc55.mc.rfapi.mixin;

import net.minecraft.fluid.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import rc55.mc.rfapi.fluid.IFluidWithSettings;

@Mixin(Fluid.class)
public class FluidMixin implements IFluidWithSettings {
}
