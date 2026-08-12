package rc55.mc.rfapi.client;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class FluidFogEvents {
    private FluidFogEvents(){}

    public static final Event<ModifyColor> COLOR = EventFactory.createArrayBacked(ModifyColor.class, ModifyColor.EMPTY_CALLBACK, listeners ->
            (camera, tickDelta, world, pos, state, viewDistance, skyDarkness) -> {
                for (ModifyColor event : listeners) {
                    int result = event.getFogColor(camera, tickDelta, world, pos, state, viewDistance, skyDarkness);
                    if (result != -1) {
                        return result;
                    }
                }
                return -1;
            }
    );

    public static final Event<ModifyDistance> DISTANCE = EventFactory.createArrayBacked(ModifyDistance.class, ModifyDistance.DEFAULT_CALLBACK, listeners ->
            (camera, fogType, viewDistance, thickFog, tickDelta, fogStart, fogEnd, fogShape) -> {
                for (ModifyDistance event : listeners) {
                    if (event.modifyFogDistance(camera, fogType, viewDistance, thickFog, tickDelta, fogStart, fogEnd, fogShape)) {
                        return true;
                    }
                }
                return false;
            }
    );

    @FunctionalInterface
    public interface ModifyColor {
        /**
         * Deeper customize for fluid fog render shapes.
         * Use {@link rc55.mc.rfapi.fluid.FluidSettings.ColorSettings#fogColor()} if you want fog in single color.
         * @param camera Client view camera
         * @param tickDelta Tick delta
         * @param world Client world instance
         * @param pos Pos the camera is in
         * @param state Fluid state the camera is in
         * @param viewDistance Client view distance
         * @param skyDarkness How dark the sky is
         * @return The fog color, in RGB format, -1 to do nothing
         */
        int getFogColor(Camera camera, float tickDelta, ClientWorld world, BlockPos pos, FluidState state, int viewDistance, float skyDarkness);

        ModifyColor EMPTY_CALLBACK = (camera, tickDelta, world, pos, state, viewDistance, skyDarkness) -> -1;
    }

    @FunctionalInterface
    public interface ModifyDistance {
        /**
         * Deeper customize for fluid fog render shapes.
         * Use {@link rc55.mc.rfapi.fluid.FluidSettings.ColorSettings.FogType} if you want vanilla like fog.
         * @param camera The client camera
         * @param fogType Current fog type
         * @param viewDistance Client view distance
         * @param thickFog If the fog should be thicker
         * @param tickDelta Tick delta
         * @param fogStart Setter for fog start distance
         * @param fogEnd Setter for fog end distance
         * @param fogShape Setter for fog shape
         * @return Whether to cancel further event execution
         */
        boolean modifyFogDistance(
                Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog,
                float tickDelta, FloatConsumer fogStart, FloatConsumer fogEnd, Consumer<FogShape> fogShape
        );

        ModifyDistance DEFAULT_CALLBACK = (camera, fogType, viewDistance, thickFog, tickDelta, fogStart, fogEnd, fogShape) -> false;
    }
}
