package studio.fantasyit.maid_rpg_task.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.mojang.serialization.MapCodec;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.condition.BaubleCraftableCondition;

public class ConditionRegistry {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MaidRpgTask.MODID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<BaubleCraftableCondition>> BAUBLE_CRAFTABLE =
            CONDITION_CODECS.register("bauble_craftable", () -> BaubleCraftableCondition.CODEC);

    public static void init(IEventBus modEventBus) {
        CONDITION_CODECS.register(modEventBus);
    }
}
