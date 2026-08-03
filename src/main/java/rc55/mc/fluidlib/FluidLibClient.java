package rc55.mc.fluidlib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.BucketItem;
import net.minecraft.text.Text;
import rc55.mc.fluidlib.client.FluidRenderRegistry;
import rc55.mc.fluidlib.fluid.FluidRegistry;
import rc55.mc.fluidlib.fluid.FluidSettings;
import rc55.mc.fluidlib.item.ExtendedBucketItem;

import java.util.function.Consumer;

public class FluidLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, list) -> {
            if (stack.getItem() instanceof BucketItem bucket) {
                if (ExtendedBucketItem.isEmpty(stack)) {
                    //displayTemperature(list::add, "tooltip.fluidlib.bucket.max_temperature", );
                } else {
                    displayTemperature(list::add, "tooltip.fluidlib.bucket.temperature", FluidSettings.get(bucket.fluid).getTemperature());
                }
            }
        });
    }

    public static void displayTemperature(Consumer<Text> text, String translationKey, int temperature) {
        text.accept(Text.translatable(translationKey, temperature, temperature - 273));
    }
}
