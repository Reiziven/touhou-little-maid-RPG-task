package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import studio.fantasyit.maid_rpg_task.Config;

import java.util.Map;
import java.util.UUID;

public class MageBehavior extends Behavior<EntityMaid> {
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("a756ae65-c7bb-4727-b054-8e679f051297");
    private static final UUID BASE_ATTACK_BOOST_ID = UUID.fromString("e37f4ecf-59d3-4cbd-a80c-95506cd97c1a");
    private static final UUID SHIELD_HEALTH_PENALTY_ID = UUID.fromString("a7c3e291-bb84-4d10-9f12-3301fabc5d78");
    private static final double HEALTH_REDUCTION_PERCENTAGE = 0.80;
    private static final double BASE_ATTACK_INCREASE_PERCENTAGE = 0.35;

    public MageBehavior() {
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
            if (Config.mageHealthReduction) {
                // MULTIPLY_BASE recomputes live against the current base value, so it stays
                // correct after favorability changes base max health — unlike a flat ADDITION
                // amount snapshotted once at start(), which goes stale until the next task switch.
                AttributeModifier healthMod = new AttributeModifier(
                        HEALTH_MODIFIER_ID,
                        "Mage task health reduction",
                        -HEALTH_REDUCTION_PERCENTAGE,
                        AttributeModifier.Operation.MULTIPLY_BASE
                );
                healthAttr.addPermanentModifier(healthMod);

                if (maid.getHealth() > maid.getMaxHealth()) {
                    maid.setHealth(maid.getMaxHealth());
                }
            }
        }

        // Extra -25% max health when sphere shield is enabled (trade-off for the shield)
        if (healthAttr != null && Config.mageShieldEnabled
                && healthAttr.getModifier(SHIELD_HEALTH_PENALTY_ID) == null) {
            healthAttr.addPermanentModifier(new AttributeModifier(
                    SHIELD_HEALTH_PENALTY_ID,
                    "Mage shield health penalty",
                    -0.50,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            if (maid.getHealth() > maid.getMaxHealth()) {
                maid.setHealth(maid.getMaxHealth());
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
            healthAttr.removeModifier(SHIELD_HEALTH_PENALTY_ID);
            if (maid.getHealth() > maid.getMaxHealth()) {
                maid.setHealth(maid.getMaxHealth());
            }
        }

        var attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            if (attackAttr.getModifier(BASE_ATTACK_BOOST_ID) != null) {
                attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
            }

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
        AttributeInstance attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr == null) return;

        if (attackAttr.getModifier(BASE_ATTACK_BOOST_ID) != null) {
            attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
        }


        AttributeModifier baseAttackMod = new AttributeModifier(
                BASE_ATTACK_BOOST_ID,
                "DPS base attack boost",
                BASE_ATTACK_INCREASE_PERCENTAGE,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        attackAttr.addPermanentModifier(baseAttackMod);

    }

}
