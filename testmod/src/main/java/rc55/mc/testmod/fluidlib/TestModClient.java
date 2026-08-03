package rc55.mc.testmod.fluidlib;

import net.fabricmc.api.ClientModInitializer;
import rc55.mc.fluidlib.client.FluidRenderRegistry;
import rc55.mc.fluidlib.fluid.FluidRegistry;
import rc55.mc.testmod.fluidlib.fluid.TestModFluids;

public class TestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FluidRenderRegistry.registerColoredWater(TestModFluids.MILK);
        FluidRenderRegistry.registerColoredWater(TestModFluids.STEAM);
    }
}
