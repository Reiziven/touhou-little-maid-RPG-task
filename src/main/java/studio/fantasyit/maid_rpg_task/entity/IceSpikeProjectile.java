package studio.fantasyit.maid_rpg_task.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.fantasyit.maid_rpg_task.Config;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.registry.EffectRegistry;
import studio.fantasyit.maid_rpg_task.util.MagicDamageHelper;
import studio.fantasyit.maid_rpg_task.util.SpellTargetHelper;

import java.util.List;
import java.util.UUID;

public class IceSpikeProjectile extends Entity implements GeoEntity {

    public static final float SPEED = 1.2f;
    public static final float IMPACT_DAMAGE = 5.0f;
    public static final float DOT_DAMAGE = 1.0f;
    public static final int ICE_EFFECT_DURATION = 600;
    public static final int STICK_DURATION = 80;
    public static final int LIFETIME_TICKS = 120;

    private static final EntityDataAccessor<Integer> TICKS_ALIVE =
            SynchedEntityData.defineId(IceSpikeProjectile.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private UUID ownerPlayerUuid;
    private boolean hasHit = false;
    private UUID homingTargetUuid = null;
    private UUID stuckTargetUuid = null;
    private int stuckTicks = 0;
    private Vec3 stuckOffset = Vec3.ZERO;

    public IceSpikeProjectile(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwnerForDamage(LivingEntity owner, UUID ownerPlayerUuid) {
        this.ownerUuid = owner.getUUID();
        this.ownerPlayerUuid = ownerPlayerUuid;
    }

    public void setHomingTarget(LivingEntity target) {
        this.homingTargetUuid = target.getUUID();
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
            if (!hasHit) {
                checkCollision();
                if (ticks >= LIFETIME_TICKS && !hasHit) this.discard();
            } else if (stuckTargetUuid != null) {
                tickStuck();
            }
        } else {
            if (!hasHit) spawnTrailParticles();
        }
    }

    private void tickStuck() {
        stuckTicks++;
        LivingEntity target = findStuckTarget();
        if (target == null || !target.isAlive()) { this.discard(); return; }

        Vec3 newPos = target.position().add(stuckOffset);
        this.setPos(newPos.x, newPos.y, newPos.z);

        if (stuckTicks == STICK_DURATION) this.setInvisible(true);

        if (stuckTicks % 20 == 0)
            MagicDamageHelper.dealMagicDamage(target, this.damageSources().magic(), DOT_DAMAGE);

        if (level() instanceof ServerLevel sl && stuckTicks % 5 == 0) {
            double w = target.getBbWidth() * 0.5;
            double h = target.getBbHeight();
            sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + h * 0.5, target.getZ(),
                    4, w, h * 0.4, w, 0.02);
            sl.sendParticles(ParticleTypes.WHITE_ASH, target.getX(), target.getY() + h * 0.5, target.getZ(),
                    2, w, h * 0.4, w, 0.01);
        }

        if (stuckTicks >= ICE_EFFECT_DURATION) this.discard();
    }

