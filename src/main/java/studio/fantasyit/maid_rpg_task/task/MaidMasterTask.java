package studio.fantasyit.maid_rpg_task.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidUseShieldTask;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityExtinguishingAgent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.behavior.*;



import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidUseShieldTask;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityExtinguishingAgent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

import java.util.List;
import java.util.function.Predicate;

public class MaidMasterTask implements IAttackTask {
    public static final ResourceLocation UID = new ResourceLocation(MaidRpgTask.MODID, "master_maid");
    private static final int MAX_STOP_ATTACK_DISTANCE = 12; // Longer range than others

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.NETHER_STAR.getDefaultInstance(); // Nether Star represents mastery
    }

    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.attackSound(maid, InitSounds.MAID_ATTACK.get(), 0.5f);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        // Combining all the best behaviors from DPS, Tank, and Support
        
        // Core attack behaviors (from DPS)
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasAssaultWeapon, IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create(target -> !hasAssaultWeapon(maid) || farAway(target, maid));
        BehaviorControl<Mob> moveToTargetTask = SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(0.7f); // Faster movement
        BehaviorControl<Mob> attackTargetTask = MeleeAttack.create(15); // Faster attack speed (between DPS 10 and Tank 40)
        
        // Shield usage (from Tank)
        MaidUseShieldTask maidUseShieldTask = new MaidUseShieldTask();
        
        // Tank behaviors (aggro, redirect, stun)
        BehaviorControl<EntityMaid> tankAggroBehavior = new TankAggroBehavior();
        BehaviorControl<EntityMaid> tankRedirectTask = new TankRedirectBehavior();
        BehaviorControl<EntityMaid> stunBehavior = new StunBehavior();
        
        // Support behaviors (revive and effects)
        BehaviorControl<EntityMaid> playerReviveBehavior = new PlayerReviveBehavior();
        BehaviorControl<EntityMaid> supportEffectBehavior = new SupportEffectBehavior();
        
        // Master modifier (best stats, no downsides)
        BehaviorControl<EntityMaid> masterModifierTask = new MasterModifierBehavior();

        return Lists.newArrayList(
                // Highest priority: Master modifier
                Pair.of(1, masterModifierTask),
                
                // Support behaviors (can revive and buff)
                Pair.of(2, playerReviveBehavior),
                Pair.of(2, supportEffectBehavior),
                
                // Tank behaviors (can tank and control aggro)
                Pair.of(3, tankAggroBehavior),
                Pair.of(3, tankRedirectTask),
                Pair.of(3, stunBehavior),
                
                // Core combat (DPS capabilities)
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, moveToTargetTask),
                Pair.of(5, attackTargetTask),
                Pair.of(5, maidUseShieldTask)
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        // Similar to walking tasks but without movement
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasAssaultWeapon, IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create(target -> !hasAssaultWeapon(maid) || farAway(target, maid));
        BehaviorControl<Mob> attackTargetTask = MeleeAttack.create(15);
        MaidUseShieldTask maidUseShieldTask = new MaidUseShieldTask();
        
        BehaviorControl<EntityMaid> tankAggroBehavior = new TankAggroBehavior();
        BehaviorControl<EntityMaid> tankRedirectTask = new TankRedirectBehavior();
        BehaviorControl<EntityMaid> stunBehavior = new StunBehavior();
        BehaviorControl<EntityMaid> playerReviveBehavior = new PlayerReviveBehavior();
        BehaviorControl<EntityMaid> supportEffectBehavior = new SupportEffectBehavior();
        BehaviorControl<EntityMaid> masterModifierTask = new MasterModifierBehavior();

        return Lists.newArrayList(
                Pair.of(1, masterModifierTask),
                Pair.of(2, playerReviveBehavior),
                Pair.of(2, supportEffectBehavior),
                Pair.of(3, tankAggroBehavior),
                Pair.of(3, tankRedirectTask),
                Pair.of(3, stunBehavior),
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, attackTargetTask),
                Pair.of(5, maidUseShieldTask)
        );
    }

    @Override
    public boolean hasExtraAttack(EntityMaid maid, Entity target) {
        return maid.getOffhandItem().is(InitItems.EXTINGUISHER.get()) && target.fireImmune();
    }

    @Override
    public boolean doExtraAttack(EntityMaid maid, Entity target) {
        Level world = maid.level();
        AABB aabb = target.getBoundingBox().inflate(1.5, 1, 1.5);
        List<EntityExtinguishingAgent> extinguishingAgents = world.getEntitiesOfClass(EntityExtinguishingAgent.class, aabb, Entity::isAlive);
        if (extinguishingAgents.isEmpty()) {
            world.addFreshEntity(new EntityExtinguishingAgent(world, target.position()));
            maid.getOffhandItem().hurtAndBreak(1, maid, (m) -> m.broadcastBreakEvent(InteractionHand.OFF_HAND));
            return true;
        }
        return false;
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of("assault_weapon", this::hasAssaultWeapon),
                Pair.of("extinguisher", this::hasExtinguisher)
        );
    }

    private boolean hasAssaultWeapon(EntityMaid maid) {
        return isWeapon(maid, maid.getMainHandItem());
    }

    private boolean isWeapon(EntityMaid maid, ItemStack mainHandItem) {
        return true; // Master Maid can use any weapon
    }

    private boolean hasExtinguisher(EntityMaid maid) {
        return maid.getOffhandItem().is(InitItems.EXTINGUISHER.get());
    }

    private boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > MAX_STOP_ATTACK_DISTANCE;
    }
}
