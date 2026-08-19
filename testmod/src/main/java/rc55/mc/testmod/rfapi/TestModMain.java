package rc55.mc.testmod.rfapi;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import rc55.mc.rfapi.event.EndServerChunkTickEvent;
import rc55.mc.rfapi.event.EntityTouchFluidEvent;
import rc55.mc.testmod.rfapi.block.TestModBlocks;
import rc55.mc.testmod.rfapi.fluid.TestModFluids;
import rc55.mc.testmod.rfapi.item.TestModItems;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestModMain implements ModInitializer {
    public static final String MODID = "reservoir-testmod";
    @Override
    public void onInitialize() {
        TestModItems.init();
        TestModBlocks.init();
        TestModFluids.init();

        EndServerChunkTickEvent.EVENT.register(EndServerChunkTickEvent.setIce(TestModFluids.MILK::matchesAndStill, TestModBlocks.MILK_ICE.getDefaultState()));

        EntityTouchFluidEvent.EVENT.register((state, world, pos, entity) -> {
            if (state.isOf(TestModFluids.STEAM.getBlock())) {
                if (entity instanceof LivingEntity living) {
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST));
                }
            }
            return false;
        });
    }

    public static <E extends Enum<E>, V> Map<E, V> mapOf(Class<E> clazz, Function<E, V> valueMapper) {
        return Arrays.stream(clazz.getEnumConstants())
                .collect(Collectors.toMap(Function.identity(), valueMapper, (v, v2) -> v, () -> new EnumMap<>(clazz)));
    }
}
