package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
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
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

import java.util.Map;

public class DpsModifierBehavior extends Behavior<EntityMaid> {
    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "dps_health_reduction");
    private static final ResourceLocation BASE_ATTACK_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "dps_base_attack_boost");
    private static final ResourceLocation OFFHAND_ATTACK_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "dps_offhand_attack_boost");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "dps_attack_speed");

    private static final double HEALTH_REDUCTION_PERCENTAGE = 0.80;

    public DpsModifierBehavior() {
        super(Map.of());
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
                // ADD_MULTIPLIED_BASE recomputes live against the current base value, so it
                // stays correct after favorability changes base max health — unlike ADD_VALUE
                // with a flat amount snapshotted once at start(), which goes stale until the
                // next task switch.
                healthAttr.addPermanentModifier(new AttributeModifier(
                        HEALTH_MODIFIER_ID, -HEALTH_REDUCTION_PERCENTAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
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
            if (maid.getHealth() > maid.getMaxHealth()) maid.setHealth(maid.getMaxHealth());
        }
        var attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
            attackAttr.removeModifier(OFFHAND_ATTACK_BOOST_ID);
        }
        var speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(ATTACK_SPEED_MODIFIER_ID);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) { return true; }

    @Override
    protected boolean timedOut(long gameTime) { return false; }

    private void updateAttackBoosts(EntityMaid maid) {
        double attackMult  = Config.survivalBalanced ? Config.dpsBaseAttackBoost   / 2.0 : Config.dpsBaseAttackBoost;
        double offhandMult = Config.survivalBalanced ? Config.dpsOffhandAttackBoost / 2.0 : Config.dpsOffhandAttackBoost;
        double speedMult   = Config.survivalBalanced ? Config.dpsAttackSpeedBoost   / 2.0 : Config.dpsAttackSpeedBoost;

        AttributeInstance attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
            attackAttr.removeModifier(OFFHAND_ATTACK_BOOST_ID);
            attackAttr.addPermanentModifier(new AttributeModifier(
                    BASE_ATTACK_BOOST_ID, attackMult, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if (isOffhandSwordOrAxe(maid)) {
                attackAttr.addPermanentModifier(new AttributeModifier(
                        OFFHAND_ATTACK_BOOST_ID, offhandMult, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }

        AttributeInstance speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(ATTACK_SPEED_MODIFIER_ID) == null) {
            speedAttr.addPermanentModifier(new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID, speedMult, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private boolean isOffhandSwordOrAxe(EntityMaid maid) {
        ItemStack offhandItem = maid.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offhandItem.isEmpty()) return false;
        Item item = offhandItem.getItem();
        return item instanceof SwordItem || item instanceof AxeItem;
    }
}
