package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

import java.util.Map;

public class MageBehavior extends Behavior<EntityMaid> {
    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "mage_health_reduction");
    private static final ResourceLocation BASE_ATTACK_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "mage_attack_boost");
    private static final double HEALTH_REDUCTION_PERCENTAGE = 0.80;
    private static final double BASE_ATTACK_INCREASE_PERCENTAGE = 0.35;

    public MageBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) { return true; }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_MODIFIER_ID) == null && Config.mageHealthReduction) {
            // ADD_MULTIPLIED_BASE recomputes live against the current base value, so it stays
            // correct after favorability changes base max health — unlike ADD_VALUE with a
            // flat amount snapshotted once at start(), which goes stale until the next task switch.
            healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, -HEALTH_REDUCTION_PERCENTAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            if (maid.getHealth() > maid.getMaxHealth()) maid.setHealth(maid.getMaxHealth());
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
        if (attackAttr != null) attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) { return true; }

    @Override
    protected boolean timedOut(long gameTime) { return false; }

    private void updateAttackBoosts(EntityMaid maid) {
        AttributeInstance attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr == null) return;
        attackAttr.removeModifier(BASE_ATTACK_BOOST_ID);
        attackAttr.addPermanentModifier(new AttributeModifier(
                BASE_ATTACK_BOOST_ID, BASE_ATTACK_INCREASE_PERCENTAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
