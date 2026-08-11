package rc55.mc.rfapi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.BucketItem;
import net.minecraft.text.Text;
import rc55.mc.rfapi.fluid.FluidSettings;
import rc55.mc.rfapi.item.ExtendedBucketItem;

import java.util.function.Consumer;

public class RFApiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, list) -> {
            if (stack.getItem() instanceof BucketItem bucket) {
                if (!tooltipContext.isAdvanced()) return;
                if (ExtendedBucketItem.isEmpty(stack)) {
                    if (ExtendedBucketItem.getMaxTemperature(stack) >= 0) {
                        displayTemperature(list::add, "tooltip.reservoir-api.bucket.max_temperature", ExtendedBucketItem.getMaxTemperature(stack));
                    } else {
                        list.add(Text.translatable("tooltip.reservoir-api.bucket.unlimited_max_temperature"));
                    }
                } else {
                    displayTemperature(list::add, "tooltip.reservoir-api.bucket.temperature", FluidSettings.get(bucket.fluid).getTemperature());
                }
            }
        });
    }

    public static void displayTemperature(Consumer<Text> text, String translationKey, int temperature) {
        text.accept(Text.translatable(translationKey, temperature, temperature - 273));
    }
}
