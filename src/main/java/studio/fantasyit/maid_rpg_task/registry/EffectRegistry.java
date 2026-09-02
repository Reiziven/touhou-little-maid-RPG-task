package studio.fantasyit.maid_rpg_task.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.effect.ArmorShredEffect;
import studio.fantasyit.maid_rpg_task.effect.DamageDecreaseEffect;
import studio.fantasyit.maid_rpg_task.effect.DefenceUpEffect;
import studio.fantasyit.maid_rpg_task.effect.FragileEffect;
import studio.fantasyit.maid_rpg_task.effect.LeechEffect;
import studio.fantasyit.maid_rpg_task.effect.ReducedHealingEffect;
import studio.fantasyit.maid_rpg_task.effect.StrengthenedEffect;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MaidRpgTask.MODID);

    public static final RegistryObject<MobEffect> FRAGILE =
            EFFECTS.register("fragile", FragileEffect::new);

    public static final RegistryObject<MobEffect> ARMOR_SHRED =
            EFFECTS.register("armor_shred", ArmorShredEffect::new);

    public static final RegistryObject<MobEffect> DAMAGE_DECREASE =
            EFFECTS.register("damage_decrease", DamageDecreaseEffect::new);

    public static final RegistryObject<MobEffect> REDUCED_HEALING =
            EFFECTS.register("reduced_healing", ReducedHealingEffect::new);

    public static final RegistryObject<MobEffect> STRENGTHENED =
            EFFECTS.register("strengthened", StrengthenedEffect::new);

    public static final RegistryObject<MobEffect> DEFENCE_UP =
            EFFECTS.register("defence_up", DefenceUpEffect::new);

    public static final RegistryObject<MobEffect> LEECH =
            EFFECTS.register("leech", LeechEffect::new);

    public static void init(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
