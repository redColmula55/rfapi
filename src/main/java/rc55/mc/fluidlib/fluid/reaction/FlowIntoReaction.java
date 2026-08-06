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

public class FlowIntoReaction implements IFluidReaction<FluidReactionType<FlowIntoReaction>> {
    protected final FluidIngredient source, ingredient;
    protected final BlockState result;
    protected final float chance;

    public FlowIntoReaction(
            FluidIngredient source,
            float chance,
            FluidIngredient ingredient,
            BlockState result
    ) {
        this.ingredient = ingredient;
        this.chance = chance;
        this.source = source;
        this.result = result;
    }

    public static final Codec<FlowIntoReaction> CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidIngredient.CODEC.fieldOf("source").forGetter(r -> r.source),
            CodecHelper.CHANCE.optionalFieldOf("chance", 1f).forGetter(r -> r.chance),
            FluidIngredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            CodecHelper.BLOCK_STATE.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, FlowIntoReaction::new));

    @Override
    public FluidReactionType<FlowIntoReaction> getType() {
        return FluidReactionType.FLOW_INTO;
    }

    @Override
    public FluidIngredient getSource() {
        return source;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public BlockState getResult() {
        return result;
    }

    @Override
    public boolean checkReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        return this.checkRandom(world)
                //&& IFluidReaction.validateFluidBlock(world, targetPos, true)
                && ingredient.test(world.getFluidState(targetPos))
                && source.test(world.getFluidState(sourcePos));
    }

    @Override
    public @Nullable BlockState performReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        return world.setBlockState(targetPos, result, Block.NOTIFY_ALL) ? result : null;
    }
}
