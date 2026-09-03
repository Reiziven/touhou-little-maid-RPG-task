package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Leech — a beneficial effect stack stolen from enemies by the DPS maid.
 * Display-only placeholder; actual steal/cleanse logic lives in LootBehavior
 * and the LivingHurtEvent handler.
 */
public class LeechEffect extends MobEffect {

    public LeechEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9B59B6); // purple tint
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
