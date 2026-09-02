package studio.fantasyit.maid_rpg_task.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.fantasyit.maid_rpg_task.MaidRpgTask;
import studio.fantasyit.maid_rpg_task.registry.EntityRegistry;
import studio.fantasyit.maid_rpg_task.util.SpellTargetHelper;

import java.util.UUID;

public class IceMagicCircleEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Integer> TICKS_ALIVE =
            SynchedEntityData.defineId(IceMagicCircleEntity.class, EntityDataSerializers.INT);

    private static final int LAUNCH_START    = 10;
    private static final int LAUNCH_INTERVAL = 10;
    private static final int SPIKE_COUNT     = 4;
    public  static final int LIFETIME_TICKS  = LAUNCH_START + LAUNCH_INTERVAL * SPIKE_COUNT + 20;

    private static final double FORWARD_OFFSET = 0.6;
    private static final double HEIGHT_FACTOR  = 0.45;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private UUID maidUuid;
    private UUID ownerPlayerUuid;
    private int spikesLaunched = 0;

    public IceMagicCircleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity maid, UUID ownerPlayerUuid) {
        this.maidUuid = maid.getUUID();
        this.ownerUuid = maid.getUUID(); // keep for position tracking
        this.ownerPlayerUuid = ownerPlayerUuid;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) {
        return true;
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

        if (!level().isClientSide && spikesLaunched < SPIKE_COUNT) {
            int nextLaunch = LAUNCH_START + spikesLaunched * LAUNCH_INTERVAL;
            if (ticks >= nextLaunch) {
                launchSpike(maid);
                spikesLaunched++;
            }
        }

        if (ticks >= LIFETIME_TICKS) this.discard();
    }

    private LivingEntity findOwner() {
        if (ownerUuid == null) return null;
        AABB box = new AABB(getX() - 64, getY() - 32, getZ() - 64,
                getX() + 64, getY() + 32, getZ() + 64);
        return level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.getUUID().equals(ownerUuid))
                .stream().findFirst().orElse(null);
    }

    private void launchSpike(LivingEntity owner) {
        if (owner == null) return;

        var look = owner.getLookAngle();
        double fx = look.x;
        double fy = look.y * 1.5; // boost vertical component
        double fz = look.z;

        double spreadAngle = (spikesLaunched - (SPIKE_COUNT - 1) / 2.0) * 0.12;
        double cos = Math.cos(spreadAngle);
        double sin = Math.sin(spreadAngle);
        double sfx = fx * cos - fz * sin;
        double sfz = fx * sin + fz * cos;

        Vec3 spawnPos = new Vec3(getX() + sfx * 1.2, getY() + 0.5, getZ() + sfz * 1.2);

        IceSpikeProjectile spike = new IceSpikeProjectile(EntityRegistry.ICE_SPIKE.get(), level());
        spike.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, getYRot(), 0);
        spike.setDeltaMovement(sfx * IceSpikeProjectile.SPEED, fy * IceSpikeProjectile.SPEED, sfz * IceSpikeProjectile.SPEED);
        spike.setOwnerForDamage(owner, ownerPlayerUuid);

        // Find nearest valid target for homing (respects owner/ally exclusion)
        AABB searchBox = new AABB(getX() - 24, getY() - 16, getZ() - 24,
                getX() + 24, getY() + 16, getZ() + 24);
        level().getEntitiesOfClass(LivingEntity.class, searchBox,
                        e -> SpellTargetHelper.isValidTarget(e, owner, maidUuid, ownerPlayerUuid))
                .stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(getX(), getY(), getZ()),
                        b.distanceToSqr(getX(), getY(), getZ())))
                .ifPresent(spike::setHomingTarget);

        level().addFreshEntity(spike);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(TICKS_ALIVE, tag.getInt("TicksAlive"));
        this.spikesLaunched = tag.getInt("SpikesLaunched");
        if (tag.hasUUID("OwnerUuid")) this.ownerUuid = tag.getUUID("OwnerUuid");
        if (tag.hasUUID("MaidUuid")) this.maidUuid = tag.getUUID("MaidUuid");
        if (tag.hasUUID("OwnerPlayerUuid")) this.ownerPlayerUuid = tag.getUUID("OwnerPlayerUuid");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksAlive", this.entityData.get(TICKS_ALIVE));
        tag.putInt("SpikesLaunched", this.spikesLaunched);
        if (ownerUuid != null) tag.putUUID("OwnerUuid", ownerUuid);
        if (maidUuid != null) tag.putUUID("MaidUuid", maidUuid);
        if (ownerPlayerUuid != null) tag.putUUID("OwnerPlayerUuid", ownerPlayerUuid);
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static ResourceLocation getModelLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "geo/magicecircle.geo.json");
    }

    public static ResourceLocation getTextureLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "textures/entity/icemcircle.png");
    }

    public static ResourceLocation getAnimationLocation() {
        return new ResourceLocation(MaidRpgTask.MODID, "animations/mcircle.animation.json");
    }
}
