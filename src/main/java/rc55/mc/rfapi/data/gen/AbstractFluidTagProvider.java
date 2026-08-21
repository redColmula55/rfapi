package rc55.mc.rfapi.data.gen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.fluid.FluidTags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractFluidTagProvider extends FabricTagProvider.FluidTagProvider {
    public AbstractFluidTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    /**
     * Add given fluids to their tag(if exists), and add their still/flowing
     * factor to {@link FluidTags#STILL} / {@link FluidTags#FLOWING} tag
     * @param tagMapper Creates tag id for this fluid, null if no tag shall be provided
     */
    protected void createBaseTagsForReference(Function<FluidReference<?>, @Nullable Identifier> tagMapper, FluidReference<?>... fluids) {
        final FabricTagBuilder still = getOrCreateTagBuilder(FluidTags.STILL);
        final FabricTagBuilder flowing = getOrCreateTagBuilder(FluidTags.FLOWING);
        for (FluidReference<?> fluid : fluids) {
            still.add(fluid.getStill());
            flowing.add(fluid.getFlowing());
            final Identifier id = tagMapper.apply(fluid);
            if (id != null) {
                this.createAndAdd(id, fluid);
            }
        }
    }

    protected FabricTagBuilder addToTag(TagKey<Fluid> tag, FluidReference<?>... fluids) {
        final FabricTagBuilder builder = getOrCreateTagBuilder(tag);
        for (FluidReference<?> fluid : fluids) {
            builder.add(fluid.getFlowing());
            builder.add(fluid.getStill());
        }
        return builder;
    }

    @SafeVarargs
    protected final FabricTagBuilder addSubTags(TagKey<Fluid> tag, TagKey<Fluid>... tags) {
        final FabricTagBuilder builder = getOrCreateTagBuilder(tag);
        for (TagKey<Fluid> tagKey : tags) {
            builder.forceAddTag(tagKey);
        }
        return builder;
    }

    protected FabricTagBuilder createAndAdd(Identifier id, FluidReference<?>... fluids) {
        final FabricTagBuilder builder = getOrCreateTagBuilder(FluidTags.of(id));
        for (FluidReference<?> fluid : fluids) {
            builder.add(fluid.getFlowing());
            builder.add(fluid.getStill());
        }
        return builder;
    }
}
