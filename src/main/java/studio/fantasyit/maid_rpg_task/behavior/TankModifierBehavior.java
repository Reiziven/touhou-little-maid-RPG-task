package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

import java.util.Map;

public class TankModifierBehavior extends Behavior<EntityMaid> {
    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "tank_health_boost");
    private static final ResourceLocation ATTACK_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "tank_attack_penalty");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "tank_speed_penalty");

    private static final double HEALTH_REDUCTION_PERCENTAGE = -0.8;
    private static final double ATTACK_INCREASE_PERCENTAGE = -1.84;
    private static final double ATTACK_SPEED_PENALTY = -0.30;

    public TankModifierBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) { return true; }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_MODIFIER_ID) == null) {
            // ADD_MULTIPLIED_BASE recomputes live against the current base value, so it stays
            // correct after favorability changes base max health — unlike ADD_VALUE with a
            // flat amount snapshotted once at start(), which goes stale until the next task switch.
            healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, -HEALTH_REDUCTION_PERCENTAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            if (maid.getHealth() > maid.getMaxHealth()) maid.setHealth(maid.getMaxHealth());
        }

        var attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null && attackAttr.getModifier(ATTACK_MODIFIER_ID) == null && Config.tankAttackPenalty) {
            attackAttr.addPermanentModifier(new AttributeModifier(
                    ATTACK_MODIFIER_ID, ATTACK_INCREASE_PERCENTAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        var speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(ATTACK_SPEED_MODIFIER_ID) == null && Config.tankAttackPenalty) {
            speedAttr.addPermanentModifier(new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID, ATTACK_SPEED_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            if (maid.getHealth() > maid.getMaxHealth()) maid.setHealth(maid.getMaxHealth());
        }
        var attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) attackAttr.removeModifier(ATTACK_MODIFIER_ID);
        var speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(ATTACK_SPEED_MODIFIER_ID);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) { return true; }

    @Override
    protected boolean timedOut(long gameTime) { return false; }
}
