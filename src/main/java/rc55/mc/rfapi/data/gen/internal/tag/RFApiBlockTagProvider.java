package rc55.mc.rfapi.data.gen.internal.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import rc55.mc.rfapi.block.RFApiBlockTags;

import java.util.concurrent.CompletableFuture;

public class RFApiBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public RFApiBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(RFApiBlockTags.FLUID).add(Blocks.WATER, Blocks.LAVA);
    }
}
