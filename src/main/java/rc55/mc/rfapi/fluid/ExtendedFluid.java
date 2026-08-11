package rc55.mc.rfapi.fluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.*;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.mixin.FlowableFluidAccessor;

import java.util.Map;

/**
 * ReservoirAPI version of {@link FlowableFluid}
 * Supports fluids that flows upward
 */
public class ExtendedFluid extends FlowableFluid implements IFluidWithSettings {

    private final FluidSettings settings;
    private final FluidReference<? extends ExtendedFluid> reference;
    private final boolean still;
    private final Direction verticalFlowDirection;
    private final Map<FluidState, VoxelShape> shapeCache;

    /**
     * Creates an instance for ExtendedFluid
     * @param settings Properties for this fluid, Should be the same for still/flow factor
     * @param reference Reference for this fluid
     * @param still Whether this represents a source block
     */
    public ExtendedFluid(FluidSettings settings, FluidReference<? extends ExtendedFluid> reference, boolean still) {
        this.settings = settings;
        this.reference = reference;
        this.still = still;
        this.verticalFlowDirection = this.getSettings().flowsUp() ? Direction.UP : Direction.DOWN;
        this.shapeCache = Maps.newIdentityHashMap();
    }

    public static ExtendedFluid ofStill(FluidSettings settings, FluidReference<ExtendedFluid> reference) {
        return new ExtendedFluid(settings, reference, true);
    }

    public static ExtendedFluid ofFlowing(FluidSettings settings, FluidReference<ExtendedFluid> reference) {
        return new ExtendedFluid(settings, reference, false);
    }

    public boolean flowsUp() {
        return this.verticalFlowDirection == Direction.UP;
    }

    public Direction getVerticalFlowDirection() {
        return verticalFlowDirection;
    }

    @Override
    public FluidSettings getSettings() {
        return this.settings;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
        super.appendProperties(builder);
        if (!this.still) {
            builder.add(LEVEL);
        }
    }

    @Override
    public int getLevel(FluidState state) {
        return this.isStill(state) ? 8 : state.get(LEVEL);
    }

    @Override
    public boolean isStill(FluidState state) {
        return this.still;
    }

    @Override
    public Fluid getFlowing() {
        return this.reference.getFlowing();
    }

    @Override
    public Fluid getStill() {
        return this.reference.getStill();
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == this.getStill() || fluid == this.getFlowing();
    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        return this.settings.getFlowSpeed(world);
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return this.settings.getLevelDecreasePerBlock(world);
    }

    @Override
    public Item getBucketItem() {
        return this.settings.getBucketItem();
    }

    @Override
    public int getTickRate(WorldView world) {
        return this.settings.getTickRate(world);
    }

    @Override
    protected float getBlastResistance() {
        return 100f;
    }

    @Override
    protected boolean isInfinite(World world) {
        return this.settings.isInfinite(world);
    }

    @Override
    protected boolean hasRandomTicks() {
        return this.settings.hasRandomTick();
    }

    @Override
    protected @Nullable ParticleEffect getParticle() {
        return this.settings.getDrippingParticle();
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        return state.getLevel() == 9 && state.getFluid().matchesType(world.getFluidState(this.flowsUp() ? pos.down() : pos.up()).getFluid())
                ? VoxelShapes.fullCube()
                : this.shapeCache.computeIfAbsent(state, state2 -> this.calculateShape(state2, world, pos));
    }

    private VoxelShape calculateShape(FluidState state, BlockView world, BlockPos pos) {
        return this.flowsUp()
                ? VoxelShapes.cuboid(0.0, 1.0 - state.getHeight(world, pos), 0.0, 1.0, 1.0, 1.0)
                : VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, state.getHeight(world, pos), 1.0);
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        return state.getFluid().matchesType(world.getFluidState(this.flowsUp() ? pos.down() : pos.up()).getFluid()) ? 1.0f : state.getHeight();
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
        if (this.settings.canSetFire()) {
            this.playExtinguishEvent(world, pos);
        }
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return FluidHelper.canBeReplacedWith(this, state, world, pos, fluid, direction);
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return this.reference.getBlock().getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    @Override
    protected FluidState getUpdatedState(World world, BlockPos pos, BlockState state) {
        return FluidHelper.getUpdatedState(this, world, pos, state, this.getLevelDecreasePerBlock(world));
    }

