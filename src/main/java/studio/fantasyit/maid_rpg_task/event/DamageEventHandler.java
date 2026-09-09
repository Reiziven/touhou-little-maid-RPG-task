package studio.fantasyit.maid_rpg_task.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.effect.DamageDecreaseEffect;
import studio.fantasyit.maid_rpg_task.effect.DefenceUpEffect;
import studio.fantasyit.maid_rpg_task.effect.StrengthenedEffect;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;

@EventBusSubscriber(modid = MaidRpgTask.MODID)
public class DamageEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        // Note: LivingDamageEvent.Post is after damage is applied in NeoForge 1.21
        // For modification of amount use LivingDamageEvent.Pre
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        float amount = event.getNewDamage();

        // Fragile: +20% damage taken
        if (target.hasEffect(EffectRegistry.FRAGILE)) {
            amount *= 1.2f;
        }

        // Defence Up: -5% damage taken per level
        if (target.hasEffect(EffectRegistry.DEFENCE_UP)) {
            int amp = target.getEffect(EffectRegistry.DEFENCE_UP).getAmplifier();
            amount *= DefenceUpEffect.damageMultiplier(amp);
        }

        // Check attacker effects
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            // Damage Decrease debuff on attacker
            MobEffectInstance ddEffect = attacker.getEffect(EffectRegistry.DAMAGE_DECREASE);
            if (ddEffect != null) {
                amount *= DamageDecreaseEffect.damageMultiplier(ddEffect.getAmplifier());
            }

            // Strengthened buff on attacker
            MobEffectInstance strEffect = attacker.getEffect(EffectRegistry.STRENGTHENED);
            if (strEffect != null) {
                amount = amount * StrengthenedEffect.damageMultiplier(strEffect.getAmplifier()) + 1f;
            }
        }

        event.setNewDamage(amount);
    }
}
