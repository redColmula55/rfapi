package rc55.mc.rfapi.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface ServerPlayerJoinEvent {
    ServerPlayerJoinEvent DEFAULT_CALLBACK = p -> {};

    Event<ServerPlayerJoinEvent> EVENT = EventFactory.createArrayBacked(ServerPlayerJoinEvent.class, DEFAULT_CALLBACK, callbacks -> player -> {
        for (ServerPlayerJoinEvent callback : callbacks) {
            callback.onJoin(player);
        }
    });
    
    void onJoin(ServerPlayerEntity player);
}
