package rc55.mc.rfapi;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import rc55.mc.rfapi.compat.rei.category.SimpleFluidReactionCategory;
import rc55.mc.rfapi.compat.rei.display.SimpleFluidReactionDisplay;
import rc55.mc.rfapi.fluid.reaction.IFluidReaction;
import rc55.mc.rfapi.fluid.reaction.SimpleFluidReaction;

@Environment(EnvType.CLIENT)
public class RFApiReiClientImpl implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SimpleFluidReactionCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(SimpleFluidReaction.class, SimpleFluidReactionDisplay::validate, SimpleFluidReactionDisplay::fromReaction);
        // Add all reactions
        IFluidReaction.CACHE.loadLazyValues();
        IFluidReaction.CACHE.getAll().forEach(registry::add);
    }
}
