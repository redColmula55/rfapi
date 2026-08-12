package rc55.mc.rfapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@FunctionalInterface
public interface FluidReplacementEvent {
    FluidReplacementEvent DEFAULT_CALLBACK = (self, state, world, pos, fluid, direction) -> TriState.DEFAULT;

    Event<FluidReplacementEvent> EVENT = EventFactory.createArrayBacked(FluidReplacementEvent.class, DEFAULT_CALLBACK, listeners ->
            (self, state, world, pos, fluid, direction) -> {
                TriState result = TriState.DEFAULT;
                for (FluidReplacementEvent event : listeners) {
                    result = event.onFluidReplacement(self, state, world, pos, fluid, direction);
                    if (result != TriState.DEFAULT) {
                        return result;
                    }
                }
                return result;
            }
    );

    /**
     * The event will be triggered before a fluid checking if
     * other fluids can flow "into" itself(Typically this replaces the fluid being flowed "into")
     * <p>
     *     This returns a {@link TriState} and has follow meaning:
     *     <ul>
     *         <li>{@link TriState#TRUE} means allowing fluids to flow in and cancel further actions</li>
     *         <li>{@link TriState#FALSE} means disallowing fluids to flow in and cancel further actions</li>
     *         <li>{@link TriState#DEFAULT} means to fall back to do nothing and continues to other events/further actions</li>
     *     </ul>
     * </p>
     * NOTE: This is called <strong>**BEFORE**</strong> checking if the fluid
     * is air or is the same, so be sure to check that as well!
     * @param self The fluid instance which tries to call this method
     * @param state Fluid state to flow into
     * @param world The world
     * @param pos Pos to flow into
     * @param fluid Fluid type to flow into
     * @param direction Flowing direction of the fluid
     * @return The result, with definitions above
     */
    TriState onFluidReplacement(Fluid self, FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction);
}
