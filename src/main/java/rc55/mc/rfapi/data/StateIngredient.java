package rc55.mc.rfapi.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
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

    /**
     * @deprecated Use {@link #of(Optional, Optional)} instead. Preserved for compatibility only.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Deprecated
    public StateIngredient(Optional<@Nullable BlockIngredient> validBlocks, Optional<@Nullable FluidIngredient> validFluids) {
        this(validBlocks.orElse(BlockIngredient.EMPTY), validFluids.orElse(FluidIngredient.EMPTY));
    }

    public static StateIngredient of(@Nullable BlockIngredient validBlocks, @Nullable FluidIngredient validFluids) {
        return of(Optional.ofNullable(validBlocks), Optional.ofNullable(validFluids));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static StateIngredient of(Optional<@Nullable BlockIngredient> validBlocks, Optional<@Nullable FluidIngredient> validFluids) {
        if (validBlocks.isEmpty() && validFluids.isEmpty()) {
            return EMPTY;
        }
        return new StateIngredient(validBlocks, validFluids);
    }

    public static StateIngredient fromBlocks(@Nullable BlockIngredient validBlocks) {
        return of(validBlocks, null);
    }

    public static StateIngredient fromBlocks(@NotNull BlockIngredient.Builder builder) {
        return fromBlocks(builder.build());
    }

    public static StateIngredient fromBlocks(@NotNull Block... blocks) {
        return fromBlocks(BlockIngredient.of(blocks));
    }

    public static StateIngredient fromFluids(@Nullable FluidIngredient validFluids) {
        return of(null, validFluids);
    }

    public static StateIngredient fromFluids(@NotNull FluidIngredient.Builder builder) {
        return fromFluids(builder.build());
    }

    public static StateIngredient fromFluids(@NotNull Fluid... fluids) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof StateIngredient) {
            return this.getValidBlocks().equals(((StateIngredient) obj).getValidBlocks())
                    && this.getValidFluids().equals(((StateIngredient) obj).getValidFluids());
        } else return false;
    }

    @Override
    public int hashCode() {
        return this.getValidBlocks().hashCode() ^ this.getValidFluids().hashCode();
    }

    public BlockIngredient getValidBlocks() {
        return validBlocks;
    }

    public FluidIngredient getValidFluids() {
        return validFluids;
    }

    public static final class Synchronizer implements ByteBufSerializer<StateIngredient> {
        public static final Synchronizer INSTANCE = new Synchronizer();
        public static final ByteBufSerializer<Optional<StateIngredient>> OPTIONAL = ByteBufSerializer.optional(INSTANCE, EMPTY);

        private Synchronizer(){}

        @Override
        public PacketByteBuf toByteBuf(StateIngredient obj) {
            if (obj == EMPTY) {
                return PacketByteBufs.empty();
            }

            PacketByteBuf buf = PacketByteBufs.create();
            ByteBufSerializer.write(obj.validBlocks, BlockIngredient.Synchronizer.INSTANCE, buf);
            ByteBufSerializer.write(obj.validFluids, FluidIngredient.Synchronizer.INSTANCE, buf);
            return buf;
        }

        @Override
        public StateIngredient fromByteBuf(PacketByteBuf buf) {
            if (buf.readableBytes() == 0) {
                return EMPTY;
            }

            BlockIngredient block = ByteBufSerializer.read(BlockIngredient.Synchronizer.INSTANCE, buf);
            FluidIngredient fluid = ByteBufSerializer.read(FluidIngredient.Synchronizer.INSTANCE, buf);
            return new StateIngredient(block, fluid);
        }
    }
}
