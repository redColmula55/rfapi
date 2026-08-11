package rc55.mc.rfapi.data.gen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.fluid.Fluid;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.item.ExtendedBucketItem;

public final class RFApiLangProviderHelper {
    private RFApiLangProviderHelper(){}

    public static void provideFluid(FabricLanguageProvider.TranslationBuilder builder, Fluid fluid, String name) {
        builder.add(fluid.getDefaultState().getBlockState().getBlock(), name);
    }

    public static void provideFluid(FabricLanguageProvider.TranslationBuilder builder, FluidReference<?> fluid, String name) {
        builder.add(fluid.getBlock(), name);
    }

    public static void provideBucket(FabricLanguageProvider.TranslationBuilder builder, ExtendedBucketItem item, String baseName, String filledName) {
        if (!item.isEmpty()) {
            throw new IllegalArgumentException("Trying to generate translation for a empty bucket, but found a filled bucket!");
        }
        builder.add(item.getBaseTranslationKey(), baseName);
        builder.add(item.getContextTranslationKey(), filledName);
    }
}
