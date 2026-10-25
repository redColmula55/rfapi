package rc55.mc.rfapi;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import rc55.mc.rfapi.compat.rei.category.*;
import rc55.mc.rfapi.compat.rei.category.SimpleFluidReactionCategory;
import rc55.mc.rfapi.compat.rei.display.*;
import rc55.mc.rfapi.compat.rei.display.SimpleFluidReactionDisplay;

public class RFApiReiServerImpl implements REIServerPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(SimpleFluidReactionCategory.ID, BasicDisplay.Serializer.ofRecipeLess(SimpleFluidReactionDisplay::of, IFluidReactionDisplay::writeChance));
        registry.register(SimpleFlowableReactionCategory.ID, BasicDisplay.Serializer.ofRecipeLess(SimpleFlowableReactionDisplay::of, IFluidReactionDisplay::writeChance));
        registry.register(FlowIntoReactionCategory.ID, BasicDisplay.Serializer.ofRecipeLess(FlowIntoReactionDisplay::of, IFluidReactionDisplay::writeChance));
        registry.register(SourceConversionReactionCategory.ID, BasicDisplay.Serializer.ofRecipeLess(SourceConversionReactionDisplay::of, IFluidReactionDisplay::writeChance));
        registry.register(InfectionReactionCategory.ID, BasicDisplay.Serializer.ofRecipeLess(InfectionReactionDisplay::of, IFluidReactionDisplay::writeChance));
    }
}