    @Override
    protected void tryFlow(World world, BlockPos pos, FluidState state) {
        if (!state.isEmpty()) {
            final BlockState blockState = world.getBlockState(pos);
            final BlockPos posDown = this.flowsUp() ? pos.up() : pos.down();
            final BlockState stateDown = world.getBlockState(posDown);

            // 向下流动
            final FluidState downNewFluidState = this.getUpdatedState(world, posDown, stateDown);
            // 检查是否能够向下流动
            if (this.canFlow(world, pos, blockState, this.getVerticalFlowDirection(), posDown, stateDown, world.getFluidState(posDown), downNewFluidState.getFluid())) {
                // 向下流动
                this.flow(world, posDown, stateDown, this.getVerticalFlowDirection(), downNewFluidState);

                // 四周有超过3个水源方块时向四周流动（？
                if (this.countNeighboringSources(world, pos) >= 3) {
                    this.flowToSides(world, pos, state, blockState);
                }
            } else if (state.isStill() || !this.canFlowVertical(world, downNewFluidState.getFluid(), pos, blockState, posDown, stateDown)) {
                // 无法向下流动时向四周流动
                // 水源在向下流动的1tick后向四周扩散
                // 水流则无法在向下流动的同时向四周扩散
                this.flowToSides(world, pos, state, blockState);
            }
        }
    }

    @Override
    protected int getFlowSpeedBetween(
            WorldView world,
            BlockPos pos,
            int i,
            Direction direction,
            BlockState state,
            BlockPos fromPos,
            Short2ObjectMap<Pair<BlockState, FluidState>> stateCache,
            Short2BooleanMap flowDownCache
    ) {
        int j = 1000;

        for (Direction direction2 : Direction.Type.HORIZONTAL) {
            if (direction2 != direction) {
                BlockPos blockPos = pos.offset(direction2);
                short s = packXZOffset(fromPos, blockPos);
                Pair<BlockState, FluidState> pair = stateCache.computeIfAbsent(s, (Short2ObjectFunction<? extends Pair<BlockState, FluidState>>)(sx -> {
                    BlockState blockStatex = world.getBlockState(blockPos);
                    return Pair.of(blockStatex, blockStatex.getFluidState());
                }));
                BlockState blockState = pair.getFirst();
                FluidState fluidState = pair.getSecond();
                if (this.canFlowThrough(world, this.getFlowing(), pos, state, direction2, blockPos, blockState, fluidState)) {
                    boolean bl = flowDownCache.computeIfAbsent(s, sx -> {
                        BlockPos blockPos2 = this.flowsUp() ? blockPos.up() : blockPos.down();
                        BlockState blockState2 = world.getBlockState(blockPos2);
                        return this.canFlowVertical(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
                    });
                    if (bl) {
                        return i;
                    }

                    if (i < this.getFlowSpeed(world)) {
                        int k = this.getFlowSpeedBetween(world, blockPos, i + 1, direction2.getOpposite(), blockState, fromPos, stateCache, flowDownCache);
                        if (k < j) {
                            j = k;
                        }
                    }
                }
            }
        }

        return j;
    }

    @Override
    protected Map<Direction, FluidState> getSpread(World world, BlockPos pos, BlockState state) {
        int i = 1000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2BooleanMap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            short s = packXZOffset(pos, blockPos);
            Pair<BlockState, FluidState> pair = short2ObjectMap.computeIfAbsent(s, (Short2ObjectFunction<? extends Pair<BlockState, FluidState>>)(sx -> {
                BlockState blockStatex = world.getBlockState(blockPos);
                return Pair.of(blockStatex, blockStatex.getFluidState());
            }));
            BlockState blockState = pair.getFirst();
            FluidState fluidState = pair.getSecond();
            FluidState fluidState2 = this.getUpdatedState(world, blockPos, blockState);
            if (this.canFlowThrough(world, fluidState2.getFluid(), pos, state, direction, blockPos, blockState, fluidState)) {
                BlockPos blockPos2 = this.flowsUp() ? blockPos.up() : blockPos.down();
                boolean bl = short2BooleanMap.computeIfAbsent(s, sx -> {
                    BlockState blockState2 = world.getBlockState(blockPos2);
                    return this.canFlowVertical(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
                });
                int j;
                if (bl) {
                    j = 0;
                } else {
                    j = this.getFlowSpeedBetween(world, blockPos, 1, direction.getOpposite(), blockState, pos, short2ObjectMap, short2BooleanMap);
                }

                if (j < i) {
                    map.clear();
                }

                if (j <= i) {
                    map.put(direction, fluidState2);
                    i = j;
                }
            }
        }

        return map;
    }

    private boolean canFlowThrough(
            BlockView world, Fluid fluid, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState
    ) {
        return !(this.matchesType(fluidState.getFluid()) && fluidState.isStill())
                && ((FlowableFluidAccessor)this).invoke_receivesFlow(face, world, pos, state, fromPos, fromState)
                && ((FlowableFluidAccessor)this).invoke_canFill(world, fromPos, fromState, fluid);
    }
    private static short packXZOffset(BlockPos from, BlockPos to) {
        int i = to.getX() - from.getX();
        int j = to.getZ() - from.getZ();
        return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
    }

    @Override
    protected boolean isFlowBlocked(BlockView world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid().matchesType(this)) {
            return false;
        } else if (direction == this.getVerticalFlowDirection().getOpposite()) {
            return true;
        } else {
            return !(blockState.getBlock() instanceof IceBlock) && blockState.isSideSolidFullSquare(world, pos, direction);
        }
    }

