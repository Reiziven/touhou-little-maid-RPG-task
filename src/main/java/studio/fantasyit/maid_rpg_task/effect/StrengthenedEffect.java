package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class StrengthenedEffect extends MobEffect {

    public StrengthenedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF4400);
    }

    public static float damageMultiplier(int amplifier) {
        return 1f + 0.05f * (amplifier + 1);
    }
}
