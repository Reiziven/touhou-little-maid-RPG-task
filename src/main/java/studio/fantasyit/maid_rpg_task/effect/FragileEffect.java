package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fragile — applied by ice spells.
 * Display-only effect; actual +20% damage taken is handled via LivingAttackEvent.
 */
public class FragileEffect extends MobEffect {

    public FragileEffect() {
        super(MobEffectCategory.HARMFUL, 0x99DDFF); // light-blue tint
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // No per-tick logic; damage amplification is handled in the event handler
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
