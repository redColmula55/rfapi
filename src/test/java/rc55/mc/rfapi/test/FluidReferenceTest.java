package rc55.mc.rfapi.test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.fluid.Fluids;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rc55.mc.rfapi.fluid.FluidReference;

public class FluidReferenceTest {
    @BeforeAll
    static void beforeAll() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testFluidReferenceInterner() {
        Assertions.assertSame(FluidReference.VANILLA_LAVA, FluidReference.of(Fluids.FLOWING_LAVA));
        Assertions.assertSame(FluidReference.VANILLA_WATER, FluidReference.of(Fluids.WATER));
    }
}
