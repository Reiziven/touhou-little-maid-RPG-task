package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Defence Up — a buff applied by the support maid.
 * Each amplifier level reduces incoming damage by 5%:
 *   amplifier 0 (Level I)   = 5% damage reduction
 *   amplifier 3 (Level IV)  = 20% damage reduction
 *
 * Intentionally separate from vanilla Resistance — does NOT contribute to
 * the Resistance V (100%) invincibility cap.
 *
 * Icon: place a 18x18 PNG at
 *   src/main/resources/assets/maid_rpg_task/textures/mob_effect/defence_up.png
 * Copy vanilla's Resistance icon from: assets/minecraft/textures/mob_effect/resistance.png
 *
 * Actual reduction is applied via LivingDamageEvent in DamageEventHandler.
 */
public class DefenceUpEffect extends MobEffect {

    public DefenceUpEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4488FF); // blue tint like Resistance
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    /**
     * Returns the damage multiplier (fraction remaining) for this amplifier.
     * amplifier 0 → 0.95 (5% reduction), amplifier 3 → 0.80 (20% reduction)
     */
    public static float damageMultiplier(int amplifier) {
        float reduction = 0.05f * (amplifier + 1);
        return Math.max(0f, 1f - reduction);
    }
}
