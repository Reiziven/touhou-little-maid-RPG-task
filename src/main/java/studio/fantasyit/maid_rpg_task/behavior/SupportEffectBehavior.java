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
import studio.fantasyit.maid_rpg_task.entity.HealMagicCircleEntity;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SupportEffectBehavior extends Behavior<EntityMaid> {

    /** Tracks the last game-time a heal circle was cast on each target UUID, to enforce per-target cooldown. */
    private final Map<UUID, Long> healCooldowns = new HashMap<>();
    public SupportEffectBehavior() {
        super(Map.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (maid.getExperience() <= 0 && Config.supportHealRequiresXp) return false;
        LivingEntity owner = maid.getOwner();
        if (owner == null) return false;

        // Always stay active so the maid can heal herself (and allies when configured)
        if (Config.supportEnableHealing) return true;

        // The maid herself counts: if she has a target she is in combat
        if (maid.getTarget() != null) return true;

        List<Entity> nearby = level.getEntities(maid, AABB.ofSize(maid.position(), 16, 16, 16));
        for (Entity e : nearby) {
            if (!(e instanceof LivingEntity living) || !living.isAlive()) continue;
            // start if any enemy is targeting an ally, or any ally is targeting an enemy
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
            if (inCombat && maid.getExperience() > 0) {
                maid.setExperience(maid.getExperience() - 1);
                reApplyEffects(level, maid);
                level.sendParticles(ParticleTypes.ENCHANT,
                        maid.getX(), maid.getY() + 1, maid.getZ(),
                        5, 0.3, 0.3, 0.3, 0.1);
            }
            // Heal triggers regardless of combat state (guarded by supportHealOutsideCombat logic upstream)
            if (Config.supportEnableHealing) {
                tickHealing(level, maid, gameTime);
            }
        }
    }

    private void reApplyEffects(ServerLevel level, EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        if (owner == null) return;

        List<Entity> entities = level.getEntities(maid, AABB.ofSize(maid.position(), 16, 16, 16));

        // Collect allies and enemies in combat with allies
        List<LivingEntity> allies = new ArrayList<>();
        List<Mob> enemies = new ArrayList<>();

        // Always include the maid herself as an ally
        allies.add(maid);

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (isAlly(living, owner)) {
                allies.add(living);
            }
        }

        // An enemy qualifies for debuff if it's targeting any ally OR any ally is targeting it
        for (Entity entity : entities) {
            if (!(entity instanceof Mob mob) || !mob.isAlive()) continue;
            if (isAlly(mob, owner)) continue;
            if (isTargetingAlly(mob, owner)) {
                enemies.add(mob);
                continue;
            }
            // check if any ally is attacking this mob
            for (LivingEntity ally : allies) {
                if (ally instanceof Mob allyMob && mob.equals(allyMob.getTarget())) {
                    enemies.add(mob);
                    break;
                }
            }
        }

        // Apply debuff to enemies
        for (Mob enemy : enemies) {
            enemy.addEffect(new MobEffectInstance(EffectRegistry.DAMAGE_DECREASE.get(), 30, Config.supportDamageDecreaseLevel, false, true));
        }

        // Apply buff to allies that are in combat:
        // - they are targeting a non-ally enemy, OR
        // - a qualified enemy is targeting them
        for (LivingEntity ally : allies) {
            boolean inCombat = false;
            // ally is attacking something that isn't another ally
            if (ally instanceof Mob allyMob) {
                LivingEntity allyTarget = allyMob.getTarget();
                if (allyTarget != null && !isAlly(allyTarget, owner)) inCombat = true;
            }
            // for the player (non-Mob), use last hurt mob as proxy for "attacking"
            if (!inCombat && ally.equals(owner)) {
                LivingEntity lastHurt = owner.getLastHurtMob();
                if (lastHurt != null && lastHurt.isAlive() && !isAlly(lastHurt, owner)
                        && owner.getLastHurtMobTimestamp() > owner.tickCount - 60) {
                    inCombat = true;
                }
            }
            // a qualified enemy is targeting this ally
            if (!inCombat) {
                for (Mob enemy : enemies) {
                    LivingEntity et = enemy.getTarget();
                    if (et != null && et.equals(ally)) { inCombat = true; break; }
                }
            }
            if (inCombat) {
                ally.addEffect(new MobEffectInstance(EffectRegistry.DEFENCE_UP.get(), 90, Config.supportDefenceUpLevel, false, false));
                ally.addEffect(new MobEffectInstance(EffectRegistry.STRENGTHENED.get(), 90, Config.supportStrengthenedLevel, false, false));
            }
        }
    }

    private void tickHealing(ServerLevel level, EntityMaid maid, long gameTime) {
        // XP gate: maid must have experience to cast heals
        if (Config.supportHealRequiresXp && maid.getExperience() <= 0) return;

        LivingEntity owner = maid.getOwner();
        if (owner == null) return;

        float threshold = (float) Config.supportHealThreshold;
        int cooldown = Config.supportHealCooldown;

        boolean inCombat = isInCombat(level, maid);

        // Build ally list — maid always heals herself regardless of combat state.
        List<LivingEntity> allies = new ArrayList<>();
        allies.add(maid);
        List<Entity> nearby = level.getEntities(maid, AABB.ofSize(maid.position(), 16, 16, 16));
        for (Entity e : nearby) {
            if (e instanceof LivingEntity living && living.isAlive() && isAlly(living, owner)) {
                // Only heal other allies outside combat when supportHealOutsideCombat is true
                if (inCombat || Config.supportHealOutsideCombat) allies.add(living);
            }
        }

        for (LivingEntity ally : allies) {
            float hpPercent = ally.getHealth() / ally.getMaxHealth();
            if (hpPercent >= threshold) continue;

            UUID id = ally.getUUID();
            Long lastHeal = healCooldowns.get(id);
            if (lastHeal != null && gameTime - lastHeal < cooldown) continue;

            float healAmount = (float)(ally.getMaxHealth() * Config.supportHealAmount);
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

    /** Returns true if the mob's target is the owner, a tamed pet, or a maid owned by the player. */
    private boolean isTargetingAlly(Mob mob, LivingEntity owner) {        LivingEntity target = mob.getTarget();
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
    protected boolean timedOut(long time) {
        return false;
    }
}
