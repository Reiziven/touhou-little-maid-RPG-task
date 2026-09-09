package studio.fantasyit.maid_rpg_task.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.util.MagicDamageHelper;
import studio.fantasyit.maid_rpg_task.util.SpellTargetHelper;

import java.util.*;

public class FireMagicCircleEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Integer> TICKS_ALIVE =
            SynchedEntityData.defineId(FireMagicCircleEntity.class, EntityDataSerializers.INT);

    public static final int LIFETIME_TICKS = 80;
    public static final double BREATH_RANGE = 7.0;
    public static final double BREATH_HALF_WIDTH = 1.8;
    public static final int MAGIC_FIRE_DURATION = 600;
    public static final float MAGIC_FIRE_BASE_DPS = 1.0f;
    public static final float MAGIC_FIRE_DPS_RAMP = 0.3f;
    private static final double FORWARD_OFFSET = 0.6;
    private static final double HEIGHT_FACTOR  = 0.45;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private UUID ownerPlayerUuid;
    private UUID lockedTargetUuid;
    private boolean hasAppliedBreath = false;
    private final Map<UUID, long[]> magicFireData = new HashMap<>();

    public FireMagicCircleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity maid, UUID ownerPlayerUuid) {
        this.ownerUuid = maid.getUUID();
        this.ownerPlayerUuid = ownerPlayerUuid;
    }

    public void setLockedTarget(LivingEntity target) {
        this.lockedTargetUuid = target.getUUID();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) { return true; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TICKS_ALIVE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        int ticks = this.entityData.get(TICKS_ALIVE) + 1;
        this.entityData.set(TICKS_ALIVE, ticks);

        LivingEntity maid = findOwner();
        if (maid != null) {
            float yaw = maid.getYRot();
            double yawRad = Math.toRadians(yaw);
            double fx = -Mth.sin((float) yawRad);
            double fz = Mth.cos((float) yawRad);
            double midY = maid.getY() + maid.getBbHeight() * HEIGHT_FACTOR;
            this.setPosRaw(maid.getX() + fx * FORWARD_OFFSET, midY, maid.getZ() + fz * FORWARD_OFFSET);
            this.setBoundingBox(this.makeBoundingBox());
            this.setYRot(yaw);
        }

        if (ticks <= LIFETIME_TICKS) spawnBreathParticles();

        if (!level().isClientSide) {
            if (!hasAppliedBreath && ticks >= 30) {
                applyBreathHit();
                hasAppliedBreath = true;
            }
            if (ticks % 20 == 0) tickMagicFire();
        }

        if (ticks >= LIFETIME_TICKS && magicFireData.isEmpty()) this.discard();
    }

    private LivingEntity findOwner() {
        if (ownerUuid == null) return null;
        AABB box = new AABB(getX() - 64, getY() - 32, getZ() - 64, getX() + 64, getY() + 32, getZ() + 64);
        return level().getEntitiesOfClass(LivingEntity.class, box, e -> e.getUUID().equals(ownerUuid))
                .stream().findFirst().orElse(null);
    }

    private void applyBreathHit() {
        LivingEntity maid = findOwner();
        double fx, fy, fz;
        if (maid != null) {
            var look = maid.getLookAngle();
            fx = look.x; fy = look.y; fz = look.z;
        } else {
            double yawRad = Math.toRadians(this.getYRot());
            fx = -Mth.sin((float) yawRad); fy = 0; fz = Mth.cos((float) yawRad);
        }

        long gameTime = level().getGameTime();

        if (lockedTargetUuid != null) {
            AABB wide = new AABB(getX() - 256, getY() - 128, getZ() - 256, getX() + 256, getY() + 128, getZ() + 256);
            final double ffx = fx, ffy = fy, ffz = fz;
            level().getEntitiesOfClass(LivingEntity.class, wide,
                            e -> e.getUUID().equals(lockedTargetUuid) && e.isAlive())
                    .forEach(t -> {
                        MagicDamageHelper.dealMagicDamage(t, this.damageSources().magic(), 5.0f);
                        if (t.fireImmune() || t.isInWaterOrRain()) spawnCursedFireParticles(t); else t.igniteForSeconds(3);
                        magicFireData.put(t.getUUID(), new long[]{gameTime + MAGIC_FIRE_DURATION, 0});
                    });
        }

        AABB searchBox = new AABB(
                getX() - BREATH_RANGE, getY() - BREATH_RANGE, getZ() - BREATH_RANGE,
                getX() + BREATH_RANGE, getY() + BREATH_RANGE, getZ() + BREATH_RANGE);
        List<LivingEntity> candidates = level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> SpellTargetHelper.isValidTarget(e, findOwner(), ownerUuid, ownerPlayerUuid)
                        && !e.getUUID().equals(lockedTargetUuid));

        for (LivingEntity target : candidates) {
            if (isInBreathCone3D(target, fx, fy, fz)) {
                MagicDamageHelper.dealMagicDamage(target, this.damageSources().magic(), 5.0f);
                if (target.fireImmune() || target.isInWaterOrRain()) spawnCursedFireParticles(target); else target.igniteForSeconds(3);
                magicFireData.put(target.getUUID(), new long[]{gameTime + MAGIC_FIRE_DURATION, 0});
            }
        }
    }

    private boolean isInBreathCone3D(LivingEntity target, double fx, double fy, double fz) {
        double dx = target.getX() - getX();
        double dy = (target.getY() + target.getBbHeight() * 0.5) - getY();
        double dz = target.getZ() - getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > BREATH_RANGE) return false;
        return (dx * fx + dy * fy + dz * fz) / dist >= 0.6;
    }

    private void tickMagicFire() {
        long gameTime = level().getGameTime();
        magicFireData.entrySet().removeIf(entry -> {
            long[] data = entry.getValue();
            if (gameTime >= data[0]) return true;
            long ticksOnFire = data[1] + 1;
            data[1] = ticksOnFire;
            float dps = MAGIC_FIRE_BASE_DPS + MAGIC_FIRE_DPS_RAMP * ticksOnFire;
            level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(512),
                            e -> e.getUUID().equals(entry.getKey()) && e.isAlive())
                    .forEach(t -> {
                        MagicDamageHelper.dealMagicDamage(t, this.damageSources().magic(), dps);
                        if (t.fireImmune() || t.isInWaterOrRain()) spawnCursedFireParticles(t); else t.igniteForSeconds(3);
                    });
            return false;
        });
    }

    private void spawnCursedFireParticles(LivingEntity target) {
        if (!(level() instanceof ServerLevel sl)) return;
        double w = target.getBbWidth() * 0.5;
        double h = target.getBbHeight();
        for (int i = 0; i < 8; i++) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX() + (random.nextDouble() * 2 - 1) * w,
                    target.getY() + random.nextDouble() * h,
                    target.getZ() + (random.nextDouble() * 2 - 1) * w,
                    1, 0, 0.05, 0, 0.02);
        }
    }

    private void spawnBreathParticles() {
        if (!(level() instanceof ServerLevel sl)) return;
        LivingEntity maid = findOwner();
        double fx, fy, fz;
        if (maid != null) {
            var look = maid.getLookAngle();
            fx = look.x; fy = look.y; fz = look.z;
        } else {
            double yawRad = Math.toRadians(this.getYRot());
            fx = -Mth.sin((float) yawRad); fy = 0; fz = Mth.cos((float) yawRad);
        }
        double perpX = fz, perpZ = -fx;
        for (int i = 0; i < 6; i++) {
            double t = (i + 1) / 6.0;
            double spread = BREATH_HALF_WIDTH * t;
            double rx = (random.nextDouble() * 2 - 1) * spread;
            double ry = (random.nextDouble() * 2 - 1) * spread * 0.5;
            double px = getX() + fx * BREATH_RANGE * t + perpX * rx;
            double py = getY() + 0.5 + fy * BREATH_RANGE * t + ry;
            double pz = getZ() + fz * BREATH_RANGE * t + perpZ * rx;
            double speed = 0.2 + random.nextDouble() * 0.1;
            sl.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, fx * speed, fy * speed, fz * speed, 0.06);
            if (i == 0) sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    getX() + fx * 0.5, getY() + 0.5 + fy * 0.5, getZ() + fz * 0.5, 2, 0, 0.02, 0, 0.02);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(TICKS_ALIVE, tag.getInt("TicksAlive"));
        this.hasAppliedBreath = tag.getBoolean("HasAppliedBreath");
        if (tag.hasUUID("OwnerUuid")) this.ownerUuid = tag.getUUID("OwnerUuid");
        if (tag.hasUUID("OwnerPlayerUuid")) this.ownerPlayerUuid = tag.getUUID("OwnerPlayerUuid");
        if (tag.hasUUID("LockedTargetUuid")) this.lockedTargetUuid = tag.getUUID("LockedTargetUuid");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksAlive", this.entityData.get(TICKS_ALIVE));
        tag.putBoolean("HasAppliedBreath", this.hasAppliedBreath);
        if (ownerUuid != null) tag.putUUID("OwnerUuid", ownerUuid);
        if (ownerPlayerUuid != null) tag.putUUID("OwnerPlayerUuid", ownerPlayerUuid);
        if (lockedTargetUuid != null) tag.putUUID("LockedTargetUuid", lockedTargetUuid);
    }

    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("animation.mcircle.spin");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, state -> {
            state.setAnimation(SPIN);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    public static ResourceLocation getModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "geo/magicecircle.geo.json");
    }

    public static ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "textures/entity/mcircle.png");
    }

    public static ResourceLocation getAnimationLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "animations/mcircle.animation.json");
    }
}
