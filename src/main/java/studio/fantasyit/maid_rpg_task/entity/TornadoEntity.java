package studio.fantasyit.maid_rpg_task.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.util.MagicDamageHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tornado elemental spell entity.
 *
 * - Lasts 20 seconds (400 ticks).
 * - Attraction radius: 10 blocks.
 * - Pulls valid targets up 4 blocks into the air and spins them around the center.
 * - Deals 5 damage per second (every 20 ticks), bypassing resistance + 50% armor.
 * - Only affects entities the maid is hostile toward (canAttack), excluding:
 *     - The maid's owner (player).
 *     - Allies: other players, tamed animals owned by the same owner.
 */
public class TornadoEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Integer> TICKS_ALIVE =
            SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.INT);

    /** 15 seconds */
    public static final int LIFETIME_TICKS = 300;
    public static final float DAMAGE_PER_SECOND = 5.0f;
    /** Horizontal attraction radius */
    public static final double PULL_RANGE = 10.0;
    /** Target altitude above tornado spawn Y */
    private static final double TARGET_HEIGHT = 4.0;
    /** Orbit radius while spinning */
    private static final double ORBIT_RADIUS = 2.0;
    /** Angular velocity (radians per tick) */
    private static final double SPIN_SPEED = 0.20;
    /** Upward pull per tick toward TARGET_HEIGHT */
    private static final double LIFT_STRENGTH = 0.18;
    /** Horizontal pull strength per tick */
    private static final double PULL_STRENGTH = 0.14;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** UUID of the maid that cast this */
    private UUID maidUuid;
    /** UUID of the maid's owner (player), may be null */
    private UUID ownerUuid;
    /** Tracks each captured target's current orbit angle (radians) */
    private final Map<UUID, Double> orbitAngles = new HashMap<>();

    public TornadoEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Called at spawn time; maid is the caster. */
    public void setMaid(LivingEntity maid) {
        this.maidUuid = maid.getUUID();
        if (maid instanceof TamableAnimal ta && ta.getOwnerUUID() != null) {
            this.ownerUuid = ta.getOwnerUUID();
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TICKS_ALIVE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        int ticks = this.entityData.get(TICKS_ALIVE) + 1;
        this.entityData.set(TICKS_ALIVE, ticks);

        if (!level().isClientSide) {
            List<LivingEntity> targets = getValidTargets();

            // Clean up orbit state for targets no longer in range
            if (ticks % 20 == 0) {
                java.util.Set<UUID> activeIds = new java.util.HashSet<>();
                for (LivingEntity t : targets) activeIds.add(t.getUUID());
                orbitAngles.keySet().retainAll(activeIds);
            }

            for (LivingEntity target : targets) {
                pullIntoTornado(target, ticks);
            }

            if (ticks % 20 == 0) {
                for (LivingEntity target : targets) {
                    MagicDamageHelper.dealMagicDamage(target, this.damageSources().magic(), DAMAGE_PER_SECOND);
                }
            }
        } else {
            spawnTornadoParticles(ticks);
        }

        if (ticks >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    private void pullIntoTornado(LivingEntity target, int ticks) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double distH = Math.sqrt(dx * dx + dz * dz);

        // Vertical: lift toward TARGET_HEIGHT above tornado Y
        double targetY = getY() + TARGET_HEIGHT;
        double deltaY = targetY - target.getY();
        double newVY;
        if (deltaY > 0.2) {
            newVY = Math.min(LIFT_STRENGTH, deltaY * 0.15);
        } else {
            newVY = 0.08;
        }

        // Once close enough horizontally, lock into orbit tracking
        if (distH < PULL_RANGE * 0.8) {
            // Initialize angle from current position if not yet tracked
            double angle = orbitAngles.computeIfAbsent(target.getUUID(),
                    id -> Math.atan2(dz, dx));
            // Advance the angle
            angle += SPIN_SPEED;
            orbitAngles.put(target.getUUID(), angle);

            // Drive the target to the exact orbit position
            double orbitX = getX() + Math.cos(angle) * ORBIT_RADIUS;
            double orbitZ = getZ() + Math.sin(angle) * ORBIT_RADIUS;

            double newVX = (orbitX - target.getX()) * 0.5;
            double newVZ = (orbitZ - target.getZ()) * 0.5;

            target.setDeltaMovement(newVX, newVY, newVZ);
        } else {
            // Still pulling in from outside — attract toward center
            double pullFactor = Math.min(1.0, distH / PULL_RANGE);
            double newVX = -dx / distH * (PULL_STRENGTH + pullFactor * 0.1);
            double newVZ = -dz / distH * (PULL_STRENGTH + pullFactor * 0.1);
            target.setDeltaMovement(newVX, newVY, newVZ);
        }

        target.hurtMarked = true;
        target.fallDistance = 0;
    }

    /**
     * Returns all living entities that:
     * 1. Are within the pull radius.
     * 2. Are not the maid or its owner.
     * 3. Are not allies (players on same team, tamed animals of same owner).
     * 4. The maid {@code canAttack} them OR they are the maid's current attack target.
     */
    private List<LivingEntity> getValidTargets() {
        AABB box = new AABB(
                getX() - PULL_RANGE, getY() - 1, getZ() - PULL_RANGE,
                getX() + PULL_RANGE, getY() + TARGET_HEIGHT + 2, getZ() + PULL_RANGE
        );

        // Find the maid to use canAttack()
        LivingEntity maid = findMaid();

        return level().getEntitiesOfClass(LivingEntity.class, box, entity -> {
            if (!entity.isAlive()) return false;
            // Never affect the maid itself
            if (maidUuid != null && entity.getUUID().equals(maidUuid)) return false;
            // Never affect the owner (player)
            if (ownerUuid != null && entity.getUUID().equals(ownerUuid)) return false;
            // Never affect other players (allies of the owner)
            if (entity instanceof Player) return false;
            // Never affect tamed animals owned by the same owner
            if (ownerUuid != null && entity instanceof TamableAnimal ta
                    && ownerUuid.equals(ta.getOwnerUUID())) return false;

            // Use maid.canAttack() if maid is available
            if (maid != null) {
                return maid.canAttack(entity);
            }
            return true;
        });
    }

    private LivingEntity findMaid() {
        if (maidUuid == null) return null;
        AABB searchBox = new AABB(
                getX() - 64, getY() - 32, getZ() - 64,
                getX() + 64, getY() + 32, getZ() + 64);
        return level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e.getUUID().equals(maidUuid))
                .stream().findFirst().orElse(null);
    }

    private void spawnTornadoParticles(int ticks) {
        double angle = ticks * 0.3;
        for (int layer = 0; layer < 3; layer++) {
            double r = 0.8 + layer * 0.5;
            double h = layer * 1.2;
            for (int i = 0; i < 3; i++) {
                double a = angle + (i * Math.PI * 2 / 3) + layer * 0.4;
                level().addParticle(ParticleTypes.CLOUD,
                        getX() + Math.cos(a) * r, getY() + h, getZ() + Math.sin(a) * r,
                        -Math.sin(a) * 0.15, 0.08, Math.cos(a) * 0.15);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(TICKS_ALIVE, tag.getInt("TicksAlive"));
        if (tag.hasUUID("MaidUuid")) this.maidUuid = tag.getUUID("MaidUuid");
        if (tag.hasUUID("OwnerUuid")) this.ownerUuid = tag.getUUID("OwnerUuid");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksAlive", this.entityData.get(TICKS_ALIVE));
        if (maidUuid != null) tag.putUUID("MaidUuid", maidUuid);
        if (ownerUuid != null) tag.putUUID("OwnerUuid", ownerUuid);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(
                    RawAnimation.begin().then("animation.model.new", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static ResourceLocation getModelLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "geo/tornado.geo.json");
    }

    public static ResourceLocation getTextureLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "textures/entity/tornado.png");
    }

    public static ResourceLocation getAnimationLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "animations/tornado.animation.json");
    }
}
