package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ReducedHealingEffect extends MobEffect {

    public ReducedHealingEffect() {
        super(MobEffectCategory.HARMFUL, 0x4466AA);
    }

    public static float healMultiplier(int amplifier) {
        float reduction = 0.05f * (amplifier + 1);
        return Math.max(0f, 1f - reduction);
    }
}
