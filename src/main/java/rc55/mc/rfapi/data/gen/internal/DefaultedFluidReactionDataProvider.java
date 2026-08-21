package rc55.mc.rfapi.data.gen.internal;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import rc55.mc.rfapi.data.FluidIngredient;
import rc55.mc.rfapi.data.StateIngredient;
import rc55.mc.rfapi.data.gen.AbstractFluidReactionDataProvider;

/**
 * Generates vanilla fluid reaction datas
 */
public class DefaultedFluidReactionDataProvider extends AbstractFluidReactionDataProvider {
    public DefaultedFluidReactionDataProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate(ReactionBuilder builder) {
        // Vanilla cobblestone/obsidian creation
        builder.ofReaction(
                new Identifier("obsidian_and_cobblestone"),
                FluidIngredient.of(FluidTags.LAVA),
                StateIngredient.fromFluids(FluidIngredient.of(FluidTags.WATER)),
                null,
                1f,
                Blocks.OBSIDIAN.getDefaultState(),
                Blocks.COBBLESTONE.getDefaultState()
        );
        // Vanilla stone creation
        builder.ofFlowIn(
                FluidIngredient.of(FluidTags.LAVA),
                FluidIngredient.of(FluidTags.WATER),
                1f,
                Blocks.STONE.getDefaultState()
        );
        // Vanilla basalt creation
        builder.ofReaction(
                FluidIngredient.of(FluidTags.LAVA),
                StateIngredient.fromBlocks(Blocks.BLUE_ICE),
                StateIngredient.fromBlocks(Blocks.SOUL_SOIL),
                1f,
                Blocks.BASALT.getDefaultState()
        );
    }
}
