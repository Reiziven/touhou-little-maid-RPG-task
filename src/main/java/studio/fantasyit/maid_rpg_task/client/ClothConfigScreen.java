package studio.fantasyit.maid_rpg_task.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import studio.fantasyit.maid_rpg_task.Config;

public class ClothConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("maid_rpg_task.config.title"))
                .setSavingRunnable(() -> {
                    Config.SPEC.save();
                });

        ConfigEntryBuilder entry = builder.entryBuilder();

        // Revive
        ConfigCategory revive = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.revive"));
        revive.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.revive.enable"), Config.enableReviveTask)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.revive.enable.tooltip"))
                .setSaveConsumer(v -> { Config.ENABLE_REVIVE.set(v); Config.enableReviveTask = v; })
                .build());
        revive.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.revive.aggro"), Config.enableReviveAggro)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.revive.aggro.tooltip"))
                .setSaveConsumer(v -> { Config.ENABLE_REVIVE_AGGRO.set(v); Config.enableReviveAggro = v; })
                .build());
        revive.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.revive.totem"), Config.enableReviveTotem)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.revive.totem.tooltip"))
                .setSaveConsumer(v -> { Config.ENABLE_REVIVE_TOTEM.set(v); Config.enableReviveTotem = v; })
                .build());
        revive.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.revive.cooldown_enabled"), Config.enableReviveCooldown)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.revive.cooldown_enabled.tooltip"))
                .setSaveConsumer(v -> { Config.ENABLE_REVIVE_COOLDOWN.set(v); Config.enableReviveCooldown = v; })
                .build());
        revive.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.revive.support_cooldown"), Config.supportReviveCooldown)
                .setDefaultValue(12000).setMin(20).setMax(144000)
                .setTooltip(Component.translatable("maid_rpg_task.config.revive.support_cooldown.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_REVIVE_COOLDOWN.set(v); Config.supportReviveCooldown = v; })
                .build());

        // Functions
        ConfigCategory functions = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.functions"));
        functions.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.functions.master"), Config.enableMasterTask)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.functions.master.tooltip"))
                .setSaveConsumer(v -> { Config.ENABLE_MASTER.set(v); Config.enableMasterTask = v; })
                .build());

        // Balance
        ConfigCategory balance = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.balance"));
        balance.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.balance.survival_balanced"), Config.survivalBalanced)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.balance.survival_balanced.tooltip"))
                .setSaveConsumer(v -> { Config.SURVIVAL_BALANCED.set(v); Config.survivalBalanced = v; })
                .build());

        // Tank
        ConfigCategory tank = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.tank"));
        tank.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.tank.ally_damage_taken"), Config.allyDamageTaken)
                .setDefaultValue(0.5).setMin(0.0).setMax(1.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.tank.ally_damage_taken.tooltip"))
                .setSaveConsumer(v -> { Config.ALLY_DAMAGE_TAKEN.set(v); Config.allyDamageTaken = v; })
                .build());
        tank.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.tank.tank_absorbs"), Config.tankAbsorbs)
                .setDefaultValue(0.5).setMin(0.0).setMax(2.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.tank.tank_absorbs.tooltip"))
                .setSaveConsumer(v -> { Config.TANK_ABSORBS.set(v); Config.tankAbsorbs = v; })
                .build());
        tank.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.tank.tank_direct_reduction"), Config.tankDirectReduction)
                .setDefaultValue(0.5).setMin(0.0).setMax(1.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.tank.tank_direct_reduction.tooltip"))
                .setSaveConsumer(v -> { Config.TANK_DIRECT_REDUCTION.set(v); Config.tankDirectReduction = v; })
                .build());
        tank.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.tank.stun_damage_decrease_level"), Config.tankStunDamageDecreaseLevel)
                .setDefaultValue(11).setMin(0).setMax(19)
                .setTooltip(Component.translatable("maid_rpg_task.config.tank.stun_damage_decrease_level.tooltip"))
                .setSaveConsumer(v -> { Config.TANK_STUN_DAMAGE_DECREASE_LEVEL.set(v); Config.tankStunDamageDecreaseLevel = v; })
                .build());
        tank.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.tank.stun_cooldown"), Config.tankStunCooldown)
                .setDefaultValue(200).setMin(20).setMax(72000)
                .setTooltip(Component.translatable("maid_rpg_task.config.tank.stun_cooldown.tooltip"))
                .setSaveConsumer(v -> { Config.TANK_STUN_COOLDOWN.set(v); Config.tankStunCooldown = v; })
                .build());
        tank.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.tank.stun_balanced_extra_cooldown"), Config.tankStunBalancedExtraCooldown)
                .setDefaultValue(20).setMin(0).setMax(72000)
                .setTooltip(Component.translatable("maid_rpg_task.config.tank.stun_balanced_extra_cooldown.tooltip"))
                .setSaveConsumer(v -> { Config.TANK_STUN_BALANCED_EXTRA_COOLDOWN.set(v); Config.tankStunBalancedExtraCooldown = v; })
                .build());

        // Support
        ConfigCategory support = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.support"));
        support.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.support.strengthened_level"), Config.supportStrengthenedLevel)
                .setDefaultValue(6).setMin(0).setMax(19)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.strengthened_level.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_STRENGTH_LEVEL.set(v); Config.supportStrengthenedLevel = v; })
                .build());
        support.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.support.defence_up_level"), Config.supportDefenceUpLevel)
                .setDefaultValue(8).setMin(0).setMax(19)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.defence_up_level.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_DEFENCE_UP_LEVEL.set(v); Config.supportDefenceUpLevel = v; })
                .build());
        support.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.support.damage_decrease_level"), Config.supportDamageDecreaseLevel)
                .setDefaultValue(3).setMin(0).setMax(19)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.damage_decrease_level.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_DAMAGE_DECREASE_LEVEL.set(v); Config.supportDamageDecreaseLevel = v; })
                .build());
        support.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.support.enable_healing"), Config.supportEnableHealing)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.enable_healing.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_ENABLE_HEALING.set(v); Config.supportEnableHealing = v; })
                .build());
        support.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.support.heal_threshold"), Config.supportHealThreshold)
                .setDefaultValue(0.5).setMin(0.01).setMax(1.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.heal_threshold.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_HEAL_THRESHOLD.set(v); Config.supportHealThreshold = v; })
                .build());
        support.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.support.heal_amount"), Config.supportHealAmount)
                .setDefaultValue(0.25).setMin(0.01).setMax(1.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.heal_amount.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_HEAL_AMOUNT.set(v); Config.supportHealAmount = v; })
                .build());
        support.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.support.heal_cooldown"), Config.supportHealCooldown)
                .setDefaultValue(600).setMin(20).setMax(72000)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.heal_cooldown.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_HEAL_COOLDOWN.set(v); Config.supportHealCooldown = v; })
                .build());
        support.addEntry(entry.startStrField(Component.translatable("maid_rpg_task.config.support.heal_style"), Config.supportHealStyle)
                .setDefaultValue("cycle")
                .setTooltip(Component.translatable("maid_rpg_task.config.support.heal_style.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_HEAL_STYLE.set(v); Config.supportHealStyle = v; })
                .build());
        support.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.support.heal_requires_xp"), Config.supportHealRequiresXp)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.heal_requires_xp.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_HEAL_REQUIRES_XP.set(v); Config.supportHealRequiresXp = v; })
                .build());
        support.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.support.damage_decrease_requires_xp"), Config.supportDamageDecreaseRequiresXp)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.damage_decrease_requires_xp.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_DAMAGE_DECREASE_REQUIRES_XP.set(v); Config.supportDamageDecreaseRequiresXp = v; })
                .build());
        support.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.support.buff_requires_xp"), Config.supportBuffRequiresXp)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.buff_requires_xp.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_BUFF_REQUIRES_XP.set(v); Config.supportBuffRequiresXp = v; })
                .build());
        support.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.support.xp_cost"), Config.supportXpCost)
                .setDefaultValue(1).setMin(1).setMax(100)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.xp_cost.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_XP_COST.set(v); Config.supportXpCost = v; })
                .build());
        support.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.support.heal_outside_combat"), Config.supportHealOutsideCombat)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.heal_outside_combat.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_HEAL_OUTSIDE_COMBAT.set(v); Config.supportHealOutsideCombat = v; })
                .build());
        support.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.support.flat_heal"), Config.supportFlatHeal)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.flat_heal.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_FLAT_HEAL.set(v); Config.supportFlatHeal = v; })
                .build());
        support.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.support.large_entity_health_threshold"), Config.supportLargeEntityHealthThreshold)
                .setDefaultValue(60.0).setMin(1.0).setMax(10000.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.large_entity_health_threshold.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_LARGE_ENTITY_HEALTH_THRESHOLD.set(v); Config.supportLargeEntityHealthThreshold = v; })
                .build());
        support.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.support.large_entity_heal_amount"), Config.supportLargeEntityHealAmount)
                .setDefaultValue(0.20).setMin(0.01).setMax(1.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.support.large_entity_heal_amount.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_LARGE_ENTITY_HEAL_AMOUNT.set(v); Config.supportLargeEntityHealAmount = v; })
                .build());

        // Master
        ConfigCategory master = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.master"));
        master.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.master.enable_healing"), Config.masterEnableHealing)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.master.enable_healing.tooltip"))
                .setSaveConsumer(v -> { Config.MASTER_ENABLE_HEALING.set(v); Config.masterEnableHealing = v; })
                .build());
        master.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.master.heal_threshold"), Config.masterHealThreshold)
                .setDefaultValue(0.5).setMin(0.01).setMax(1.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.master.heal_threshold.tooltip"))
                .setSaveConsumer(v -> { Config.MASTER_HEAL_THRESHOLD.set(v); Config.masterHealThreshold = v; })
                .build());
        master.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.master.heal_amount"), Config.masterHealAmount)
                .setDefaultValue(0.20).setMin(0.01).setMax(1.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.master.heal_amount.tooltip"))
                .setSaveConsumer(v -> { Config.MASTER_HEAL_AMOUNT.set(v); Config.masterHealAmount = v; })
                .build());
        master.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.master.heal_cooldown"), Config.masterHealCooldown)
                .setDefaultValue(400).setMin(20).setMax(72000)
                .setTooltip(Component.translatable("maid_rpg_task.config.master.heal_cooldown.tooltip"))
                .setSaveConsumer(v -> { Config.MASTER_HEAL_COOLDOWN.set(v); Config.masterHealCooldown = v; })
                .build());

        // Mage
        ConfigCategory mage = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.mage"));
        mage.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.mage.spell_cooldown_min"), Config.mageSpellCooldownMin)
                .setDefaultValue(400).setMin(20).setMax(72000)
                .setTooltip(Component.translatable("maid_rpg_task.config.mage.spell_cooldown_min.tooltip"))
                .setSaveConsumer(v -> { Config.MAGE_SPELL_COOLDOWN_MIN.set(v); Config.mageSpellCooldownMin = v; })
                .build());
        mage.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.mage.spell_cooldown_max"), Config.mageSpellCooldownMax)
                .setDefaultValue(520).setMin(20).setMax(72000)
                .setTooltip(Component.translatable("maid_rpg_task.config.mage.spell_cooldown_max.tooltip"))
                .setSaveConsumer(v -> { Config.MAGE_SPELL_COOLDOWN_MAX.set(v); Config.mageSpellCooldownMax = v; })
                .build());
        mage.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.mage.ice_spike_reduced_healing_duration"), Config.iceSpikeReducedHealingDuration)
                .setDefaultValue(1000).setMin(0).setMax(72000)
                .setTooltip(Component.translatable("maid_rpg_task.config.mage.ice_spike_reduced_healing_duration.tooltip"))
                .setSaveConsumer(v -> { Config.ICE_SPIKE_REDUCED_HEALING_DURATION.set(v); Config.iceSpikeReducedHealingDuration = v; })
                .build());
        mage.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.mage.ice_spike_reduced_healing_level"), Config.iceSpikeReducedHealingLevel)
                .setDefaultValue(8).setMin(0).setMax(20)
                .setTooltip(Component.translatable("maid_rpg_task.config.mage.ice_spike_reduced_healing_level.tooltip"))
                .setSaveConsumer(v -> { Config.ICE_SPIKE_REDUCED_HEALING_LEVEL.set(v); Config.iceSpikeReducedHealingLevel = v; })
                .build());
        mage.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.mage.bypass_totem"), Config.mageBypassTotem)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.mage.bypass_totem.tooltip"))
                .setSaveConsumer(v -> { Config.MAGE_BYPASS_TOTEM.set(v); Config.mageBypassTotem = v; })
                .build());

        // DPS
        ConfigCategory dps = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.dps"));
        dps.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.dps.base_attack_boost"), Config.dpsBaseAttackBoost)
                .setDefaultValue(0.35).setMin(0.0).setMax(10.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.base_attack_boost.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_BASE_ATTACK_BOOST.set(v); Config.dpsBaseAttackBoost = v; })
                .build());
        dps.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.dps.offhand_attack_boost"), Config.dpsOffhandAttackBoost)
                .setDefaultValue(0.15).setMin(0.0).setMax(10.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.offhand_attack_boost.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_OFFHAND_ATTACK_BOOST.set(v); Config.dpsOffhandAttackBoost = v; })
                .build());
        dps.addEntry(entry.startDoubleField(Component.translatable("maid_rpg_task.config.dps.attack_speed_boost"), Config.dpsAttackSpeedBoost)
                .setDefaultValue(0.30).setMin(0.0).setMax(10.0)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.attack_speed_boost.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_ATTACK_SPEED_BOOST.set(v); Config.dpsAttackSpeedBoost = v; })
                .build());
        dps.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.dps.leech_enabled"), Config.dpsLeechEnabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.leech_enabled.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_LEECH_ENABLED.set(v); Config.dpsLeechEnabled = v; })
                .build());
        dps.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.dps.leech_max_stacks"), Config.dpsLeechMaxStacks)
                .setDefaultValue(5).setMin(1).setMax(20)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.leech_max_stacks.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_LEECH_MAX_STACKS.set(v); Config.dpsLeechMaxStacks = v; })
                .build());
        dps.addEntry(entry.startIntField(Component.translatable("maid_rpg_task.config.dps.leech_duration_ticks"), Config.dpsLeechDurationTicks)
                .setDefaultValue(6000).setMin(20).setMax(144000)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.leech_duration_ticks.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_LEECH_DURATION_TICKS.set(v); Config.dpsLeechDurationTicks = v; })
                .build());
        dps.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.dps.leech_steal_whitelist_mode"), Config.dpsLeechStealWhitelistMode)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.leech_steal_whitelist_mode.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_LEECH_STEAL_WHITELIST_MODE.set(v); Config.dpsLeechStealWhitelistMode = v; })
                .build());
        dps.addEntry(entry.startStrList(Component.translatable("maid_rpg_task.config.dps.leech_steal_list"), Config.dpsLeechStealList)
                .setDefaultValue(java.util.List.of("minecraft:fire_resistance"))
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.leech_steal_list.tooltip"))
                .setSaveConsumer(v -> {
                    Config.DPS_LEECH_STEAL_LIST.set(v);
                    Config.dpsLeechStealList = new java.util.ArrayList<>(v);
                })
                .build());
        dps.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.dps.leech_cleanse_whitelist_mode"), Config.dpsLeechCleanseWhitelistMode)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.leech_cleanse_whitelist_mode.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_LEECH_CLEANSE_WHITELIST_MODE.set(v); Config.dpsLeechCleanseWhitelistMode = v; })
                .build());
        dps.addEntry(entry.startStrList(Component.translatable("maid_rpg_task.config.dps.leech_cleanse_list"), Config.dpsLeechCleanseList)
                .setDefaultValue(java.util.List.of())
                .setTooltip(Component.translatable("maid_rpg_task.config.dps.leech_cleanse_list.tooltip"))
                .setSaveConsumer(v -> {
                    Config.DPS_LEECH_CLEANSE_LIST.set(v);
                    Config.dpsLeechCleanseList = new java.util.ArrayList<>(v);
                })
                .build());

        // Downsides
        ConfigCategory downsides = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.downsides"));
        downsides.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.downsides.dps_health_reduction"), Config.dpsHealthReduction)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.downsides.dps_health_reduction.tooltip"))
                .setSaveConsumer(v -> { Config.DPS_HEALTH_REDUCTION.set(v); Config.dpsHealthReduction = v; })
                .build());
        downsides.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.downsides.tank_attack_penalty"), Config.tankAttackPenalty)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.downsides.tank_attack_penalty.tooltip"))
                .setSaveConsumer(v -> { Config.TANK_ATTACK_PENALTY.set(v); Config.tankAttackPenalty = v; })
                .build());
        downsides.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.downsides.mage_health_reduction"), Config.mageHealthReduction)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.downsides.mage_health_reduction.tooltip"))
                .setSaveConsumer(v -> { Config.MAGE_HEALTH_REDUCTION.set(v); Config.mageHealthReduction = v; })
                .build());
        downsides.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.downsides.support_can_attack"), Config.supportCanAttack)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("maid_rpg_task.config.downsides.support_can_attack.tooltip"))
                .setSaveConsumer(v -> { Config.SUPPORT_CAN_ATTACK.set(v); Config.supportCanAttack = v; })
                .build());

        // Bauble
        ConfigCategory bauble = builder.getOrCreateCategory(Component.translatable("maid_rpg_task.config.bauble"));
        bauble.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.bauble.master_soul_spell_craftable"), Config.MASTER_SOUL_SPELL_CRAFTABLE.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.bauble.master_soul_spell_craftable.tooltip"))
                .setSaveConsumer(v -> Config.MASTER_SOUL_SPELL_CRAFTABLE.set(v))
                .build());
        bauble.addEntry(entry.startBooleanToggle(Component.translatable("maid_rpg_task.config.bauble.master_soul_spell_alt_craftable"), Config.MASTER_SOUL_SPELL_ALT_CRAFTABLE.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("maid_rpg_task.config.bauble.master_soul_spell_alt_craftable.tooltip"))
                .setSaveConsumer(v -> Config.MASTER_SOUL_SPELL_ALT_CRAFTABLE.set(v))
                .build());

        return builder.build();
    }
}
