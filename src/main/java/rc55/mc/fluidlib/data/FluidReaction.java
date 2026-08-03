package rc55.mc.fluidlib.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import rc55.mc.fluidlib.fluid.FluidHelper;
import rc55.mc.fluidlib.fluid.FluidSettings;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FluidReaction {
    protected final Type type;
    protected final FluidIngredient ingredient;
    protected final Optional<@Nullable StateIngredient> horizontalMaterial, verticalMaterial;
    protected final BlockState result;
    protected final float chance;

    public FluidReaction(
            Type type,
            FluidIngredient ingredient,
            float chance,
            Optional<@Nullable StateIngredient> horizontalMaterial,
            Optional<@Nullable StateIngredient> verticalMaterial,
            BlockState result
    ) {
        this.type = type;
        this.ingredient = ingredient;
        this.chance = chance;
        this.horizontalMaterial = horizontalMaterial;
        this.verticalMaterial = verticalMaterial;
        this.result = result;
        if (verticalMaterial.isEmpty() && horizontalMaterial.isEmpty()) {
            throw new IllegalArgumentException("Must exist at least one type of material!");
        }
    }

    public static final Codec<FluidReaction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Type.CODEC.optionalFieldOf("type", Type.REACTION).forGetter(r -> r.type),
            FluidIngredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            CodecHelper.CHANCE.optionalFieldOf("chance", 1f).forGetter(r -> r.chance),
            StateIngredient.CODEC.optionalFieldOf("horizontal_material").forGetter(r -> r.horizontalMaterial),
            StateIngredient.CODEC.optionalFieldOf("vertical_material").forGetter(r -> r.verticalMaterial),
            CodecHelper.BLOCK_STATE.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, FluidReaction::new));

    public static final ResourceMap<Fluid, FluidReaction> CACHE = new ResourceMap<>(true, r -> r.ingredient.stream()
            .flatMap(FluidIngredient.Entry::stream).collect(Collectors.toList())
    );

    /**
     * Controls how much fluid can be infected per tick
     * Since fluid infection operation is performed instantly, it is
     * necessary to throttle count that can be infested per tick,
     * otherwise it may cause a {@link StackOverflowError}
     */
    public static final AtomicInteger infectionDepth = new AtomicInteger(0);

    public static boolean reaction(Type type, WorldAccess world, BlockPos pos, BlockState state) {
        final Fluid fluid = state.getFluidState().getFluid();
        for (final FluidReaction reaction : CACHE.get(fluid)) {
            if (reaction.checkReaction(type, world, pos, state)) {
                return reaction.performReaction(world, pos, state);
            }
        }
        return false;
    }

    public Type getType() {
        return type;
    }

    public float getChance() {
        return chance;
    }

    public BlockState getResult() {
        return result;
    }

    public boolean checkReaction(Type type, WorldAccess world, BlockPos pos, BlockState state) {
        return type == this.type && world.getRandom().nextFloat() < this.chance && switch (this.type) {
            case REACTION, INFECTION -> this.checkHorizontal(world, pos, state) && this.checkVertical(world, pos, state);
            case FLOWS_INTO -> this.verticalMaterial.filter(ingredient -> ingredient.test(state)).isPresent();
            case SOURCE_CONVERSION -> this.horizontalMaterial.filter(ingredient -> ingredient.test(state)).isPresent();
        };
    }

    protected boolean checkHorizontal(BlockView world, BlockPos pos, BlockState state) {
        final FluidSettings settings = FluidSettings.get(state.getFluidState());
        if (this.horizontalMaterial.isPresent()) {
            for (final Direction direction : settings.flowsUp() ? FluidSettings.FLOW_DIRECTIONS : FluidSettings.AIR_FLOW_DIRECTIONS) {
                final BlockPos sidePos = pos.offset(direction);
                if (this.horizontalMaterial.get().test(world.getBlockState(sidePos))) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    protected boolean checkVertical(BlockView world, BlockPos pos, BlockState state) {
        final FluidSettings settings = FluidSettings.get(state.getFluidState());
        if (this.verticalMaterial.isPresent()) {
            final BlockPos verticalPos = pos.offset(settings.flowsUp() ? Direction.UP : Direction.DOWN);
            return this.verticalMaterial.get().test(world.getBlockState(verticalPos));
        } else {
            return true;
        }
    }

    public boolean performReaction(WorldAccess world, BlockPos pos, BlockState state) {
        final Fluid fluid = state.getFluidState().getFluid();
        final FluidSettings settings = fluid.getSettings();

        boolean flag = false;
        switch (this.type) {
            case REACTION, SOURCE_CONVERSION -> flag = world.setBlockState(pos, this.result, Block.NOTIFY_ALL);
            case FLOWS_INTO -> {
                if (state.getBlock() instanceof FluidBlock) {
                    flag = world.setBlockState(pos, this.result, Block.NOTIFY_ALL);
                }
            }
            case INFECTION -> {
                for (final Direction direction : Direction.shuffle(world.getRandom())) {
                    // Prevent StackOverflowError
                    // TODO: config
                    if (infectionDepth.addAndGet(1) > 256) {
                        return false;
                    }
                    final BlockPos newPos = pos.offset(direction);
                    // Preserve waterlogged block
                    if (world.getBlockState(newPos).getBlock() instanceof FluidBlock) {
                        BlockState newState = this.result;
                        if (!world.getFluidState(newPos).isStill()) {
                            newState = copyProperty(state, newState);
                        }
                        flag |= world.setBlockState(newPos, newState, Block.FORCE_STATE | Block.REDRAW_ON_MAIN_THREAD | Block.NOTIFY_LISTENERS, 1);
                    }
                }
            }
        }
        if (flag && settings.canSetFire()) {
            FluidHelper.playExtinguishEvent(world, pos);
        }
        return flag;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Comparable<T>> BlockState copyProperty(BlockState originalState, BlockState newState) {
        for (Property<?> value : originalState.getProperties()) {
            Property<T> property = (Property<T>) value;
            newState = newState.withIfExists(property, originalState.get(property));
        }
        return newState;
    }

    /**
     * Declaims type of this recation
     */
    public enum Type implements StringIdentifiable {
        /**
         * Normal fluid reactions(e.g. cobblestone generator)
         */
        REACTION,
        /**
         * When a fluid flows into another fluid(e.g. stone generator)
         * NOTE: Due to vanilla limits(and I didn't find a better way to implement it), this requires mods to support it!
         */
        FLOWS_INTO,
        /**
         * When a fluid touches another fluid and turns that fluid into another type of fluid
         */
        INFECTION,
        /**
         * When 2 fluid source block tries to create new source block
         */
        SOURCE_CONVERSION,
        ;

        public static final com.mojang.serialization.Codec<Type> CODEC = StringIdentifiable.createCodec(Type::values);

        @Override
        public String asString() {
            return this.name().toLowerCase();
        }
    }
}
