package studio.fantasyit.maid_rpg_task.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.data.MaidReviveConfig;
import studio.fantasyit.maid_rpg_task.entity.HealMagicCircleEntity;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;

import java.util.*;

public class SupportEffectBehavior extends Behavior<EntityMaid> {

    private final Map<UUID, Long> healCooldowns = new HashMap<>();

    public SupportEffectBehavior() {
        super(Map.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (maid.getExperience() <= 0 && Config.supportHealRequiresXp) return false;
        LivingEntity owner = maid.getOwner();
        if (owner == null) return false;
        if (Config.supportEnableHealing) return true;
        if (maid.getTarget() != null) return true;
        List<Entity> nearby = level.getEntities(maid, AABB.ofSize(maid.position(), 16, 16, 16));
        for (Entity e : nearby) {
            if (!(e instanceof LivingEntity living) || !living.isAlive()) continue;
            if (e instanceof Mob mob && isTargetingAlly(mob, owner)) return true;
            if (isAlly(living, owner) && living instanceof Mob allyMob && allyMob.getTarget() != null) return true;
        }
        return false;
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        super.tick(level, maid, gameTime);
        if (gameTime % 20 == 0) {
            boolean inCombat = isInCombat(level, maid);
            if (inCombat) {
                if (maid.getExperience() > 0) {
                    maid.setExperience(maid.getExperience() - Config.supportXpCost);
                    level.sendParticles(ParticleTypes.ENCHANT,
                            maid.getX(), maid.getY() + 1, maid.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
                }
                reApplyEffects(level, maid);
            }
            if (Config.supportEnableHealing) tickHealing(level, maid, gameTime);
        }
    }

    private void reApplyEffects(ServerLevel level, EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        if (owner == null) return;

        List<Entity> entities = level.getEntities(maid, AABB.ofSize(maid.position(), 16, 16, 16));
        List<LivingEntity> allies = new ArrayList<>();
        List<Mob> enemies = new ArrayList<>();
        allies.add(maid);

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (isAlly(living, owner)) allies.add(living);
        }

        for (Entity entity : entities) {
            if (!(entity instanceof Mob mob) || !mob.isAlive()) continue;
            if (isAlly(mob, owner)) continue;
            if (isTargetingAlly(mob, owner)) { enemies.add(mob); continue; }
            for (LivingEntity ally : allies) {
                if (ally instanceof Mob allyMob && mob.equals(allyMob.getTarget())) {
                    enemies.add(mob); break;
                }
            }
        }

        if (!Config.supportDamageDecreaseRequiresXp || maid.getExperience() > 0) {
            for (Mob enemy : enemies) {
                enemy.addEffect(new MobEffectInstance(EffectRegistry.DAMAGE_DECREASE, 30,
                        Config.supportDamageDecreaseLevel, false, true));
            }
        }

        MaidReviveConfig.Data cfg = maid.getOrCreateData(MaidReviveConfig.KEY, MaidReviveConfig.Data.getDefault());
        for (LivingEntity ally : allies) {
            boolean isSelf = ally.equals(maid);
            boolean isOwner = ally.equals(owner);
            boolean allowed = isSelf ? cfg.buffSelf() : (isOwner ? cfg.buffOwner() : cfg.buffAllies());
            if (!allowed) continue;
            boolean inCombat = false;
            if (ally instanceof Mob allyMob) {
                LivingEntity t = allyMob.getTarget();
                if (t != null && !isAlly(t, owner)) inCombat = true;
            }
            if (!inCombat && isOwner) {
                LivingEntity lastHurt = owner.getLastHurtMob();
                if (lastHurt != null && lastHurt.isAlive() && !isAlly(lastHurt, owner)
                        && owner.getLastHurtMobTimestamp() > owner.tickCount - 60) {
                    inCombat = true;
                }
            }
            if (!inCombat) {
                for (Mob enemy : enemies) {
                    LivingEntity et = enemy.getTarget();
                    if (et != null && et.equals(ally)) { inCombat = true; break; }
                }
            }
            if (inCombat && (!Config.supportBuffRequiresXp || maid.getExperience() > 0)) {
                ally.addEffect(new MobEffectInstance(EffectRegistry.DEFENCE_UP, 90,
                        Config.supportDefenceUpLevel, false, false));
                ally.addEffect(new MobEffectInstance(EffectRegistry.STRENGTHENED, 90,
                        Config.supportStrengthenedLevel, false, false));
            }
        }
    }

    private void tickHealing(ServerLevel level, EntityMaid maid, long gameTime) {
        if (Config.supportHealRequiresXp && maid.getExperience() <= 0) return;
        LivingEntity owner = maid.getOwner();
        if (owner == null) return;

        float threshold = (float) Config.supportHealThreshold;
        int cooldown = Config.supportHealCooldown;
        boolean inCombat = isInCombat(level, maid);
        MaidReviveConfig.Data cfg = maid.getOrCreateData(MaidReviveConfig.KEY, MaidReviveConfig.Data.getDefault());

        List<LivingEntity> allies = new ArrayList<>();
        if (cfg.healSelf()) allies.add(maid);
        List<Entity> nearby = level.getEntities(maid, AABB.ofSize(maid.position(), 16, 16, 16));
        for (Entity e : nearby) {
            if (e instanceof LivingEntity living && living.isAlive() && isAlly(living, owner)) {
                boolean isOwner = living.equals(owner);
                if (isOwner ? !cfg.healOwner() : !cfg.healAllies()) continue;
                if (inCombat || Config.supportHealOutsideCombat) allies.add(living);
            }
        }

        for (LivingEntity ally : allies) {
            float hpPercent = ally.getHealth() / ally.getMaxHealth();
            if (hpPercent >= threshold) continue;
            UUID id = ally.getUUID();
            Long lastHeal = healCooldowns.get(id);
            if (lastHeal != null && gameTime - lastHeal < cooldown) continue;
            float rawAmount = ally.getMaxHealth() >= Config.supportLargeEntityHealthThreshold
                    ? (float) Config.supportLargeEntityHealAmount : (float) Config.supportHealAmount;
            float healAmount = Config.supportFlatHeal ? rawAmount : ally.getMaxHealth() * rawAmount;
            HealMagicCircleEntity circle = new HealMagicCircleEntity(EntityRegistry.HEAL_MAGIC_CIRCLE.get(), level);
            circle.setTarget(ally, healAmount);
            level.addFreshEntity(circle);
            healCooldowns.put(id, gameTime);
        }
    }

    private boolean isInCombat(ServerLevel level, EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        if (owner == null) return false;
        if (maid.getTarget() != null) return true;
        List<Entity> nearby = level.getEntities(maid, AABB.ofSize(maid.position(), 16, 16, 16));
        for (Entity e : nearby) {
            if (!(e instanceof LivingEntity living) || !living.isAlive()) continue;
            if (e instanceof Mob mob && isTargetingAlly(mob, owner)) return true;
            if (isAlly(living, owner) && living instanceof Mob allyMob && allyMob.getTarget() != null) return true;
        }
        return false;
    }

    private boolean isTargetingAlly(Mob mob, LivingEntity owner) {
        LivingEntity target = mob.getTarget();
        return target != null && isAlly(target, owner);
    }

    private boolean isAlly(LivingEntity entity, LivingEntity owner) {
        return entity.equals(owner) || isTamedByPlayer(entity, owner) || isPlayerMaid(entity, owner);
    }

    private boolean isTamedByPlayer(LivingEntity entity, LivingEntity owner) {
        return entity instanceof TamableAnimal animal && owner.equals(animal.getOwner());
    }

    private boolean isPlayerMaid(LivingEntity entity, LivingEntity owner) {
        return entity instanceof EntityMaid m && owner.equals(m.getOwner());
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {}

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return checkExtraStartConditions(level, maid);
    }

    @Override
    protected boolean timedOut(long time) { return false; }
}
