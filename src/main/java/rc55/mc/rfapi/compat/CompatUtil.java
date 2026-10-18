package rc55.mc.rfapi.compat;

import net.fabricmc.loader.impl.FabricLoaderImpl;

public class CompatUtil {
    public static final String REI_ID = "rei";
    public static final String SODIUM_ID = "sodium";

    public static boolean isModLoaded(String modId) {
        return FabricLoaderImpl.INSTANCE.isModLoaded(modId);
    }

    public static boolean hasRei() {
        return isModLoaded(REI_ID);
    }

    public static boolean hasSodium() {
        return isModLoaded(SODIUM_ID);
    }
}
