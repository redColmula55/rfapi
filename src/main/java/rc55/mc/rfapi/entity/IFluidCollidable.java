package rc55.mc.rfapi.entity;

import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * Provides entity collision for fluids
 * Will be automatically injected to {@link net.minecraft.entity.Entity}
 */
@ApiStatus.NonExtendable
@ApiStatus.Internal
public interface IFluidCollidable {
    default boolean isTouchingFluid(Predicate<Fluid> predicate) {
        return false;
    }
}
