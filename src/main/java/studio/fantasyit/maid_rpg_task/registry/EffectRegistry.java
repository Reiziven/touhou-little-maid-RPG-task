package studio.fantasyit.maid_rpg_task.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.effect.*;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MaidRpgTask.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> FRAGILE =
            EFFECTS.register("fragile", FragileEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> ARMOR_SHRED =
            EFFECTS.register("armor_shred", ArmorShredEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> DAMAGE_DECREASE =
            EFFECTS.register("damage_decrease", DamageDecreaseEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> REDUCED_HEALING =
            EFFECTS.register("reduced_healing", ReducedHealingEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> STRENGTHENED =
            EFFECTS.register("strengthened", StrengthenedEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> DEFENCE_UP =
            EFFECTS.register("defence_up", DefenceUpEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> LEECH =
            EFFECTS.register("leech", LeechEffect::new);

    public static void init(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
