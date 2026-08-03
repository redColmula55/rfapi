package rc55.mc.fluidlib.data.gen;

import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.data.client.*;
import net.minecraft.item.BucketItem;
import net.minecraft.util.Identifier;
import rc55.mc.fluidlib.FluidLib;

import java.util.Optional;

public final class FluidLibModelGenerationHelper {
    private FluidLibModelGenerationHelper(){}

    public static final Identifier BUCKET_BASE = new Identifier(FluidLib.MODID, "item/bucket_base");
    public static final Identifier BUCKET_OVERLAY = new Identifier(FluidLib.MODID, "item/bucket_overlay");
    public static final Identifier BUCKET_INV_BASE = new Identifier(FluidLib.MODID, "item/bucket_upside_down_base");
    public static final Identifier BUCKET_INV_OVERLAY = new Identifier(FluidLib.MODID, "item/bucket_upside_down_overlay");

    private static final Model FLUID_BLOCK_MODEL = new Model(Optional.empty(), Optional.empty(), TextureKey.PARTICLE);

    public static void createVanillaBucketItemModel(ItemModelGenerator generator, BucketItem... buckets) {
        for (BucketItem item : buckets) {
            if (item.fluid.getSettings().flowsUp()) {
                createCustomBucketItemModel(generator, BUCKET_INV_BASE, BUCKET_INV_OVERLAY, item);
            } else {
                createCustomBucketItemModel(generator, BUCKET_BASE, BUCKET_OVERLAY, item);
            }
        }
    }

    public static void createCustomBucketItemModel(ItemModelGenerator generator, Identifier baseTexture, Identifier overlayTexture, BucketItem item) {
        Models.GENERATED_TWO_LAYERS.upload(
                ModelIds.getItemModelId(item.asItem()),
                TextureMap.layered(baseTexture, overlayTexture),
                generator.writer
        );
    }

    public static void createFluidBlockModel(BlockStateModelGenerator generator, FluidBlock... blocks) {
        for (FluidBlock block : blocks) {
            generator.registerSimpleState(block);
            FLUID_BLOCK_MODEL.upload(
                    ModelIds.getBlockModelId(block),
                    TextureMap.particle(Blocks.WATER),
                    generator.modelCollector
            );
        }
    }
}
