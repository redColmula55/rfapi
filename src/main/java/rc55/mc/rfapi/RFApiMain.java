package rc55.mc.rfapi;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rc55.mc.rfapi.event.ServerPlayerJoinEvent;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;
import rc55.mc.rfapi.fluid.reaction.IFluidReaction;

public class RFApiMain implements ModInitializer {
	public static final String MODID = "reservoir-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    static final ConfigHolder<RFApiConfigs> CONFIG_HOLDER = AutoConfig.register(RFApiConfigs.class, GsonConfigSerializer::new);

    @Override
	public void onInitialize() {
        FluidReactionType.init();

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (!success) return;
            // Refreshes cache
            IFluidReaction.CACHE.loadLazyValues();
            for (final var player : server.getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player, IFluidReaction.RELOAD_LISTENER.getFabricId(), IFluidReaction.CACHE.toByteBuf());
            }
        });

        ServerPlayerJoinEvent.EVENT.register(player -> ServerPlayNetworking.send(player, IFluidReaction.RELOAD_LISTENER.getFabricId(), IFluidReaction.CACHE.toByteBuf()));
	}
}
