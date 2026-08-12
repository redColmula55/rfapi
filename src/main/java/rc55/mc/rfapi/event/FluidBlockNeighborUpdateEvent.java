package rc55.mc.rfapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface FluidBlockNeighborUpdateEvent {
    FluidBlockNeighborUpdateEvent DEFAULT_CALLBACK = (self, state, world, pos, sourceBlock, sourcePos, notify) -> false;

    Event<FluidBlockNeighborUpdateEvent> EVENT = EventFactory.createArrayBacked(FluidBlockNeighborUpdateEvent.class, DEFAULT_CALLBACK, listeners ->
            (self, state, world, pos, sourceBlock, sourcePos, notify) -> {
                for (FluidBlockNeighborUpdateEvent event : listeners) {
                    if (event.onNeighborUpdate(self, state, world, pos, sourceBlock, sourcePos, notify)) {
                        return true;
                    }
                }
                return false;
            }
    );

    /**
     * This event will be triggered when a fluid block receives a
     * neighboring block update and before any action performs
     * @param self The fluid block instance to call from
     * @param state Original state
     * @param world The world
     * @param pos Pos to update
     * @param sourceBlock Block which triggers the update
     * @param sourcePos Where the update triggers from
     * @param notify Notify others
     * @return If any further actions should be cancelled
     * @see net.minecraft.block.AbstractBlock.AbstractBlockState#neighborUpdate(World, BlockPos, Block, BlockPos, boolean)
     */
    boolean onNeighborUpdate(FluidBlock self, BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify);
}
