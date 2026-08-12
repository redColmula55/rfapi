package rc55.mc.rfapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface EntityTouchFluidEvent {
    EntityTouchFluidEvent DEFAULT_CALLBACK = (state, world, pos, entity) -> false;

    Event<EntityTouchFluidEvent> EVENT = EventFactory.createArrayBacked(EntityTouchFluidEvent.class, DEFAULT_CALLBACK, listeners ->
            (state, world, pos, entity) -> {
                for (EntityTouchFluidEvent event : listeners) {
                    if (event.onEntityCollision(state, world, pos, entity)) {
                        return true;
                    }
                }
                return false;
            }
    );

    /**
     * This event will be triggered when entity collides with a fluid block
     * @param state Block state the entity collides with
     * @param world The world
     * @param pos Pos of the block which the entity collides
     * @param entity The entity collides with the block
     * @return If any further actions should be cancelled
     */
    boolean onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity);
}
