package rc55.mc.rfapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

@FunctionalInterface
public interface FluidScheduledTickEvent {
    FluidScheduledTickEvent DEFAULT_CALLBACK = (self, world, pos, state, levelDecreasePerBlock, nextTickDelay) -> false;

    Event<FluidScheduledTickEvent> EVENT = EventFactory.createArrayBacked(FluidScheduledTickEvent.class, DEFAULT_CALLBACK, listeners ->
            (self, world, pos, state, levelDecreasePerBlock, nextTickDelay) -> {
                for (final FluidScheduledTickEvent event : listeners) {
                    if (event.onScheduledTick(self, world, pos, state, levelDecreasePerBlock, nextTickDelay)) {
                        return true;
                    }
                }
                return false;
            }
    );

    /**
     * The event that will be triggered before fluid scheduled tick
     * @param self Fluid instance ticking
     * @param world Thw world
     * @param pos Pos to update
     * @param state Fluid state used to be in the pos
     * @param levelDecreasePerBlock See {@link FlowableFluid#getLevelDecreasePerBlock(WorldView)}
     * @param nextTickDelay See {@link FlowableFluid#getNextTickDelay(World, BlockPos, FluidState, FluidState)} ,
     *                      note both {@link FluidState} paras are the old states
     * @return If any further actions should be cancelled
     */
    boolean onScheduledTick(FlowableFluid self, World world, BlockPos pos, FluidState state, int levelDecreasePerBlock, int nextTickDelay);
}
