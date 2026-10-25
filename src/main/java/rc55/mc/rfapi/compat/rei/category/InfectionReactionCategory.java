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
import rc55.mc.rfapi.compat.rei.display.InfectionReactionDisplay;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;

import java.util.List;

public class InfectionReactionCategory implements DisplayCategory<InfectionReactionDisplay> {
    public static final CategoryIdentifier<InfectionReactionDisplay> ID = ReiUtils.categoryId(FluidReactionType.INFECTION);

    @Override
    public CategoryIdentifier<? extends InfectionReactionDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Text getTitle() {
        return FluidReactionType.INFECTION.getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.JIGSAW);
    }

    @Override
    public List<Widget> setupDisplay(InfectionReactionDisplay display, Rectangle bounds) {
        return ReiUtils.build2To1Display(bounds, display, display.getTarget());
    }
}
