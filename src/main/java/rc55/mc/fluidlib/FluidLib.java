package rc55.mc.fluidlib;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rc55.mc.fluidlib.data.*;
import rc55.mc.fluidlib.fluid.reaction.FluidReactionType;
import rc55.mc.fluidlib.fluid.reaction.IFluidReaction;

public class FluidLib implements ModInitializer {
	public static final String MODID = "fluidlib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    static final ConfigHolder<FluidLibConfigs> CONFIG_HOLDER = AutoConfig.register(FluidLibConfigs.class, GsonConfigSerializer::new);

    @Override
	public void onInitialize() {
        FluidReactionType.init();
        ResourceReloadListenerImpl.ofServer(Identifier.of(MODID, "fluid_reaction"), IFluidReaction.BASE_CODEC, IFluidReaction.CACHE);
	}
}
