package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DefenceUpEffect extends MobEffect {

    public DefenceUpEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4488FF);
    }

    public static float damageMultiplier(int amplifier) {
        float reduction = 0.05f * (amplifier + 1);
        return Math.max(0f, 1f - reduction);
    }
}
