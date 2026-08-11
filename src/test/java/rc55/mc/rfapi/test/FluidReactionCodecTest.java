package rc55.mc.rfapi.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rc55.mc.rfapi.data.BlockIngredient;
import rc55.mc.rfapi.data.FluidIngredient;
import rc55.mc.rfapi.data.StateIngredient;
import rc55.mc.rfapi.fluid.reaction.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.Optional;

public class FluidReactionCodecTest {
    @BeforeAll
    static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
        FluidReactionType.init();
    }

    @Test
    void simpleDecodeTest() {
        IFluidReaction<?> result = loadReaction("/simple.json");
        Assertions.assertInstanceOf(SimpleFluidReaction.class, result);

        final SimpleFluidReaction expected = new SimpleFluidReaction(
                FluidIngredient.builder().add(Fluids.WATER).add(FluidTags.LAVA).build(),
                1f,
                Optional.of(StateIngredient.fromBlocks(BlockIngredient.builder().add(BlockTags.BAMBOO_BLOCKS))),
                Optional.empty(),
                Blocks.REDSTONE_LAMP.getDefaultState().with(RedstoneLampBlock.LIT, true)
        );
        assertReactionSame(expected, result);
    }

    @Test
    void flowableDecodeTest() {
        IFluidReaction<?> result = loadReaction("/flowable.json");
        Assertions.assertInstanceOf(SimpleFlowableFluidReaction.class, result);

        final SimpleFlowableFluidReaction expected = new SimpleFlowableFluidReaction(
                FluidIngredient.builder().add(Fluids.WATER).add(FluidTags.LAVA).build(),
                0.87f,
                Optional.of(StateIngredient.fromBlocks(BlockIngredient.builder().add(BlockTags.BAMBOO_BLOCKS))),
                Optional.of(new StateIngredient(Optional.of(BlockIngredient.of(Blocks.STONE, Blocks.BEDROCK)), Optional.of(FluidIngredient.of(Fluids.EMPTY)))),
                Blocks.AIR.getDefaultState(),
                Blocks.REDSTONE_LAMP.getDefaultState().with(RedstoneLampBlock.LIT, true)
        );
        assertReactionSame(expected, result);
    }

    @Test
    void flowIntoDecodeTest() {
        IFluidReaction<?> result = loadReaction("/flow_into.json");
        Assertions.assertInstanceOf(FlowIntoReaction.class, result);

        final FlowIntoReaction expected = new FlowIntoReaction(
                FluidIngredient.of(FluidTags.LAVA),
                1f,
                FluidIngredient.of(FluidTags.WATER),
                Blocks.STONE.getDefaultState()
        );
        assertReactionSame(expected, result);
    }

    @Test
    void sourceConversionDecodeTest() {
        IFluidReaction<?> result = loadReaction("/source_conversion.json");
        Assertions.assertInstanceOf(SourceConversionReaction.class, result);

        final SourceConversionReaction expected = new SourceConversionReaction(
                FluidIngredient.of(FluidTags.LAVA),
                1f,
                FluidIngredient.of(FluidTags.WATER),
                Blocks.GRASS_BLOCK.getDefaultState()
        );
        assertReactionSame(expected, result);
    }

    @Test
    void infectionDecodeTest() {
        IFluidReaction<?> result = loadReaction("/infection.json");
        Assertions.assertInstanceOf(InfectionReaction.class, result);

        final InfectionReaction expected = new InfectionReaction(
                Fluids.WATER,
                FluidIngredient.of(),
                0.5f
        );
        assertReactionSame(expected, result);
    }

    static JsonElement getJson(String path) {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(FluidReactionCodecTest.class.getResourceAsStream(path), "No such file!"))) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static IFluidReaction<?> loadReaction(String path) {
        return IFluidReaction.BASE_CODEC.parse(JsonOps.INSTANCE, getJson(path)).result().orElseThrow();
    }

    static void assertReactionSame(IFluidReaction<?> expected, IFluidReaction<?> actual) {
        Assertions.assertEquals(expected.getType(), actual.getType());
        Assertions.assertEquals(expected.getChance(), actual.getChance());
        Assertions.assertEquals(expected.getSource(), actual.getSource());
        Assertions.assertEquals(expected.getResult(), actual.getResult());
    }
}
