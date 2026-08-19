package rc55.mc.rfapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import rc55.mc.rfapi.fluid.FluidHelper;

import java.util.function.Predicate;

@FunctionalInterface
public interface EndServerChunkTickEvent {
    EndServerChunkTickEvent DEFAULT_CALLBACK = (world, chunk, randomTickSpeed) -> {};

    Event<EndServerChunkTickEvent> EVENT = EventFactory.createArrayBacked(EndServerChunkTickEvent.class, DEFAULT_CALLBACK, listeners -> (world, chunk, randomTickSpeed) -> {
        for (EndServerChunkTickEvent event : listeners) {
            event.onChunkTickEnd(world, chunk, randomTickSpeed);
        }
    });

    void onChunkTickEnd(ServerWorld world, WorldChunk chunk, int randomTickSpeed);

    /**
     * Creates an evnet callback for changing fluids to ice like vanilla water.
     * @param predicate Check if the chosen fluid matches type of the ice.
     *                  Note that this should also include fluid height checks.
     *                  If you want it to act like vanilla water, consider
     *                  using {@link rc55.mc.rfapi.fluid.FluidReference#matchesAndStill(Fluid)}
     * @param result The ice state to place
     * @return The event callback
     */
    static EndServerChunkTickEvent setIce(Predicate<Fluid> predicate, BlockState result) {
        return (world, chunk, randomTickSpeed) -> {
            world.getProfiler().swap("rfapi_customIceLogics");
            if (world.getRandom().nextInt(16) != 0) return;
            BlockPos upPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, world.getRandomPosInChunk(chunk.getPos().getStartX(), 0, chunk.getPos().getStartZ(), 15));
            BlockPos fluidPos = upPos.down();
            if (FluidHelper.canSetIce(world, fluidPos, predicate, true)) {
                world.setBlockState(fluidPos, result);
            }
        };
    }
}
