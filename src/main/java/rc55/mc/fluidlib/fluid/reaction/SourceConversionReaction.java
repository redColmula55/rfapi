package rc55.mc.fluidlib.fluid.reaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import rc55.mc.fluidlib.data.CodecHelper;
import rc55.mc.fluidlib.data.FluidIngredient;

public class SourceConversionReaction implements IFluidReaction<FluidReactionType<SourceConversionReaction>> {
    private final FluidIngredient source, ingredient;
    private final float chance;
    private final BlockState result;

    public SourceConversionReaction(
            FluidIngredient source,
            float chance,
            FluidIngredient ingredient,
            BlockState result
    ) {
        this.source = source;
        this.chance = chance;
        this.ingredient = ingredient;
        this.result = result;
    }

    public static final Codec<SourceConversionReaction> CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidIngredient.CODEC.fieldOf("source").forGetter(r -> r.source),
            CodecHelper.CHANCE.optionalFieldOf("chance", 1f).forGetter(r -> r.chance),
            FluidIngredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            CodecHelper.BLOCK_STATE.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, SourceConversionReaction::new));

    @Override
    public FluidReactionType<SourceConversionReaction> getType() {
        return FluidReactionType.SOURCE_CONVERSION;
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
        for (final Direction direction : Direction.Type.HORIZONTAL) {
            if (this.ingredient.test(world.getFluidState(targetPos.offset(direction)))) {
                return this.source.test(world.getFluidState(sourcePos));
            }
        }
        return this.checkRandom(world) && world.getBlockState(targetPos).isAir();
    }

    @Override
    public @Nullable BlockState performReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        return world.setBlockState(targetPos, this.getResult(), Block.NOTIFY_ALL) ? this.getResult() : null;
    }
}
