package studio.fantasyit.maid_rpg_task.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;

import java.util.UUID;

/**
 * Utility for applying elemental magic damage that bypasses resistance effects
 * and reduces effective armor by 50% (for spell hits) or a custom percentage.
 */
public class MagicDamageHelper {

    // Armor reduction UUID for spell impacts (50% armor bypass)
    private static final UUID SPELL_ARMOR_BYPASS_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    /**
     * Deal magic damage that bypasses resistance effects and reduces effective armor by 50%.
     * Used by direct spell impacts (tornado, earth, fire, ice).
     *
     * When {@code Config.mageBypassTotem} is true, uses {@code out_of_world} as the damage source,
     * which bypasses invulnerability, resistance, and totem-of-undying saves (including modded undying).
     * Side effect: also bypasses creative mode protection.
     */
    public static void dealMagicDamage(LivingEntity target, DamageSource src, float amount) {
        // Strip resistance for this hit
        MobEffectInstance resistance = target.getEffect(MobEffects.DAMAGE_RESISTANCE);
        int savedAmplifier = -1;
        if (resistance != null) {
            savedAmplifier = resistance.getAmplifier();
            target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        }

        // When totem bypass is on, use out_of_world — it has bypasses_invulnerability +
        // bypasses_resistance tags in vanilla, killing through totems and modded undying abilities.
        DamageSource effectiveSrc = Config.mageBypassTotem
                ? target.level().damageSources().fellOutOfWorld()
                : src;

        // Temporarily reduce armor by 50%
        AttributeInstance armorAttr = target.getAttribute(Attributes.ARMOR);
        boolean addedBypass = false;
        if (armorAttr != null && armorAttr.getModifier(SPELL_ARMOR_BYPASS_ID) == null) {
            armorAttr.addTransientModifier(new AttributeModifier(
                    SPELL_ARMOR_BYPASS_ID, "spell_armor_bypass",
                    -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            addedBypass = true;
        }

        target.hurt(effectiveSrc, amount);

        // Restore armor modifier
        if (addedBypass && armorAttr != null) {
            armorAttr.removeModifier(SPELL_ARMOR_BYPASS_ID);
        }

        // Restore resistance
        if (savedAmplifier >= 0) {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    resistance.getDuration(), savedAmplifier, resistance.isAmbient(),
                    resistance.isVisible(), resistance.showIcon()));
        }
    }

    /**
     * Apply Armor Shred effect: -50% armor for 60 seconds (1200 ticks).
     * The effect carries the attribute modifier — auto-removed on expiry.
     * No particles shown.
     */
    public static void applyEarthArmorDebuff(LivingEntity target) {
        target.addEffect(new MobEffectInstance(
                EffectRegistry.ARMOR_SHRED.get(),
                1200, // 60 seconds
                0,
                false, false, true)); // no ambient, no particles, show icon
    }

    /**
     * Remove earth armor shred effect.
     */
    public static void removeEarthArmorDebuff(LivingEntity target) {
        target.removeEffect(EffectRegistry.ARMOR_SHRED.get());
    }

    /**
     * Apply Fragile effect: target takes 20% more damage for 60 seconds (1200 ticks).
     */
    public static void applyVulnerability(LivingEntity target) {
        target.addEffect(new MobEffectInstance(
                EffectRegistry.FRAGILE.get(),
                1200, // 60 seconds
                0,
                false, true, true));
    }

    public static void removeVulnerability(LivingEntity target) {
        target.removeEffect(EffectRegistry.FRAGILE.get());
    }
}
