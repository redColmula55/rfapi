package rc55.mc.rfapi.data.gen.internal.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import rc55.mc.rfapi.item.RFApiItemTags;

import java.util.concurrent.CompletableFuture;

public class RFApiItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public RFApiItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(RFApiItemTags.BUCKETS_EMPTY).add(Items.BUCKET);
        getOrCreateTagBuilder(RFApiItemTags.BUCKETS_FILLED)
                .add(Items.WATER_BUCKET, Items.LAVA_BUCKET)
                .forceAddTag(ConventionalItemTags.WATER_BUCKETS)
                .forceAddTag(ConventionalItemTags.LAVA_BUCKETS)
                .forceAddTag(ConventionalItemTags.ENTITY_WATER_BUCKETS)
                .forceAddTag(ConventionalItemTags.MILK_BUCKETS);

        getOrCreateTagBuilder(RFApiItemTags.BUCKETS)
                .addTag(RFApiItemTags.BUCKETS_EMPTY)
                .addTag(RFApiItemTags.BUCKETS_FILLED);

        getOrCreateTagBuilder(ConventionalItemTags.EMPTY_BUCKETS).addTag(RFApiItemTags.BUCKETS_EMPTY);
    }
}
