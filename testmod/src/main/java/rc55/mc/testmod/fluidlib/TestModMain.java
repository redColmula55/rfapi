package rc55.mc.testmod.fluidlib;

import net.fabricmc.api.ModInitializer;
import rc55.mc.testmod.fluidlib.fluid.TestModFluids;
import rc55.mc.testmod.fluidlib.item.TestModItems;

public class TestModMain implements ModInitializer {
    public static final String MODID = "fluidlib-testmod";
    @Override
    public void onInitialize() {
        TestModItems.init();
        TestModFluids.init();
    }
}
