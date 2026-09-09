package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Strengthened — a buff applied by the support maid.
 * Each amplifier level adds 5% outgoing damage:
 *   amplifier 0 (Level I)   = +5%
 *   amplifier 3 (Level IV)  = +20%
 *
 * Icon: place a 18x18 PNG at
 *   src/main/resources/assets/maid_rpg_task/textures/mob_effect/strengthened.png
 * Copy vanilla's Strength icon from: assets/minecraft/textures/mob_effect/strength.png
 *
 * Actual damage boost is applied via LivingDamageEvent in DamageEventHandler.
 */
public class StrengthenedEffect extends MobEffect {

    public StrengthenedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF4400); // orange-red tint like Strength
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    /**
     * Returns the outgoing damage multiplier for this amplifier.
     * amplifier 0 → 1.05 (+5%), amplifier 3 → 1.20 (+20%)
     * A flat +1 bonus is added on top of this in DamageEventHandler.
     */
    public static float damageMultiplier(int amplifier) {
        return 1f + 0.05f * (amplifier + 1);
    }
}
