package studio.fantasyit.maid_rpg_task.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.util.MagicDamageHelper;

import java.util.*;

public class TornadoEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Integer> TICKS_ALIVE =
            SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.INT);

    public static final int LIFETIME_TICKS = 300;
    public static final float DAMAGE_PER_SECOND = 5.0f;
    public static final double PULL_RANGE = 10.0;
    private static final double TARGET_HEIGHT = 4.0;
    private static final double ORBIT_RADIUS = 2.0;
    private static final double SPIN_SPEED = 0.20;
    private static final double LIFT_STRENGTH = 0.18;
    private static final double PULL_STRENGTH = 0.14;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID maidUuid;
    private UUID ownerUuid;
    private final Map<UUID, Double> orbitAngles = new HashMap<>();

    public TornadoEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setMaid(LivingEntity maid) {
        this.maidUuid = maid.getUUID();
        if (maid instanceof TamableAnimal ta && ta.getOwnerUUID() != null) {
            this.ownerUuid = ta.getOwnerUUID();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TICKS_ALIVE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        int ticks = this.entityData.get(TICKS_ALIVE) + 1;
        this.entityData.set(TICKS_ALIVE, ticks);

        if (!level().isClientSide) {
            List<LivingEntity> targets = getValidTargets();

            if (ticks % 20 == 0) {
                Set<UUID> activeIds = new HashSet<>();
                for (LivingEntity t : targets) activeIds.add(t.getUUID());
                orbitAngles.keySet().retainAll(activeIds);
            }

            for (LivingEntity target : targets) pullIntoTornado(target, ticks);

            if (ticks % 20 == 0) {
                for (LivingEntity target : targets)
                    MagicDamageHelper.dealMagicDamage(target, this.damageSources().magic(), DAMAGE_PER_SECOND);
            }
        } else {
            spawnTornadoParticles(ticks);
        }

        if (ticks >= LIFETIME_TICKS) this.discard();
    }

    private void pullIntoTornado(LivingEntity target, int ticks) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double distH = Math.sqrt(dx * dx + dz * dz);

        double targetY = getY() + TARGET_HEIGHT;
        double deltaY = targetY - target.getY();
        double newVY = deltaY > 0.2 ? Math.min(LIFT_STRENGTH, deltaY * 0.15) : 0.08;

        if (distH < PULL_RANGE * 0.8) {
            double angle = orbitAngles.computeIfAbsent(target.getUUID(), id -> Math.atan2(dz, dx));
            angle += SPIN_SPEED;
            orbitAngles.put(target.getUUID(), angle);
            double orbitX = getX() + Math.cos(angle) * ORBIT_RADIUS;
            double orbitZ = getZ() + Math.sin(angle) * ORBIT_RADIUS;
            target.setDeltaMovement((orbitX - target.getX()) * 0.5, newVY, (orbitZ - target.getZ()) * 0.5);
        } else {
            double pullFactor = Math.min(1.0, distH / PULL_RANGE);
            target.setDeltaMovement(
                    -dx / distH * (PULL_STRENGTH + pullFactor * 0.1),
                    newVY,
                    -dz / distH * (PULL_STRENGTH + pullFactor * 0.1));
        }

        target.hurtMarked = true;
        target.fallDistance = 0;
    }

    private List<LivingEntity> getValidTargets() {
        AABB box = new AABB(
                getX() - PULL_RANGE, getY() - 1, getZ() - PULL_RANGE,
                getX() + PULL_RANGE, getY() + TARGET_HEIGHT + 2, getZ() + PULL_RANGE);
        LivingEntity maid = findMaid();
        return level().getEntitiesOfClass(LivingEntity.class, box, entity -> {
            if (!entity.isAlive()) return false;
            if (maidUuid != null && entity.getUUID().equals(maidUuid)) return false;
            if (ownerUuid != null && entity.getUUID().equals(ownerUuid)) return false;
            if (entity instanceof Player) return false;
            if (ownerUuid != null && entity instanceof TamableAnimal ta && ownerUuid.equals(ta.getOwnerUUID())) return false;
            return maid == null || maid.canAttack(entity);
        });
    }

    private LivingEntity findMaid() {
        if (maidUuid == null) return null;
        AABB box = new AABB(getX() - 64, getY() - 32, getZ() - 64, getX() + 64, getY() + 32, getZ() + 64);
        return level().getEntitiesOfClass(LivingEntity.class, box, e -> e.getUUID().equals(maidUuid))
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
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    public static ResourceLocation getModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "geo/tornado.geo.json");
    }

    public static ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "textures/entity/tornado.png");
    }

    public static ResourceLocation getAnimationLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "animations/tornado.animation.json");
    }
}
