package rc55.mc.fluidlib.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public final class BucketItemDispenserBehavior {
    private BucketItemDispenserBehavior(){}

    public static final ItemDispenserBehavior FALLBACK_BEHAVIOR = new ItemDispenserBehavior();

    public static DispenserBehavior emptyBucketBehavior(BucketItem bucket) {
        return new ItemDispenserBehavior() {
            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                final WorldAccess world = pointer.getWorld();
                final BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                final BlockState blockState = world.getBlockState(blockPos);
                final Block block = blockState.getBlock();
                if (block instanceof FluidDrainable) {
                    Item item = null;
                    if (bucket instanceof ExtendedBucketItem) {
                        item = ((ExtendedBucketItem) bucket).getBucketFor(blockState.getFluidState().getFluid());
                        if (item == null) {
                            return super.dispenseSilently(pointer, stack);
                        }
                    }
                    final ItemStack vanillaBucketStack = ((FluidDrainable)block).tryDrainFluid(world, blockPos, blockState);
                    if (vanillaBucketStack.isEmpty()) {
                        return super.dispenseSilently(pointer, stack);
                    } else {
                        if (item == null) {
                            item = vanillaBucketStack.getItem();
                        }
                        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                        stack.decrement(1);
                        if (stack.isEmpty()) {
                            return new ItemStack(item);
                        } else {
                            if (pointer.<DispenserBlockEntity>getBlockEntity().addToFirstFreeSlot(new ItemStack(item)) < 0) {
                                FALLBACK_BEHAVIOR.dispense(pointer, new ItemStack(item));
                            }

                            return stack;
                        }
                    }
                } else {
                    return super.dispenseSilently(pointer, stack);
                }
            }
        };
    }

    public static DispenserBehavior fluidBucketBehavior(BucketItem bucket) {
        return new ItemDispenserBehavior() {
            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                FluidModificationItem fluidModificationItem = (FluidModificationItem)stack.getItem();
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                World world = pointer.getWorld();
                if (fluidModificationItem.placeFluid(null, world, blockPos, null)) {
                    fluidModificationItem.onEmptied(null, world, stack, blockPos);
                    if (stack.getCount() > 1 && pointer.<DispenserBlockEntity>getBlockEntity().addToFirstFreeSlot(new ItemStack(bucket)) == -1) {
                        FALLBACK_BEHAVIOR.dispense(pointer, new ItemStack(bucket));
                    }
                    return stack.getCount() > 1 ? stack.split(stack.getCount() - 1) : new ItemStack(bucket);
                } else {
                    return FALLBACK_BEHAVIOR.dispense(pointer, stack);
                }
            }
        };
    }
}
