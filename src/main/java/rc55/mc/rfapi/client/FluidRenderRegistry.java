package rc55.mc.rfapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.fluid.FluidHelper;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.fluid.FluidRegistry;
import rc55.mc.rfapi.item.ExtendedBucketItem;

import java.util.BitSet;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public class FluidRenderRegistry {
    private static final BitSet CUSTOM_RENDERER_FLUID_IDS = new BitSet(256);

    /**
     * Register a fluid with custom renderer
     * @param fluid The fluid
     * @param renderHandler The renderer
     */
    public static void register(FluidReference<?> fluid, FluidRenderHandler renderHandler) {
        CUSTOM_RENDERER_FLUID_IDS.set(FluidRegistry.getRawId(fluid.getStill()));

        FluidRenderHandlerRegistry.INSTANCE.register(fluid.getStill(), fluid.getFlowing(), renderHandler);
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), fluid.getStill(), fluid.getFlowing());

        if (fluid.getSettings().getColor().itemColor() != -1) {
            registerCustomColorProvider(
                    fluid.getSettings().getColor().itemColor(),
                    appendToArray(
                            Item[]::new,
                            ExtendedBucketItem.BUCKET_ITEM_MAP.values().stream()
                                    .filter(map -> map.containsKey(fluid.getStill()))
                                    .map(map -> map.get(fluid.getStill()))
                                    .toArray(Item[]::new),
                            fluid.getSettings().getBucketItem()
                    )
            );
        }
    }

    /**
     * Register a fluid renderer using custom texture
     * @param fluid The fluid
     * @param stillTexture Texture when the fluid is still
     * @param flowingTexture Texture when the fluid is flowing
     * @param overlayTexture Texture behind {@linkplain FluidRenderHandlerRegistry#setBlockTransparency registered transparent blocks}(e.g. glass, leaves)
     */
    public static void register(FluidReference<?> fluid, Identifier stillTexture, Identifier flowingTexture, @Nullable Identifier overlayTexture) {
        register(fluid, new ExtendedFluidRenderHandler(fluid.getSettings().flowsUp(), stillTexture, flowingTexture, overlayTexture));
    }

    /**
     * Register a fluid renderer using water texture
     * @param fluid The fluid
     */
    public static void registerColoredWater(FluidReference<?> fluid) {
        register(fluid, ExtendedFluidRenderHandler.coloredWater(fluid.getSettings().flowsUp()));
    }

    /**
     * Will make items have color on {@code tintIndex == 1}
     * @param color The color, in RGB
     * @param items Items to color
     */
    public static void registerCustomColorProvider(int color, ItemConvertible... items) {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 1 ? color : 0xFFFFFF, items);
    }

    // For Sodium compat
    @ApiStatus.Internal
    public static boolean hasCustomRenderer(Fluid fluid) {
        return CUSTOM_RENDERER_FLUID_IDS.get(FluidRegistry.getRawId(FluidHelper.trim(fluid)));
    }

    private static <T> T[] appendToArray(IntFunction<T[]> arrProvider, T[] arr, T obj) {
        T[] newArr = arrProvider.apply(arr.length + 1);
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = obj;
        return newArr;
    }
}
