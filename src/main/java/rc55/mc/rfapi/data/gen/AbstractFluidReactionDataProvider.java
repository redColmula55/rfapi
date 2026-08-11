package rc55.mc.rfapi.data.gen;

import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.RFApiMain;
import rc55.mc.rfapi.data.FluidIngredient;
import rc55.mc.rfapi.data.StateIngredient;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.fluid.reaction.*;

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
            IFluidReaction.BASE_CODEC.encodeStart(JsonOps.INSTANCE, reaction).promotePartial(RFApiMain.LOGGER::error).result().ifPresentOrElse(json ->
                features.add(DataProvider.writeToPath(writer, json,
                        this.dataOutput.getResolver(DataOutput.OutputType.DATA_PACK, IFluidReaction.RELOAD_LISTENER.getPath()).resolve(id, "json")
                ))
            , () -> RFApiMain.LOGGER.error("Failed to generate data for provided reaction with id {}!", id))
        );

        return CompletableFuture.allOf(features.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return String.format("%s / %s", IFluidReaction.RELOAD_LISTENER.getFabricId(), this.dataOutput.getModId());
    }

    public abstract void generate(ReactionBuilder builder);

    @ApiStatus.NonExtendable
    @FunctionalInterface
    public interface ReactionBuilder {
        void append(Identifier id, IFluidReaction<?> reaction);

        /**
         * Generate a fluid reaction data with defaulted name
         * @param reaction The reaction instance
         * @see #createDefaultId(IFluidReaction) Default id format
         */
        default void append(IFluidReaction<?> reaction) {
            this.append(this.createDefaultId(reaction), reaction);
        }

        default void ofReaction(
                Identifier id,
                FluidIngredient source,
                @Nullable StateIngredient surroundingIngredient,
                @Nullable StateIngredient verticalIngredient,
                float chance,
                BlockState result
        ) {
            this.append(id, new SimpleFluidReaction(source, chance, Optional.ofNullable(surroundingIngredient), Optional.ofNullable(verticalIngredient), result));
        }

        default void ofReaction(
                FluidIngredient source,
                @Nullable StateIngredient surroundingIngredient,
                @Nullable StateIngredient verticalIngredient,
                float chance,
                BlockState result
        ) {
            this.ofReaction(this.createDefaultId(result), source, surroundingIngredient, verticalIngredient, chance, result);
        }

        default void ofReaction(
                Identifier id,
                FluidIngredient source,
                @Nullable StateIngredient surroundingIngredient,
                @Nullable StateIngredient verticalIngredient,
                float chance,
                BlockState stillResult,
                BlockState flowingResult
        ) {
            this.append(id, new SimpleFlowableFluidReaction(
                    source, chance, Optional.ofNullable(surroundingIngredient), Optional.ofNullable(verticalIngredient), stillResult, flowingResult
            ));
        }

        default void ofFlowIn(
                Identifier id, FluidIngredient source, FluidIngredient ingredient, float chance, BlockState result
        ) {
            this.append(id, new FlowIntoReaction(source, chance, ingredient, result));
        }

        default void ofFlowIn(
                FluidIngredient source, FluidIngredient ingredient, float chance, BlockState result
        ) {
            this.ofFlowIn(this.createDefaultId(result), source, ingredient, chance, result);
        }

        default void ofSourceConversion(
                Identifier id,
                FluidIngredient source,
                FluidIngredient ingredient,
                float chance,
                BlockState result
        ) {
            this.append(id, new SourceConversionReaction(source, chance, ingredient, result));
        }

        default void ofSourceConversion(
                FluidIngredient source,
                FluidIngredient ingredient,
                float chance,
                BlockState result
        ) {
            this.ofSourceConversion(this.createDefaultId(result), source, ingredient, chance, result);
        }

        default void ofInfection(
                Identifier id, FluidReference<?> source, FluidIngredient target, float chance
        ) {
            this.append(id, new InfectionReaction(source.getStill(), target, chance));
        }

        default void ofInfection(
                FluidReference<?> source, FluidIngredient target, float chance
        ) {
            this.ofInfection(source.getBlockId(), source, target, chance);
        }

        default Identifier createDefaultId(IFluidReaction<?> reaction) {
            return this.createDefaultId(reaction.getResult());
        }

        default Identifier createDefaultId(BlockState result) {
            return Registries.BLOCK.getId(result.getBlock());
        }
    }
}
