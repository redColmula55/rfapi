package rc55.mc.fluidlib.fluid.reaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import rc55.mc.fluidlib.data.CodecHelper;
import rc55.mc.fluidlib.data.FluidIngredient;
import rc55.mc.fluidlib.data.StateIngredient;

import java.util.Optional;

public class SimpleFlowableFluidReaction extends SimpleFluidReaction {
    protected final BlockState flowingResult;

    public SimpleFlowableFluidReaction(
            FluidIngredient source, float chance, Optional<@Nullable StateIngredient> surroundingIngredient,
            Optional<@Nullable StateIngredient> verticalIngredient, BlockState stillResult, BlockState flowingResult
    ) {
        super(source, chance, surroundingIngredient, verticalIngredient, stillResult);
        this.flowingResult = flowingResult;
    }

    public static final Codec<SimpleFlowableFluidReaction> CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidIngredient.CODEC.fieldOf("source").forGetter(r -> r.source),
            CodecHelper.CHANCE.optionalFieldOf("chance", 1f).forGetter(r -> r.chance),
            StateIngredient.CODEC.optionalFieldOf("surrounding_ingredient").forGetter(r -> r.surroundingIngredient),
            StateIngredient.CODEC.optionalFieldOf("vertical_ingredient").forGetter(r -> r.verticalIngredient),
            CodecHelper.BLOCK_STATE.fieldOf("still_result").forGetter(r -> r.result),
            CodecHelper.BLOCK_STATE.fieldOf("flowing_result").forGetter(r -> r.flowingResult)
    ).apply(i, SimpleFlowableFluidReaction::new));

    @Override
    public FluidReactionType<? extends SimpleFlowableFluidReaction> getType() {
        return FluidReactionType.FLOWABLE;
    }

    @Override
    public @Nullable BlockState performReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        BlockState result = world.getFluidState(sourcePos).isStill() ? this.result : this.flowingResult;
        return world.setBlockState(targetPos, result, Block.NOTIFY_ALL) ? result : null;
    }
}
