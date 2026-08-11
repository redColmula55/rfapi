package rc55.mc.testmod.rfapi;

import net.fabricmc.api.ModInitializer;
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
        TestModFluids.init();
    }

    public static <E extends Enum<E>, V> Map<E, V> mapOf(Class<E> clazz, Function<E, V> valueMapper) {
        return Arrays.stream(clazz.getEnumConstants())
                .collect(Collectors.toMap(Function.identity(), valueMapper, (v, v2) -> v, () -> new EnumMap<>(clazz)));
    }
}
