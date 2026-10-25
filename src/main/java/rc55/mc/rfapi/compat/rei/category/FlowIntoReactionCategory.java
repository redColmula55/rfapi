package rc55.mc.rfapi.compat.rei.category;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import rc55.mc.rfapi.compat.rei.ReiUtils;
import rc55.mc.rfapi.compat.rei.display.FlowIntoReactionDisplay;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;

import java.util.List;

public class FlowIntoReactionCategory implements DisplayCategory<FlowIntoReactionDisplay> {
    public static final CategoryIdentifier<FlowIntoReactionDisplay> ID = ReiUtils.categoryId(FluidReactionType.FLOW_INTO);

    @Override
    public CategoryIdentifier<? extends FlowIntoReactionDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Text getTitle() {
        return FluidReactionType.FLOW_INTO.getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.STONE);
    }

    @Override
    public List<Widget> setupDisplay(FlowIntoReactionDisplay display, Rectangle bounds) {
        return ReiUtils.build2To1Display(bounds, display, display.getIngredient());
    }
}
