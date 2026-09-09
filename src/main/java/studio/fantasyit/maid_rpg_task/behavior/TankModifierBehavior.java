package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_rpg_task.Config;

import java.util.Map;
import java.util.UUID;

public class TankModifierBehavior extends Behavior<EntityMaid> {
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("f0131a22-3e71-4786-84b2-27c079511f20");
    private static final UUID ATTACK_MODIFIER_ID = UUID.fromString("c62e3828-b4c9-4e9e-9207-e8cbd5e6765e");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final double HEALTH_REDUCTION_PERCENTAGE = -0.8; // increase max health by 70%
    private static final double ATTACK_INCREASE_PERCENTAGE = -1.84; // decrease attack by 50%
    private static final double ATTACK_SPEED_PENALTY = -0.30; // 30% slower attacks (tank is durable, not fast)

    public TankModifierBehavior() {
        super(Map.of()); // No required memory modules
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        // Reduce Max Health
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_MODIFIER_ID) == null) {
            // MULTIPLY_BASE recomputes live against the current base value, so it stays
            // correct after favorability changes base max health — unlike a flat ADDITION
            // amount snapshotted once at start(), which goes stale until the next task switch.
            AttributeModifier healthMod = new AttributeModifier(
                    HEALTH_MODIFIER_ID,
                    "Tank task health reduction",
                    -HEALTH_REDUCTION_PERCENTAGE,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );
            healthAttr.addPermanentModifier(healthMod);

            if (maid.getHealth() > maid.getMaxHealth()) {
                maid.setHealth(maid.getMaxHealth());
            }
        }

        // Increase Attack Damage
        var attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null && attackAttr.getModifier(ATTACK_MODIFIER_ID) == null) {
            if (Config.tankAttackPenalty) {
                AttributeModifier attackMod = new AttributeModifier(
                        ATTACK_MODIFIER_ID,
                        "Tank task attack boost",
                        ATTACK_INCREASE_PERCENTAGE,
                        AttributeModifier.Operation.MULTIPLY_BASE
                );
                attackAttr.addPermanentModifier(attackMod);
            }
        }

        // Apply attack speed penalty on top of weapon's own speed
        var speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(ATTACK_SPEED_MODIFIER_ID) == null) {
            if (Config.tankAttackPenalty) {
                AttributeModifier speedMod = new AttributeModifier(
                        ATTACK_SPEED_MODIFIER_ID,
                        "Tank attack speed penalty",
                        ATTACK_SPEED_PENALTY,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                speedAttr.addPermanentModifier(speedMod);
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        // Restore Max Health
        var healthAttr = maid.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            if (maid.getHealth() > maid.getMaxHealth()) {
                maid.setHealth(maid.getMaxHealth());
            }
        }

        // Remove Attack Boost
        var attackAttr = maid.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(ATTACK_MODIFIER_ID);
        }

        // Remove Attack Speed Penalty
        var speedAttr = maid.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null) {
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
}
