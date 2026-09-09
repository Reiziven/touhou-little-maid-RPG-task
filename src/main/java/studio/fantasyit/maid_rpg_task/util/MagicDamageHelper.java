package studio.fantasyit.maid_rpg_task.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;

public class MagicDamageHelper {

    private static final ResourceLocation SPELL_ARMOR_BYPASS_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "spell_armor_bypass");

    public static void dealMagicDamage(LivingEntity target, DamageSource src, float amount) {
        MobEffectInstance resistance = target.getEffect(MobEffects.DAMAGE_RESISTANCE);
        int savedAmplifier = -1;
        if (resistance != null) {
            savedAmplifier = resistance.getAmplifier();
            target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        }

        DamageSource effectiveSrc = Config.mageBypassTotem
                ? target.level().damageSources().fellOutOfWorld()
                : src;

        AttributeInstance armorAttr = target.getAttribute(Attributes.ARMOR);
        boolean addedBypass = false;
        if (armorAttr != null && armorAttr.getModifier(SPELL_ARMOR_BYPASS_ID) == null) {
            armorAttr.addTransientModifier(new AttributeModifier(
                    SPELL_ARMOR_BYPASS_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            addedBypass = true;
        }

        target.hurt(effectiveSrc, amount);

        if (addedBypass && armorAttr != null) armorAttr.removeModifier(SPELL_ARMOR_BYPASS_ID);

        if (savedAmplifier >= 0) {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    resistance.getDuration(), savedAmplifier, resistance.isAmbient(),
                    resistance.isVisible(), resistance.showIcon()));
        }
    }

    public static void applyEarthArmorDebuff(LivingEntity target) {
        target.addEffect(new MobEffectInstance(
                EffectRegistry.ARMOR_SHRED, 1200, 0, false, false, true));
    }

    public static void removeEarthArmorDebuff(LivingEntity target) {
        target.removeEffect(EffectRegistry.ARMOR_SHRED);
    }

    public static void applyVulnerability(LivingEntity target) {
        target.addEffect(new MobEffectInstance(
                EffectRegistry.FRAGILE, 1200, 0, false, true, true));
    }

    public static void removeVulnerability(LivingEntity target) {
        target.removeEffect(EffectRegistry.FRAGILE);
    }
}
