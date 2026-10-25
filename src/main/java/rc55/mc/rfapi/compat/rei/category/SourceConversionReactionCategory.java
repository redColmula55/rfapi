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
import rc55.mc.rfapi.compat.rei.display.SourceConversionReactionDisplay;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;

import java.util.List;

public class SourceConversionReactionCategory implements DisplayCategory<SourceConversionReactionDisplay> {
    public static final CategoryIdentifier<SourceConversionReactionDisplay> ID = ReiUtils.categoryId(FluidReactionType.SOURCE_CONVERSION);

    @Override
    public CategoryIdentifier<? extends SourceConversionReactionDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Text getTitle() {
        return FluidReactionType.SOURCE_CONVERSION.getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.WATER_BUCKET);
    }

    @Override
    public List<Widget> setupDisplay(SourceConversionReactionDisplay display, Rectangle bounds) {
        return ReiUtils.build2To1Display(bounds, display, display.getIngredient());
    }
}
