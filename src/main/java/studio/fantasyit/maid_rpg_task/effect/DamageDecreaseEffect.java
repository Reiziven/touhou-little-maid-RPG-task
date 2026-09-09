package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Damage Decrease — applied by support/master task to enemies targeting the player.
 * Each amplifier level reduces outgoing attack damage by 5%:
 *   amplifier 0 (level I)  =  5% reduction
 *   amplifier 3 (level IV) = 20% reduction
 * The actual reduction is applied in DamageEventHandler via LivingDamageEvent
 * so it can scale with the amplifier at runtime.
 */
public class DamageDecreaseEffect extends MobEffect {

    public DamageDecreaseEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF8C00); // dark orange tint
        // No static attribute modifier — scaling is handled in the event handler
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    /** Returns the damage multiplier for this amplifier: amplifier 3 → 0.80 (20% reduction). */
    public static float damageMultiplier(int amplifier) {
        float reduction = 0.05f * (amplifier + 1);
        return Math.max(0f, 1f - reduction);
    }
}
