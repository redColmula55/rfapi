package rc55.mc.rfapi;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import rc55.mc.rfapi.compat.rei.category.SimpleFluidReactionCategory;
import rc55.mc.rfapi.compat.rei.display.SimpleFluidReactionDisplay;

public class RFApiReiServerImpl implements REIServerPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(SimpleFluidReactionCategory.ID, BasicDisplay.Serializer.ofRecipeLess(SimpleFluidReactionDisplay::of, (display, tag) -> {
            tag.putFloat("Chance", display.chance);
        }));
    }
}
