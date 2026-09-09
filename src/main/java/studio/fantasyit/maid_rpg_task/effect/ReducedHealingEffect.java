package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Reduced Healing — applied on hit by Ice Spike.
 * Each level reduces incoming healing by 5%:
 *   Level I   (amplifier 0) =  5% reduction
 *   Level II  (amplifier 1) = 10% reduction
 *   Level VIII (amplifier 7) = 40% reduction
 * Applied via LivingHealEvent in MaidEventHandler.
 */
public class ReducedHealingEffect extends MobEffect {

    public ReducedHealingEffect() {
        super(MobEffectCategory.HARMFUL, 0x4466AA); // dark blue tint
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    /**
     * Returns the fraction of healing to keep.
     * amplifier 0 (Level I) → 95% kept (5% reduction)
     * amplifier 7 (Level VIII) → 60% kept (40% reduction)
     */
    public static float healMultiplier(int amplifier) {
        float reduction = 0.05f * (amplifier + 1);
        return Math.max(0f, 1f - reduction);
    }
}