    @Override
    public void onScheduledTick(World world, BlockPos pos, FluidState state) {
        FluidHelper.onScheduledTick(this, world, pos, state, this.getLevelDecreasePerBlock(world));
    }

    @Override
    protected void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        if (this.isIn(FluidTags.LAVA)) {
            ((LavaFluid)Fluids.LAVA).randomDisplayTick(world, pos, state, random);
        } else if (this.isIn(FluidTags.WATER)) {
            ((WaterFluid)Fluids.WATER).randomDisplayTick(world, pos, state, random);
        }
    }

    @Override
    public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        //着火
        if (this.settings.canSetFire() && world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            int i = random.nextInt(3);
            if (i > 0) {
                BlockPos blockPos = pos;

                for (int j = 0; j < i; j++) {
                    blockPos = blockPos.add(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                    if (!world.canSetBlock(blockPos)) {
                        return;
                    }

                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.isAir()) {
                        if (this.canLightFire(world, blockPos)) {
                            world.setBlockState(blockPos, AbstractFireBlock.getState(world, blockPos));
                            return;
                        }
                    } else if (blockState.blocksMovement()) {
                        return;
                    }
                }
            } else {
                for (int k = 0; k < 3; k++) {
                    BlockPos blockPos2 = pos.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (!world.canSetBlock(blockPos2)) {
                        return;
                    }

                    if (world.isAir(blockPos2.up()) && this.hasBurnableBlock(world, blockPos2)) {
                        world.setBlockState(blockPos2.up(), AbstractFireBlock.getState(world, blockPos2));
                    }
                }
            }
        }
    }

    private boolean canLightFire(WorldView world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.hasBurnableBlock(world, pos.offset(direction))) {
                return true;
            }
        }

        return false;
    }

    private boolean hasBurnableBlock(WorldView world, BlockPos pos) {
        return (pos.getY() < world.getBottomY() || pos.getY() >= world.getTopY() || world.isChunkLoaded(pos)) && world.getBlockState(pos).isBurnable();
    }

    private void playExtinguishEvent(WorldAccess world, BlockPos pos) {
        world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
    }

    /**
     * Check how many source blocks are neighboring
     * @see FlowableFluid#countNeighboringSources(WorldView, BlockPos) 
     */
    public int countNeighboringSources(WorldView world, BlockPos pos) {
        int adjacentSources = 0;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos adjacentPos = pos.offset(direction);
            FluidState adjacentFluid = world.getFluidState(adjacentPos);
            if (this.isMatchingAndStill(adjacentFluid)) {
                adjacentSources++;
            }
        }
        return adjacentSources;
    }

    /**
     * If the fluid state is of this fluid and is still
     * @see FlowableFluid#isMatchingAndStill(FluidState)
     */
    public boolean isMatchingAndStill(FluidState state) {
        return state.getFluid().matchesType(this) && state.isStill();
    }

    /**
     * @see FlowableFluid#flowToSides(World, BlockPos, FluidState, BlockState)
     */
    public void flowToSides(World world, BlockPos pos, FluidState sourceFluid, BlockState sourceState) {
        int adjacentAmount = sourceFluid.getLevel() - this.getLevelDecreasePerBlock(world);
        if (sourceFluid.get(FALLING)) {
            // 从上方流下，可向周围扩散
            adjacentAmount = 7;
        }
        if (adjacentAmount > 0) {
            // 计算向周围流动
            final Map<Direction, FluidState> map = this.getSpread(world, pos, sourceState);
            for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
                final Direction direction = entry.getKey();
                final FluidState spreadFluid = entry.getValue();
                final BlockPos destPos = pos.offset(direction);
                final BlockState destState = world.getBlockState(destPos);
                if (this.canFlow(world, pos, sourceState, direction, destPos, destState, world.getFluidState(destPos), spreadFluid.getFluid())) {
                    this.flow(world, destPos, destState, direction, spreadFluid);
                }
            }
        }
    }

    /**
     * @see FlowableFluid#canFlowDownTo(BlockView, Fluid, BlockPos, BlockState, BlockPos, BlockState)
     */
    private boolean canFlowVertical(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        if (!((FlowableFluidAccessor)this).invoke_receivesFlow(this.getVerticalFlowDirection(), world, pos, state, fromPos, fromState)) {
            return false;
        } else {
            return fromState.getFluidState().getFluid().matchesType(this) || ((FlowableFluidAccessor)this).invoke_canFill(world, fromPos, fromState, fluid);
        }
    }
}
