package rc55.mc.fluidlib.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import rc55.mc.fluidlib.fluid.FluidReference;

import java.util.function.Function;

public class BucketItemRegistry {
    protected static <T extends Item> T register(Identifier id, Function<Item.Settings, T> itemFactory, Item.Settings settings) {
        return Registry.register(Registries.ITEM, id, itemFactory.apply(settings));
    }

    public static ExtendedBucketItem registerEmpty(Identifier id, int maxTemperature, Item.Settings settings) {
        final ExtendedBucketItem item = register(id, s -> new ExtendedBucketItem(maxTemperature, s), settings);
        DispenserBlock.registerBehavior(item, BucketItemDispenserBehavior.emptyBucketBehavior(item));
        return item;
    }

    public static ExtendedBucketItem registerVanilla(Identifier id, FluidReference<?> fluid, Item.Settings settings) {
        return register(id, s -> ExtendedBucketItem.ofVanilla(fluid, s), settings);
    }

    public static ExtendedBucketItem register(Identifier id, @NotNull BucketItem base, FluidReference<?> fluid, Item.Settings settings) {
        final ExtendedBucketItem item = register(id, s -> new ExtendedBucketItem(fluid, base, s), settings);
        DispenserBlock.registerBehavior(item, BucketItemDispenserBehavior.fluidBucketBehavior(base));
        return item;
    }

    public static ExtendedBucketItem register(Identifier id, @NotNull BucketItem base, Fluid fluid, Item.Settings settings) {
        final ExtendedBucketItem item = register(id, s -> new ExtendedBucketItem(fluid, base, s), settings);
        DispenserBlock.registerBehavior(item, BucketItemDispenserBehavior.fluidBucketBehavior(base));
        return item;
    }
}
