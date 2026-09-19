package rc55.mc.rfapi.entity;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * Provides entity collision for fluids
 * Will be automatically injected to {@link net.minecraft.entity.Entity}
 */
@ApiStatus.NonExtendable
public interface IFluidCollidable {
    default boolean isTouchingFluid(Predicate<Fluid> predicate) {
        return false;
    }

    default double getTouchingFluidHeight(Fluid fluid) {
        return 0;
    }

    default double getTouchingFluidHeight(FluidState state) {
        return this.getTouchingFluidHeight(state.getFluid());
    }
}
