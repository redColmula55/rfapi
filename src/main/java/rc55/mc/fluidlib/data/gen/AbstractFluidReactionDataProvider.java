package rc55.mc.fluidlib.data.gen;

import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import rc55.mc.fluidlib.FluidLib;
import rc55.mc.fluidlib.data.FluidIngredient;
import rc55.mc.fluidlib.data.FluidReaction;
import rc55.mc.fluidlib.data.StateIngredient;
import rc55.mc.fluidlib.fluid.FluidReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractFluidReactionDataProvider implements DataProvider {
    protected final FabricDataOutput dataOutput;

    public AbstractFluidReactionDataProvider(FabricDataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    @Override
    public final CompletableFuture<?> run(DataWriter writer) {
        final List<CompletableFuture<?>> features = new ArrayList<>();

        this.generate((id, reaction) ->
            FluidReaction.CODEC.encodeStart(JsonOps.INSTANCE, reaction).promotePartial(FluidLib.LOGGER::error).result().ifPresentOrElse(json ->
                features.add(DataProvider.writeToPath(writer, json,
                        this.dataOutput.getResolver(DataOutput.OutputType.DATA_PACK, "fluid_reaction").resolve(id, "json")
                ))
            , () -> FluidLib.LOGGER.error("Failed to generate data for provided reaction with id {}!", id))
        );

        return CompletableFuture.allOf(features.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return String.format("fluidlib:fluid_reaction[%s]", this.dataOutput.getModId());
    }

    public abstract void generate(ReactionBuilder builder);

    @ApiStatus.NonExtendable
    @FunctionalInterface
    public interface ReactionBuilder {
        void append(Identifier id, FluidReaction reaction);

        /**
         * Generate a fluid reaction data with defaulted name
         * @param reaction The reaction instance
         * @see #createDefaultId(FluidReaction) Default id format
         */
        default void append(FluidReaction reaction) {
            this.append(this.createDefaultId(reaction), reaction);
        }

        default void ofReaction(
                Identifier id,
                FluidIngredient ingredient,
                Optional<StateIngredient> horizontalMaterial,
                Optional<StateIngredient> verticalMaterial,
                float chance,
                BlockState result
        ) {
            this.append(id, new FluidReaction(FluidReaction.Type.REACTION, ingredient, chance, horizontalMaterial, verticalMaterial, result));
        }

        default void ofReaction(
                FluidIngredient ingredient,
                Optional<StateIngredient> horizontalMaterial,
                Optional<StateIngredient> verticalMaterial,
                float chance,
                BlockState result
        ) {
            this.ofReaction(this.createDefaultId(result), ingredient, horizontalMaterial, verticalMaterial, chance, result);
        }

        default void ofFlowIn(
                Identifier id, FluidIngredient ingredient, StateIngredient material, float chance, BlockState result
        ) {
            this.append(id, new FluidReaction(FluidReaction.Type.FLOWS_INTO, ingredient, chance, Optional.empty(), Optional.of(material), result));
        }

        default void ofFlowIn(
                FluidIngredient ingredient, StateIngredient material, float chance, BlockState result
        ) {
            this.ofFlowIn(this.createDefaultId(result), ingredient, material, chance, result);
        }

        default void ofSourceConversion(
                Identifier id,
                FluidIngredient ingredient,
                FluidIngredient material,
                float chance,
                BlockState result
        ) {
            this.append(id, new FluidReaction(
                    FluidReaction.Type.SOURCE_CONVERSION,
                    ingredient,
                    chance,
                    Optional.of(StateIngredient.fromFluids(material)),
                    Optional.empty(),
                    result
            ));
        }

        default void ofSourceConversion(
                FluidIngredient ingredient,
                FluidIngredient material,
                float chance,
                BlockState result
        ) {
            this.ofSourceConversion(this.createDefaultId(result), ingredient, material, chance, result);
        }

        default void ofInfection(
                Identifier id, FluidReference<?> fluid, FluidIngredient material, float chance
        ) {
            this.append(id, new FluidReaction(
                    FluidReaction.Type.INFECTION,
                    FluidIngredient.of(fluid),
                    chance,
                    Optional.of(StateIngredient.fromFluids(material)),
                    Optional.empty(),
                    fluid.getBlock().getDefaultState()
            ));
        }

        default void ofInfection(
                FluidReference<?> fluid, FluidIngredient material, float chance
        ) {
            this.ofInfection(fluid.getBlockId(), fluid, material, chance);
        }

        default Identifier createDefaultId(FluidReaction reaction) {
            return Registries.BLOCK.getId(reaction.getResult().getBlock());
        }

        default Identifier createDefaultId(BlockState result) {
            return Registries.BLOCK.getId(result.getBlock());
        }
    }
}
