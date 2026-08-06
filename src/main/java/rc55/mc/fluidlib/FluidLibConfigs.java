package rc55.mc.fluidlib;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Config(name = FluidLib.MODID)
@Config.Gui.Background("minecraft:textures/block/lava_still.png")
public class FluidLibConfigs implements ConfigData {
    FluidLibConfigs() {
    }

    public static FluidLibConfigs getInstance() {
        return FluidLib.CONFIG_HOLDER.getConfig();
    }

    public boolean fluidUpdates = true;

    public boolean blockVanillaFluidReactions = true;

    public boolean disableOpenWaterCheck = false;

    @Environment(EnvType.CLIENT)
    public boolean clearLavaFog = false;

    @Environment(EnvType.CLIENT)
    public boolean clearSnowFog = false;
}
