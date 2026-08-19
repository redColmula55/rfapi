package rc55.mc.rfapi.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rc55.mc.rfapi.event.EndServerChunkTickEvent;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            method = "tickChunk"
    )
    public void rfapi$fireChunkTickEvent(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        EndServerChunkTickEvent.EVENT.invoker().onChunkTickEnd((ServerWorld)(Object) this, chunk, randomTickSpeed);
    }
}
