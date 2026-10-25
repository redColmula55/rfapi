package rc55.mc.rfapi;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import rc55.mc.rfapi.compat.rei.category.*;
import rc55.mc.rfapi.compat.rei.display.*;
import rc55.mc.rfapi.fluid.reaction.*;

@Environment(EnvType.CLIENT)
public class RFApiReiClientImpl implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SimpleFluidReactionCategory());
        registry.add(new SimpleFlowableReactionCategory());
        registry.add(new FlowIntoReactionCategory());
        registry.add(new SourceConversionReactionCategory());
        registry.add(new InfectionReactionCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(SimpleFluidReaction.class, SimpleFluidReactionDisplay::validate, SimpleFluidReactionDisplay::fromReaction);
        registry.registerFiller(SimpleFlowableFluidReaction.class, SimpleFlowableReactionDisplay::validate, SimpleFlowableReactionDisplay::fromReaction);
        registry.registerFiller(FlowIntoReaction.class, FlowIntoReactionDisplay::validate, FlowIntoReactionDisplay::fromReaction);
        registry.registerFiller(SourceConversionReaction.class, SourceConversionReactionDisplay::validate, SourceConversionReactionDisplay::fromReaction);
        registry.registerFiller(InfectionReaction.class, InfectionReactionDisplay::validate, InfectionReactionDisplay::fromReaction);
        // Add all reactions
        IFluidReaction.CACHE.loadLazyValues();
        IFluidReaction.CACHE.getAll().forEach(registry::add);
    }
}
