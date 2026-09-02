package studio.fantasyit.maid_rpg_task.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.effect.DamageDecreaseEffect;
import studio.fantasyit.maid_rpg_task.effect.DefenceUpEffect;
import studio.fantasyit.maid_rpg_task.effect.StrengthenedEffect;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;

@Mod.EventBusSubscriber(modid = MaidRpgTask.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        float amount = event.getAmount();

        // Fragile: +20% damage taken
        if (target.hasEffect(EffectRegistry.FRAGILE.get())) {
            amount *= 1.2f;
        }

        // Defence Up: -5% damage taken per level (multiplicative, separate from Resistance)
        // With Resistance IV (80% reduction) + Defence Up IV (20% reduction):
        //   entity takes 0.20 * 0.80 = 16% of original damage (84% total reduction, not invincible)
        if (target.hasEffect(EffectRegistry.DEFENCE_UP.get())) {
            int amp = target.getEffect(EffectRegistry.DEFENCE_UP.get()).getAmplifier();
            amount *= DefenceUpEffect.damageMultiplier(amp);
        }

        event.setAmount(amount);
    }

    /**
     * Damage Decrease and Strengthened are debuffs/buffs on the ATTACKER.
     * We check the attacker's effects to scale outgoing damage.
     */
    @SubscribeEvent
    public static void onAttackerDamageDecrease(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            // Damage Decrease debuff — reduces attacker's outgoing damage
            MobEffectInstance ddEffect = attacker.getEffect(EffectRegistry.DAMAGE_DECREASE.get());
            if (ddEffect != null) {
                event.setAmount(event.getAmount() * DamageDecreaseEffect.damageMultiplier(ddEffect.getAmplifier()));
            }

            // Strengthened buff — increases attacker's outgoing damage by 5% per level + 1 flat
            MobEffectInstance strEffect = attacker.getEffect(EffectRegistry.STRENGTHENED.get());
            if (strEffect != null) {
                event.setAmount(event.getAmount() * StrengthenedEffect.damageMultiplier(strEffect.getAmplifier()) + 1f);
            }
        }
    }
}