    private LivingEntity findStuckTarget() {
        if (stuckTargetUuid == null) return null;
        List<LivingEntity> found = level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(64), e -> e.getUUID().equals(stuckTargetUuid));
        return found.isEmpty() ? null : found.get(0);
    }

    private void checkCollision() {
        if (homingTargetUuid != null) {
            List<LivingEntity> homing = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(32), e -> e.getUUID().equals(homingTargetUuid) && e.isAlive());
            if (!homing.isEmpty()) {
                LivingEntity t = homing.get(0);
                Vec3 toTarget = new Vec3(
                        t.getX() - getX(),
                        (t.getY() + t.getBbHeight() * 0.5) - getY(),
                        t.getZ() - getZ()).normalize().scale(SPEED);
                Vec3 cur = getDeltaMovement();
                setDeltaMovement(
                        cur.x + (toTarget.x - cur.x) * 0.15,
                        cur.y + (toTarget.y - cur.y) * 0.15,
                        cur.z + (toTarget.z - cur.z) * 0.15);
            }
        }

        Vec3 motion = getDeltaMovement();
        Vec3 end = position().add(motion);
        AABB scanBox = getBoundingBox().expandTowards(motion).inflate(0.3);
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, scanBox,
                e -> SpellTargetHelper.isValidTarget(e, null, ownerUuid, ownerPlayerUuid)
                        && (homingTargetUuid == null || e.getUUID().equals(homingTargetUuid)));

        for (LivingEntity target : nearby) {
            onHitEntity(target);
            return;
        }

        this.setPos(end.x, end.y, end.z);
        if (!level().noCollision(this, getBoundingBox())) onHitBlock();
    }

    private void onHitEntity(LivingEntity target) {
        hasHit = true;
        stuckTargetUuid = target.getUUID();
        stuckOffset = position().subtract(target.position());
        MagicDamageHelper.dealMagicDamage(target, this.damageSources().magic(), IMPACT_DAMAGE);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ICE_EFFECT_DURATION, 1, false, true, true));
        target.addEffect(new MobEffectInstance(EffectRegistry.REDUCED_HEALING,
                Config.iceSpikeReducedHealingDuration, Config.iceSpikeReducedHealingLevel, false, true, true));
        MagicDamageHelper.applyVulnerability(target);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY(), getZ(), 30, 0.5, 0.5, 0.5, 0.1);
            sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 15, 0.3, 0.3, 0.3, 0.15);
        }
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void onHitBlock() {
        hasHit = true;
        this.setDeltaMovement(Vec3.ZERO);
        this.discard();
    }

    private void spawnTrailParticles() {
        level().addParticle(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(),
                -getDeltaMovement().x * 0.5, 0.05, -getDeltaMovement().z * 0.5);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(TICKS_ALIVE, tag.getInt("TicksAlive"));
        this.hasHit = tag.getBoolean("HasHit");
        this.stuckTicks = tag.getInt("StuckTicks");
        if (tag.hasUUID("OwnerUuid")) this.ownerUuid = tag.getUUID("OwnerUuid");
        if (tag.hasUUID("OwnerPlayerUuid")) this.ownerPlayerUuid = tag.getUUID("OwnerPlayerUuid");
        if (tag.hasUUID("HomingUuid")) this.homingTargetUuid = tag.getUUID("HomingUuid");
        if (tag.hasUUID("StuckTargetUuid")) this.stuckTargetUuid = tag.getUUID("StuckTargetUuid");
        if (tag.contains("OffsetX"))
            this.stuckOffset = new Vec3(tag.getDouble("OffsetX"), tag.getDouble("OffsetY"), tag.getDouble("OffsetZ"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksAlive", this.entityData.get(TICKS_ALIVE));
        tag.putBoolean("HasHit", this.hasHit);
        tag.putInt("StuckTicks", this.stuckTicks);
        if (ownerUuid != null) tag.putUUID("OwnerUuid", ownerUuid);
        if (ownerPlayerUuid != null) tag.putUUID("OwnerPlayerUuid", ownerPlayerUuid);
        if (homingTargetUuid != null) tag.putUUID("HomingUuid", homingTargetUuid);
        if (stuckTargetUuid != null) tag.putUUID("StuckTargetUuid", stuckTargetUuid);
        tag.putDouble("OffsetX", stuckOffset.x);
        tag.putDouble("OffsetY", stuckOffset.y);
        tag.putDouble("OffsetZ", stuckOffset.z);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    public static ResourceLocation getModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "geo/ice.geo.json");
    }

    public static ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "textures/entity/ice.png");
    }

    public static ResourceLocation getAnimationLocation() {
        return ResourceLocation.fromNamespaceAndPath(MaidRpgTask.MODID, "animations/mcircle.animation.json");
    }
}
