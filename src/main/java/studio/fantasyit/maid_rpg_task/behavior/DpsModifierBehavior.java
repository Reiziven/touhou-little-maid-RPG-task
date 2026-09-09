package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import studio.fantasyit.maid_rpg_task.Config;

import java.util.Map;
import java.util.UUID;

public class DpsModifierBehavior extends Behavior<EntityMaid> {
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("c30a5d42-a47f-4a93-b377-8d1838458ba8");

    private static final UUID BASE_ATTACK_BOOST_ID = UUID.fromString("41588729-df65-4e88-88ad-10cc5d7ccd9f");
    private static final UUID OFFHAND_ATTACK_BOOST_ID = UUID.fromString("97ffc139-4cfd-4822-89b7-aae7b36493eb");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private static final double HEALTH_REDUCTION_PERCENTAGE = 0.80; // Reduces max health by 80%

    public DpsModifierBehavior() {
        super(Map.of()); // No required memory modules
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_MODIFIER_ID) == null) {
            if (Config.dpsHealthReduction) {
                // MULTIPLY_BASE: recomputed live against the CURRENT base value on every
                // attribute read, so it stays correct even after favorability changes the
                // maid's base max health later — unlike a flat ADDITION amount, which would
                // go stale and require a task switch to recompute.
                AttributeModifier healthMod = new AttributeModifier(
                        HEALTH_MODIFIER_ID,
                        "DPS task health reduction",
                        -HEALTH_REDUCTION_PERCENTAGE,
                        AttributeModifier.Operation.MULTIPLY_BASE
                );
                healthAttr.addPermanentModifier(healthMod);

                if (maid.getHealth() > maid.getMaxHealth()) {
                    maid.setHealth(maid.getMaxHealth());
                }
            }
        }

        updateAttackBoosts(maid);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        updateAttackBoosts(maid);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            if (maid.getHealth() > maid.getMaxHealth()) {
                maid.setHealth(maid.getMaxHealth());
            }
        }

        var attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            if (attackAttr.getModifier(BASE_ATTACK_BOOST_ID) != null) {
                attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
            }
            if (attackAttr.getModifier(OFFHAND_ATTACK_BOOST_ID) != null) {
                attackAttr.removeModifier(OFFHAND_ATTACK_BOOST_ID);
            }
        }

        var speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(ATTACK_SPEED_MODIFIER_ID) != null) {
            speedAttr.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return true;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    private void updateAttackBoosts(EntityMaid maid) {
        double attackMult  = Config.survivalBalanced ? Config.dpsBaseAttackBoost   / 2.0 : Config.dpsBaseAttackBoost;
        double offhandMult = Config.survivalBalanced ? Config.dpsOffhandAttackBoost / 2.0 : Config.dpsOffhandAttackBoost;
        double speedMult   = Config.survivalBalanced ? Config.dpsAttackSpeedBoost   / 2.0 : Config.dpsAttackSpeedBoost;

        AttributeInstance attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            if (attackAttr.getModifier(BASE_ATTACK_BOOST_ID) != null) {
                attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
            }
            if (attackAttr.getModifier(OFFHAND_ATTACK_BOOST_ID) != null) {
                attackAttr.removeModifier(OFFHAND_ATTACK_BOOST_ID);
            }

            AttributeModifier baseAttackMod = new AttributeModifier(
                    BASE_ATTACK_BOOST_ID,
                    "DPS base attack boost",
                    attackMult,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            attackAttr.addPermanentModifier(baseAttackMod);

            if (isOffhandSwordOrAxe(maid)) {
                AttributeModifier offhandMod = new AttributeModifier(
                        OFFHAND_ATTACK_BOOST_ID,
                        "DPS offhand weapon boost",
                        offhandMult,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
                attackAttr.addPermanentModifier(offhandMod);
            }
        }

        // Apply attack speed bonus on top of weapon's own speed
        AttributeInstance speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(ATTACK_SPEED_MODIFIER_ID) == null) {
            AttributeModifier speedMod = new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID,
                    "DPS attack speed bonus",
                    speedMult,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            speedAttr.addPermanentModifier(speedMod);
        }
    }

    private boolean isOffhandSwordOrAxe(EntityMaid maid) {
        ItemStack offhandItem = maid.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offhandItem.isEmpty()) return false;
        Item item = offhandItem.getItem();
        return item instanceof SwordItem || item instanceof AxeItem;
    }
}
