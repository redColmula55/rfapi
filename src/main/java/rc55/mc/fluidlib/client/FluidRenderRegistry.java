package rc55.mc.fluidlib.client;

import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import rc55.mc.fluidlib.fluid.FluidHelper;
import rc55.mc.fluidlib.fluid.FluidReference;
import rc55.mc.fluidlib.fluid.FluidRegistry;
import rc55.mc.fluidlib.item.ExtendedBucketItem;

import java.util.Collection;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public class FluidRenderRegistry {
    private static final Collection<Identifier> CUSTOM_RENDERER_FLUID_IDS = Sets.newHashSet();

    public static void register(FluidReference<?> fluid, FluidRenderHandler renderHandler) {
        CUSTOM_RENDERER_FLUID_IDS.add(fluid.getStillId());

        FluidRenderHandlerRegistry.INSTANCE.register(fluid.getStill(), fluid.getFlowing(), renderHandler);
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), fluid.getStill(), fluid.getFlowing());

        if (fluid.getSettings().getColor().itemColor() != -1) {
            ColorProviderRegistry.ITEM.register(
                    (stack, tintIndex) -> tintIndex == 1 ? fluid.getSettings().getColor().itemColor() : 0xFFFFFF,
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

    public static void register(FluidReference<?> fluid, Identifier stillTexture, Identifier flowingTexture, @Nullable Identifier overlayTexture) {
        register(fluid, new ExtendedFluidRenderHandler(fluid.getSettings().flowsUp(), stillTexture, flowingTexture, overlayTexture));
    }

    public static void registerColoredWater(FluidReference<?> fluid) {
        register(fluid, ExtendedFluidRenderHandler.coloredWater(fluid.getSettings().flowsUp()));
    }

    // For Sodium compat
    @ApiStatus.Internal
    public static boolean hasCustomRenderer(Fluid fluid) {
        return CUSTOM_RENDERER_FLUID_IDS.contains(FluidRegistry.getId(FluidHelper.trim(fluid)));
    }

    private static <T> T[] appendToArray(IntFunction<T[]> arrProvider, T[] arr, T obj) {
        T[] newArr = arrProvider.apply(arr.length + 1);
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = obj;
        return newArr;
    }
}
