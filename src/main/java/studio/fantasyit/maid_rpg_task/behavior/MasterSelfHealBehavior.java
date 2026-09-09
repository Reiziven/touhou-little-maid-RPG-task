package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.entity.HealMagicCircleEntity;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;

import java.util.Map;

public class MasterSelfHealBehavior extends Behavior<EntityMaid> {

    private long lastHealTime = -1;

    public MasterSelfHealBehavior() {
        super(Map.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!Config.masterEnableHealing) return false;
        float hpPercent = maid.getHealth() / maid.getMaxHealth();
        if (hpPercent >= (float) Config.masterHealThreshold) return false;
        long now = level.getGameTime();
        return lastHealTime < 0 || now - lastHealTime >= Config.masterHealCooldown;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        float heal = Config.supportFlatHeal
                ? (float) Config.masterHealAmount
                : maid.getMaxHealth() * (float) Config.masterHealAmount;
        HealMagicCircleEntity circle = new HealMagicCircleEntity(EntityRegistry.HEAL_MAGIC_CIRCLE.get(), level);
        circle.setTarget(maid, heal);
        level.addFreshEntity(circle);
        lastHealTime = gameTime;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) { return false; }

    @Override
    protected boolean timedOut(long gameTime) { return false; }
}
