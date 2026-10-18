package rc55.mc.rfapi;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

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

    /**
     * Determines if the player can target at fluid blocks
     * This means you can select & break fluid blocks like breaking normal blocks in Creative mode
     */
    public boolean drawFluidBlockOutline = false;

    // May cause crash when the annotation applied
    //@Environment(EnvType.CLIENT)
    public boolean clearLavaFog = false;

    //@Environment(EnvType.CLIENT)
    public boolean clearSnowFog = false;

    //@Environment(EnvType.CLIENT)
    public boolean replaceVanillaFog = true;
}
