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
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;

import java.util.UUID;

public class SphereShieldEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Integer> SHIELD_HP =
            SynchedEntityData.defineId(SphereShieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ACTIVE =
            SynchedEntityData.defineId(SphereShieldEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID maidUuid;
    private int maxHp = 500; // stored ×10 for sub-integer precision

    // Client-side interpolation targets, used to smooth out the per-tick
    // teleport-to-maid updates coming from the server instead of snapping.
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;

    public SphereShieldEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void init(LivingEntity maid, float maxHpFloat, int savedHpRaw) {
        this.maidUuid = maid.getUUID();
        this.maxHp = (int)(maxHpFloat * 10);
        // -1 means full HP (first time spawning)
        this.entityData.set(SHIELD_HP, savedHpRaw < 0 ? this.maxHp : Math.min(savedHpRaw, this.maxHp));
        this.entityData.set(ACTIVE, false);
        this.setPos(maid.getX(), maid.getY(), maid.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SHIELD_HP, 500);
        this.entityData.define(ACTIVE, false);
    }

    public float   getShieldHp()    { return this.entityData.get(SHIELD_HP) / 10f; }
    public int     getShieldHpRaw() { return this.entityData.get(SHIELD_HP); }
    public UUID    getMaidUuid()    { return maidUuid; }
    public boolean isActive()       { return this.entityData.get(ACTIVE); }
    public void    setActive(boolean v) { this.entityData.set(ACTIVE, v); }

    /** Drains shield HP by amount. Returns -1 if inactive or already empty. */
    public float absorbDamage(float amount) {
        if (!isActive()) return -1;
        int hp = this.entityData.get(SHIELD_HP);
        if (hp <= 0) return -1;
        this.entityData.set(SHIELD_HP, Math.max(0, hp - (int)(amount * 10)));
        return 0;
    }

    public void breakShield() {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 6, 0.5, 0.5, 0.5, 0.1);
            sl.sendParticles(ParticleTypes.END_ROD,   getX(), getY() + 1, getZ(), 30, 0.6, 0.6, 0.6, 0.05);
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            this.setInvisible(!isActive());
            // Step towards the last position sent by the server instead of
            // snapping straight to it, so the follow motion is smooth.
            if (this.lerpSteps > 0) {
                double nx = getX() + (lerpX - getX()) / lerpSteps;
                double ny = getY() + (lerpY - getY()) / lerpSteps;
                double nz = getZ() + (lerpZ - getZ()) / lerpSteps;
                this.lerpSteps--;
                this.setPos(nx, ny, nz);
            }
            return;
        }
        if (maidUuid == null) { discard(); return; }
        LivingEntity maid = level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(64, 32, 64),
                e -> e.getUUID().equals(maidUuid)).stream().findFirst().orElse(null);
        if (maid == null || !maid.isAlive()) { discard(); return; }
        this.setPos(maid.getX(), maid.getY(), maid.getZ());
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean interpolate) {
        // Buffer the target position/step count instead of the default
        // Entity behavior, which just snaps directly to (x, y, z).
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpSteps = steps;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("MaidUuid")) this.maidUuid = tag.getUUID("MaidUuid");
        this.entityData.set(SHIELD_HP, tag.getInt("ShieldHp"));
        this.maxHp = tag.getInt("MaxHp");
        this.entityData.set(ACTIVE, tag.getBoolean("Active"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (maidUuid != null) tag.putUUID("MaidUuid", maidUuid);
        tag.putInt("ShieldHp", this.entityData.get(SHIELD_HP));
        tag.putInt("MaxHp", this.maxHp);
        tag.putBoolean("Active", isActive());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(RawAnimation.begin().then("Shield.Idle", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    public static ResourceLocation getModelLocation()     { return new ResourceLocation(MaidRpgTask.MODID, "geo/shield.geo.json"); }
    public static ResourceLocation getTextureLocation()   { return new ResourceLocation(MaidRpgTask.MODID, "textures/entity/shield.png"); }
    public static ResourceLocation getAnimationLocation() { return new ResourceLocation(MaidRpgTask.MODID, "animations/shield.animation.json"); }
}
