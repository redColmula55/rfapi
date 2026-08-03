package rc55.mc.testmod.fluidlib.item;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import rc55.mc.fluidlib.item.BucketItemRegistry;
import rc55.mc.testmod.fluidlib.fluid.TestModFluids;

import static rc55.mc.testmod.fluidlib.TestModMain.MODID;

public class TestModItems {
    public static final Item BUCKET = Registry.register(Registries.ITEM, new Identifier(MODID, "milk_bucket"), new BucketItem(TestModFluids.MILK.getStill(), new Item.Settings().maxCount(1)));
    public static final Item STEAM_BUCKET = BucketItemRegistry.registerVanilla(Identifier.of(MODID, "steam_bucket"), TestModFluids.STEAM, new Item.Settings());

    public static final BucketItem WOODEN_BUCKET = BucketItemRegistry.registerEmpty(new Identifier(MODID, "wooden_bucket"), 900, new Item.Settings());
    public static final BucketItem WOODEN_WATER_BUCKET = BucketItemRegistry.register(new Identifier(MODID, "wooden_bucket/water"), WOODEN_BUCKET, Fluids.WATER, new Item.Settings());
    public static final BucketItem WOODEN_MILK_BUCKET = BucketItemRegistry.register(new Identifier(MODID, "wooden_bucket/milk"), WOODEN_BUCKET, TestModFluids.MILK, new Item.Settings());
    public static final BucketItem CERAMIC_BUCKET = BucketItemRegistry.registerEmpty(new Identifier(MODID, "ceramic_bucket"), 1000, new Item.Settings());
    public static final BucketItem CERAMIC_WATER_BUCKET = BucketItemRegistry.register(new Identifier(MODID, "ceramic_bucket/water"), CERAMIC_BUCKET, Fluids.WATER, new Item.Settings());

    public static void init() {
    }
}
