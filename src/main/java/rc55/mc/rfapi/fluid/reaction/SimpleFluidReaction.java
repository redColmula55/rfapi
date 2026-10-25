package rc55.mc.rfapi.fluid.reaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.data.ByteBufSerializer;
import rc55.mc.rfapi.data.CodecHelper;
import rc55.mc.rfapi.data.FluidIngredient;
import rc55.mc.rfapi.data.StateIngredient;
import rc55.mc.rfapi.fluid.FluidSettings;

import java.util.Optional;

public class SimpleFluidReaction implements IFluidReaction<FluidReactionType<? extends SimpleFluidReaction>> {
    protected final FluidIngredient source;
    protected final Optional<@Nullable StateIngredient> surroundingIngredient, verticalIngredient;
    protected final BlockState result;
    protected final float chance;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public SimpleFluidReaction(
            FluidIngredient source,
            float chance,
            Optional<@Nullable StateIngredient> surroundingIngredient,
            Optional<@Nullable StateIngredient> verticalIngredient,
            BlockState result
    ) {
        this.source = source;
        this.chance = chance;
        this.surroundingIngredient = surroundingIngredient;
        this.verticalIngredient = verticalIngredient;
        this.result = result;
        if (verticalIngredient.isEmpty() && surroundingIngredient.isEmpty()) {
            throw new IllegalArgumentException("Must exist at least one type of material!");
        }
    }

    public static final Codec<SimpleFluidReaction> CODEC = RecordCodecBuilder.create(i -> i.group(
            FluidIngredient.CODEC.fieldOf("source").forGetter(r -> r.source),
            CodecHelper.CHANCE.optionalFieldOf("chance", 1f).forGetter(r -> r.chance),
            StateIngredient.CODEC.optionalFieldOf("surrounding_ingredient").forGetter(r -> r.surroundingIngredient),
            StateIngredient.CODEC.optionalFieldOf("vertical_ingredient").forGetter(r -> r.verticalIngredient),
            CodecHelper.BLOCK_STATE.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, SimpleFluidReaction::new));

    public static final ByteBufSerializer<SimpleFluidReaction> BUF_CODEC = ByteBufSerializer.create(
            SimpleFluidReaction::new,
            r -> r.source, FluidIngredient.Synchronizer.INSTANCE,
            r -> r.chance, ByteBufSerializer.FLOAT,
            r -> r.surroundingIngredient, StateIngredient.Synchronizer.OPTIONAL,
            r -> r.verticalIngredient, StateIngredient.Synchronizer.OPTIONAL,
            r -> r.result, ByteBufSerializer.BLOCK_STATE
    );

    @Override
    public FluidReactionType<? extends SimpleFluidReaction> getType() {
        return FluidReactionType.SIMPLE;
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
        return this.checkRandom(world) && this.checkSurrounding(world, sourcePos, targetPos) && this.checkVertical(world, sourcePos, targetPos);
    }

    @Override
    public @Nullable BlockState performReaction(WorldAccess world, BlockPos sourcePos, BlockPos targetPos) {
        return world.setBlockState(targetPos, this.result, Block.NOTIFY_ALL) ? this.getResult() : null;
    }


    public StateIngredient getSurroundingIngredient() {
        return surroundingIngredient.orElse(StateIngredient.EMPTY);
    }

    public StateIngredient getVerticalIngredient() {
        return verticalIngredient.orElse(StateIngredient.EMPTY);
    }

    protected boolean checkSurrounding(BlockView world, BlockPos sourcePos, BlockPos pos) {
        final FluidSettings settings = world.getFluidState(sourcePos).getFluid().getSettings();
        if (this.surroundingIngredient.isPresent()) {
            for (final Direction direction : settings.flowsUp() ? FluidSettings.FLOW_DIRECTIONS : FluidSettings.AIR_FLOW_DIRECTIONS) {
                final BlockPos sidePos = pos.offset(direction);
                if (this.surroundingIngredient.get().test(world.getBlockState(sidePos))) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    protected boolean checkVertical(BlockView world, BlockPos sourcePos, BlockPos pos) {
        final FluidSettings settings = world.getFluidState(sourcePos).getFluid().getSettings();
        if (this.verticalIngredient.isPresent()) {
            final BlockPos verticalPos = pos.offset(settings.flowsUp() ? Direction.UP : Direction.DOWN);
            return this.verticalIngredient.get().test(world.getBlockState(verticalPos));
        } else {
            return true;
        }
    }
}
