package rc55.mc.testmod.rfapi.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import rc55.mc.rfapi.data.gen.AbstractFluidTagProvider;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.fluid.FluidRegistry;
import rc55.mc.rfapi.fluid.FluidTags;
import rc55.mc.testmod.rfapi.TestModMain;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;

import java.util.concurrent.CompletableFuture;

public class TestModFluidTagDataGen extends AbstractFluidTagProvider {
    public TestModFluidTagDataGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.createBaseTagsForReference(
                FluidReference::getBlockId,
                FluidRegistry.stream(fluid -> TestModMain.MODID.equals(FluidRegistry.getId(fluid).getNamespace()))
                        .filter(fluid -> fluid instanceof FlowableFluid)
                        .map(fluid -> FluidReference.of((FlowableFluid) fluid))
                        .distinct()
                        .toArray(FluidReference[]::new)
        );

        final TagKey<Fluid> dyeTag = FluidTags.of(new Identifier(TestModMain.MODID, "dyes"));
        this.addToTag(dyeTag, TestModFluids.DYE_FLUIDS.values().toArray(FluidReference[]::new));
    }
}
