package rc55.mc.fluidlib;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rc55.mc.fluidlib.data.*;
import rc55.mc.fluidlib.fluid.FluidRegistry;
import rc55.mc.fluidlib.item.BucketItemRegistry;

public class FluidLib implements ModInitializer {
	public static final String MODID = "fluidlib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
	public void onInitialize() {
        ResourceReloadListenerImpl.ofServer(Identifier.of(MODID, "fluid_reaction"), FluidReaction.CODEC, FluidReaction.CACHE);

        ServerTickEvents.START_WORLD_TICK.register(world -> FluidReaction.infectionDepth.set(0));
	}
}
