package rc55.mc.testmod.rfapi.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.item.BucketItem;
import net.minecraft.util.Identifier;
import rc55.mc.rfapi.data.gen.RFApiModelGenerationHelper;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.testmod.rfapi.TestModMain;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;
import rc55.mc.testmod.rfapi.item.TestModItems;

public class TestModModelDataGen extends FabricModelProvider {
    public TestModModelDataGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        RFApiModelGenerationHelper.createFluidBlockModel(generator, TestModFluids.DYE_FLUIDS.values().toArray(FluidReference[]::new));
        RFApiModelGenerationHelper.createFluidBlockModel(generator, TestModFluids.MILK, TestModFluids.STEAM);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        RFApiModelGenerationHelper.createVanillaBucketItemModel(generator, TestModItems.DYE_BUCKETS.values().toArray(BucketItem[]::new));
        RFApiModelGenerationHelper.createVanillaBucketItemModel(generator, TestModItems.MILK_FLUID_BUCKET, TestModItems.STEAM_BUCKET);

        generator.register(TestModItems.WOODEN_BUCKET, Models.GENERATED);
        generator.register(TestModItems.CERAMIC_BUCKET, Models.GENERATED);

        RFApiModelGenerationHelper.createCustomBucketItemModel(generator,
                new Identifier(TestModMain.MODID, "item/wooden_bucket"),
                RFApiModelGenerationHelper.BUCKET_OVERLAY,
                TestModItems.WOODEN_MILK_BUCKET, TestModItems.WOODEN_WATER_BUCKET
        );
        RFApiModelGenerationHelper.createCustomBucketItemModel(generator,
                new Identifier(TestModMain.MODID, "item/ceramic_bucket"),
                RFApiModelGenerationHelper.BUCKET_OVERLAY,
                TestModItems.CERAMIC_WATER_BUCKET
        );

        RFApiModelGenerationHelper.createCustomBucketItemModel(generator,
                new Identifier(TestModMain.MODID, "item/ceramic_bucket"),
                RFApiModelGenerationHelper.BUCKET_OVERLAY,
                TestModItems.CERAMIC_DYE_BUCKETS.values().toArray(BucketItem[]::new)
        );
    }
}
