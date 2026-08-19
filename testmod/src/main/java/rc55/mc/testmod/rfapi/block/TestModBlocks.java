package rc55.mc.testmod.rfapi.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import rc55.mc.rfapi.block.FluidConvertibleBlock;
import rc55.mc.testmod.rfapi.TestModMain;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;

import java.util.function.Function;

public class TestModBlocks {
    public static final FluidConvertibleBlock MILK_ICE = register("milk_ice", s -> new FluidConvertibleBlock(TestModFluids.MILK, s), AbstractBlock.Settings.copy(Blocks.ICE));

    private static <T extends Block, S extends AbstractBlock.Settings> T register(String id, Function<S, T> blockFactory, S settings, boolean withItem) {
        RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, new Identifier(TestModMain.MODID, id));
        T block = Registry.register(Registries.BLOCK, registryKey, blockFactory.apply(settings));
        if (withItem) {
            Registry.register(Registries.ITEM, new Identifier(TestModMain.MODID, id), new BlockItem(block, new Item.Settings()));
        }
        return block;
    }

    private static <T extends Block, S extends AbstractBlock.Settings> T register(String id, Function<S, T> blockFactory, S settings) {
        return register(id, blockFactory, settings, true);
    }

    public static void init() {
    }
}
