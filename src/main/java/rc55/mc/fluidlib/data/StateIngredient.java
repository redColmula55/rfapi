package rc55.mc.fluidlib.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class StateIngredient implements Predicate<State<?, ?>> {
    private final BlockIngredient validBlocks;
    private final FluidIngredient validFluids;

    protected StateIngredient(@NotNull BlockIngredient validBlocks, @NotNull FluidIngredient validFluids) {
        this.validBlocks = validBlocks;
        this.validFluids = validFluids;
    }

    public StateIngredient(Optional<@Nullable BlockIngredient> validBlocks, Optional<@Nullable FluidIngredient> validFluids) {
        this(validBlocks.orElse(BlockIngredient.EMPTY), validFluids.orElse(FluidIngredient.EMPTY));
    }

    public static StateIngredient fromBlocks(@Nullable BlockIngredient validBlocks) {
        return new StateIngredient(Optional.ofNullable(validBlocks), Optional.empty());
    }

    public static StateIngredient fromBlocks(@NotNull BlockIngredient.Builder builder) {
        return fromBlocks(builder.build());
    }

    public static StateIngredient fromBlocks(Block... blocks) {
        return fromBlocks(BlockIngredient.of(blocks));
    }

    public static StateIngredient fromFluids(@Nullable FluidIngredient validFluids) {
        return new StateIngredient(Optional.empty(), Optional.ofNullable(validFluids));
    }

    public static StateIngredient fromFluids(@NotNull FluidIngredient.Builder builder) {
        return fromFluids(builder.build());
    }

    public static StateIngredient fromFluids(Fluid... fluids) {
        return fromFluids(FluidIngredient.of(fluids));
    }

    public static final StateIngredient EMPTY = new StateIngredient(BlockIngredient.EMPTY, FluidIngredient.EMPTY);

    public static final Codec<StateIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockIngredient.CODEC.optionalFieldOf("block", BlockIngredient.EMPTY).forGetter(si -> si.validBlocks),
            FluidIngredient.CODEC.optionalFieldOf("fluid", FluidIngredient.EMPTY).forGetter(si -> si.validFluids)
    ).apply(i, StateIngredient::new));

    @Override
    public boolean test(State<?, ?> state) {
        if (state instanceof BlockState blockState) {
            return this.validBlocks.test(blockState) || this.validFluids.test(blockState.getFluidState());
        } else if (state instanceof FluidState fluidState) {
            return this.validFluids.test(fluidState);
        }
        return false;
    }

    public BlockIngredient getValidBlocks() {
        return validBlocks;
    }

    public FluidIngredient getValidFluids() {
        return validFluids;
    }
}
