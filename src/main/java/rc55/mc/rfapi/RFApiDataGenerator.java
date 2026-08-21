package rc55.mc.rfapi;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import rc55.mc.rfapi.data.gen.internal.DefaultedFluidReactionDataProvider;
import rc55.mc.rfapi.data.gen.internal.tag.RFApiBlockTagProvider;
import rc55.mc.rfapi.data.gen.internal.tag.RFApiFluidTagProvider;
import rc55.mc.rfapi.data.gen.internal.tag.RFApiItemTagProvider;

public class RFApiDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack generator = fabricDataGenerator.createPack();
        generator.addProvider(DefaultedFluidReactionDataProvider::new);
        // Tags
        generator.addProvider(RFApiBlockTagProvider::new);
        generator.addProvider(RFApiItemTagProvider::new);
        generator.addProvider(RFApiFluidTagProvider::new);
	}
}
