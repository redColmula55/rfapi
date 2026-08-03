package rc55.mc.testmod.fluidlib.fluid;

import net.minecraft.block.MapColor;
import net.minecraft.util.Identifier;
import rc55.mc.fluidlib.fluid.ExtendedFluid;
import rc55.mc.fluidlib.fluid.FluidReference;
import rc55.mc.fluidlib.fluid.FluidRegistry;
import rc55.mc.fluidlib.fluid.FluidSettings;
import rc55.mc.testmod.fluidlib.item.TestModItems;

import static rc55.mc.testmod.fluidlib.TestModMain.MODID;

public class TestModFluids {
    public static final FluidReference<ExtendedFluid> MILK = FluidRegistry.registerSimple(
            Identifier.of(MODID, "milk"),
            FluidSettings.waterLike()
                    .color(FluidSettings.ColorSettings.ofFixedColor(0xFFE211, MapColor.WATER_BLUE, FluidSettings.ColorSettings.FogType.WATER))
                    .temperature(799)
                    .bucket(() -> TestModItems.BUCKET)
    );

    public static final FluidReference<ExtendedFluid> STEAM = FluidRegistry.registerSimple(
            Identifier.of(MODID, "steam"),
            FluidSettings.waterLike().temperature(400).bucket(() -> TestModItems.STEAM_BUCKET).flowsUp()
    );

    public static void init() {
    }
}
