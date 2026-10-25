package rc55.mc.rfapi.item;

import com.google.common.collect.Iterators;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.BucketItem;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class EmptyExtendedBucketStorage implements InsertionOnlyStorage<FluidVariant> {
    private final Iterator<StorageView<FluidVariant>> iterator = Iterators.singletonIterator(new BlankVariantView<>(FluidVariant.blank(), FluidConstants.BUCKET));

    protected final ContainerItemContext context;
    protected final ExtendedBucketItem bucket;

    protected EmptyExtendedBucketStorage(ContainerItemContext context, ExtendedBucketItem bucket) {
        this.context = context;
        this.bucket = bucket;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!context.getItemVariant().isOf(this.bucket)) return 0;

        BucketItem fullBucket = ExtendedBucketItem.BUCKET_ITEM_MAP.get(this.bucket).get(resource.getFluid());
        if (fullBucket != null && resource.isOf(fullBucket.fluid)) {
            if (maxAmount >= FluidConstants.BUCKET) {
                ItemVariant newVariant = ItemVariant.of(fullBucket, context.getItemVariant().getNbt());

                if (context.exchange(newVariant, 1, transaction) == 1) {
                    return FluidConstants.BUCKET;
                }
            }
        }

        return 0;
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        return this.iterator;
    }
}
