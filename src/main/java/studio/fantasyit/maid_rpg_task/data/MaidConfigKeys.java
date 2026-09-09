package studio.fantasyit.maid_rpg_task.data;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MaidConfigKeys {
    record KeyAndDefSupp<T>(TaskDataKey<T> key, Supplier<T> defaultValue) {}

    public static Map<ResourceLocation, KeyAndDefSupp<?>> keys = new HashMap<>();

    public static <T> void addKey(ResourceLocation key, TaskDataKey<T> dataKey, Supplier<T> defaultValue) {
        keys.put(key, new KeyAndDefSupp<>(dataKey, defaultValue));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(EntityMaid maid, ResourceLocation key) {
        KeyAndDefSupp<T> pair = (KeyAndDefSupp<T>) keys.get(key);
        return maid.getOrCreateData(pair.key(), pair.defaultValue().get());
    }
}
