package studio.fantasyit.maid_rpg_task;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@EventBusSubscriber(modid = MaidRpgTask.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_REVIVE;
    public static final ModConfigSpec.BooleanValue ENABLE_REVIVE_AGGRO;
    public static final ModConfigSpec.BooleanValue ENABLE_REVIVE_TOTEM;
    public static final ModConfigSpec.BooleanValue ENABLE_REVIVE_COOLDOWN;
    public static final ModConfigSpec.IntValue SUPPORT_REVIVE_COOLDOWN;

    public static final ModConfigSpec.BooleanValue ENABLE_SUPPORT_TASK;
    public static final ModConfigSpec.BooleanValue ENABLE_MASTER;
    public static final ModConfigSpec.BooleanValue ENABLE_FEED_TASK;

    public static final ModConfigSpec.BooleanValue SURVIVAL_BALANCED;

    public static final ModConfigSpec.DoubleValue ALLY_DAMAGE_TAKEN;
    public static final ModConfigSpec.DoubleValue TANK_ABSORBS;
    public static final ModConfigSpec.DoubleValue TANK_DIRECT_REDUCTION;
    public static final ModConfigSpec.IntValue TANK_STUN_DAMAGE_DECREASE_LEVEL;
    public static final ModConfigSpec.IntValue TANK_STUN_COOLDOWN;
    public static final ModConfigSpec.IntValue TANK_STUN_BALANCED_EXTRA_COOLDOWN;

    public static final ModConfigSpec.IntValue SUPPORT_STRENGTH_LEVEL;
    public static final ModConfigSpec.IntValue SUPPORT_DEFENCE_UP_LEVEL;
    public static final ModConfigSpec.IntValue SUPPORT_DAMAGE_DECREASE_LEVEL;
    public static final ModConfigSpec.BooleanValue SUPPORT_ENABLE_HEALING;
    public static final ModConfigSpec.DoubleValue SUPPORT_HEAL_THRESHOLD;
    public static final ModConfigSpec.DoubleValue SUPPORT_HEAL_AMOUNT;
    public static final ModConfigSpec.IntValue SUPPORT_HEAL_COOLDOWN;
    public static final ModConfigSpec.ConfigValue<String> SUPPORT_HEAL_STYLE;
    public static final ModConfigSpec.BooleanValue SUPPORT_HEAL_REQUIRES_XP;
    public static final ModConfigSpec.BooleanValue SUPPORT_DAMAGE_DECREASE_REQUIRES_XP;
    public static final ModConfigSpec.BooleanValue SUPPORT_BUFF_REQUIRES_XP;
    public static final ModConfigSpec.IntValue SUPPORT_XP_COST;
    public static final ModConfigSpec.BooleanValue SUPPORT_HEAL_OUTSIDE_COMBAT;
    public static final ModConfigSpec.BooleanValue SUPPORT_FLAT_HEAL;
    public static final ModConfigSpec.DoubleValue SUPPORT_LARGE_ENTITY_HEALTH_THRESHOLD;
    public static final ModConfigSpec.DoubleValue SUPPORT_LARGE_ENTITY_HEAL_AMOUNT;

    public static final ModConfigSpec.BooleanValue MASTER_ENABLE_HEALING;
    public static final ModConfigSpec.DoubleValue MASTER_HEAL_THRESHOLD;
    public static final ModConfigSpec.DoubleValue MASTER_HEAL_AMOUNT;
    public static final ModConfigSpec.IntValue MASTER_HEAL_COOLDOWN;

    public static final ModConfigSpec.IntValue MAGE_SPELL_COOLDOWN_MIN;
    public static final ModConfigSpec.IntValue MAGE_SPELL_COOLDOWN_MAX;
    public static final ModConfigSpec.IntValue ICE_SPIKE_REDUCED_HEALING_DURATION;
    public static final ModConfigSpec.IntValue ICE_SPIKE_REDUCED_HEALING_LEVEL;
    public static final ModConfigSpec.BooleanValue MAGE_BYPASS_TOTEM;

    public static final ModConfigSpec.DoubleValue DPS_BASE_ATTACK_BOOST;
    public static final ModConfigSpec.DoubleValue DPS_OFFHAND_ATTACK_BOOST;
    public static final ModConfigSpec.DoubleValue DPS_ATTACK_SPEED_BOOST;
    public static final ModConfigSpec.BooleanValue DPS_LEECH_ENABLED;
    public static final ModConfigSpec.IntValue DPS_LEECH_MAX_STACKS;
    public static final ModConfigSpec.IntValue DPS_LEECH_DURATION_TICKS;
    public static final ModConfigSpec.BooleanValue DPS_LEECH_STEAL_WHITELIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DPS_LEECH_STEAL_LIST;
    public static final ModConfigSpec.BooleanValue DPS_LEECH_CLEANSE_WHITELIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DPS_LEECH_CLEANSE_LIST;

    public static final ModConfigSpec.BooleanValue DPS_HEALTH_REDUCTION;
    public static final ModConfigSpec.BooleanValue TANK_ATTACK_PENALTY;
    public static final ModConfigSpec.BooleanValue MAGE_HEALTH_REDUCTION;
    public static final ModConfigSpec.BooleanValue SUPPORT_CAN_ATTACK;

    public static final ModConfigSpec.BooleanValue MASTER_SOUL_SPELL_CRAFTABLE;
    public static final ModConfigSpec.BooleanValue MASTER_SOUL_SPELL_ALT_CRAFTABLE;

    static {
        BUILDER.push("revive");
        ENABLE_REVIVE = BUILDER.define("enable", true);
        ENABLE_REVIVE_AGGRO = BUILDER
                .comment("When true, nearby hostile mobs will aggro and target the maid while she is reviving a bleeding player.")
                .define("aggro", true);
        ENABLE_REVIVE_TOTEM = BUILDER
                .comment("When true, the maid can use a Totem of Undying placed in her bauble inventory to instantly revive the owner player.")
                .define("totem", true);
        ENABLE_REVIVE_COOLDOWN = BUILDER
                .comment("When true, the revive ability is limited by resources.")
                .define("cooldown_enabled", true);
        SUPPORT_REVIVE_COOLDOWN = BUILDER
                .comment("Cooldown in ticks before a support maid can revive again after a successful revive. Default: 12000 (10 minutes)")
                .defineInRange("support_cooldown", 12000, 20, 144000);
        BUILDER.pop();

        BUILDER.push("functions");
        ENABLE_SUPPORT_TASK = BUILDER
                .comment("When true, the Support task is available for maids to select. Default: true")
                .define("support", true);
        ENABLE_MASTER = BUILDER.define("master", false);
        ENABLE_FEED_TASK = BUILDER
                .comment("When true, Support and Master maids can feed food to their owner when hungry or hurt,\n"
                       + "reusing Touhou Little Maid's own feed-owner logic. The maid will not feed the owner while\n"
                       + "they are in a fallen/bleeding-out state from Player Revive. Default: true")
                .define("feed_owner", true);
        BUILDER.pop();

        BUILDER.push("balance");
        SURVIVAL_BALANCED = BUILDER
                .comment("Quick preset toggle. When true, overwrites all tuning fields below with balanced values at load time.")
                .define("survival_balanced", true);
        BUILDER.pop();

        BUILDER.push("tank");
        ALLY_DAMAGE_TAKEN = BUILDER
                .comment("Fraction of a hit that allies receive when a tank maid is nearby. Default: 0.5")
                .defineInRange("ally_damage_taken", 0.5, 0.0, 1.0);
        TANK_ABSORBS = BUILDER
                .comment("Fraction of the original ally hit that is redirected to the tank. Default: 0.5")
                .defineInRange("tank_absorbs", 0.5, 0.0, 2.0);
        TANK_DIRECT_REDUCTION = BUILDER
                .comment("Fraction by which the tank's OWN incoming damage is reduced. Default: 0.5")
                .defineInRange("tank_direct_reduction", 0.5, 0.0, 1.0);
        TANK_STUN_DAMAGE_DECREASE_LEVEL = BUILDER
                .comment("Amplifier of the Damage Decrease effect applied to enemies during tank stun. Default: 11")
                .defineInRange("stun_damage_decrease_level", 11, 0, 19);
        TANK_STUN_COOLDOWN = BUILDER
                .comment("Cooldown in ticks between stun ability uses. Default: 200")
                .defineInRange("stun_cooldown", 200, 20, 72000);
        TANK_STUN_BALANCED_EXTRA_COOLDOWN = BUILDER
                .comment("Extra ticks added to the stun cooldown when survival_balanced=true. Default: 20")
                .defineInRange("stun_balanced_extra_cooldown", 20, 0, 72000);
        BUILDER.pop();

        BUILDER.push("support");
        SUPPORT_STRENGTH_LEVEL = BUILDER
                .comment("Strengthened amplifier for allies in combat. Default: 6")
                .defineInRange("strengthened_level", 6, 0, 19);
        SUPPORT_DEFENCE_UP_LEVEL = BUILDER
                .comment("Defence Up amplifier for allies in combat. Default: 8")
                .defineInRange("defence_up_level", 8, 0, 19);
        SUPPORT_DAMAGE_DECREASE_LEVEL = BUILDER
                .comment("Damage Decrease amplifier applied to enemies. Default: 3")
                .defineInRange("damage_decrease_level", 3, 0, 19);
        SUPPORT_ENABLE_HEALING = BUILDER
                .comment("When true, the support maid can cast healing circles on injured allies.")
                .define("enable_healing", true);
        SUPPORT_HEAL_THRESHOLD = BUILDER
                .comment("HP percentage below which an ally triggers a healing circle. Default: 0.5")
                .defineInRange("heal_threshold", 0.5, 0.01, 1.0);
        SUPPORT_HEAL_AMOUNT = BUILDER
                .comment("Percentage of max health restored by the healing circle. Default: 0.25")
                .defineInRange("heal_amount", 0.25, 0.01, 1.0);
        SUPPORT_HEAL_COOLDOWN = BUILDER
                .comment("Cooldown in ticks between healing circle casts per target. Default: 600")
                .defineInRange("heal_cooldown", 600, 20, 72000);
        SUPPORT_HEAL_STYLE = BUILDER
                .comment("Visual style of the healing circle. Valid: spiral, end_rod, entity_effect, cycle")
                .define("heal_style", "cycle",
                        o -> o instanceof String s && java.util.List.of("spiral", "end_rod", "entity_effect", "cycle").contains(s));
        SUPPORT_HEAL_REQUIRES_XP = BUILDER
                .comment("When true, the support maid must have XP to cast healing circles. Default: true")
                .define("heal_requires_xp", true);
        SUPPORT_DAMAGE_DECREASE_REQUIRES_XP = BUILDER
                .comment("When true, the support/master maid must have XP to apply the Damage Decrease debuff. Default: false")
                .define("damage_decrease_requires_xp", false);
        SUPPORT_BUFF_REQUIRES_XP = BUILDER
                .comment("When true, the support/master maid must have XP to apply ally buffs. Default: true")
                .define("buff_requires_xp", true);
        SUPPORT_XP_COST = BUILDER
                .comment("Amount of XP drained per tick (every 20 ticks) when in combat. Default: 1")
                .defineInRange("xp_cost", 1, 1, 100);
        SUPPORT_HEAL_OUTSIDE_COMBAT = BUILDER
                .comment("When true, healing circles can trigger outside combat. Default: false")
                .define("heal_outside_combat", false);
        SUPPORT_FLAT_HEAL = BUILDER
                .comment("When true, heal_amount / large_entity_heal_amount / master heal_amount are treated as flat HP values instead of percentages. Default: false")
                .define("flat_heal", false);
        SUPPORT_LARGE_ENTITY_HEALTH_THRESHOLD = BUILDER
                .comment("Max-health threshold above which the reduced heal amount is used instead of heal_amount. Default: 60.0")
                .defineInRange("large_entity_health_threshold", 60.0, 1.0, 10000.0);
        SUPPORT_LARGE_ENTITY_HEAL_AMOUNT = BUILDER
                .comment("Percentage of max health restored for entities at or above large_entity_health_threshold. Default: 0.20")
                .defineInRange("large_entity_heal_amount", 0.20, 0.01, 1.0);
        BUILDER.pop();

        BUILDER.push("master");
        MASTER_ENABLE_HEALING = BUILDER
                .comment("When true, the master maid self-heals via a healing circle when injured.")
                .define("enable_healing", true);
        MASTER_HEAL_THRESHOLD = BUILDER
                .comment("HP percentage below which the master maid triggers self-heal. Default: 0.5")
                .defineInRange("heal_threshold", 0.5, 0.01, 1.0);
        MASTER_HEAL_AMOUNT = BUILDER
                .comment("Percentage of max health restored per self-heal. Default: 0.20")
                .defineInRange("heal_amount", 0.20, 0.01, 1.0);
        MASTER_HEAL_COOLDOWN = BUILDER
                .comment("Cooldown in ticks between self-heal casts. Default: 400")
                .defineInRange("heal_cooldown", 400, 20, 72000);
        BUILDER.pop();

        BUILDER.push("mage");
        MAGE_SPELL_COOLDOWN_MIN = BUILDER
                .comment("Minimum ticks between elemental spell casts. Default: 400")
                .defineInRange("spell_cooldown_min", 400, 20, 72000);
        MAGE_SPELL_COOLDOWN_MAX = BUILDER
                .comment("Maximum ticks between elemental spell casts. Default: 520")
                .defineInRange("spell_cooldown_max", 520, 20, 72000);
        ICE_SPIKE_REDUCED_HEALING_DURATION = BUILDER
                .comment("Duration in ticks of the Reduced Healing effect applied by Ice Spike. Default: 1000")
                .defineInRange("ice_spike_reduced_healing_duration", 1000, 0, 72000);
        ICE_SPIKE_REDUCED_HEALING_LEVEL = BUILDER
                .comment("Amplifier of the Reduced Healing effect applied by Ice Spike. Default: 8")
                .defineInRange("ice_spike_reduced_healing_level", 8, 0, 20);
        MAGE_BYPASS_TOTEM = BUILDER
                .comment("When true, elemental spells bypass Totem of Undying. Default: false")
                .define("bypass_totem", false);
        BUILDER.pop();

        BUILDER.push("dps");
        DPS_BASE_ATTACK_BOOST = BUILDER
                .comment("Fraction added to the DPS maid's attack damage via MULTIPLY_TOTAL. Default: 0.35")
                .defineInRange("base_attack_boost", 0.35, 0.0, 10.0);
        DPS_OFFHAND_ATTACK_BOOST = BUILDER
                .comment("Additional MULTIPLY_TOTAL bonus when the DPS maid wields a sword or axe in the offhand. Default: 0.15")
                .defineInRange("offhand_attack_boost", 0.15, 0.0, 10.0);
        DPS_ATTACK_SPEED_BOOST = BUILDER
                .comment("Fraction added to the DPS maid's attack speed via MULTIPLY_TOTAL. Default: 0.30")
                .defineInRange("attack_speed_boost", 0.30, 0.0, 10.0);
        DPS_LEECH_ENABLED = BUILDER
                .comment("When true, the DPS maid has a 5% chance on each hit to steal one beneficial effect.")
                .define("leech_enabled", true);
        DPS_LEECH_MAX_STACKS = BUILDER
                .comment("Maximum number of stolen effects the maid can hold at once. Default: 5")
                .defineInRange("leech_max_stacks", 5, 1, 20);
        DPS_LEECH_DURATION_TICKS = BUILDER
                .comment("Duration in ticks that a stolen effect lasts on the maid. Default: 6000")
                .defineInRange("leech_duration_ticks", 6000, 20, 144000);
        DPS_LEECH_STEAL_WHITELIST_MODE = BUILDER
                .comment("When true, only effects in dps.leech_steal_list can be stolen.")
                .define("leech_steal_whitelist_mode", false);
        DPS_LEECH_STEAL_LIST = BUILDER
                .comment("Effect resource locations for steal blacklist.")
                .defineListAllowEmpty("leech_steal_list",
                        java.util.List.of("minecraft:fire_resistance"),
                        o -> o instanceof String);
        DPS_LEECH_CLEANSE_WHITELIST_MODE = BUILDER
                .comment("When true, only effects in dps.leech_cleanse_list are removed from the target.")
                .define("leech_cleanse_whitelist_mode", false);
        DPS_LEECH_CLEANSE_LIST = BUILDER
                .comment("Effect resource locations for cleanse blacklist.")
                .defineListAllowEmpty("leech_cleanse_list",
                        java.util.List.of(),
                        o -> o instanceof String);
        BUILDER.pop();

        BUILDER.push("downsides");
        DPS_HEALTH_REDUCTION = BUILDER
                .comment("When true, DPS maid has reduced max health. Default: true")
                .define("dps_health_reduction", true);
        TANK_ATTACK_PENALTY = BUILDER
                .comment("When true, Tank maid attacks slower and deals less damage. Default: true")
                .define("tank_attack_penalty", true);
        MAGE_HEALTH_REDUCTION = BUILDER
                .comment("When true, Mage maid has reduced max health. Default: true")
                .define("mage_health_reduction", true);
        SUPPORT_CAN_ATTACK = BUILDER
                .comment("When true, Support maid can attack enemies. Default: false")
                .define("support_can_attack", false);
        BUILDER.pop();

        BUILDER.push("bauble");
        MASTER_SOUL_SPELL_CRAFTABLE = BUILDER
                .comment("If true, the Master Soul Spell bauble can be crafted at the altar in survival.")
                .define("master_soul_spell_craftable", true);
        MASTER_SOUL_SPELL_ALT_CRAFTABLE = BUILDER
                .comment("If true, the alternate Master Soul Spell altar recipe is enabled.")
                .define("master_soul_spell_alt_craftable", true);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    // Runtime fields
    public static boolean enableReviveTask = true;
    public static boolean enableReviveAggro = true;
    public static boolean enableReviveTotem = true;
    public static boolean enableReviveCooldown = true;
    public static int supportReviveCooldown = 12000;
    public static boolean enableMasterTask = false;
    public static boolean enableSupportTask = true;
    public static boolean enableFeedTask = true;
    public static boolean survivalBalanced = true;

    public static double allyDamageTaken = 0.5;
    public static double tankAbsorbs = 0.5;
    public static double tankDirectReduction = 0.5;
    public static int tankStunDamageDecreaseLevel = 11;

    public static int supportStrengthLevel = 1;
    public static int supportDamageDecreaseLevel = 3;
    public static int supportStrengthenedLevel = 6;
    public static int supportDefenceUpLevel = 8;

    public static boolean supportEnableHealing = true;
    public static double supportHealThreshold = 0.5;
    public static double supportHealAmount = 0.30;
    public static int supportHealCooldown = 260;
    public static String supportHealStyle = "spiral";
    public static boolean supportHealRequiresXp = true;
    public static boolean supportDamageDecreaseRequiresXp = false;
    public static boolean supportBuffRequiresXp = true;
    public static int supportXpCost = 1;
    public static boolean supportHealOutsideCombat = false;
    public static boolean supportFlatHeal = false;
    public static double supportLargeEntityHealthThreshold = 60.0;
    public static double supportLargeEntityHealAmount = 0.20;

    public static boolean masterEnableHealing = true;
    public static double masterHealThreshold = 0.5;
    public static double masterHealAmount = 0.20;
    public static int masterHealCooldown = 400;

    public static int mageSpellCooldownMin = 400;
    public static int mageSpellCooldownMax = 520;

    public static boolean dpsHealthReduction = true;
    public static boolean tankAttackPenalty = true;
    public static boolean mageHealthReduction = true;
    public static boolean supportCanAttack = false;

    public static boolean mageBypassTotem = false;

    public static int iceSpikeReducedHealingDuration = 1000;
    public static int iceSpikeReducedHealingLevel = 8;

    public static double dpsBaseAttackBoost = 0.35;
    public static double dpsOffhandAttackBoost = 0.15;
    public static double dpsAttackSpeedBoost = 0.30;

    public static int tankStunCooldown = 200;
    public static int tankStunBalancedExtraCooldown = 20;

    public static boolean dpsLeechEnabled = true;
    public static int dpsLeechMaxStacks = 5;
    public static int dpsLeechDurationTicks = 6000;
    public static boolean dpsLeechStealWhitelistMode = false;
    public static List<String> dpsLeechStealList = java.util.List.of("minecraft:fire_resistance");
    public static boolean dpsLeechCleanseWhitelistMode = false;
    public static List<String> dpsLeechCleanseList = java.util.List.of();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableReviveTask = ENABLE_REVIVE.get();
        enableReviveAggro = ENABLE_REVIVE_AGGRO.get();
        enableReviveTotem = ENABLE_REVIVE_TOTEM.get();
        enableReviveCooldown = ENABLE_REVIVE_COOLDOWN.get();
        supportReviveCooldown = SUPPORT_REVIVE_COOLDOWN.get();
        enableMasterTask = ENABLE_MASTER.get();
        enableSupportTask = ENABLE_SUPPORT_TASK.get();
        enableFeedTask = ENABLE_FEED_TASK.get();
        survivalBalanced = SURVIVAL_BALANCED.get();

        if (survivalBalanced) {
            allyDamageTaken = 0.8;
            tankAbsorbs = 0.2;
            tankDirectReduction = 0.2;
            supportStrengthLevel = 0;
            supportStrengthenedLevel = 1;
            supportDefenceUpLevel = 2;
            mageSpellCooldownMin = 400;
            mageSpellCooldownMax = 520;
            supportDamageDecreaseLevel = SUPPORT_DAMAGE_DECREASE_LEVEL.get();
            enableReviveAggro = true;
        } else {
            allyDamageTaken = ALLY_DAMAGE_TAKEN.get();
            tankAbsorbs = TANK_ABSORBS.get();
            tankDirectReduction = TANK_DIRECT_REDUCTION.get();
            supportStrengthLevel = SUPPORT_STRENGTH_LEVEL.get();
            supportStrengthenedLevel = SUPPORT_STRENGTH_LEVEL.get();
            supportDefenceUpLevel = SUPPORT_DEFENCE_UP_LEVEL.get();
            supportDamageDecreaseLevel = SUPPORT_DAMAGE_DECREASE_LEVEL.get();
            mageSpellCooldownMin = MAGE_SPELL_COOLDOWN_MIN.get();
            mageSpellCooldownMax = Math.max(MAGE_SPELL_COOLDOWN_MAX.get(), mageSpellCooldownMin);
        }
        dpsHealthReduction = DPS_HEALTH_REDUCTION.get();
        tankAttackPenalty = TANK_ATTACK_PENALTY.get();
        tankStunDamageDecreaseLevel = TANK_STUN_DAMAGE_DECREASE_LEVEL.get();
        tankStunCooldown = TANK_STUN_COOLDOWN.get();
        tankStunBalancedExtraCooldown = TANK_STUN_BALANCED_EXTRA_COOLDOWN.get();
        dpsBaseAttackBoost = DPS_BASE_ATTACK_BOOST.get();
        dpsOffhandAttackBoost = DPS_OFFHAND_ATTACK_BOOST.get();
        dpsAttackSpeedBoost = DPS_ATTACK_SPEED_BOOST.get();
        mageHealthReduction = MAGE_HEALTH_REDUCTION.get();
        supportCanAttack = SUPPORT_CAN_ATTACK.get();
        mageBypassTotem = MAGE_BYPASS_TOTEM.get();
        iceSpikeReducedHealingDuration = ICE_SPIKE_REDUCED_HEALING_DURATION.get();
        iceSpikeReducedHealingLevel = ICE_SPIKE_REDUCED_HEALING_LEVEL.get();

        supportEnableHealing = SUPPORT_ENABLE_HEALING.get();
        supportHealThreshold = SUPPORT_HEAL_THRESHOLD.get();
        supportHealAmount = SUPPORT_HEAL_AMOUNT.get();
        supportHealCooldown = SUPPORT_HEAL_COOLDOWN.get();
        supportHealStyle = SUPPORT_HEAL_STYLE.get();
        supportHealRequiresXp = SUPPORT_HEAL_REQUIRES_XP.get();
        supportDamageDecreaseRequiresXp = SUPPORT_DAMAGE_DECREASE_REQUIRES_XP.get();
        supportBuffRequiresXp = SUPPORT_BUFF_REQUIRES_XP.get();
        supportXpCost = SUPPORT_XP_COST.get();
        supportHealOutsideCombat = SUPPORT_HEAL_OUTSIDE_COMBAT.get();
        supportFlatHeal = SUPPORT_FLAT_HEAL.get();
        supportLargeEntityHealthThreshold = SUPPORT_LARGE_ENTITY_HEALTH_THRESHOLD.get();
        supportLargeEntityHealAmount = SUPPORT_LARGE_ENTITY_HEAL_AMOUNT.get();

        masterEnableHealing = MASTER_ENABLE_HEALING.get();
        masterHealThreshold = MASTER_HEAL_THRESHOLD.get();
        masterHealAmount = MASTER_HEAL_AMOUNT.get();
        masterHealCooldown = MASTER_HEAL_COOLDOWN.get();

        dpsLeechEnabled = DPS_LEECH_ENABLED.get();
        dpsLeechMaxStacks = DPS_LEECH_MAX_STACKS.get();
        dpsLeechDurationTicks = DPS_LEECH_DURATION_TICKS.get();
        dpsLeechStealWhitelistMode = DPS_LEECH_STEAL_WHITELIST_MODE.get();
        //noinspection unchecked
        dpsLeechStealList = (List<String>) (List<?>) DPS_LEECH_STEAL_LIST.get();
        dpsLeechCleanseWhitelistMode = DPS_LEECH_CLEANSE_WHITELIST_MODE.get();
        //noinspection unchecked
        dpsLeechCleanseList = (List<String>) (List<?>) DPS_LEECH_CLEANSE_LIST.get();
    }
}
