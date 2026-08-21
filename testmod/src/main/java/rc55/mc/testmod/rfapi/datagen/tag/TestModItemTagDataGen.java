package rc55.mc.testmod.rfapi.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import rc55.mc.rfapi.item.RFApiItemTags;
import rc55.mc.testmod.rfapi.item.TestModItems;

import java.util.concurrent.CompletableFuture;

public class TestModItemTagDataGen extends FabricTagProvider.ItemTagProvider {
    public TestModItemTagDataGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        final TagKey<Item> ceramicBucket = RFApiItemTags.ofConventional("buckets/ceramic");
        final TagKey<Item> ceramicBucketEmpty = RFApiItemTags.ofConventional("buckets/empty/ceramic");
        final TagKey<Item> ceramicBucketFilled = RFApiItemTags.ofConventional("buckets/filled/ceramic");
        final TagKey<Item> woodenBucket = RFApiItemTags.ofConventional("buckets/wooden");
        final TagKey<Item> woodenBucketEmpty = RFApiItemTags.ofConventional("buckets/empty/wooden");
        final TagKey<Item> woodenBucketFilled = RFApiItemTags.ofConventional("buckets/filled/wooden");

        getOrCreateTagBuilder(RFApiItemTags.BUCKETS_FILLED)
                .add(TestModItems.STEAM_BUCKET, TestModItems.MILK_FLUID_BUCKET)
                .add(TestModItems.DYE_BUCKETS.values().toArray(Item[]::new));

        getOrCreateTagBuilder(ceramicBucketEmpty).add(TestModItems.CERAMIC_BUCKET);
        getOrCreateTagBuilder(ceramicBucketFilled).add(TestModItems.CERAMIC_WATER_BUCKET).add(TestModItems.CERAMIC_DYE_BUCKETS.values().toArray(Item[]::new));
        getOrCreateTagBuilder(woodenBucketEmpty).add(TestModItems.WOODEN_BUCKET);
        getOrCreateTagBuilder(woodenBucketFilled).add(TestModItems.WOODEN_WATER_BUCKET, TestModItems.WOODEN_MILK_BUCKET);

        getOrCreateTagBuilder(ceramicBucket).addTag(ceramicBucketFilled).addTag(ceramicBucketEmpty);
        getOrCreateTagBuilder(woodenBucket).addTag(woodenBucketFilled).addTag(woodenBucketEmpty);

        getOrCreateTagBuilder(RFApiItemTags.BUCKETS_EMPTY).addTag(ceramicBucketEmpty).addTag(woodenBucketEmpty);
        getOrCreateTagBuilder(RFApiItemTags.BUCKETS_FILLED).addTag(ceramicBucketFilled).addTag(woodenBucketFilled);
    }
}
