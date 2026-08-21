package rc55.mc.rfapi.data.gen.internal.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;
import rc55.mc.rfapi.data.gen.AbstractFluidTagProvider;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.fluid.FluidTags;

import java.util.concurrent.CompletableFuture;

public class RFApiFluidTagProvider extends AbstractFluidTagProvider {
    public RFApiFluidTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.createBaseTagsForReference(r -> null, FluidReference.VANILLA_LAVA, FluidReference.VANILLA_WATER);

        this.addSubTags(FluidTags.CORAL_SURVIVES, FluidTags.WATER);
        this.addSubTags(FluidTags.DISAPPEAR_IN_ULTRAWARM, FluidTags.WATER);
        this.addSubTags(FluidTags.HAS_FISHES, FluidTags.WATER);
    }
}
