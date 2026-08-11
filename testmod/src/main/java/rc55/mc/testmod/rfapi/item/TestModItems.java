package rc55.mc.testmod.rfapi.item;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import rc55.mc.rfapi.item.BucketItemRegistry;
import rc55.mc.testmod.rfapi.TestModMain;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;

import java.util.Map;

import static rc55.mc.testmod.rfapi.TestModMain.MODID;

public class TestModItems {
    public static final BucketItem MILK_FLUID_BUCKET = Registry.register(Registries.ITEM, new Identifier(MODID, "milk_bucket"), new BucketItem(TestModFluids.MILK.getStill(), new Item.Settings().maxCount(1)));
    public static final BucketItem STEAM_BUCKET = BucketItemRegistry.registerVanilla(Identifier.of(MODID, "steam_bucket"), TestModFluids.STEAM, new Item.Settings());

    public static final BucketItem WOODEN_BUCKET = BucketItemRegistry.registerEmpty(new Identifier(MODID, "wooden_bucket"), 900, new Item.Settings());
    public static final BucketItem WOODEN_WATER_BUCKET = BucketItemRegistry.register(new Identifier(MODID, "wooden_bucket/water"), WOODEN_BUCKET, Fluids.WATER, new Item.Settings());
    public static final BucketItem WOODEN_MILK_BUCKET = BucketItemRegistry.register(new Identifier(MODID, "wooden_bucket/milk"), WOODEN_BUCKET, TestModFluids.MILK, new Item.Settings());
    public static final BucketItem CERAMIC_BUCKET = BucketItemRegistry.registerEmpty(new Identifier(MODID, "ceramic_bucket"), 1000, new Item.Settings());
    public static final BucketItem CERAMIC_WATER_BUCKET = BucketItemRegistry.register(new Identifier(MODID, "ceramic_bucket/water"), CERAMIC_BUCKET, Fluids.WATER, new Item.Settings());

    public static final Map<DyeColor, BucketItem> DYE_BUCKETS = TestModMain.mapOf(DyeColor.class, color -> BucketItemRegistry.registerVanilla(
            Identifier.of(MODID, color.getName() + "_dye_bucket"),
            TestModFluids.DYE_FLUIDS.get(color),
            new Item.Settings().maxCount(16)
    ));

    public static void init() {
    }
}
