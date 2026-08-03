package rc55.mc.fluidlib.fluid;

import net.minecraft.fluid.Fluid;

/**
 * Provides settings for the fluid
 * Will be injected to {@link net.minecraft.fluid.Fluid}
 */
public interface IFluidWithSettings {
    default FluidSettings getSettings() {
        return FluidSettings.get((Fluid) this);
    }
}
