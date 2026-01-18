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

import java.util.Map;
import java.util.UUID;

public class MasterModifierBehavior extends Behavior<EntityMaid> {
    // Health boost (no reduction like DPS/Tank)
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("1bf2c8c1-e98b-5b7d-b4b4-776f037f4e04");
    
    // Attack boosts (combining DPS benefits)
    private static final UUID BASE_ATTACK_BOOST_ID = UUID.fromString("2cf3d9d2-fa9c-6c8e-c5c5-887f148f5f15");
    private static final UUID OFFHAND_ATTACK_BOOST_ID = UUID.fromString("3dg4eae3-gb0d-7d9f-d6d6-998f259f6g26");
    
    // Speed boost
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("4eh5fbf4-hc1e-8e0g-e7e7-009f360f7h37");

    // Master Maid has the best of all worlds - no downsides!
    private static final double HEALTH_INCREASE_PERCENTAGE = 0.50; // +50% max health (Tank benefit without downside)
    private static final double BASE_ATTACK_INCREASE_PERCENTAGE = 0.50; // +50% attack (better than DPS 35%)
    private static final double OFFHAND_ATTACK_INCREASE_PERCENTAGE = 0.25; // +25% offhand boost (better than DPS 15%)
    private static final double SPEED_INCREASE_PERCENTAGE = 0.20; // +20% movement speed

    public MasterModifierBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        // Increase Max Health (no reduction!)
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_MODIFIER_ID) == null) {
            double increaseAmount = healthAttr.getBaseValue() * HEALTH_INCREASE_PERCENTAGE;
            AttributeModifier healthMod = new AttributeModifier(
                    HEALTH_MODIFIER_ID,
                    "Master Maid health boost",
                    increaseAmount,
                    AttributeModifier.Operation.ADDITION
            );
            healthAttr.addPermanentModifier(healthMod);
            
            // Heal to new max health
            maid.setHealth(maid.getMaxHealth());
        }

        // Increase Movement Speed
        var speedAttr = maid.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null && speedAttr.getModifier(SPEED_MODIFIER_ID) == null) {
            AttributeModifier speedMod = new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    "Master Maid speed boost",
                    SPEED_INCREASE_PERCENTAGE,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            speedAttr.addPermanentModifier(speedMod);
        }

        updateAttackBoosts(maid);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        updateAttackBoosts(maid);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        // Remove all modifiers
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            if (maid.getHealth() > maid.getMaxHealth()) {
                maid.setHealth(maid.getMaxHealth());
            }
        }

        var speedAttr = maid.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_ID);
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

        // Remove existing modifiers
        if (attackAttr.getModifier(BASE_ATTACK_BOOST_ID) != null) {
            attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
        }
        if (attackAttr.getModifier(OFFHAND_ATTACK_BOOST_ID) != null) {
            attackAttr.removeModifier(OFFHAND_ATTACK_BOOST_ID);
        }

        // Apply base attack boost
        AttributeModifier baseAttackMod = new AttributeModifier(
                BASE_ATTACK_BOOST_ID,
                "Master Maid base attack boost",
                BASE_ATTACK_INCREASE_PERCENTAGE,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        attackAttr.addPermanentModifier(baseAttackMod);

        // Apply offhand boost if applicable
        if (isOffhandSwordOrAxe(maid)) {
            AttributeModifier offhandMod = new AttributeModifier(
                    OFFHAND_ATTACK_BOOST_ID,
                    "Master Maid offhand weapon boost",
                    OFFHAND_ATTACK_INCREASE_PERCENTAGE,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            attackAttr.addPermanentModifier(offhandMod);
        }
    }

    private boolean isOffhandSwordOrAxe(EntityMaid maid) {
        ItemStack offhandItem = maid.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offhandItem.isEmpty()) return false;
        Item item = offhandItem.getItem();
        return item instanceof SwordItem || item instanceof AxeItem;
    }
}
