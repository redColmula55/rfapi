package rc55.mc.testmod.rfapi.fluid;

import net.minecraft.block.MapColor;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import rc55.mc.rfapi.fluid.ExtendedFluid;
import rc55.mc.rfapi.fluid.FluidReference;
import rc55.mc.rfapi.fluid.FluidRegistry;
import rc55.mc.rfapi.fluid.FluidSettings;
import rc55.mc.testmod.rfapi.TestModMain;
import rc55.mc.testmod.rfapi.item.TestModItems;

import java.util.Map;

import static rc55.mc.testmod.rfapi.TestModMain.MODID;

public class TestModFluids {
    public static final FluidReference<ExtendedFluid> MILK = FluidRegistry.registerSimple(
            Identifier.of(MODID, "milk"),
            FluidSettings.lavaLike()
                    .color(FluidSettings.ColorSettings.ofFixedColor(0xE5E5E5, MapColor.OFF_WHITE, FluidSettings.ColorSettings.FogType.LAVA))
                    .temperature(350)
                    .setsFire(false)
                    .luminance(0)
                    .ticksRandomly(false)
                    .bucket(() -> TestModItems.MILK_FLUID_BUCKET)
    );

    public static final FluidReference<ExtendedFluid> STEAM = FluidRegistry.registerSimple(
            Identifier.of(MODID, "steam"),
            FluidSettings.waterLike().temperature(450).bucket(() -> TestModItems.STEAM_BUCKET).flowsUp()
    );

    public static final Map<DyeColor, FluidReference<ExtendedFluid>> DYE_FLUIDS = TestModMain.mapOf(DyeColor.class, color -> FluidRegistry.registerSimple(
            Identifier.of(MODID, color.getName() + "_dye"),
            FluidSettings.waterLike()
                    .color(FluidSettings.ColorSettings.ofFixedColor(colorFor(color), color.getMapColor(), FluidSettings.ColorSettings.FogType.SNOW))
                    .luminance(color.getId() & 15)
                    .isInfinite(false)
                    .setsFire()
                    .bucket(() -> TestModItems.DYE_BUCKETS.get(color))
    ));

    private static int colorFor(DyeColor dyeColor) {
        float[] color = dyeColor.getColorComponents();
        return FluidSettings.ColorSettings.toRgb(color[0], color[1], color[2]);
    }

    public static void init() {
    }
}
