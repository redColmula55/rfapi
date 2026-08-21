package rc55.mc.rfapi.fluid;

import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides settings for the fluid
 * Will be injected to {@link net.minecraft.fluid.Fluid}
 */
@ApiStatus.NonExtendable
public interface IFluidWithSettings {
    default FluidSettings getSettings() {
        return FluidSettings.get((Fluid) this);
    }
}
