package rc55.mc.testmod.rfapi.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper;
import rc55.mc.rfapi.block.RFApiBlockTags;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.testmod.rfapi.block.TestModBlocks;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;

import java.util.concurrent.CompletableFuture;

public class TestModBlockTagDataGen extends FabricTagProvider.BlockTagProvider {
    public TestModBlockTagDataGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(RFApiBlockTags.FLUID)
                .add(TestModFluids.MILK.getBlock(), TestModFluids.STEAM.getBlock())
                .add(TestModFluids.DYE_FLUIDS.values()
                        .stream()
                        .map(FluidReference::getBlock)
                        .toArray(Block[]::new)
                );
        getOrCreateTagBuilder(RFApiBlockTags.ICY).add(TestModBlocks.MILK_ICE);
        getOrCreateTagBuilder(RFApiBlockTags.SPONGE_LIKE).add(TestModBlocks.LAVA_SPONGE);
    }
}
