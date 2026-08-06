package rc55.mc.fluidlib.fluid.reaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import rc55.mc.fluidlib.data.CodecHelper;
import rc55.mc.fluidlib.data.FluidIngredient;
import rc55.mc.fluidlib.fluid.FluidHelper;

public class InfectionReaction implements IFluidReaction<FluidReactionType<InfectionReaction>> {
    private final Fluid source;
    private final FluidIngredient sourceIngredient, target;
    private final float chance;

    public InfectionReaction(
            Fluid source,
            FluidIngredient target,
            float chance
    ) {
        this.source = FluidHelper.trim(source);
        this.target = target;
        this.chance = chance;

        if (source instanceof FlowableFluid) {
            this.sourceIngredient = FluidIngredient.of(this.source, ((FlowableFluid) source).getFlowing());
        } else {
            this.sourceIngredient = FluidIngredient.of(source);
        }
    }

    public static final Codec<InfectionReaction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Registries.FLUID.getCodec().fieldOf("source").forGetter(r -> r.source),
            FluidIngredient.CODEC.fieldOf("target").forGetter(r -> r.target),
            CodecHelper.CHANCE.optionalFieldOf("chance", 1f).forGetter(r -> r.chance)
    ).apply(i, InfectionReaction::new));

    @Override
    public FluidReactionType<InfectionReaction> getType() {
        return FluidReactionType.INFECTION;
    }

    @Override
    public FluidIngredient getSource() {
        return sourceIngredient;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public BlockState getResult() {
        return source.getDefaultState().getBlockState();
    }

    @Override
    public boolean checkReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        return this.checkRandom(world)
                && IFluidReaction.validateFluidBlock(world, targetPos, false)
                && this.getSource().test(world.getFluidState(sourcePos))
                && this.target.test(world.getFluidState(targetPos));
    }

    @Override
    public @Nullable BlockState performReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        final BlockState result = copyProperty(world.getBlockState(targetPos), this.getResult());
        return world.setBlockState(targetPos, result, Block.FORCE_STATE | Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD) ? result : null;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Comparable<T>> BlockState copyProperty(BlockState originalState, BlockState newState) {
        for (Property<?> value : originalState.getProperties()) {
            Property<T> property = (Property<T>) value;
            newState = newState.withIfExists(property, originalState.get(property));
        }
        return newState;
    }
}
