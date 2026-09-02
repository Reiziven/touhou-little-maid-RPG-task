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
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("0af1c7b0-d87a-4a6c-a3a3-665f926e3d93");

    private static final UUID BASE_ATTACK_BOOST_ID = UUID.fromString("3e418842-f531-4c80-9e3c-e7ccfe44db9c");
    private static final UUID OFFHAND_ATTACK_BOOST_ID = UUID.fromString("4f528852-b631-4d81-ae4b-7cd9fe44dc1e");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private static final double HEALTH_REDUCTION_PERCENTAGE = 0.80; // Reduces max health by 80%
    private static final double BASE_ATTACK_INCREASE_PERCENTAGE = 0.35;   // 35% attack boost (17.5% in survival_balanced)
    private static final double OFFHAND_ATTACK_INCREASE_PERCENTAGE = 0.15; // 15% offhand boost (7.5% in survival_balanced)
    private static final double ATTACK_SPEED_BONUS = 0.30;                 // 30% speed bonus (15% in survival_balanced)

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
                double reductionAmount = -healthAttr.getBaseValue() * HEALTH_REDUCTION_PERCENTAGE;
                AttributeModifier healthMod = new AttributeModifier(
                        HEALTH_MODIFIER_ID,
                        "DPS task health reduction",
                        reductionAmount,
                        AttributeModifier.Operation.ADDITION
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
        double attackMult = Config.survivalBalanced ? BASE_ATTACK_INCREASE_PERCENTAGE / 2.0 : BASE_ATTACK_INCREASE_PERCENTAGE;
        double offhandMult = Config.survivalBalanced ? OFFHAND_ATTACK_INCREASE_PERCENTAGE / 2.0 : OFFHAND_ATTACK_INCREASE_PERCENTAGE;
        double speedMult = Config.survivalBalanced ? ATTACK_SPEED_BONUS / 2.0 : ATTACK_SPEED_BONUS;

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
