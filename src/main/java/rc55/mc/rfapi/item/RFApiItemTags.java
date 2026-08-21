package rc55.mc.rfapi.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class RFApiItemTags {
    public static final TagKey<Item> BUCKETS = ofConventional("buckets");
    public static final TagKey<Item> BUCKETS_FILLED = ofConventional("buckets/filled");
    public static final TagKey<Item> BUCKETS_EMPTY = ofConventional("buckets/empty");

    public static TagKey<Item> of(Identifier id) {
        return TagKey.of(RegistryKeys.ITEM, id);
    }

    public static TagKey<Item> ofConventional(String id) {
        return of(Identifier.of("c", id));
    }
}
