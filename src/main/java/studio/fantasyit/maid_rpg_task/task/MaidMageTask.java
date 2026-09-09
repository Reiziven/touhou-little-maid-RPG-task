package studio.fantasyit.maid_rpg_task.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidAttackStrafingTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidRangedWalkToTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidShootTargetTask;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuShoot;
import com.github.tartaricacid.touhoulittlemaid.datagen.EnchantmentKeys;
import com.github.tartaricacid.touhoulittlemaid.item.ItemHakureiGohei;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import studio.fantasyit.maid_rpg_task.util.EnchantUtil;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.behavior.ElementalSpellBehavior;
import studio.fantasyit.maid_rpg_task.behavior.GoheiAttackBehavior;
import studio.fantasyit.maid_rpg_task.behavior.MageBehavior;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class MaidMageTask implements IRangedAttackTask {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "mage_task");

    @Override
    public ResourceLocation getUid() { return UID; }

    @Override
    public ItemStack getIcon() { return InitItems.SANAE_GOHEI.get().getDefaultInstance(); }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.attackSound(maid, InitSounds.MAID_DANMAKU_ATTACK.get(), 0.5f);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of(1, new MageBehavior()),
                Pair.of(5, StartAttacking.create(this::hasGohei, IRangedAttackTask::findFirstValidAttackTarget)),
                Pair.of(5, StopAttackingIfTargetInvalid.create(target -> !hasGohei(maid) || farAway(target, maid))),
                Pair.of(5, MaidRangedWalkToTarget.create(0.6f)),
                Pair.of(5, new MaidAttackStrafingTask()),
                Pair.of(5, new MaidShootTargetTask(6)),
                Pair.of(4, new ElementalSpellBehavior())
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of(5, StartAttacking.create(this::hasGohei, IRangedAttackTask::findFirstValidAttackTarget)),
                Pair.of(5, StopAttackingIfTargetInvalid.create(target -> !hasGohei(maid) || farAway(target, maid))),
                Pair.of(5, new MaidShootTargetTask(6)),
                Pair.of(4, new ElementalSpellBehavior())
        );
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        return IRangedAttackTask.targetConditionsTest(maid, target, MaidConfig.DANMAKU_RANGE);
    }

    @Override
    public AABB searchDimension(EntityMaid maid) {
        if (hasGohei(maid)) {
            float range = this.searchRadius(maid);
            return maid.hasRestriction()
                    ? new AABB(maid.getRestrictCenter()).inflate(range)
                    : maid.getBoundingBox().inflate(range);
        }
        return IRangedAttackTask.super.searchDimension(maid);
    }

    @Override
    public float searchRadius(EntityMaid maid) { return MaidConfig.DANMAKU_RANGE.get(); }

    @Override
    public void performRangedAttack(EntityMaid shooter, LivingEntity target, float distanceFactor) {
        shooter.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent(livingEntities -> {
            ItemStack mainHandItem = shooter.getMainHandItem();
            if (!ItemHakureiGohei.isGohei(mainHandItem)) return;
            long entityCount = livingEntities.stream().filter(test -> enemyEntityTest(shooter, target, test)).count();
            Level level = shooter.level();
            AttributeInstance attackDamage = shooter.getAttribute(Attributes.ATTACK_DAMAGE);
            float attackValue = attackDamage != null ? (float) attackDamage.getBaseValue() : 2.0f;
            int impedingLevel = EnchantUtil.getLevel(shooter.level(), EnchantmentKeys.IMPEDING, mainHandItem);
            int speedyLevel = EnchantUtil.getLevel(shooter.level(), EnchantmentKeys.SPEEDY, mainHandItem);
            int multiShotLevel = EnchantUtil.getLevel(shooter.level(), Enchantments.MULTISHOT, mainHandItem);
            int endersEnderLevel = EnchantUtil.getLevel(shooter.level(), EnchantmentKeys.ENDERS_ENDER, mainHandItem);
            float speed = (0.3f * (distanceFactor + 1)) * (speedyLevel + 1);
            boolean hurtEnderman = endersEnderLevel > 0;
            float distance = shooter.distanceTo(target);
            speed = speed + Mth.clamp(distance / 40f - 0.4f, 0, 2.4f);
            float inaccuracy = 1 - Mth.clamp(distance / 100f, 0, 0.8f);

            if (entityCount <= 1) {
                if (multiShotLevel > 0) {
                    DanmakuShoot.create().setWorld(level).setThrower(shooter).setTarget(target)
                            .setRandomColor().setRandomType().setDamage(attackValue * (distanceFactor + 1.2f))
                            .setGravity(0).setVelocity(speed).setHurtEnderman(hurtEnderman)
                            .setInaccuracy(inaccuracy).setFanNum(3).setYawTotal(Math.PI / 12)
                            .setImpedingLevel(impedingLevel).fanShapedShot();
                } else {
                    DanmakuShoot.create().setWorld(level).setThrower(shooter).setTarget(target)
                            .setRandomColor().setRandomType().setDamage(attackValue * (distanceFactor + 1))
                            .setGravity(0).setVelocity(speed).setHurtEnderman(hurtEnderman)
                            .setInaccuracy(inaccuracy / 5).setImpedingLevel(impedingLevel).aimedShot();
                }
            } else if (entityCount <= 5) {
                DanmakuShoot.create().setWorld(level).setThrower(shooter).setTarget(target)
                        .setRandomColor().setRandomType().setDamage(attackValue * (distanceFactor + 1.2f))
                        .setGravity(0).setVelocity(speed).setHurtEnderman(hurtEnderman)
                        .setInaccuracy(inaccuracy / 5).setFanNum(8).setYawTotal(Math.PI / 3)
                        .setImpedingLevel(impedingLevel).fanShapedShot();
            } else {
                DanmakuShoot.create().setWorld(level).setThrower(shooter).setTarget(target)
                        .setRandomColor().setRandomType().setDamage(attackValue * (distanceFactor + 1.5f))
                        .setGravity(0).setVelocity(speed).setHurtEnderman(hurtEnderman)
                        .setInaccuracy(inaccuracy / 5).setFanNum(32).setYawTotal(2 * Math.PI / 3)
                        .setImpedingLevel(impedingLevel).fanShapedShot();
            }
            mainHandItem.hurtAndBreak(1, shooter, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
        });
    }

    @Override
    public boolean isWeapon(EntityMaid maid, ItemStack stack) { return ItemHakureiGohei.isGohei(stack); }

    private boolean enemyEntityTest(EntityMaid shooter, LivingEntity target, LivingEntity test) {
        return shooter.canAttack(test) && target.getType().equals(test.getType()) && shooter.canSee(test);
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Collections.singletonList(Pair.of("has_gohei", this::hasGohei));
    }

    private boolean hasGohei(EntityMaid maid) { return ItemHakureiGohei.isGohei(maid.getMainHandItem()); }

    private boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > this.searchRadius(maid);
    }
}
