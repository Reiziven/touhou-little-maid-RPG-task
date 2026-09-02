package studio.fantasyit.maid_rpg_task;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = MaidRpgTask.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_REVIVE = BUILDER
            .define("functions.revive", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_REVIVE_AGGRO = BUILDER
            .comment("When true, nearby hostile mobs will aggro and target the maid while she is reviving a bleeding player.\n"
                   + "This makes reviving riskier in combat — the maid becomes a target until the revive finishes.")
            .define("revive.aggro", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_REVIVE_TOTEM = BUILDER
            .comment("When true, the maid can use a Totem of Undying placed in her bauble inventory to instantly revive the owner player.\n"
                   + "The totem is consumed on use, acting exactly like the player held it themselves.")
            .define("revive.totem", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_REVIVE_COOLDOWN = BUILDER
            .comment("When true, the revive ability is limited by resources:\n"
                   + "  Support maid: can revive but is placed on a cooldown afterwards (see revive.support_cooldown).\n"
                   + "  Master maid:  can revive an unlimited number of times, but each revive consumes 1 durability\n"
                   + "                from an Ultramarine Orb Elixir in her bauble inventory.\n"
                   + "                Without an Elixir equipped the master maid can still revive freely.\n"
                   + "Default: true")
            .define("revive.cooldown_enabled", true);
    private static final ForgeConfigSpec.IntValue SUPPORT_REVIVE_COOLDOWN = BUILDER
            .comment("Cooldown in ticks before a support maid can revive again after a successful revive.\n"
                   + "Only applies when revive.cooldown_enabled=true. Default: 12000 (10 minutes)")
            .defineInRange("revive.support_cooldown", 12000, 20, 144000);
    private static final ForgeConfigSpec.BooleanValue ENABLE_MASTER = BUILDER
            .define("functions.master", false);

    // ---------------------------------------------------------------------------
    // Survival-balanced preset
    // When true, the runtime fields below are overwritten with the preset values
    // on load, ignoring whatever the individual config entries say.
    //
    // Preset values:
    //   DPS:     attack boost halved, offhand boost halved, speed bonus halved
    //   Tank:    ally_damage_taken=0.8, tank_absorbs=0.2, tank_direct_reduction=0
    //            weakness amplifier=0 (level I), stun cooldown +1 s
    //   Support: strength_level=0 (Strength I)
    //   Mage:    spell_cooldown_min=400, spell_cooldown_max=520 (defaults)
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.BooleanValue SURVIVAL_BALANCED = BUILDER
            .comment("Quick preset toggle. When true, overwrites all tuning fields below with balanced values at load time. some things are not overwriten, it's a suggestion for values but it also changes a few things you cannot in config like tank stun cooldown is increased by +1 second")
            .define("balance.survival_balanced", true);

    // ---------------------------------------------------------------------------
    // Tank — damage redirect
    //   ally_damage_taken     : fraction of the original hit the ally receives
    //                           (0.5 = ally takes 50%, i.e. 50% reduction)
    //   tank_absorbs          : fraction of the original hit redirected to the tank
    //                           (0.5 = tank takes 50% of what the ally was going to take)
    //   tank_direct_reduction : fraction by which the tank's OWN incoming damage is reduced
    //                           (0.5 = tank takes 50%, 0.0 = no mitigation)
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.DoubleValue ALLY_DAMAGE_TAKEN = BUILDER
            .comment("Fraction of a hit that allies (owner + pets) receive when a tank maid is nearby.\n"
                   + "0.5 = ally takes 50% (50% reduction). Default: 0.5")
            .defineInRange("tank.ally_damage_taken", 0.5, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue TANK_ABSORBS = BUILDER
            .comment("Fraction of the original ally hit that is redirected to the tank.\n"
                   + "0.5 = tank absorbs 50% of the original hit. Default: 0.5")
            .defineInRange("tank.tank_absorbs", 0.5, 0.0, 2.0);

    private static final ForgeConfigSpec.DoubleValue TANK_DIRECT_REDUCTION = BUILDER
            .comment("Fraction by which the tank's OWN incoming damage is reduced when hit directly.\n"
                   + "0.5 = tank takes 50% (50% reduction). 0.0 = no reduction. Default: 0.5")
            .defineInRange("tank.tank_direct_reduction", 0.5, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue TANK_STUN_DAMAGE_DECREASE_LEVEL = BUILDER
            .comment("Amplifier of the Damage Decrease effect applied to enemies during tank stun.\n"
                   + "0 = Level I, 11 = Level XII (60% damage reduction). Default: 11")
            .defineInRange("tank.stun_damage_decrease_level", 11, 0, 19);

    // ---------------------------------------------------------------------------
    // Support — effect levels (amplifier: 0 = level I, 1 = level II, …)
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.IntValue SUPPORT_STRENGTH_LEVEL = BUILDER
            .comment("Strengthened amplifier for allies in combat. 0=Level I (+5% dmg), 3=Level IV (+20% dmg). Default: 6 (+35% dmg, non-balanced mode)")
            .defineInRange("support.strengthened_level", 6, 0, 19);

    private static final ForgeConfigSpec.IntValue SUPPORT_DEFENCE_UP_LEVEL = BUILDER
            .comment("Defence Up amplifier for allies in combat. 0=Level I (5% dmg reduction), 3=Level IV (20% reduction). Default: 8 (45% reduction, non-balanced mode)")
            .defineInRange("support.defence_up_level", 8, 0, 19);

    private static final ForgeConfigSpec.IntValue SUPPORT_DAMAGE_DECREASE_LEVEL = BUILDER
            .comment("Damage Decrease amplifier applied to enemies. Each level = 5% reduction.\n"
                   + "0=5%, 3=20%. Default: 3")
            .defineInRange("support.damage_decrease_level", 3, 0, 19);

    private static final ForgeConfigSpec.BooleanValue SUPPORT_ENABLE_HEALING = BUILDER
            .comment("When true, the support maid can cast healing circles on injured allies.")
            .define("support.enable_healing", true);

    private static final ForgeConfigSpec.DoubleValue SUPPORT_HEAL_THRESHOLD = BUILDER
            .comment("HP percentage (0.0–1.0) below which an ally triggers a healing circle.\n"
                   + "0.5 = ally must be below 50% HP. Default: 0.5")
            .defineInRange("support.heal_threshold", 0.5, 0.01, 1.0);

    private static final ForgeConfigSpec.DoubleValue SUPPORT_HEAL_AMOUNT = BUILDER
            .comment("Percentage of max health restored by the healing circle (0.0–1.0). Default: 0.20 (20%)")
            .defineInRange("support.heal_amount", 0.20, 0.01, 1.0);

    private static final ForgeConfigSpec.IntValue SUPPORT_HEAL_COOLDOWN = BUILDER
            .comment("Cooldown in ticks between healing circle casts per target. Default: 600 (30 seconds)")
            .defineInRange("support.heal_cooldown", 600, 20, 72000);

    private static final ForgeConfigSpec.ConfigValue<String> SUPPORT_HEAL_STYLE = BUILDER
            .comment("Visual style of the healing circle.\n"
                   + "Valid values: \"spiral\", \"end_rod\", \"entity_effect\", \"cycle\"\n"
                   + "  spiral        — crimson-to-pink DustColorTransition arms converging inward (default)\n"
                   + "  end_rod       — white END_ROD spiral arms\n"
                   + "  entity_effect — hollow ring of Instant Health colored ENTITY_EFFECT particles (red, #F82423)\n"
                   + "  cycle         — randomly picks spiral or end_rod on each cast")
            .defineInList("support.heal_style", "cycle",
                    java.util.List.of("spiral", "end_rod", "entity_effect", "cycle"));

    private static final ForgeConfigSpec.BooleanValue SUPPORT_HEAL_REQUIRES_XP = BUILDER
            .comment("When true, the support maid must have XP (experience > 0) to cast healing circles.\n"
                   + "She already spends 1 XP per second while the support behavior is active. Default: true")
            .define("support.heal_requires_xp", true);

    private static final ForgeConfigSpec.BooleanValue SUPPORT_HEAL_OUTSIDE_COMBAT = BUILDER
            .comment("When true, healing circles can trigger even when no enemies are nearby (outside combat).\n"
                   + "Default: false — healing only triggers while the support behavior is actively running.")
            .define("support.heal_outside_combat", false);

    // ---------------------------------------------------------------------------
    // Master — self-heal
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.BooleanValue MASTER_ENABLE_HEALING = BUILDER
            .comment("When true, the master maid self-heals via a healing circle when injured.")
            .define("master.enable_healing", true);

    private static final ForgeConfigSpec.DoubleValue MASTER_HEAL_THRESHOLD = BUILDER
            .comment("HP percentage (0.0–1.0) below which the master maid triggers self-heal.\n"
                   + "0.5 = triggers below 50% HP. Default: 0.5")
            .defineInRange("master.heal_threshold", 0.5, 0.01, 1.0);

    private static final ForgeConfigSpec.DoubleValue MASTER_HEAL_AMOUNT = BUILDER
            .comment("Percentage of max health restored per self-heal (0.0–1.0). Default: 0.20 (20%)")
            .defineInRange("master.heal_amount", 0.20, 0.01, 1.0);

    private static final ForgeConfigSpec.IntValue MASTER_HEAL_COOLDOWN = BUILDER
            .comment("Cooldown in ticks between self-heal casts. Default: 400 (20 seconds)")
            .defineInRange("master.heal_cooldown", 400, 20, 72000);

    // ---------------------------------------------------------------------------
    // Mage — spell casting cooldown (ticks; 20 ticks = 1 second)
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.IntValue MAGE_SPELL_COOLDOWN_MIN = BUILDER
            .comment("Minimum ticks between elemental spell casts. Default: 400 (20s)")
            .defineInRange("mage.spell_cooldown_min", 400, 20, 72000);

    private static final ForgeConfigSpec.IntValue MAGE_SPELL_COOLDOWN_MAX = BUILDER
            .comment("Maximum ticks between elemental spell casts. Clamped to >= min at load time. Default: 520 (26s)")
            .defineInRange("mage.spell_cooldown_max", 520, 20, 72000);

    // ---------------------------------------------------------------------------
    // Ice Spike — reduced healing effect
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.IntValue ICE_SPIKE_REDUCED_HEALING_DURATION = BUILDER
            .comment("Duration in ticks of the Reduced Healing effect applied by Ice Spike. 20 ticks = 1 second. Default: 1000 (50s)")
            .defineInRange("mage.ice_spike_reduced_healing_duration", 1000, 0, 72000);

    private static final ForgeConfigSpec.IntValue ICE_SPIKE_REDUCED_HEALING_LEVEL = BUILDER
            .comment("Amplifier of the Reduced Healing effect applied by Ice Spike. 0=Level I, 1=Level II, etc. Default: 8")
            .defineInRange("mage.ice_spike_reduced_healing_level", 8, 0, 20);

    // ---------------------------------------------------------------------------
    // Mage — totem bypass
    //   When true, elemental spell damage uses out_of_world as its source,
    //   which bypasses invulnerability, resistance, and totem-of-undying saves.
    //   Downside: also bypasses creative mode protection and similar.
    //   Default: true
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.BooleanValue MAGE_BYPASS_TOTEM = BUILDER
            .comment("When true, elemental spells bypass Totem of Undying and modded undying abilities\n"
                   + "by using out_of_world damage. Also bypasses creative invulnerability as a side effect.\n"
                   + "Default: true")
            .define("mage.bypass_totem", false);

    // ---------------------------------------------------------------------------
    // Downside toggles — set to false to disable each task's built-in penalty
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.BooleanValue DPS_HEALTH_REDUCTION = BUILDER
            .comment("When true, DPS maid has reduced max health. Default: true")
            .define("downsides.dps_health_reduction", true);

    private static final ForgeConfigSpec.BooleanValue TANK_ATTACK_PENALTY = BUILDER
            .comment("When true, Tank maid attacks slower and deals less damage. Default: true")
            .define("downsides.tank_attack_penalty", true);

    private static final ForgeConfigSpec.BooleanValue MAGE_HEALTH_REDUCTION = BUILDER
            .comment("When true, Mage maid has reduced max health. Default: true")
            .define("downsides.mage_health_reduction", true);

    private static final ForgeConfigSpec.BooleanValue SUPPORT_CAN_ATTACK = BUILDER
            .comment("When true, Support maid can attack enemies in addition to supporting allies. Default: false")
            .define("downsides.support_can_attack", false);

    // ---------------------------------------------------------------------------
    // Bauble crafting
    // ---------------------------------------------------------------------------
    public static final ForgeConfigSpec.BooleanValue MASTER_SOUL_SPELL_CRAFTABLE = BUILDER
            .comment("If true, the Master Soul Spell bauble can be crafted at the altar in survival. Still obtainable via commands.")
            .define("bauble.master_soul_spell_craftable", true);

    public static final ForgeConfigSpec.BooleanValue MASTER_SOUL_SPELL_ALT_CRAFTABLE = BUILDER
            .comment("If true, the alternate Master Soul Spell altar recipe (empty smart slab + logs) is enabled.")
            .define("bauble.master_soul_spell_alt_craftable", true);

    // ---------------------------------------------------------------------------
    // DPS — Leech ability (beneficial effect steal on hit)
    // ---------------------------------------------------------------------------
    private static final ForgeConfigSpec.BooleanValue DPS_LEECH_ENABLED = BUILDER
            .comment("When true, the DPS maid has a 5% chance on each hit to steal one beneficial effect from the target.")
            .define("dps.leech_enabled", true);

    private static final ForgeConfigSpec.IntValue DPS_LEECH_MAX_STACKS = BUILDER
            .comment("Maximum number of stolen effects the maid can hold at once (per source, up to level 5). Default: 5")
            .defineInRange("dps.leech_max_stacks", 5, 1, 20);

    private static final ForgeConfigSpec.IntValue DPS_LEECH_DURATION_TICKS = BUILDER
            .comment("Duration in ticks that a stolen effect lasts on the maid. Default: 6000 (5 minutes)")
            .defineInRange("dps.leech_duration_ticks", 6000, 20, 144000);

    private static final ForgeConfigSpec.BooleanValue DPS_LEECH_STEAL_WHITELIST_MODE = BUILDER
            .comment("When true, only effects in dps.leech_steal_list can be stolen. When false, all effects EXCEPT those in the list are stealable.")
            .define("dps.leech_steal_whitelist_mode", false);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DPS_LEECH_STEAL_LIST = BUILDER
            .comment("Effect resource locations for steal blacklist (or whitelist when leech_steal_whitelist_mode=true).\n"
                   + "Example: [\"minecraft:regeneration\", \"minecraft:speed\"]")
            .defineListAllowEmpty("dps.leech_steal_list",
                    java.util.List.of("minecraft:fire_resistance"),
                    o -> o instanceof String);

    private static final ForgeConfigSpec.BooleanValue DPS_LEECH_CLEANSE_WHITELIST_MODE = BUILDER
            .comment("When true, only effects in dps.leech_cleanse_list are removed from the target. When false, all beneficial effects EXCEPT those in the list are removed.")
            .define("dps.leech_cleanse_whitelist_mode", false);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DPS_LEECH_CLEANSE_LIST = BUILDER
            .comment("Effect resource locations for cleanse blacklist (or whitelist when leech_cleanse_whitelist_mode=true).\n"
                   + "Example: [\"minecraft:regeneration\"]")
            .defineListAllowEmpty("dps.leech_cleanse_list",
                    java.util.List.of(),
                    o -> o instanceof String);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // ---------------------------------------------------------------------------
    // Runtime fields — read these everywhere in behavior/event code.
    // When survival_balanced is true these are set to the preset values,
    // overriding whatever the config file says.
    // ---------------------------------------------------------------------------
    public static boolean enableReviveTask = true;
    public static boolean enableReviveAggro = false;
    public static boolean enableReviveTotem = false;
    public static boolean enableReviveCooldown = true;
    public static int supportReviveCooldown = 12000;
    public static boolean enableMasterTask = false;
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
    public static boolean supportHealOutsideCombat = false;

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

    /** When true, elemental spells use out_of_world damage to bypass totems and undying abilities. */
    public static boolean mageBypassTotem = false;

    public static int iceSpikeReducedHealingDuration = 1000;
    public static int iceSpikeReducedHealingLevel = 8;

    // DPS Leech
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
        survivalBalanced = SURVIVAL_BALANCED.get();

        if (survivalBalanced) {
            // Preset values — individual config entries are ignored
            allyDamageTaken      = 0.8;
            tankAbsorbs          = 0.2;
            tankDirectReduction  = 0.0;
            supportStrengthLevel = 0;            supportStrengthenedLevel = 1; // +10% damage (Level II)
            supportDefenceUpLevel    = 2; // 15% damage reduction (Level III)
            mageSpellCooldownMin = 400;
            mageSpellCooldownMax = 520;
            // supportDamageDecreaseLevel and DPS multipliers have their own
            // handling in behavior code via survivalBalanced flag
            supportDamageDecreaseLevel = SUPPORT_DAMAGE_DECREASE_LEVEL.get();
        } else {
            allyDamageTaken      = ALLY_DAMAGE_TAKEN.get();
            tankAbsorbs          = TANK_ABSORBS.get();
            tankDirectReduction  = TANK_DIRECT_REDUCTION.get();
            supportStrengthLevel = SUPPORT_STRENGTH_LEVEL.get();            supportStrengthenedLevel = SUPPORT_STRENGTH_LEVEL.get();
            supportDefenceUpLevel    = SUPPORT_DEFENCE_UP_LEVEL.get();
            supportDamageDecreaseLevel = SUPPORT_DAMAGE_DECREASE_LEVEL.get();
            mageSpellCooldownMin = MAGE_SPELL_COOLDOWN_MIN.get();
            mageSpellCooldownMax = Math.max(MAGE_SPELL_COOLDOWN_MAX.get(), mageSpellCooldownMin);
        }
        dpsHealthReduction  = DPS_HEALTH_REDUCTION.get();
        tankAttackPenalty   = TANK_ATTACK_PENALTY.get();
        tankStunDamageDecreaseLevel = TANK_STUN_DAMAGE_DECREASE_LEVEL.get();        mageHealthReduction = MAGE_HEALTH_REDUCTION.get();
        supportCanAttack    = SUPPORT_CAN_ATTACK.get();
        mageBypassTotem     = MAGE_BYPASS_TOTEM.get();
        iceSpikeReducedHealingDuration = ICE_SPIKE_REDUCED_HEALING_DURATION.get();
        iceSpikeReducedHealingLevel    = ICE_SPIKE_REDUCED_HEALING_LEVEL.get();

        supportEnableHealing   = SUPPORT_ENABLE_HEALING.get();
        supportHealThreshold   = SUPPORT_HEAL_THRESHOLD.get();
        supportHealAmount      = SUPPORT_HEAL_AMOUNT.get();
        supportHealCooldown    = SUPPORT_HEAL_COOLDOWN.get();
        supportHealStyle       = SUPPORT_HEAL_STYLE.get();
        supportHealRequiresXp  = SUPPORT_HEAL_REQUIRES_XP.get();
        supportHealOutsideCombat = SUPPORT_HEAL_OUTSIDE_COMBAT.get();

        masterEnableHealing  = MASTER_ENABLE_HEALING.get();
        masterHealThreshold  = MASTER_HEAL_THRESHOLD.get();
        masterHealAmount     = MASTER_HEAL_AMOUNT.get();
        masterHealCooldown   = MASTER_HEAL_COOLDOWN.get();

        dpsLeechEnabled              = DPS_LEECH_ENABLED.get();
        dpsLeechMaxStacks            = DPS_LEECH_MAX_STACKS.get();
        dpsLeechDurationTicks        = DPS_LEECH_DURATION_TICKS.get();
        dpsLeechStealWhitelistMode   = DPS_LEECH_STEAL_WHITELIST_MODE.get();
        //noinspection unchecked
        dpsLeechStealList            = (List<String>) (List<?>) DPS_LEECH_STEAL_LIST.get();
        dpsLeechCleanseWhitelistMode = DPS_LEECH_CLEANSE_WHITELIST_MODE.get();
        //noinspection unchecked
        dpsLeechCleanseList          = (List<String>) (List<?>) DPS_LEECH_CLEANSE_LIST.get();
    }
}
