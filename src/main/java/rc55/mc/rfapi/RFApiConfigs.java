package rc55.mc.rfapi;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Config(name = RFApiMain.MODID)
@Config.Gui.Background("minecraft:textures/block/dirt.png")
public class RFApiConfigs implements ConfigData {
    RFApiConfigs() {
    }

    public static RFApiConfigs getInstance() {
        return RFApiMain.CONFIG_HOLDER.getConfig();
    }

    /**
     * If allowing fluids to perform scheduled tick.
     * Typically this controls if the fluid can flow
     */
    public boolean fluidUpdates = true;

    public boolean blockVanillaFluidReactions = true;

    public boolean disableOpenWaterCheck = false;

    @Environment(EnvType.CLIENT)
    public boolean clearLavaFog = false;

    @Environment(EnvType.CLIENT)
    public boolean clearSnowFog = false;
}
