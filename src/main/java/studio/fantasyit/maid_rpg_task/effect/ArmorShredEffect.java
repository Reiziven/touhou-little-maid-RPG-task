package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * Armor Shred — applied by earth spikes.
 * Reduces armor by 50% for the duration. No particles.
 * Minecraft automatically removes the attribute modifier when the effect expires.
 */
public class ArmorShredEffect extends MobEffect {

    public static final UUID ARMOR_SHRED_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    public ArmorShredEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513); // brown tint
        this.addAttributeModifier(Attributes.ARMOR, ARMOR_SHRED_UUID.toString(),
                -0.50, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
