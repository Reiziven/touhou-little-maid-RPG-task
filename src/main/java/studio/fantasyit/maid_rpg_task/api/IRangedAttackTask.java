package studio.fantasyit.maid_rpg_task.api;

import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Optional;

public interface IRangedAttackTask extends IAttackTask {

    TargetingConditions TARGET_CONDITIONS = TargetingConditions.forCombat();

    static Optional<? extends LivingEntity> findFirstValidAttackTarget(EntityMaid maid) {
        if (maid.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).isPresent()) {
            List<LivingEntity> list = maid.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
            return list.stream().filter(e -> maid.canAttack(e) && maid.canSee(e)).findAny();
        }
        return Optional.empty();
    }

    static boolean targetConditionsTest(EntityMaid maid, LivingEntity target, ModConfigSpec.IntValue configRange) {
        TARGET_CONDITIONS.range(configRange.get());
        return TARGET_CONDITIONS.test(maid, target);
    }

    void performRangedAttack(EntityMaid shooter, LivingEntity target, float distanceFactor);

    default boolean canSee(EntityMaid maid, LivingEntity target) {
        return BehaviorUtils.canSee(maid, target);
    }
}
