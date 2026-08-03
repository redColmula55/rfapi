package rc55.mc.fluidlib.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;
import rc55.mc.fluidlib.fluid.FluidReference;
import rc55.mc.fluidlib.fluid.FluidSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * FluidLib version of buckets
 * <ul>
 *     <li>Can have multiple type of buckets</li>
 *     <li>Stackable</li>
 *     <li>Placing water in the nether now depends on {@link FluidLibFluidTags#DISAPPEAR_IN_ULTRAWARM #c:disappear_in_ultrawarm}</li>
 * </ul>
 * Use {@link BucketItemRegistry} to automatically handle extra stuff(e.g. dispenser behaviors)
 * @see BucketItem
 */
public class ExtendedBucketItem extends BucketItem {

    /**
     * Map of different type of buckets
     * Note that vanilla bucket aren't included
     * Key： The empty bucket
     * Value： Map of fluid and their item with this type of bucket
     */
    public static final Map<BucketItem, Map<Fluid, BucketItem>> BUCKET_ITEM_MAP = new HashMap<>();

    /**
     * Empty factor of this bucket
     * null when the bucket is already empty
     */
    @Nullable
    private final BucketItem baseItem;
    private final int maxTemperature;

    /**
     * Create bucket with fluid
     * @param fluid Fluid in the bucket
     * @param base Empty factor of the fluid bucket
     */
    public ExtendedBucketItem(@NotNull Fluid fluid, @NotNull BucketItem base, Settings settings) {
        super(fluid, settings.recipeRemainder(base));
        if (base instanceof ExtendedBucketItem bucket && fluid.getSettings().getTemperature() > bucket.maxTemperature) {
            throw new IllegalArgumentException("Given fluid %s has temperature %d greater than its base item(max temperature %d)!"
                    .formatted(fluid, fluid.getSettings().getTemperature(), bucket.maxTemperature));
        }
        this.maxTemperature = Integer.MAX_VALUE;
        this.baseItem = base;
        if (base != Items.BUCKET) {
            if (!BUCKET_ITEM_MAP.containsKey(base)) {
                BUCKET_ITEM_MAP.put(base, Util.make(new HashMap<>(), map -> map.put(fluid, this)));
            } else {
                BUCKET_ITEM_MAP.get(base).put(fluid, this);
            }
        }
    }

    /**
     * @see ExtendedBucketItem#ExtendedBucketItem(Fluid, BucketItem, Settings) ExtendedBucketItem
     */
    public ExtendedBucketItem(FluidReference<?> fluid, @NotNull BucketItem base, Settings settings) {
        this(fluid.getStill(), base, settings);
    }

    /**
     * Create empty bucket
     * @param maxTemperature Max temperature the bucket can hold
     */
    public ExtendedBucketItem(int maxTemperature, Settings settings) {
        super(Fluids.EMPTY, settings);
        this.maxTemperature = maxTemperature;
        this.baseItem = null;
        if (!BUCKET_ITEM_MAP.containsKey(this)) {
            BUCKET_ITEM_MAP.put(this, new HashMap<>());
        }
    }

    /**
     * Creates a custom fluid bucket which use vanilla bucket as base
     */
    public static ExtendedBucketItem ofVanilla(FluidReference<?> fluid, Settings settings) {
        return new ExtendedBucketItem(fluid, (BucketItem) Items.BUCKET, settings);
    }

    /**
     * Check if the stack is an empty bucket
     */
    public static boolean isEmpty(ItemStack stack) {
        return isEmpty(stack.getItem());
    }

    /**
     * Check if the item is an empty bucket
     */
    public static boolean isEmpty(Item item) {
        return (item instanceof ExtendedBucketItem z && z.isEmpty()) || item == Items.BUCKET;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        BlockHitResult hitResult = raycast(world, user, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
        // Miss
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(stack);
        } else if (hitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stack);
        }
        // Hit pos
        BlockPos hitPos = hitResult.getBlockPos();
        Direction hitSide = hitResult.getSide();
        BlockPos hitSideOffsetPos = hitPos.offset(hitSide);
        if (!world.canPlayerModifyAt(user, hitPos) || !user.canPlaceOn(hitSideOffsetPos, hitSide, stack)) {
            // Not placeable
            return TypedActionResult.fail(stack);
        } else if (this.isEmpty()) {
            // Pick up
            BlockState blockState = world.getBlockState(hitPos);
            if (blockState.getBlock() instanceof FluidDrainable fluidDrainable) {
                Fluid fluid = blockState.getFluidState().getFluid();
                BucketItem bucketItem = BUCKET_ITEM_MAP.get(this).get(fluid);
                if (bucketItem == null || fluid.getSettings().getTemperature() > this.maxTemperature) {
                    return TypedActionResult.fail(stack);
                }
                if (!fluidDrainable.tryDrainFluid(world, hitPos, blockState).isEmpty()) {
                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    fluidDrainable.getBucketFillSound().ifPresent(sound -> user.playSound(sound, 1f, 1f));
                    world.emitGameEvent(user, GameEvent.FLUID_PICKUP, hitPos);

                    ItemStack resultStack = new ItemStack(bucketItem);
                    ItemStack itemStack = ItemUsage.exchangeStack(stack, user, resultStack);
                    if (!world.isClient) {
                        Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)user, resultStack);
                    }

                    return TypedActionResult.success(itemStack, world.isClient());
                }
            }
            return TypedActionResult.fail(stack);
        } else {
            // Place
            BlockPos pos = world.getBlockState(hitPos).getBlock() instanceof FluidFillable && this.fluid.matchesType(Fluids.WATER) ? hitPos : hitSideOffsetPos;
            if (this.placeFluid(user, world, pos, hitResult)) {
                this.onEmptied(user, world, stack, pos);
                if (user instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, pos, stack);
                }

                ItemStack resultStack = ItemUsage.exchangeStack(stack, user, new ItemStack(this::getBaseItem));
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.success(resultStack, world.isClient());
            } else {
                return TypedActionResult.fail(stack);
            }
        }
    }

    @Override
    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean canPlaceHere = state.isAir() || state.canBucketPlace(this.fluid) ||
                (block instanceof FluidFillable fillable && fillable.canFillWithFluid(world, pos, state, this.fluid));
        if (!canPlaceHere) {
            return hitResult != null && this.placeFluid(player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
        } else if (world.getDimension().ultrawarm() && this.fluid.isIn(FluidLibFluidTags.DISAPPEAR_IN_ULTRAWARM)) {
            // Not avail in ultrawarm, disappear
            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();
            // Sound
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                    0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
            // Particle
            for (int i = 0; i < 8; i++) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(), z + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        } else if (block instanceof FluidFillable fillable && Fluids.WATER.matchesType(this.fluid)) {
            // Waterlog block
            fillable.tryFillWithFluid(world, pos, state, ((FlowableFluid)this.fluid).getStill(false));
            this.playEmptyingSound(player, world, pos);
            return true;
        } else {
            // Break block
            if (!world.isClient && state.canBucketPlace(this.fluid) && !state.isLiquid()) {
                world.breakBlock(pos, true);
            }
            // Place fluid block
            if (!world.setBlockState(pos, this.fluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD)
                    && !state.getFluidState().isStill()
            ) {
                return false;
            } else {
                this.playEmptyingSound(player, world, pos);
                return true;
            }
        }
    }

    @Override
    protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
        final SoundEvent sound = FluidSettings.get(fluid).getBucketEmptySound().orElse(SoundEvents.ITEM_BUCKET_EMPTY);
        world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
    }

    /**
     * Get the empty factor of the bucket, will return self if the bucket is already empty
     */
    public BucketItem getBaseItem() {
        return this.baseItem == null ? this : this.baseItem;
    }

    /**
     * Max temperature this bucket can held, will return {@link Integer#MAX_VALUE} if this bucket is not empty
     */
    public int getMaxTemperature() {
        return maxTemperature;
    }

    /**
     * Fluid this bucket contains, {@link Fluids#EMPTY} if this bucket is empty
     */
    public Fluid getFluid() {
        return this.fluid;
    }

    /**
     * Get fluid bucket for this bucket
     * @param fluid Fluid type
     * @return Bucket item contains the given fluid, null if this bucket can't hold given fluid
     * @throws UnsupportedOperationException If this is not called from an empty bucket
     */
    public @Nullable BucketItem getBucketFor(Fluid fluid) {
        if (!this.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        return BUCKET_ITEM_MAP.get(this).get(fluid);
    }

    /**
     * Whether this bucket is empty
     */
    public boolean isEmpty() {
        return this.baseItem == null || this.fluid == Fluids.EMPTY;
    }
}
