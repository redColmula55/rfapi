package rc55.mc.rfapi;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;

public class RFApiMain implements ModInitializer {
	public static final String MODID = "reservoir-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    static final ConfigHolder<RFApiConfigs> CONFIG_HOLDER = AutoConfig.register(RFApiConfigs.class, GsonConfigSerializer::new);

    @Override
	public void onInitialize() {
        FluidReactionType.init();
	}
}
