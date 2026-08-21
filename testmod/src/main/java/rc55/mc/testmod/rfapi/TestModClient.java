package rc55.mc.testmod.rfapi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroups;
import rc55.mc.rfapi.client.FluidRenderRegistry;
import rc55.mc.testmod.rfapi.block.TestModBlocks;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;
import rc55.mc.testmod.rfapi.item.TestModItems;

public class TestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FluidRenderRegistry.registerColoredWater(TestModFluids.MILK);
        FluidRenderRegistry.registerColoredWater(TestModFluids.STEAM);

        TestModFluids.DYE_FLUIDS.values().forEach(FluidRenderRegistry::registerColoredWater);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            TestModItems.DYE_BUCKETS.values().forEach(entries::add);
            entries.add(TestModItems.MILK_FLUID_BUCKET);
            entries.add(TestModItems.STEAM_BUCKET);
            
            entries.add(TestModItems.CERAMIC_BUCKET);
            entries.add(TestModItems.CERAMIC_WATER_BUCKET);
            TestModItems.CERAMIC_DYE_BUCKETS.values().forEach(entries::add);
            entries.add(TestModItems.WOODEN_BUCKET);
            entries.add(TestModItems.WOODEN_WATER_BUCKET);
            entries.add(TestModItems.WOODEN_MILK_BUCKET);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> entries.add(TestModBlocks.MILK_ICE));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(TestModBlocks.LAVA_SPONGE);
            entries.add(TestModBlocks.WET_LAVA_SPONGE);
        });

        // Water has no item color by default, so we must register them manually
        FluidRenderRegistry.registerCustomColorProvider(0x0080FF, TestModItems.CERAMIC_WATER_BUCKET, TestModItems.WOODEN_WATER_BUCKET);

        FluidRenderRegistry.registerTransparentBlock(TestModBlocks.MILK_ICE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), TestModBlocks.MILK_ICE);
    }
}
