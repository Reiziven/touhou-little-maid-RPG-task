package studio.fantasyit.maid_rpg_task.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

/**
 * Armor Shred — applied by earth spikes.
 * Reduces armor by 50% for the duration.
 */
public class ArmorShredEffect extends MobEffect {

    public ArmorShredEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513);
        this.addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "armor_shred_debuff"),
                -0.50,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
