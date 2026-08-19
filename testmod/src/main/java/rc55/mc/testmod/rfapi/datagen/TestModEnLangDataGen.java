package rc55.mc.testmod.rfapi.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import rc55.mc.rfapi.data.gen.RFApiLangProviderHelper;
import rc55.mc.rfapi.item.ExtendedBucketItem;
import rc55.mc.testmod.rfapi.block.TestModBlocks;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;
import rc55.mc.testmod.rfapi.item.TestModItems;

public class TestModEnLangDataGen extends FabricLanguageProvider {
    public TestModEnLangDataGen(FabricDataOutput dataOutput) {
        super(dataOutput, "en_us");
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {

        TestModFluids.DYE_FLUIDS.forEach((color, fluid) -> {
            String[] colors = {
                    "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime",
                    "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue",
                    "Brown", "Green", "Red", "Black"
            };
            RFApiLangProviderHelper.provideFluid(builder, fluid, colors[color.getId()] + " Dye");
        });

        RFApiLangProviderHelper.provideFluid(builder, TestModFluids.MILK, "Milk(Fluid)");
        RFApiLangProviderHelper.provideFluid(builder, TestModFluids.STEAM, "Steam");

        RFApiLangProviderHelper.provideBucket(builder, (ExtendedBucketItem) TestModItems.CERAMIC_BUCKET, "Ceramic Bucket", "Ceramic %s Bucket");
        RFApiLangProviderHelper.provideBucket(builder, (ExtendedBucketItem) TestModItems.WOODEN_BUCKET, "Wooden Bucket", "Wooden %s Bucket");

        builder.add(TestModBlocks.MILK_ICE, "Frozen Milk");
    }
}
