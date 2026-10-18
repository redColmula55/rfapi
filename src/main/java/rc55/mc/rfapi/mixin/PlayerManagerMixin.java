package rc55.mc.rfapi.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rc55.mc.rfapi.event.ServerPlayerJoinEvent;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    public void rfapi$hookPlayerJoinEvent(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        ServerPlayerJoinEvent.EVENT.invoker().onJoin(player);
    }
}
